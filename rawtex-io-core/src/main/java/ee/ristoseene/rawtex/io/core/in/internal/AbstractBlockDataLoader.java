package ee.ristoseene.rawtex.io.core.in.internal;

import ee.ristoseene.rawtex.io.core.common.format.BlockSize;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * An abstract base class for data loaders capable of loading data with specific endianness and block size.
 * <p>
 * This base class encapsulates no mutable state and supports thread-safe implementations.
 */
public abstract class AbstractBlockDataLoader {

    /**
     * Endianness of the data to load.
     */
    protected final Endianness endianness;

    /**
     * Block size of the data to load.
     */
    protected final BlockSize blockSize;

    /**
     * Constructs an {@code AbstractBlockDataLoader} with the specified endianness and block size.
     *
     * @param endianness endianness of the data to load
     * @param blockSize block size of the data to load
     *
     * @throws NullPointerException if {@code endianness} or {@code blockSize} is {@code null}
     */
    protected AbstractBlockDataLoader(Endianness endianness, BlockSize blockSize) {
        this.endianness = Objects.requireNonNull(endianness, "Endianness not provided");
        this.blockSize = Objects.requireNonNull(blockSize, "Block size not provided");
    }

    /**
     * Ensures that the specified data length is a valid multiple of the block size of the data to load.
     * Throws an exception if the specified data length is less than the block size of the data to load,
     * or if the specified data length is not a valid multiple of the block size of the data to load.
     *
     * @param dataLength the data length to test
     *
     * @throws IllegalArgumentException if {@code dataLength} is less than the block size
     * of the data to load or not a valid multiple of the block size of the data to load
     */
    protected void ensureDataLengthIsValidMultipleOfBlockSize(long dataLength) {
        if (dataLength < blockSize.octets || blockSize.remainderOf(dataLength) != 0L) {
            throw new IllegalArgumentException(String.format(
                    "Data length (%d) is not a valid multiple of block size (%d)",
                    dataLength, blockSize.octets
            ));
        }
    }

    /**
     * Ensures that the specified byte buffer is not {@code null}.
     *
     * @param targetBuffer the byte buffer to test
     *
     * @throws NullPointerException if the specified byte buffer if {@code null}
     */
    protected static void ensureTargetBufferNotNull(ByteBuffer targetBuffer) {
        if (targetBuffer == null) {
            throw new NullPointerException("Target buffer is missing");
        }
    }

    /**
     * Ensures that the specified byte buffer qualifies as a target buffer for data loading operations,
     * and returns its remaining length.
     * <p>
     * A byte buffer is considered as a valid target buffer if it is <b>not</b> a
     * {@link ByteBuffer#isReadOnly() read-only} buffer and its {@link ByteBuffer#remaining() remaining length}
     * satisfies the following conditions:
     * <ul>
     *     <li>the remaining length of the buffer does not exceed the specified remaining data length</li>
     *     <li>the remaining length of the buffer fits at least a single block of data</li>
     *     <li>the remaining length of the buffer is a multiple of the block size of the data to load</li>
     * </ul>
     *
     * @param targetBuffer the byte buffer to validate
     * @param remainingDataLength the remaining length of the data to load
     *
     * @return the {@link ByteBuffer#remaining() remaining length} of the byte buffer
     *
     * @throws IllegalStateException if the byte buffer is read-only or
     * if the remaining length of the buffer is not a positive multiple
     * of the block size of the data to load
     *
     * @see ByteBuffer#isReadOnly()
     * @see ByteBuffer#remaining()
     */
    protected int validateTargetBufferAndReturnLength(ByteBuffer targetBuffer, long remainingDataLength) {
        if (targetBuffer.isReadOnly()) {
            throw new IllegalStateException("Target buffer is read-only");
        }

        final int targetRemainingLength = targetBuffer.remaining();

        if (
                (targetRemainingLength > remainingDataLength) ||
                (targetRemainingLength < blockSize.octets) ||
                (blockSize.remainderOf(targetRemainingLength) != 0)
        ) {
            throw new IllegalStateException("Invalid target buffer length: " + targetRemainingLength);
        }

        return targetRemainingLength;
    }

    /**
     * Returns {@code true} if input data can be transferred into the specified byte buffer without
     * explicit endianness conversion, or {@code false} otherwise.
     * <p>
     * Data is considered directly transferable if the block size of the data to load is not greater than {@code 1}
     * or if the byte order of the target buffer is the same as the byte order of the data to load.
     *
     * @param targetBuffer the byte buffer to test
     *
     * @return {@code true} if input data can be transferred into the specified byte buffer without
     * an explicit endianness conversion, or {@code false} otherwise
     */
    protected boolean isDirectTransferPossibleForTargetBuffer(ByteBuffer targetBuffer) {
        return (blockSize == BlockSize.OCTETS_1) || (targetBuffer.order() == endianness.byteOrder);
    }

    /**
     * Transfers data from the input array into the destination buffer in chunks equal to the block size
     * of the data to load, performing automatic endianness conversion as necessary.
     *
     * @param in input array to transfer data from, not {@code null}
     * @param inOffset the starting offset of the input data in the input array {@code in}
     * @param blockCount the number of blocks of data to transfer
     * @param out output buffer to transfer data to, not {@code null}
     */
    protected void transferBlockWise(byte[] in, int inOffset, int blockCount, ByteBuffer out) {
        final int limit = inOffset + blockSize.multipleOf(blockCount);

        switch (blockSize) {

            case OCTETS_1:
                for (int i = inOffset; i < limit; i += Byte.BYTES) {
                    out.put(endianness.read8(in, i));
                }
                break;

            case OCTETS_2:
                for (int i = inOffset; i < limit; i += Short.BYTES) {
                    out.putShort(endianness.read16(in, i));
                }
                break;

            case OCTETS_4:
                for (int i = inOffset; i < limit; i += Integer.BYTES) {
                    out.putInt(endianness.read32(in, i));
                }
                break;

            case OCTETS_8:
                for (int i = inOffset; i < limit; i += Long.BYTES) {
                    out.putLong(endianness.read64(in, i));
                }
                break;

            default:
                throw new IllegalStateException("Unsupported block size: " + blockSize);

        }
    }

}
