package ee.ristoseene.rawtex.io.core.common.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * I/O utility methods.
 */
public final class CommonIO {

    /**
     * Reads a single octet from an input stream and returns it,
     * or throws an exception if the end of the stream has already been reached.
     *
     * @param in the input stream to read from
     *
     * @return the octet read from the stream
     *
     * @throws EOFException if no octet can be read because the end of the stream has already been reached
     * @throws IOException if an I/O error occurs
     *
     * @see InputStream#read()
     */
    public static byte readOctet(InputStream in) throws IOException {
        final int octetValue = in.read();

        if (octetValue >= 0) {
            return (byte) octetValue;
        }

        throw unexpectedEndOfInputException();
    }

    /**
     * Reads up to the requested number of octets from an input stream into a destination array,
     * or throws an exception if the end of the stream has already been reached.
     *
     * In case the requested maximum number of octets to read is greater than {@code 0},
     * then at least one octet is guaranteed to be read, unless an exception is thrown.
     *
     * @param in the input stream to read from
     * @param dst the array into which the octets are read
     * @param dstOffset the start offset in array {@code dst} at which the octets are written
     * @param maxOctetsToRead the maximum number of octets to read
     *
     * @return the actual number of octets read
     *
     * @throws EOFException if no octet can be read because the end of the stream has already been reached
     * @throws IOException if an I/O error occurs
     *
     * @see InputStream#read(byte[], int, int)
     */
    public static int readOctets(InputStream in, byte[] dst, int dstOffset, int maxOctetsToRead) throws IOException {
        final int octetsRead = in.read(dst, dstOffset, maxOctetsToRead);

        if (octetsRead > 0) {
            return octetsRead;
        } else if (octetsRead < 0) {
            throw unexpectedEndOfInputException();
        } else if (maxOctetsToRead > 0) {
            // Just in case to avoid any potential infinite loops
            dst[dstOffset] = readOctet(in);
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Reads the requested number of octets from an input stream into a destination array,
     * or throws an exception if the end of the stream is reached before reading the requested number of octets.
     *
     * In case an exception is thrown, the number of octets written into the destination array
     * is undefined.
     *
     * @param in the input stream to read from
     * @param dst the array into which the octets are read
     * @param dstOffset the start offset in array {@code dst} at which the octets are written
     * @param octetsToRead the number of octets to read
     *
     * @throws EOFException if the end of the stream is reached before reading the requested amount of octets
     * @throws IOException if an I/O error occurs
     *
     * @see InputStream#read(byte[], int, int)
     */
    public static void readNOctets(InputStream in, byte[] dst, int dstOffset, int octetsToRead) throws IOException {
        do {
            final int octetsRead = readOctets(in, dst, dstOffset, octetsToRead);

            octetsToRead -= octetsRead;
            dstOffset += octetsRead;
        } while (octetsToRead > 0);
    }

    /**
     * Returns a new {@link EOFException} with the message "Unexpected end of input".
     *
     * @return an instance of {@link EOFException}
     */
    public static EOFException unexpectedEndOfInputException() {
        return new EOFException("Unexpected end of input");
    }

    private CommonIO() {
        throw new UnsupportedOperationException();
    }

}
