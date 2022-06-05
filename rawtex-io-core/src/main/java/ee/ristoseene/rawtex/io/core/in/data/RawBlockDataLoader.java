package ee.ristoseene.rawtex.io.core.in.data;

import ee.ristoseene.rawtex.io.core.common.data.TransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.common.format.BlockSize;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;
import ee.ristoseene.rawtex.io.core.common.internal.CommonIO;
import ee.ristoseene.rawtex.io.core.in.RawTexDataLoader;
import ee.ristoseene.rawtex.io.core.in.RawTexLoadTarget;
import ee.ristoseene.rawtex.io.core.in.internal.AbstractTransferBufferingBlockDataLoader;
import ee.ristoseene.rawtex.io.core.in.internal.ArraySource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * An implementation of {@link RawTexDataLoader} for loading uncompressed data from input streams into
 * {@link ByteBuffer}s, providing automatic endianness conversion as necessary.
 * The loader counts loadable data in blocks, thus the loader can only load and transfer data into destination
 * buffers in chunks equal to multiples of specific block size.
 * <p>
 * Depending on the type of input and/or output, certain fast paths could be chosen:
 * <ul>
 *     <li>Input data is dumped directly into the target buffer, if input data is
 *     {@link ArraySource backed by a byte array} and the target endianness matches the source endianness</li>
 *     <li>Input data is read directly from stream into the target buffer, if the target buffer is an array-backed
 *     buffer and the target endianness matches the source endianness</li>
 * </ul>
 * <p>
 * In case input data is not backed by a byte array, and target buffer is either not an array-backed buffer or
 * endianness conversion is necessary, then temporary byte arrays need to be allocated in order to be able to read
 * bytes from input streams in chunks. Such temporary byte arrays are obtained automatically as needed via the
 * {@link TransferBufferAllocator} interface.
 * In case allocating transfer buffers is necessary, a call to
 * {@link #load(InputStream, int, RawTexLoadTarget, int) load} is guaranteed to allocate a single transfer buffer
 * at a time - i.e. a call to {@link TransferBufferAllocator#allocate(int, int)} is always followed by a corresponding
 * call to {@link TransferBufferAllocator#free(byte[])} before another
 * {@link TransferBufferAllocator#allocate(int, int)} is invoked.
 * The minimum required transfer buffer size is equal to {@link #blockSize block size}.
 * <p>
 * Instances of this class hold no direct mutable state, and are thread-safe as long as the
 * implementation of the assigned {@link TransferBufferAllocator} is thread-safe.
 */
public class RawBlockDataLoader extends AbstractTransferBufferingBlockDataLoader implements RawTexDataLoader {

    private static final int MINIMUM_TRANSFER_LENGTH_VIA_PUT = 1;

    /**
     * Constructs a {@code RawBlockDataLoader} with the specified input data endianness, block size and transfer
     * buffer allocator.
     *
     * @param endianness endianness of the input data
     * @param blockSize block size of the input data
     * @param transferBufferAllocator an allocator providing temporary byte arrays for transferring data from input
     *                                stream into the destination buffer
     *
     * @throws NullPointerException if {@code endianness}, {@code blockSize} or {@code transferBufferAllocator}
     * is {@code null}
     */
    public RawBlockDataLoader(Endianness endianness, BlockSize blockSize, TransferBufferAllocator transferBufferAllocator) {
        super(endianness, blockSize, transferBufferAllocator);
    }

    /**
     * {@inheritDoc}
     *
     * @param in {@inheritDoc}
     * @param inputLength {@inheritDoc}
     * @param loadTarget {@inheritDoc}
     * @param dataLength {@inheritDoc}
     *
     * @throws IOException {@inheritDoc}
     * @throws IllegalArgumentException if data length is not a multiple of block size
     * or if input length does not match data length
     * @throws IllegalStateException if destination buffer's {@link ByteBuffer#remaining()} is less than block size,
     * is not a multiple of block size or is greater than the remaining data length
     */
    @Override
    public void load(InputStream in, int inputLength, RawTexLoadTarget loadTarget, int dataLength) throws IOException {
        ensureDataLengthIsValidMultipleOfBlockSize(dataLength);

        if (inputLength != dataLength) {
            throw new IllegalArgumentException(String.format("Input length (%d) does not match data length (%d)", inputLength, dataLength));
        }

        if (in instanceof ArraySource) {
            final ArraySource arraySource = (ArraySource) in;
            loadFromArray(arraySource.array, arraySource.ensureAvailableAndAdvance(inputLength), inputLength, loadTarget);
        } else {
            loadFromStream(in, inputLength, loadTarget);
        }
    }

    private void loadFromArray(byte[] in, int inOffset, int remainingLength, RawTexLoadTarget loadTarget) {
        int dataOffset = 0;

        do {
            final ByteBuffer targetBuffer = loadTarget.acquire(dataOffset, remainingLength);
            ensureTargetBufferNotNull(targetBuffer);

            boolean complete = false;
            try {
                final int transferLength = validateTargetBufferAndReturnLength(targetBuffer, remainingLength);

                if (isDirectTransferPossibleForTargetBuffer(targetBuffer)) {
                    targetBuffer.put(in, inOffset + dataOffset, transferLength);
                } else {
                    transferBlockWise(in, inOffset + dataOffset, blockSize.quotientOf(transferLength), targetBuffer);
                }

                dataOffset += transferLength;
                remainingLength -= transferLength;
                complete = true;
            } finally {
                loadTarget.release(targetBuffer, complete);
            }
        } while (remainingLength > 0);
    }

    private void loadFromStream(InputStream in, int remainingLength, RawTexLoadTarget loadTarget) throws IOException {
        int dataOffset = 0;

        do {
            final ByteBuffer targetBuffer = loadTarget.acquire(dataOffset, remainingLength);
            ensureTargetBufferNotNull(targetBuffer);

            boolean complete = false;
            try {
                final int transferLength = validateTargetBufferAndReturnLength(targetBuffer, remainingLength);

                if (!isDirectTransferPossibleForTargetBuffer(targetBuffer)) {
                    transferViaTransferBuffer(in, targetBuffer, blockSize.quotientOf(transferLength));
                } else if (targetBuffer.hasArray()) {
                    readIntoArrayBackedBuffer(in, targetBuffer, transferLength);
                } else {
                    putViaTransferBuffer(in, targetBuffer, transferLength);
                }

                dataOffset += transferLength;
                remainingLength -= transferLength;
                complete = true;
            } finally {
                loadTarget.release(targetBuffer, complete);
            }
        } while (remainingLength > 0);
    }

    private void putViaTransferBuffer(InputStream in, ByteBuffer out, int length) throws IOException {
        final byte[] transferBuffer = allocateTransferBuffer(MINIMUM_TRANSFER_LENGTH_VIA_PUT, length);

        try {
            final int transferBufferLength = validateTransferBufferAndReturnLength(transferBuffer, MINIMUM_TRANSFER_LENGTH_VIA_PUT);

            do {
                final int transferLength = Math.min(length, transferBufferLength);

                CommonIO.readOctets(in, transferBuffer, 0, transferLength);
                out.put(transferBuffer, 0, transferLength);

                length -= transferLength;
            } while (length > 0);
        } finally {
            transferBufferAllocator.free(transferBuffer);
        }
    }

    private void transferViaTransferBuffer(InputStream in, ByteBuffer out, int blockCount) throws IOException {
        final byte[] transferBuffer = allocateTransferBufferForBlockWiseTransfers(blockCount);

        try {
            final int transferBufferBlockCount = validateTransferBufferForBlockWiseTransfersAndReturnBlockCount(transferBuffer);

            do {
                final int transferBlockCount = Math.min(blockCount, transferBufferBlockCount);

                CommonIO.readOctets(in, transferBuffer, 0, blockSize.multipleOf(transferBlockCount));
                transferBlockWise(transferBuffer, 0, transferBlockCount, out);

                blockCount -= transferBlockCount;
            } while (blockCount > 0);
        } finally {
            transferBufferAllocator.free(transferBuffer);
        }
    }

    private static void readIntoArrayBackedBuffer(InputStream in, ByteBuffer out, int length) throws IOException {
        final int position = out.position();

        CommonIO.readOctets(in, out.array(), out.arrayOffset() + position, length);
        out.position(position + length);
    }

}
