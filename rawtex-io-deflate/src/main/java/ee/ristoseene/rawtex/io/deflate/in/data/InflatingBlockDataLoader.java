package ee.ristoseene.rawtex.io.deflate.in.data;

import ee.ristoseene.rawtex.io.core.common.data.TransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.common.exceptions.RawTexInvalidInputException;
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
import java.util.Objects;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * An implementation of {@link RawTexDataLoader} for loading compressed data stored in DEFLATE format from input
 * streams into {@link ByteBuffer}s, providing automatic endianness conversion as necessary.
 * The loader counts loadable data in blocks, thus the loader can only load and transfer data into destination
 * buffers in chunks equal to multiples of specific block size.
 * <p>
 * In case input data is not {@link ArraySource backed by a byte array}, then a temporary byte array needs to be
 * allocated in order to be able to load input from stream into the inflater. In case target buffer is either not
 * an array-backed buffer or endianness conversion is necessary, then temporary byte arrays need to be allocated
 * in order to be able to transfer inflater output into the destination. Such temporary byte arrays are obtained
 * automatically as needed via the {@link TransferBufferAllocator} interface.
 * <ul>
 *     <li>In case allocating a buffer for loading input from a stream into the inflater is necessary, a call to
 *     {@link #load(InputStream, int, RawTexLoadTarget, int) load} is guaranteed to allocate a single read buffer
 *     for that purpose. The minimum required read buffer size is guaranteed to not exceed
 *     {@value #MINIMUM_READ_LENGTH_FOR_INFLATION}.</li>
 *     <li>In case allocating buffers is necessary for transferring inflater output, a call to
 *     {@link #load(InputStream, int, RawTexLoadTarget, int) load} is guaranteed to allocate a single transfer
 *     buffer at a time - i.e. a call to {@link TransferBufferAllocator#allocate(int, int)} is always followed by
 *     a corresponding call to {@link TransferBufferAllocator#free(byte[])} before another
 *     {@link TransferBufferAllocator#allocate(int, int)} is invoked. The minimum required transfer buffer size is
 *     equal to {@link #blockSize block size}.</li>
 *     <li>In case both read buffer and transfer buffers are necessary, at most two buffers obtained from the same
 *     {@link TransferBufferAllocator} instance could be in flight simultaneously.</li>
 * </ul>
 * <p>
 * In case any input buffers are bound to the inflater allocated via the {@link InflaterAllocator} interface,
 * the {@link Inflater#reset() inflater is guaranteed to be reset} before it is returned to its allocator in order
 * to prevent any leakage from the scope of {@link #load(InputStream, int, RawTexLoadTarget, int) load}.
 * <p>
 * Instances of this class hold no direct mutable state, and are thread-safe as long as the
 * implementations of the assigned {@link InflaterAllocator} and {@link TransferBufferAllocator} are thread-safe.
 */
public class InflatingBlockDataLoader extends AbstractTransferBufferingBlockDataLoader implements RawTexDataLoader {

    private static final int MINIMUM_INPUT_LENGTH = 1;
    private static final int MINIMUM_READ_LENGTH_FOR_INFLATION = 1;

    private final InflaterAllocator inflaterAllocator;

    /**
     * Constructs an {@code InflatingBlockDataLoader} with the specified input data endianness, block size,
     * inflater allocator and transfer buffer allocator.
     *
     * @param endianness endianness of the input data
     * @param blockSize block size of the input data
     * @param inflaterAllocator an allocator providing temporary instances of {@link Inflater} for decompressing the
     *                          input data
     * @param transferBufferAllocator an allocator providing temporary byte arrays for reading input into the inflater
     *                                and/or transferring data from inflater into the destination buffer
     *
     * @throws NullPointerException if {@code endianness}, {@code blockSize}, {@code inflaterAllocator}
     * or {@code transferBufferAllocator} is {@code null}
     */
    public InflatingBlockDataLoader(Endianness endianness, BlockSize blockSize, InflaterAllocator inflaterAllocator, TransferBufferAllocator transferBufferAllocator) {
        super(endianness, blockSize, transferBufferAllocator);

        this.inflaterAllocator = Objects.requireNonNull(inflaterAllocator, "Inflater allocator not provided");
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
     * @throws IllegalStateException if destination buffer's {@link ByteBuffer#remaining()} is less than block size,
     * is not a multiple of block size or is greater than the remaining data length
     */
    @Override
    public void load(InputStream in, int inputLength, RawTexLoadTarget loadTarget, int dataLength) throws IOException {
        ensureDataLengthIsValidMultipleOfBlockSize(dataLength);

        if (inputLength < MINIMUM_INPUT_LENGTH) {
            throw new IllegalArgumentException("Invalid input length: " + inputLength);
        }

        final Inflater inflater = inflaterAllocator.allocate();

        if (inflater == null) {
            throw new NullPointerException("Inflater is missing");
        }

        try {
            ensureInflaterStateForInput(inflater);

            if (in instanceof ArraySource) {
                load(inflater, (ArraySource) in, inputLength, loadTarget, dataLength);
            } else {
                load(inflater, in, inputLength, loadTarget, dataLength);
            }
        } finally {
            inflaterAllocator.free(inflater);
        }
    }

    private void load(Inflater inflater, ArraySource in, int inputLength, RawTexLoadTarget loadTarget, int dataLength) throws IOException {
        final int inputOffset = in.ensureAvailableAndAdvance(inputLength);

        try {
            inflater.setInput(in.array, inputOffset, inputLength);
            loadFromInflater(inflater, dataLength, loadTarget);
        } finally {
            inflater.reset();
        }
    }

    private void load(Inflater inflater, InputStream in, int inputLength, RawTexLoadTarget loadTarget, int dataLength) throws IOException {
        final byte[] readBuffer = allocateTransferBuffer(MINIMUM_READ_LENGTH_FOR_INFLATION, inputLength);

        try {
            final int readLength = Math.min(validateTransferBufferAndReturnLength(readBuffer, MINIMUM_READ_LENGTH_FOR_INFLATION), inputLength);

            CommonIO.readOctets(in, readBuffer, 0, readLength);
            inputLength -= readLength;

            try {
                inflater.setInput(readBuffer, 0, readLength);

                if (inputLength > 0) {
                    loadFromStream(new InflationState(inflater, readBuffer, in, inputLength), dataLength, loadTarget);
                } else {
                    loadFromInflater(inflater, dataLength, loadTarget);
                }
            } finally {
                inflater.reset();
            }
        } finally {
            transferBufferAllocator.free(readBuffer);
        }
    }

    private void loadFromInflater(Inflater inflater, int remainingLength, RawTexLoadTarget loadTarget) throws IOException {
        int dataOffset = 0;

        do {
            final ByteBuffer targetBuffer = loadTarget.acquire(dataOffset, remainingLength);
            ensureTargetBufferNotNull(targetBuffer);

            boolean complete = false;
            try {
                final int transferLength = validateTargetBufferAndReturnLength(targetBuffer, remainingLength);

                if (isDirectTransferPossibleForTargetBuffer(targetBuffer) && targetBuffer.hasArray()) {
                    inflateFullyIntoArrayBackedBuffer(inflater, targetBuffer, transferLength);
                } else {
                    inflateFullyViaTransferBuffer(inflater, targetBuffer, blockSize.quotientOf(transferLength));
                }

                dataOffset += transferLength;
                remainingLength -= transferLength;
                complete = true;
            } finally {
                loadTarget.release(targetBuffer, complete);
            }
        } while (remainingLength > 0);
    }

    private void loadFromStream(InflationState inflationState, int remainingLength, RawTexLoadTarget loadTarget) throws IOException {
        int dataOffset = 0;

        do {
            final ByteBuffer targetBuffer = loadTarget.acquire(dataOffset, remainingLength);
            ensureTargetBufferNotNull(targetBuffer);

            boolean complete = false;
            try {
                final int transferLength = validateTargetBufferAndReturnLength(targetBuffer, remainingLength);

                if (isDirectTransferPossibleForTargetBuffer(targetBuffer) && targetBuffer.hasArray()) {
                    inflateIntoArrayBackedBuffer(inflationState, targetBuffer, transferLength);
                } else {
                    inflateViaTransferBuffer(inflationState, targetBuffer, blockSize.quotientOf(transferLength));
                }

                dataOffset += transferLength;
                remainingLength -= transferLength;
                complete = true;
            } finally {
                loadTarget.release(targetBuffer, complete);
            }
        } while (remainingLength > 0);
    }

    private void inflateFullyViaTransferBuffer(Inflater inflater, ByteBuffer out, int blockCount) throws IOException {
        byte[] transferBuffer = allocateTransferBufferForBlockWiseTransfers(blockCount);

        try {
            final int transferBufferBlockCount = validateTransferBufferForBlockWiseTransfersAndReturnBlockCount(transferBuffer);

            do {
                final int inflateBlockCount = Math.min(blockCount, transferBufferBlockCount);
                final int inflateLength = blockSize.multipleOf(inflateBlockCount);

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

    private static void inflateFullyIntoArrayBackedBuffer(Inflater inflater, ByteBuffer out, int length) throws IOException {
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

    private void inflateViaTransferBuffer(InflationState inflationState, ByteBuffer out, int blockCount) throws IOException {
        final byte[] transferBuffer = allocateTransferBufferForBlockWiseTransfers(blockCount);

        try {
            final int transferBufferBlockCount = validateTransferBufferForBlockWiseTransfersAndReturnBlockCount(transferBuffer);

            do {
                final int inflateBlockCount = Math.min(blockCount, transferBufferBlockCount);

                inflateIntoArray(inflationState, transferBuffer, 0, blockSize.multipleOf(inflateBlockCount));
                transferBlockWise(transferBuffer, 0, inflateBlockCount, out);

                blockCount -= inflateBlockCount;
            } while (blockCount > 0);
        } finally {
            transferBufferAllocator.free(transferBuffer);
        }
    }

    private static void inflateIntoArrayBackedBuffer(InflationState inflationState, ByteBuffer out, int length) throws IOException {
        final int position = out.position();

        inflateIntoArray(inflationState, out.array(), out.arrayOffset() + position, length);
        out.position(position + length);
    }

    private static void inflateIntoArray(InflationState inflationState, byte[] out, int offset, int length) throws IOException {
        try {
            int inflatedLength = inflationState.inflater.inflate(out, offset, length);

            offset += inflatedLength;
            length -= inflatedLength;

            while (length > 0) {
                inflationState.readNextBatchIntoInflater();
                inflatedLength = inflationState.inflater.inflate(out, offset, length);

                offset += inflatedLength;
                length -= inflatedLength;
            }
        } catch (DataFormatException e) {
            throw compressedDataFormatException(e);
        }
    }

    private static IOException compressedDataFormatException(DataFormatException e) {
        return new RawTexInvalidInputException("Invalid DEFLATE data stream", e);
    }

    static final class InflationState {

        public final Inflater inflater;

        private final InputStream in;
        private final byte[] readBuffer;

        private int remainingInputLength;

        public InflationState(Inflater inflater, byte[] readBuffer, InputStream in, int inputLength) {
            this.inflater = Objects.requireNonNull(inflater);
            this.readBuffer = Objects.requireNonNull(readBuffer);
            this.in = Objects.requireNonNull(in);

            remainingInputLength = inputLength;
        }

        public void readNextBatchIntoInflater() throws IOException {
            if (remainingInputLength <= 0) {
                throw CommonIO.unexpectedEndOfInputException();
            }

            ensureInflaterStateForInput(inflater);

            final int readLength = Math.min(remainingInputLength, readBuffer.length);

            CommonIO.readOctets(in, readBuffer, 0, readLength);
            inflater.setInput(readBuffer, 0, readLength);

            remainingInputLength -= readLength;
        }

    }

    static void ensureInflaterStateForInput(Inflater inflater) {
        if (inflater.needsDictionary() || !inflater.needsInput()) {
            throw new IllegalStateException("Inflater is in an unexpected state");
        }
    }

}
