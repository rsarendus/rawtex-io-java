package ee.ristoseene.rawtex.common.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ThreadLocalRandom;

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

    private TestBufferUtils() {}

}
