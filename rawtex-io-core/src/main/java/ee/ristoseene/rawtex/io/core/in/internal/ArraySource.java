package ee.ristoseene.rawtex.io.core.in.internal;

import ee.ristoseene.rawtex.io.core.common.internal.CommonIO;

import java.io.EOFException;
import java.io.InputStream;

/**
 * A thin non-thread-safe implementation of {@link InputStream}, wrapping a byte array.
 * The backing byte array is accessible and can be used in conjunction with the
 * {@link #ensureAvailableAndAdvance(int)} method.
 * <p>
 * This class does not override the default implementations of the {@link #mark(int) mark}
 * and {@link #reset() reset} methods of the {@link InputStream} class.
 */
public class ArraySource extends InputStream {

    /**
     * The backing byte array.
     */
    public final byte[] array;

    private int position;
    private int available;

    /**
     * Constructs an {@code ArraySource} using the specified byte array as its backing array.
     * The initial value of the internal counter specifying the next byte to read is set to {@code 0}
     * and the initial number of remaining bytes to read is set to be equal to the length of the
     * specified byte array.
     *
     * @param array the byte array to use as the backing array, not {@code null}
     */
    public ArraySource(byte[] array) {
        this(array, 0, array.length);
    }

    /**
     * Constructs an {@code ArraySource} using the specified byte array as its backing array.
     * The initial value of the internal counter specifying the next byte to read is set to {@code offset}
     * and the initial number of remaining bytes to read is set to {@code length}.
     *
     * @param array the byte array to use as the backing array, not {@code null}
     * @param offset the offset in the byte array of the first byte to read
     * @param length the maximum number of bytes to read from the byte array
     *
     * @throws ArrayIndexOutOfBoundsException if {@code offset} or {@code length} is less than {@code 0},
     * or if {@code offset + length} is outside the range of the specified array
     */
    public ArraySource(byte[] array, int offset, int length) {
        if (offset < 0 || length < 0 || array.length - offset < length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        this.array = array;

        position = offset;
        available = length;
    }

    /**
     * Advances the stream by the requested amount or throws an exception
     * if not enough bytes are remaining in this stream.
     *
     * Returns an index in the backing array specifying the first byte to read before the stream was advanced.
     * The returned index can be used for accessing the backing array directly:
     *
     * <pre>{@code
     * final int length = ...; // the amount of bytes to read
     * final int offset = arraySource.ensureAvailableAndAdvance(length);
     * readDirectlyFromBackingArray(arraySource.array, offset, length);
     * }</pre>
     *
     * Has no effect if the requested amount to advance the stream is not a positive value.
     *
     * @param amount the amount to advance the stream
     *
     * @return an index in the backing array specifying the first byte to read before the stream was advanced
     *
     * @throws EOFException if {@code amount} is greater than the number of remaining bytes in this stream
     */
    public int ensureAvailableAndAdvance(int amount) throws EOFException {
        if (amount > available) {
            throw CommonIO.unexpectedEndOfInputException();
        }

        final int initialPosition = position;

        if (amount > 0) {
            position += amount;
            available -= amount;
        }

        return initialPosition;
    }

    /**
     * Returns the number of remaining bytes that can be read (or skipped over) from this stream.
     *
     * @return the number of remaining bytes that can be read (or skipped over) from this stream
     */
    @Override
    public int available() {
        return available;
    }

    /**
     * Closing the {@code ArraySource} has no effect.
     */
    @Override
    public void close() {}

    /**
     * Reads the next byte of data from this stream.
     * The value byte is returned as an {@code int} in the range {@code 0} to {@code 255}.
     * If no byte is available because the end of the stream has been reached, the value {@code -1} is returned.
     * <p>
     * This read method cannot block.
     *
     * @return the next byte of data, or {@code -1} if the end of the stream has been reached
     */
    @Override
    public int read() {
        if (available > 0) {
            --available;
            return array[position++] & 0xff;
        } else {
            return -1;
        }
    }

    /**
     * Reads up to the specified number of bytes of data into an array of bytes from this stream.
     * If no remaining bytes are available to read, then {@code -1} is returned to indicate the end of the stream.
     * Otherwise, the number of bytes to read is equal to the minimum of {@code len} and the number of remaining bytes.
     * If the number of bytes to read is positive, then that many bytes are copied into the destination array of bytes,
     * the stream is advanced by that amount and the number of bytes read is returned.
     * <p>
     * This read method cannot block.
     *
     * @param b the array of bytes into which the data is read
     * @param off the start offset in array {@code b} at which the data is written
     * @param len the maximum number of bytes to read
     *
     * @return the total number of bytes read into the array of bytes,
     * or {@code -1} if there is no more data because the end of the stream has been reached
     *
     * @throws ArrayIndexOutOfBoundsException if {@code off} or {@code len} is less than {@code 0},
     * or if {@code off + len} is outside the range of the specified array
     */
    @Override
    public int read(byte[] b, int off, int len) {
        if (off < 0 || len < 0 || b.length - off < len) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else if (available > 0) {
            final int amount = Math.min(len, available);

            System.arraycopy(array, position, b, off, amount);

            position += amount;
            available -= amount;

            return amount;
        } else {
            return -1;
        }
    }

    /**
     * Skips up to the specified number of bytes of input from this stream.
     * Fewer bytes might be skipped if the end of the stream is reached.
     * The actual number of bytes to skip is equal to the minimum of {@code n} and the number of remaining bytes.
     * If the number of bytes to skip is positive, then the stream is advanced by that amount
     * and the number of bytes skipped is returned.
     *
     * @param n the maximum number of bytes to skip
     *
     * @return the actual number of bytes skipped
     */
    @Override
    public long skip(long n) {
        if (n > 0L && available > 0) {
            final int amount = (int) Math.min(n, available);

            position += amount;
            available -= amount;

            return amount;
        } else {
            return 0L;
        }
    }

}
