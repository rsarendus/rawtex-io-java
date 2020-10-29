package ee.ristoseene.rawtex.in.data;

import ee.ristoseene.rawtex.common.RawTexFormat;
import ee.ristoseene.rawtex.common.internal.ArraySource;
import ee.ristoseene.rawtex.common.internal.CommonIO;
import ee.ristoseene.rawtex.common.internal.Endianness;
import ee.ristoseene.rawtex.in.RawTexLoadTarget;
import ee.ristoseene.rawtex.in.internal.AbstractBlockWiseDataLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * An implementation of {@link ee.ristoseene.rawtex.in.RawTexDataLoader} for loading uncompressed data from input stream
 * into any kind of {@link ByteBuffer}s, providing automatic endianness conversion as necessary.
 * The loader counts loadable data in blocks, thus the loader can only load and transfer into the destination buffer
 * chunks of data with length evenly divisible by block size ({@link RawTexFormat#getOctetsPerBlock()}).
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
 * conversion is necessary, then a transfer buffer (a byte array) needs to be used for loading. For transfer buffer,
 * a pre-allocated byte array could be specified or maximum length for the buffer could be specified, in which
 * case the buffer will be allocated and cached internally, as necessary.
 */
public class RawBlockDataLoader extends AbstractBlockWiseDataLoader {

    private static final int DEFAULT_TRANSFER_BUFFER_BLOCK_COUNT = 1024;

    private final int transferBufferBlockCount;
    private byte[] transferBuffer;

    /**
     * Constructs a {@code RawBlockDataLoader} with the specified input data format and input data endianness,
     * using the default transfer buffer size of '{@link RawTexFormat#getOctetsPerBlock()}
     * {@code *} {@value DEFAULT_TRANSFER_BUFFER_BLOCK_COUNT}'.
     *
     * @param format format of the input data (dictating the block size in number of octets)
     * @param endianness endianness of the input data
     */
    public RawBlockDataLoader(RawTexFormat format, Endianness endianness) {
        super(format, endianness);
        transferBufferBlockCount = DEFAULT_TRANSFER_BUFFER_BLOCK_COUNT;
    }

    /**
     * Constructs a {@code RawBlockDataLoader} with the specified input data format and input data endianness,
     * using the specified maximum transfer buffer size.
     *
     * @param format format of the input data (dictating the block size in number of octets)
     * @param endianness endianness of the input data
     * @param maxTransferBufferSize maximum number of bytes to allocate for a transfer buffer
     *
     * @throws IllegalArgumentException if the maximum transfer buffer size is less than {@link RawTexFormat#getOctetsPerBlock()}
     */
    public RawBlockDataLoader(RawTexFormat format, Endianness endianness, int maxTransferBufferSize) {
        super(format, endianness);

        if (maxTransferBufferSize < blockSize) {
            throw new IllegalArgumentException("Invalid maximum transfer buffer size: " + maxTransferBufferSize);
        }

        transferBufferBlockCount = maxTransferBufferSize / blockSize;
    }

    /**
     * Constructs a {@code RawBlockDataLoader} with the specified input data format and input data endianness,
     * with the specified byte array as a transfer buffer.
     *
     * @param format format of the input data (dictating the block size in number of octets)
     * @param endianness endianness of the input data
     * @param preAllocatedTransferBuffer pre-allocated byte array to use as a transfer buffer
     *
     * @throws IllegalArgumentException if the pre-allocated transfer buffer size is less than {@link RawTexFormat#getOctetsPerBlock()}
     */
    public RawBlockDataLoader(RawTexFormat format, Endianness endianness, byte[] preAllocatedTransferBuffer) {
        super(format, endianness);

        if (preAllocatedTransferBuffer.length < blockSize) {
            throw new IllegalArgumentException("Invalid pre-allocated transfer buffer size: " + preAllocatedTransferBuffer.length);
        }

        transferBufferBlockCount = preAllocatedTransferBuffer.length / blockSize;
        transferBuffer = preAllocatedTransferBuffer;
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
     * @throws IllegalStateException if load target buffer's {@link ByteBuffer#remaining()} is less that block size,
     * isn't evenly divisible by block size or is greater than the remaining data length
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
        if (transferBuffer == null) {
            transferBuffer = new byte[blockSize * transferBufferBlockCount];
        }

        final int transferBufferLength = transferBuffer.length;

        do {
            final int transferLength = Math.min(length, transferBufferLength);

            if (in.read(transferBuffer, 0, transferLength) != transferLength) {
                throw CommonIO.unexpectedEndOfInputException();
            }

            out.put(transferBuffer, 0, transferLength);

            length -= transferLength;
        } while (length > 0);
    }

    private void transferViaTransferBuffer(InputStream in, ByteBuffer out, int blockCount) throws IOException {
        if (transferBuffer == null) {
            transferBuffer = new byte[blockSize * transferBufferBlockCount];
        }

        do {
            final int transferBlockCount = Math.min(blockCount, transferBufferBlockCount);
            final int transferLength = blockSize * transferBlockCount;

            if (in.read(transferBuffer, 0, transferLength) != transferLength) {
                throw CommonIO.unexpectedEndOfInputException();
            }

            transferBlockWise(transferBuffer, 0, transferBlockCount, out);

            blockCount -= transferBlockCount;
        } while (blockCount > 0);
    }

    private static void readIntoArrayBackedBuffer(InputStream in, ByteBuffer out, int length) throws IOException {
        final int position = out.position();

        if (in.read(out.array(), out.arrayOffset() + position, length) != length) {
            throw CommonIO.unexpectedEndOfInputException();
        }

        out.position(position + length);
    }

}
