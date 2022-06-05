package ee.ristoseene.rawtex.io.core.common.format;

import ee.ristoseene.rawtex.io.core.common.internal.CommonIO;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * An enum representing endianness.
 */
public enum Endianness {

    /**
     * The singleton instance representing the big-endian byte order.
     */
    BIG_ENDIAN(ByteOrder.BIG_ENDIAN) {

        @Override
        public final short read16(InputStream in) throws IOException {
            return (short) (
                    ((CommonIO.readOctet(in) & 0xff) << 8) |
                     (CommonIO.readOctet(in) & 0xff)
            );
        }

        @Override
        public final int read32(InputStream in) throws IOException {
            return
                    ((CommonIO.readOctet(in) & 0xff) << 24) |
                    ((CommonIO.readOctet(in) & 0xff) << 16) |
                    ((CommonIO.readOctet(in) & 0xff) <<  8) |
                     (CommonIO.readOctet(in) & 0xff)
            ;
        }

        @Override
        public final long read64(InputStream in) throws IOException {
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
        public final short read16(byte[] in, int offset) {
            return (short) (
                    ((in[  offset] & 0xff) << 8) |
                     (in[++offset] & 0xff)
            );
        }

        @Override
        public final int read32(byte[] in, int offset) {
            return
                    ((in[  offset] & 0xff) << 24) |
                    ((in[++offset] & 0xff) << 16) |
                    ((in[++offset] & 0xff) <<  8) |
                     (in[++offset] & 0xff)
            ;
        }

        @Override
        public final long read64(byte[] in, int offset) {
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

    /**
     * The singleton instance representing the little-endian byte order.
     */
    LITTLE_ENDIAN(ByteOrder.LITTLE_ENDIAN) {

        @Override
        public final short read16(InputStream in) throws IOException {
            return (short) (
                     (CommonIO.readOctet(in) & 0xff) |
                    ((CommonIO.readOctet(in) & 0xff) << 8)
            );
        }

        @Override
        public final int read32(InputStream in) throws IOException {
            return
                     (CommonIO.readOctet(in) & 0xff)        |
                    ((CommonIO.readOctet(in) & 0xff) <<  8) |
                    ((CommonIO.readOctet(in) & 0xff) << 16) |
                    ((CommonIO.readOctet(in) & 0xff) << 24)
            ;
        }

        @Override
        public final long read64(InputStream in) throws IOException {
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
        public final short read16(byte[] in, int offset) {
            return (short) (
                     (in[  offset] & 0xff) |
                    ((in[++offset] & 0xff) << 8)
            );
        }

        @Override
        public final int read32(byte[] in, int offset) {
            return
                     (in[  offset] & 0xff)        |
                    ((in[++offset] & 0xff) <<  8) |
                    ((in[++offset] & 0xff) << 16) |
                    ((in[++offset] & 0xff) << 24)
            ;
        }

        @Override
        public final long read64(byte[] in, int offset) {
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

    /**
     * Byte order of the endianness.
     */
    public final ByteOrder byteOrder;

    Endianness(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    /**
     * Reads an 8-bit value (a single octet) from an input stream and returns it.
     *
     * @param in the input stream to read from
     *
     * @return the 8-bit value read from the stream
     *
     * @throws IOException if an I/O error occurs
     */
    public final byte read8(InputStream in) throws IOException {
        return CommonIO.readOctet(in);
    }

    /**
     * Reads a 16-bit value (2 octets) from an input stream and returns it.
     * The value is interpreted as having the byte order of this endianness.
     *
     * @param in the input stream to read from
     *
     * @return the 16-bit value read from the stream
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract short read16(InputStream in) throws IOException;

    /**
     * Reads a 32-bit value (4 octets) from an input stream and returns it.
     * The value is interpreted as having the byte order of this endianness.
     *
     * @param in the input stream to read from
     *
     * @return the 32-bit value read from the stream
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract int read32(InputStream in) throws IOException;

    /**
     * Reads a 64-bit value (8 octets) from an input stream and returns it.
     * The value is interpreted as having the byte order of this endianness.
     *
     * @param in the input stream to read from
     *
     * @return the 64-bit value read from the stream
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract long read64(InputStream in) throws IOException;

    /**
     * Reads an 8-bit value (a single octet) from a byte array and returns it.
     *
     * @param in the byte array to read from
     * @param offset the start offset of the value to read
     *
     * @return the 8-bit value read from the array
     *
     * @throws IndexOutOfBoundsException if {@code offset} is less than {@code 0}
     * or if {@code offset + 1} is greater than the length of the array
     */
    public final byte read8(byte[] in, int offset) {
        return in[offset];
    }

    /**
     * Reads a 16-bit value (2 octets) from a byte array and returns it.
     * The value is interpreted as having the byte order of this endianness.
     *
     * @param in the byte array to read from
     * @param offset the start offset of the value to read
     *
     * @return the 16-bit value read from the array
     *
     * @throws IndexOutOfBoundsException if {@code offset} is less than {@code 0}
     * or if {@code offset + 2} is greater than the length of the array
     */
    public abstract short read16(byte[] in, int offset);

    /**
     * Reads a 32-bit value (4 octets) from a byte array and returns it.
     * The value is interpreted as having the byte order of this endianness.
     *
     * @param in the byte array to read from
     * @param offset the start offset of the value to read
     *
     * @return the 32-bit value read from the array
     *
     * @throws IndexOutOfBoundsException if {@code offset} is less than {@code 0}
     * or if {@code offset + 4} is greater than the length of the array
     */
    public abstract int read32(byte[] in, int offset);

    /**
     * Reads a 64-bit value (8 octets) from a byte array and returns it.
     * The value is interpreted as having the byte order of this endianness.
     *
     * @param in the byte array to read from
     * @param offset the start offset of the value to read
     *
     * @return the 64-bit value read from the array
     *
     * @throws IndexOutOfBoundsException if {@code offset} is less than {@code 0}
     * or if {@code offset + 8} is greater than the length of the array
     */
    public abstract long read64(byte[] in, int offset);

    /**
     * Obtains an instance of {@link Endianness} from the specified byte order.
     *
     * @param byteOrder the byte order of the endianness
     *
     * @return the endianness, not {@code null}
     *
     * @throws IllegalArgumentException if no endianness
     * corresponds to the specified byte order
     */
    public static Endianness of(ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return LITTLE_ENDIAN;
        } else if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return BIG_ENDIAN;
        } else {
            throw new IllegalArgumentException("Invalid byte order: " + byteOrder);
        }
    }

}
