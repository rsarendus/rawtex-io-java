package ee.ristoseene.rawtex.io.core.common.test;

import ee.ristoseene.rawtex.io.core.common.format.Endianness;

public final class BitTwiddlingUtils {

    public static byte byteAt(short value, int byteIndex, Endianness endianness) {
        switch (endianness) {
            case BIG_ENDIAN:
                return (byte) ((value >>> (8 - (byteIndex & 0b1) * 8)) & 0xff);
            case LITTLE_ENDIAN:
                return (byte) ((value >>> ((byteIndex & 0b1) * 8)) & 0xff);
            default:
                throw new IllegalArgumentException(endianness.toString());
        }
    }

    public static byte byteAt(int value, int byteIndex, Endianness endianness) {
        switch (endianness) {
            case BIG_ENDIAN:
                return (byte) ((value >>> (24 - (byteIndex & 0b11) * 8)) & 0xff);
            case LITTLE_ENDIAN:
                return (byte) ((value >>> ((byteIndex & 0b11) * 8)) & 0xff);
            default:
                throw new IllegalArgumentException(endianness.toString());
        }
    }

    public static byte byteAt(long value, int byteIndex, Endianness endianness) {
        switch (endianness) {
            case BIG_ENDIAN:
                return (byte) ((value >>> (56 - (byteIndex & 0b111) * 8)) & 0xffL);
            case LITTLE_ENDIAN:
                return (byte) ((value >>> ((byteIndex & 0b111) * 8)) & 0xffL);
            default:
                throw new IllegalArgumentException(endianness.toString());
        }
    }

    private BitTwiddlingUtils() {
        throw new UnsupportedOperationException();
    }

}
