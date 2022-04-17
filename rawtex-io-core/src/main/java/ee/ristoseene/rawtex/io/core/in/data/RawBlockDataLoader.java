package ee.ristoseene.rawtex.io.core.in.data;

import ee.ristoseene.rawtex.io.core.common.RawTexFormat;
import ee.ristoseene.rawtex.io.core.common.data.TransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.common.internal.ArraySource;
import ee.ristoseene.rawtex.io.core.common.internal.CommonIO;
import ee.ristoseene.rawtex.io.core.common.internal.Endianness;
import ee.ristoseene.rawtex.io.core.in.RawTexLoadTarget;
import ee.ristoseene.rawtex.io.core.in.internal.AbstractBlockDataLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * An implementation of {@link ee.ristoseene.rawtex.io.core.in.RawTexDataLoader} for loading uncompressed data from
 * input stream into any kind of {@link ByteBuffer}s, providing automatic endianness conversion as necessary.
 * The loader counts loadable data in blocks, thus the loader can only load and transfer into the destination buffer
 * chunks of data with lengths equal to multiples of block size ({@link RawTexFormat#getOctetsPerBlock()}).
 * <p>
 * Depending on the type of input and/or output, certain fast paths could be chosen:
 * <ul>
 *     <li>Input data is dumped directly into the target buffer, if input data is backed by a byte array and the target
 *     endianness matches the source endianness</li>
 *     <li>Input data is read directly from stream into the target buffer, if the target buffer is an array-backed buffer
 *     and the target endianness matches the source endianness</li>
 * </ul>
 * <p>
 * In case input data is not backed by a byte array, and target buffer is either not an array-backed buffer or endianness
 * conversion is necessary, then temporary byte arrays need to be allocated in order to be able read bytes from input
 * streams in chunks. Such temporary byte arrays are obtained automatically as needed via
 * {@link TransferBufferAllocator} interface.
 * In case allocating transfer buffers is necessary, instances of this class are guaranteed to allocate a single transfer
 * buffer at a time, provided an instance is not accessed from multiple threads concurrently nor called recursively - i.e.
 * a call to {@link TransferBufferAllocator#allocate(int, int)} is always followed by a corresponding call to
 * {@link TransferBufferAllocator#free(byte[])} before another {@link TransferBufferAllocator#allocate(int, int)}
 * is invoked. The minimum required transfer buffer size is equal to block size.
 */
public class RawBlockDataLoader extends AbstractBlockDataLoader {

    private final TransferBufferAllocator transferBufferAllocator;

    /**
     * Constructs a {@code RawBlockDataLoader} with the specified input data format, input data endianness and transfer
     * buffer allocator.
     *
     * @param format format of the input data (dictating the block size in number of octets)
     * @param endianness endianness of the input data
     * @param transferBufferAllocator an allocator providing temporary byte arrays for transferring data from input stream
     *                                into the destination buffer
     *
     * @throws NullPointerException if {@code format}, {@code endianness} or {@code transferBufferAllocator} is {@code null}
     */
    public RawBlockDataLoader(RawTexFormat format, Endianness endianness, TransferBufferAllocator transferBufferAllocator) {
        super(format, endianness);

        this.transferBufferAllocator = Objects.requireNonNull(transferBufferAllocator, "Transfer buffer allocator not provided");
    }

    /**
     * Performs a load operation from the specified input stream into the specified destination.
     *
     * @param in the stream to perform the data load from
     * @param inputLength number of octets to read from the input stream {@code in}
     * @param loadTarget destination for the load operation
     * @param dataLength data length in number of octets
     *
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if input length does not match data length or if data length is not a multiple of block size
     * @throws IllegalStateException if load target buffer's {@link ByteBuffer#remaining()} is less than block size,
     * is not a multiple of block size or is greater than the remaining data length
     */
    @Override
    public void load(InputStream in, int inputLength, RawTexLoadTarget loadTarget, int dataLength) throws IOException {
        if (inputLength != dataLength) {
            throw new IllegalArgumentException(String.format("Input length (%d) does not match data length (%d)", inputLength, dataLength));
        } else if (dataLength % blockSize != 0) {
            throw new IllegalArgumentException(String.format("Data length (%d) is not a multiple of block size (%d)", dataLength, blockSize));
        }

        if (in instanceof ArraySource) {
            final ArraySource arraySource = (ArraySource) in;
            loadFromArray(arraySource.array, arraySource.ensureAvailableAndAdvance(inputLength), loadTarget, dataLength);
        } else {
            loadFromStream(in, loadTarget, dataLength);
        }
    }

    private void loadFromArray(byte[] in, int inOffset, RawTexLoadTarget loadTarget, int dataLength) {
        int dataOffset = 0;

        while (dataLength > 0) {
            final ByteBuffer targetBuffer = loadTarget.acquire(dataOffset, dataLength);
            boolean complete = false;
            try {
                final int transferBlockCount = validateTargetBufferAndAcquireBlockCount(targetBuffer);
                final int transferLength = blockSize * transferBlockCount;

                if (transferLength > dataLength) {
                    throw invalidTargetBufferLengthException(transferLength);
                } else if (blockSize > 1 && targetBuffer.order() != endianness.byteOrder) {
                    transferBlockWise(in, inOffset + dataOffset, transferBlockCount, targetBuffer);
                } else {
                    targetBuffer.put(in, inOffset + dataOffset, transferLength);
                }

                dataOffset += transferLength;
                dataLength -= transferLength;
                complete = true;
            } finally {
                loadTarget.release(targetBuffer, complete);
            }
        }
    }

    private void loadFromStream(InputStream in, RawTexLoadTarget loadTarget, int dataLength) throws IOException {
        int dataOffset = 0;

        while (dataLength > 0) {
            final ByteBuffer targetBuffer = loadTarget.acquire(dataOffset, dataLength);
            boolean complete = false;
            try {
                final int transferBlockCount = validateTargetBufferAndAcquireBlockCount(targetBuffer);
                final int transferLength = blockSize * transferBlockCount;

                if (transferLength > dataLength) {
                    throw invalidTargetBufferLengthException(transferLength);
                } else if (blockSize > 1 && targetBuffer.order() != endianness.byteOrder) {
                    transferViaTransferBuffer(in, targetBuffer, transferBlockCount);
                } else if (targetBuffer.hasArray()) {
                    readIntoArrayBackedBuffer(in, targetBuffer, transferLength);
                } else {
                    putViaTransferBuffer(in, targetBuffer, transferLength);
                }

                dataOffset += transferLength;
                dataLength -= transferLength;
                complete = true;
            } finally {
                loadTarget.release(targetBuffer, complete);
            }
        }
    }

    private void putViaTransferBuffer(InputStream in, ByteBuffer out, int length) throws IOException {
        byte[] transferBuffer = transferBufferAllocator.allocate(blockSize, length);

        try {
            final int transferBufferLength = validateTransferBufferAndGetBlockCount(transferBuffer) * blockSize;

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
        byte[] transferBuffer = transferBufferAllocator.allocate(blockSize, blockSize * blockCount);

        try {
            final int transferBufferBlockCount = validateTransferBufferAndGetBlockCount(transferBuffer);

            do {
                final int transferBlockCount = Math.min(blockCount, transferBufferBlockCount);
                final int transferLength = blockSize * transferBlockCount;

                CommonIO.readOctets(in, transferBuffer, 0, transferLength);
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

    private int validateTransferBufferAndGetBlockCount(byte[] transferBuffer) {
        if (transferBuffer == null) {
            throw new NullPointerException("Transfer buffer missing");
        }

        final int transferBufferLength = transferBuffer.length;

        if (transferBufferLength < blockSize) {
            throw new IllegalStateException("Transfer buffer too short: " + transferBufferLength);
        }

        return transferBufferLength / blockSize;
    }

}
