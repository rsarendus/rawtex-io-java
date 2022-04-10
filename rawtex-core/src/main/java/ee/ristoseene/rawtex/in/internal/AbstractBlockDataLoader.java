package ee.ristoseene.rawtex.in.internal;

import ee.ristoseene.rawtex.common.RawTexFormat;
import ee.ristoseene.rawtex.common.internal.Endianness;
import ee.ristoseene.rawtex.in.RawTexDataLoader;

import java.nio.ByteBuffer;
import java.util.Objects;

public abstract class AbstractBlockDataLoader implements RawTexDataLoader {

    protected final Endianness endianness;
    protected final int blockSize;

    protected AbstractBlockDataLoader(RawTexFormat format, Endianness endianness) {
        this.blockSize = Objects.requireNonNull(format, "Format not provided").getOctetsPerBlock();
        this.endianness = Objects.requireNonNull(endianness, "Endianness not provided");
    }

    protected int validateTargetBufferAndAcquireBlockCount(ByteBuffer targetBuffer) {
        if (targetBuffer == null) {
            throw new NullPointerException("Target buffer missing");
        } else if (targetBuffer.isReadOnly()) {
            throw new IllegalStateException("Target buffer is read-only");
        }

        final int targetRemainingLength = targetBuffer.remaining();

        if (targetRemainingLength < blockSize || targetRemainingLength % blockSize != 0) {
            throw invalidTargetBufferLengthException(targetRemainingLength);
        }

        return targetRemainingLength / blockSize;
    }

    protected static IllegalStateException invalidTargetBufferLengthException(int length) {
        return new IllegalStateException("Invalid target buffer length: " + length);
    }

    protected void transferBlockWise(byte[] in, int inOffset, int blockCount, ByteBuffer out) {
        final int limit = inOffset + blockSize * blockCount;

        switch (blockSize) {

            case 1:
                for (int i = inOffset; i < limit; ++i) {
                    out.put(endianness.read8(in, i));
                }
                break;

            case 2:
                for (int i = inOffset; i < limit; i += 2) {
                    out.putShort(endianness.read16(in, i));
                }
                break;

            case 4:
                for (int i = inOffset; i < limit; i += 4) {
                    out.putInt(endianness.read32(in, i));
                }
                break;

            case 8:
                for (int i = inOffset; i < limit; i += 8) {
                    out.putLong(endianness.read64(in, i));
                }
                break;

            default:
                throw new IllegalStateException("Unsupported block size: " + blockSize);

        }
    }

}
