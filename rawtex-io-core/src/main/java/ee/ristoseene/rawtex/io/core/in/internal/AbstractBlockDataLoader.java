package ee.ristoseene.rawtex.io.core.in.internal;

import ee.ristoseene.rawtex.io.core.common.format.BlockSize;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;
import ee.ristoseene.rawtex.io.core.in.RawTexDataLoader;

import java.nio.ByteBuffer;
import java.util.Objects;

public abstract class AbstractBlockDataLoader implements RawTexDataLoader {

    protected final Endianness endianness;
    protected final BlockSize blockSize;

    protected AbstractBlockDataLoader(Endianness endianness, BlockSize blockSize) {
        this.endianness = Objects.requireNonNull(endianness, "Endianness not provided");
        this.blockSize = Objects.requireNonNull(blockSize, "Block size not provided");
    }

    protected void ensureDataLengthIsValidMultipleOfBlockSize(int dataLength) {
        if (dataLength < blockSize.octets || blockSize.remainderOf(dataLength) != 0) {
            throw new IllegalArgumentException(String.format(
                    "Data length (%d) is not a valid multiple of block size (%d)",
                    dataLength, blockSize.octets
            ));
        }
    }

    protected static void ensureTargetBufferNotNull(ByteBuffer targetBuffer) {
        if (targetBuffer == null) {
            throw new NullPointerException("Target buffer is missing");
        }
    }

    protected int validateTargetBufferAndReturnLength(ByteBuffer targetBuffer, int remainingDataLength) {
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

    protected boolean isDirectTransferPossibleForTargetBuffer(ByteBuffer targetBuffer) {
        return (blockSize == BlockSize.OCTETS_1) || (targetBuffer.order() == endianness.byteOrder);
    }

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
