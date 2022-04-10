package ee.ristoseene.rawtex.common.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * I/O utility methods.
 */
public final class CommonIO {

    /**
     * Reads a single octet from an {@link InputStream} and returns it,
     * or throws an {@link EOFException} if the end of the stream has already been reached.
     *
     * @param in the {@link InputStream} to read from
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

        if (octetValue < 0) {
            throw unexpectedEndOfInputException();
        }

        return (byte) octetValue;
    }

    /**
     * Reads the requested number of octets from an {@link InputStream} and writes them
     * into a destination array, or throws an {@link EOFException} if the end of the stream
     * is reached before reading the requested number of octets.
     *
     * In case an exception is thrown, the number of octets written into the destination array
     * is undefined.
     *
     * @param in the {@link InputStream} to read from
     * @param dst the array into which the octets are read
     * @param dstOffset the start offset in array {@code dst} at which the octets are written
     * @param octetsToRead the number of octets to read
     *
     * @throws EOFException if the end of the stream is reached before reading the requested amount of octets
     * @throws IOException if an I/O error occurs
     *
     * @see InputStream#read(byte[], int, int)
     */
    public static void readOctets(InputStream in, byte[] dst, int dstOffset, int octetsToRead) throws IOException {
        while (octetsToRead > 0) {
            final int octetsRead = in.read(dst, dstOffset, octetsToRead);

            if (octetsRead < 0) {
                throw unexpectedEndOfInputException();
            }

            octetsToRead -= octetsRead;
            dstOffset += octetsRead;
        }
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
