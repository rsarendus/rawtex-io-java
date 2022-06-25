package ee.ristoseene.rawtex.io.core.common.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public final class TestBufferUtils {

    public static byte[] generateRandom8(int count) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        byte[] destination = new byte[count];
        for (int i = 0; i < count; ++i) {
            destination[i] = (byte) (random.nextInt() & 0xff);
        }
        return destination;
    }

    public static short[] generateRandom16(int count) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        short[] destination = new short[count];
        for (int i = 0; i < count; ++i) {
            destination[i] = (short) (random.nextInt() & 0xffff);
        }
        return destination;
    }

    public static int[] generateRandom32(int count) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int[] destination = new int[count];
        for (int i = 0; i < count; ++i) {
            destination[i] = random.nextInt();
        }
        return destination;
    }

    public static long[] generateRandom64(int count) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long[] destination = new long[count];
        for (int i = 0; i < count; ++i) {
            destination[i] = random.nextLong();
        }
        return destination;
    }

    public static byte[] toBytes(short[] source, ByteOrder endianness) {
        byte[] destination = new byte[source.length * Short.BYTES];
        ByteBuffer.wrap(destination).order(endianness).asShortBuffer().put(source);
        return destination;
    }

    public static byte[] toBytes(int[] source, ByteOrder endianness) {
        byte[] destination = new byte[source.length * Integer.BYTES];
        ByteBuffer.wrap(destination).order(endianness).asIntBuffer().put(source);
        return destination;
    }

    public static byte[] toBytes(long[] source, ByteOrder endianness) {
        byte[] destination = new byte[source.length * Long.BYTES];
        ByteBuffer.wrap(destination).order(endianness).asLongBuffer().put(source);
        return destination;
    }

    public static byte[] createByteArray(int length, byte filler) {
        byte[] destination = new byte[length];
        Arrays.fill(destination, filler);
        return destination;
    }

    public static byte[] createByteArray(int length, byte filler, int offset, byte[] bytes) {
        byte[] destination = createByteArray(length, filler);
        System.arraycopy(destination, offset, bytes, 0, bytes.length);
        return destination;
    }

    public static byte[] createByteArray(int length, byte filler, int offset, IntStream byteStream) {
        byte[] destination = createByteArray(length, filler);
        final AtomicInteger byteOffset = new AtomicInteger(offset);
        byteStream.forEach(i -> {
            destination[byteOffset.getAndIncrement()] = (byte) (i & 0xff);
        });
        return destination;
    }

    public static void zeroFillBuffer(ByteBuffer buffer, int offset, int length) {
        if (offset < 0 || length < 0 || buffer.capacity() - offset < length) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (buffer.hasArray()) {
            final int fromIndex = buffer.arrayOffset() + offset;
            final int toIndex = fromIndex + length;

            Arrays.fill(buffer.array(), fromIndex, toIndex, (byte) 0);
        } else {
            final int blockSize = Long.BYTES;
            final int remainder = length % blockSize;
            final int blockWiseLimit = length - remainder + offset;

            // It's significantly faster to write larger blocks instead of individual bytes
            for (int i = offset; i < blockWiseLimit; i += blockSize) {
                buffer.putLong(i, 0L);
            }

            // Write the remaining bytes
            for (int i = blockWiseLimit; i < length; ++i) {
                buffer.put(i, (byte) 0);
            }
        }
    }

    private TestBufferUtils() {
        throw new UnsupportedOperationException();
    }

}
