package ee.ristoseene.rawtex.io.core.in.internal;

import ee.ristoseene.rawtex.io.core.common.internal.CommonIO;

import java.io.EOFException;
import java.io.InputStream;

/**
 * A thin non-thread-safe implementation of {@link InputStream} wrapping a byte array.
 */
public class ArraySource extends InputStream {

    public final byte[] array;

    private int position;
    private int available;

    public ArraySource(byte[] array) {
        this(array, 0, array.length);
    }

    public ArraySource(byte[] array, int offset, int length) {
        if (offset < 0 || length < 0 || array.length - offset < length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        this.array = array;

        position = offset;
        available = length;
    }

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

    @Override
    public int available() {
        return available;
    }

    @Override
    public void close() {}

    @Override
    public int read() {
        if (available > 0) {
            --available;
            return array[position++] & 0xff;
        } else {
            return -1;
        }
    }

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
