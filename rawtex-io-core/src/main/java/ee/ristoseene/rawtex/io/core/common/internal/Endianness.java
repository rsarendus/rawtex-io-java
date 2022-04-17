package ee.ristoseene.rawtex.io.core.common.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public enum Endianness {

    BIG_ENDIAN(ByteOrder.BIG_ENDIAN) {

        @Override
        public short read16(InputStream in) throws IOException {
            return (short) (
                    ((CommonIO.readOctet(in) & 0xff) << 8) |
                     (CommonIO.readOctet(in) & 0xff)
            );
        }

        @Override
        public int read32(InputStream in) throws IOException {
            return
                    ((CommonIO.readOctet(in) & 0xff) << 24) |
                    ((CommonIO.readOctet(in) & 0xff) << 16) |
                    ((CommonIO.readOctet(in) & 0xff) <<  8) |
                     (CommonIO.readOctet(in) & 0xff)
            ;
        }

        @Override
        public long read64(InputStream in) throws IOException {
            return
                    ((CommonIO.readOctet(in) & 0xffL) << 56) |
                    ((CommonIO.readOctet(in) & 0xffL) << 48) |
                    ((CommonIO.readOctet(in) & 0xffL) << 40) |
                    ((CommonIO.readOctet(in) & 0xffL) << 32) |
                    ((CommonIO.readOctet(in) & 0xffL) << 24) |
                    ((CommonIO.readOctet(in) & 0xffL) << 16) |
                    ((CommonIO.readOctet(in) & 0xffL) <<  8) |
                     (CommonIO.readOctet(in) & 0xffL)
            ;
        }

        @Override
        public short read16(byte[] in, int offset) {
            return (short) (
                    ((in[  offset] & 0xff) << 8) |
                     (in[++offset] & 0xff)
            );
        }

        @Override
        public int read32(byte[] in, int offset) {
            return
                    ((in[  offset] & 0xff) << 24) |
                    ((in[++offset] & 0xff) << 16) |
                    ((in[++offset] & 0xff) <<  8) |
                     (in[++offset] & 0xff)
            ;
        }

        @Override
        public long read64(byte[] in, int offset) {
            return
                    ((in[  offset] & 0xffL) << 56) |
                    ((in[++offset] & 0xffL) << 48) |
                    ((in[++offset] & 0xffL) << 40) |
                    ((in[++offset] & 0xffL) << 32) |
                    ((in[++offset] & 0xffL) << 24) |
                    ((in[++offset] & 0xffL) << 16) |
                    ((in[++offset] & 0xffL) <<  8) |
                     (in[++offset] & 0xffL)
            ;
        }

    },

    LITTLE_ENDIAN(ByteOrder.LITTLE_ENDIAN) {

        @Override
        public short read16(InputStream in) throws IOException {
            return (short) (
                     (CommonIO.readOctet(in) & 0xff) |
                    ((CommonIO.readOctet(in) & 0xff) << 8)
            );
        }

        @Override
        public int read32(InputStream in) throws IOException {
            return
                     (CommonIO.readOctet(in) & 0xff)        |
                    ((CommonIO.readOctet(in) & 0xff) <<  8) |
                    ((CommonIO.readOctet(in) & 0xff) << 16) |
                    ((CommonIO.readOctet(in) & 0xff) << 24)
            ;
        }

        @Override
        public long read64(InputStream in) throws IOException {
            return
                     (CommonIO.readOctet(in) & 0xffL)        |
                    ((CommonIO.readOctet(in) & 0xffL) <<  8) |
                    ((CommonIO.readOctet(in) & 0xffL) << 16) |
                    ((CommonIO.readOctet(in) & 0xffL) << 24) |
                    ((CommonIO.readOctet(in) & 0xffL) << 32) |
                    ((CommonIO.readOctet(in) & 0xffL) << 40) |
                    ((CommonIO.readOctet(in) & 0xffL) << 48) |
                    ((CommonIO.readOctet(in) & 0xffL) << 56)
            ;
        }

        @Override
        public short read16(byte[] in, int offset) {
            return (short) (
                     (in[  offset] & 0xff) |
                    ((in[++offset] & 0xff) << 8)
            );
        }

        @Override
        public int read32(byte[] in, int offset) {
            return
                     (in[  offset] & 0xff)        |
                    ((in[++offset] & 0xff) <<  8) |
                    ((in[++offset] & 0xff) << 16) |
                    ((in[++offset] & 0xff) << 24)
            ;
        }

        @Override
        public long read64(byte[] in, int offset) {
            return
                     (in[  offset] & 0xffL)        |
                    ((in[++offset] & 0xffL) <<  8) |
                    ((in[++offset] & 0xffL) << 16) |
                    ((in[++offset] & 0xffL) << 24) |
                    ((in[++offset] & 0xffL) << 32) |
                    ((in[++offset] & 0xffL) << 40) |
                    ((in[++offset] & 0xffL) << 48) |
                    ((in[++offset] & 0xffL) << 56)
            ;
        }

    };

    public final ByteOrder byteOrder;

    Endianness(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public byte read8(InputStream in) throws IOException {
        return CommonIO.readOctet(in);
    }

    public abstract short read16(InputStream in) throws IOException;
    public abstract int read32(InputStream in) throws IOException;
    public abstract long read64(InputStream in) throws IOException;

    public byte read8(byte[] in, int offset) {
        return in[offset];
    }

    public abstract short read16(byte[] in, int offset);
    public abstract int read32(byte[] in, int offset);
    public abstract long read64(byte[] in, int offset);

    public static Endianness of(ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return LITTLE_ENDIAN;
        } else if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return BIG_ENDIAN;
        } else {
            throw new IllegalArgumentException("Invalid endianness: " + byteOrder);
        }
    }

}
