package ee.ristoseene.rawtex.deflate.in;

import ee.ristoseene.rawtex.common.RawTexFormat;
import ee.ristoseene.rawtex.common.data.TransferBufferAllocator;
import ee.ristoseene.rawtex.common.exceptions.RawTexInvalidInputException;
import ee.ristoseene.rawtex.common.internal.ArraySource;
import ee.ristoseene.rawtex.common.internal.CommonIO;
import ee.ristoseene.rawtex.common.internal.Endianness;
import ee.ristoseene.rawtex.in.RawTexLoadTarget;
import ee.ristoseene.rawtex.in.internal.AbstractBlockDataLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * An implementation of {@link ee.ristoseene.rawtex.in.RawTexDataLoader} for loading compressed data stored in DEFLATE
 * format from input stream into any kind of {@link ByteBuffer}s, providing automatic endianness conversion as necessary.
 * The loader counts loadable data in blocks, thus the loader can only load and transfer into the destination buffer
 * chunks of data with lengths equal to multiples of block size ({@link RawTexFormat#getOctetsPerBlock()}).
 * <p>
 * In case input data is not backed by a byte array, then a temporary byte array needs to be allocated in order to be
 * able to load input from stream into the inflater. In case target buffer is either not an array-backed buffer or
 * endianness conversion is necessary, then temporary byte arrays need to be allocated in order to be able to transfer
 * inflater output into the destination. Such temporary byte arrays are obtained automatically as needed via
 * {@link TransferBufferAllocator} interface.
 * <ul>
 *     <li>In case allocating a buffer for loading input from stream into the inflater is necessary, instances of this
 *     class are guaranteed to allocate a single read buffer per call to
 *     {@link #load(InputStream, int, RawTexLoadTarget, int)}. The minimum required read buffer size id guaranteed to
 *     not exceed 1.</li>
 *     <li>In case allocating buffers is necessary for transferring inflater output, instances of this class are
 *     guaranteed to allocate a single transfer buffer at a time, provided an instance is not accessed from multiple
 *     threads concurrently nor called recursively - i.e. a call to {@link TransferBufferAllocator#allocate(int, int)}
 *     is always followed by a corresponding call to {@link TransferBufferAllocator#free(byte[])} before another
 *     {@link TransferBufferAllocator#allocate(int, int)} is invoked. The minimum required transfer buffer size is
 *     equal to block size.</li>
 *     <li>In case both read buffer and transfer buffers are necessary, at most two buffers obtained from the same
 *     {@link TransferBufferAllocator} instance could be in flight simultaneously.</li>
 * </ul>
 */
public class DeflateBlockDataLoader extends AbstractBlockDataLoader {

    private static final String TRANSFER_BUFFER_ALLOCATOR_NOT_PROVIDED_MESSAGE = "Transfer buffer allocator not provided";
    private static final String INFLATER_NOT_PROVIDED_MESSAGE = "Inflater not provided";

    private final TransferBufferAllocator transferBufferAllocator;

    private final Inflater inflater;
    private final boolean endOnClose;

    /**
     * Constructs a {@code DeflateBlockDataLoader} with the specified input data format, input data endianness and
     * transfer buffer allocator, using an internally allocated instance of an {@link Inflater}.
     *
     * @param format format of the input data (dictating the block size in number of octets)
     * @param endianness endianness of the input data
     * @param transferBufferAllocator an allocator providing temporary byte arrays for reading input into the inflater
     *                                and/or transferring data from inflater into the destination buffer
     *
     * @throws NullPointerException if {@code format}, {@code endianness} or {@code transferBufferAllocator} is {@code null}
     */
    public DeflateBlockDataLoader(RawTexFormat format, Endianness endianness, TransferBufferAllocator transferBufferAllocator) {
        super(format, endianness);

        this.transferBufferAllocator = Objects.requireNonNull(transferBufferAllocator, TRANSFER_BUFFER_ALLOCATOR_NOT_PROVIDED_MESSAGE);

        this.inflater = new Inflater(true);
        this.endOnClose = true;
    }

    /**
     * Constructs a {@code DeflateBlockDataLoader} with the specified input data format, input data endianness, transfer
     * buffer allocator and inflater.
     *
     * @param format format of the input data (dictating the block size in number of octets)
     * @param endianness endianness of the input data
     * @param transferBufferAllocator an allocator providing temporary byte arrays for reading input into the inflater
     *                                and/or transferring data from inflater into the destination buffer
     * @param inflater an instance of an {@link Inflater} to use
     * @param endOnClose whether to call {@link Inflater#end()} on invoking {@link #close()} or not
     *
     * @throws NullPointerException if {@code format}, {@code endianness}, {@code transferBufferAllocator} or {@code inflater} is {@code null}
     */
    public DeflateBlockDataLoader(RawTexFormat format, Endianness endianness, TransferBufferAllocator transferBufferAllocator, Inflater inflater, boolean endOnClose) {
        super(format, endianness);

        this.transferBufferAllocator = Objects.requireNonNull(transferBufferAllocator, TRANSFER_BUFFER_ALLOCATOR_NOT_PROVIDED_MESSAGE);

        this.inflater = Objects.requireNonNull(inflater, INFLATER_NOT_PROVIDED_MESSAGE);
        this.endOnClose = endOnClose;
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
     * @throws IllegalArgumentException if data length is not a multiple of block size
     * @throws IllegalStateException if load target buffer's {@link ByteBuffer#remaining()} is less than block size,
     * is not a multiple of block size or is greater than the remaining data length
     */
    @Override
    public void load(InputStream in, int inputLength, RawTexLoadTarget loadTarget, int dataLength) throws IOException {
        if (dataLength % blockSize != 0) {
            throw new IllegalArgumentException(String.format("Data length (%d) is not a multiple of block size (%d)", dataLength, blockSize));
        }

        if (in instanceof ArraySource) {
            final ArraySource arraySource = (ArraySource) in;
            inflater.setInput(arraySource.array, arraySource.ensureAvailableAndAdvance(inputLength), inputLength);
            loadFromInflater(loadTarget, dataLength);
        } else {
            final int minReadBufferLength = Math.min(inputLength, 1);
            final byte[] readBuffer = transferBufferAllocator.allocate(minReadBufferLength, inputLength);
            try {
                validateTransferBufferAndGetLength(readBuffer, minReadBufferLength);
                inputLength = readIntoInflaterAndReturnRemainingInputLength(in, readBuffer, inputLength);
                if (inputLength > 0) {
                    loadFromStream(in, readBuffer, inputLength, loadTarget, dataLength);
                } else {
                    loadFromInflater(loadTarget, dataLength);
                }
            } finally {
                transferBufferAllocator.free(readBuffer);
            }
        }
    }

    /**
     * Calls {@link Inflater#end()} if this instance either uses in internal inflater or {@code endOnClose} was set to
     * {@code true} on construction of this instance.
     */
    @Override
    public void close() {
        if (endOnClose) {
            inflater.end();
        }
    }

    private void loadFromInflater(RawTexLoadTarget loadTarget, int dataLength) throws IOException {
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
                    inflateFullyViaTransferBuffer(targetBuffer, transferBlockCount);
                } else if (targetBuffer.hasArray()) {
                    inflateFullyIntoArrayBackedBuffer(targetBuffer, transferLength);
                } else {
                    inflateFullyViaTransferBuffer(targetBuffer, transferBlockCount);
                }

                dataOffset += transferLength;
                dataLength -= transferLength;
                complete = true;
            } finally {
                loadTarget.release(targetBuffer, complete);
            }
        }
    }

    private void loadFromStream(InputStream in, byte[] readBuffer, int remainingInput, RawTexLoadTarget loadTarget, int dataLength) throws IOException {
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
                    remainingInput = inflateViaTransferBufferAndReturnRemainingInputLength(in, readBuffer, remainingInput, targetBuffer, transferBlockCount);
                } else if (targetBuffer.hasArray()) {
                    remainingInput = inflateIntoArrayBackedBufferAndReturnRemainingInputLength(in, readBuffer, remainingInput, targetBuffer, transferLength);
                } else {
                    remainingInput = inflateViaTransferBufferAndReturnRemainingInputLength(in, readBuffer, remainingInput, targetBuffer, transferBlockCount);
                }

                dataOffset += transferLength;
                dataLength -= transferLength;
                complete = true;
            } finally {
                loadTarget.release(targetBuffer, complete);
            }
        }
    }

    private void inflateFullyViaTransferBuffer(ByteBuffer out, int blockCount) throws IOException {
        byte[] transferBuffer = transferBufferAllocator.allocate(blockSize, blockSize * blockCount);

        try {
            final int transferBufferBlockCount = validateTransferBufferAndGetLength(transferBuffer, blockSize) / blockSize;

            do {
                final int inflateBlockCount = Math.min(blockCount, transferBufferBlockCount);
                final int inflateLength = blockSize * inflateBlockCount;

                if (inflater.inflate(transferBuffer, 0, inflateLength) != inflateLength) {
                    throw CommonIO.unexpectedEndOfInputException();
                }

                transferBlockWise(transferBuffer, 0, inflateBlockCount, out);

                blockCount -= inflateBlockCount;
            } while (blockCount > 0);
        } catch (DataFormatException e) {
            throw compressedDataFormatException(e);
        } finally {
            transferBufferAllocator.free(transferBuffer);
        }
    }

    private void inflateFullyIntoArrayBackedBuffer(ByteBuffer out, int length) throws IOException {
        final int position = out.position();

        try {
            if (inflater.inflate(out.array(), out.arrayOffset() + position, length) != length) {
                throw CommonIO.unexpectedEndOfInputException();
            }
        } catch (DataFormatException e) {
            throw compressedDataFormatException(e);
        }

        out.position(position + length);
    }

    private int inflateViaTransferBufferAndReturnRemainingInputLength(
            InputStream in, byte[] readBuffer, int remainingInputLength, ByteBuffer out, int blockCount
    ) throws IOException {
        final byte[] transferBuffer = transferBufferAllocator.allocate(blockSize, blockSize * blockCount);

        try {
            final int transferBufferBlockCount = validateTransferBufferAndGetLength(transferBuffer, blockSize) / blockSize;

            do {
                final int inflateBlockCount = Math.min(blockCount, transferBufferBlockCount);

                remainingInputLength = inflateAndReturnRemainingInputLength(
                        in, readBuffer, remainingInputLength,
                        transferBuffer, 0, blockSize * inflateBlockCount
                );

                transferBlockWise(transferBuffer, 0, inflateBlockCount, out);

                blockCount -= inflateBlockCount;
            } while (blockCount > 0);
        } finally {
            transferBufferAllocator.free(transferBuffer);
        }

        return remainingInputLength;
    }

    private int inflateIntoArrayBackedBufferAndReturnRemainingInputLength(
            InputStream in, byte[] readBuffer, int remainingInputLength, ByteBuffer out, int outLength
    ) throws IOException {
        final int position = out.position();

        remainingInputLength = inflateAndReturnRemainingInputLength(
                in, readBuffer, remainingInputLength,
                out.array(), out.arrayOffset() + position, outLength
        );

        out.position(position + outLength);

        return remainingInputLength;
    }

    private int inflateAndReturnRemainingInputLength(
            InputStream in, byte[] readBuffer, int remainingInputLength,
            byte[] outBuffer, int outOffset, int outLength
    ) throws IOException {
        try {
            int inflatedLength = inflater.inflate(outBuffer, outOffset, outLength);

            outOffset += inflatedLength;
            outLength -= inflatedLength;

            while (outLength > 0) {
                if (remainingInputLength <= 0) {
                    throw CommonIO.unexpectedEndOfInputException();
                }

                remainingInputLength = readIntoInflaterAndReturnRemainingInputLength(in, readBuffer, remainingInputLength);
                inflatedLength = inflater.inflate(outBuffer, outOffset, outLength);

                outOffset += inflatedLength;
                outLength -= inflatedLength;
            }
        } catch (DataFormatException e) {
            throw compressedDataFormatException(e);
        }

        return remainingInputLength;
    }

    private int readIntoInflaterAndReturnRemainingInputLength(InputStream in, byte[] readBuffer, int remainingInputLength) throws IOException {
        final int readLength = Math.min(readBuffer.length, remainingInputLength);

        CommonIO.readOctets(in, readBuffer, 0, readLength);
        inflater.setInput(readBuffer, 0, readLength);

        return (remainingInputLength - readLength);
    }

    private static int validateTransferBufferAndGetLength(byte[] transferBuffer, int minimumLength) {
        if (transferBuffer == null) {
            throw new NullPointerException("Transfer buffer missing");
        }

        final int transferBufferLength = transferBuffer.length;

        if (transferBufferLength < minimumLength) {
            throw new IllegalStateException("Transfer buffer too short: " + transferBufferLength);
        }

        return transferBufferLength;
    }

    private static IOException compressedDataFormatException(DataFormatException e) {
        return new RawTexInvalidInputException("Invalid DEFLATE data stream", e);
    }

}
