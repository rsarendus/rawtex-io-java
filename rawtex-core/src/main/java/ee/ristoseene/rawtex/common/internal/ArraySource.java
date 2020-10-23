package ee.ristoseene.rawtex.common.internal;

import java.io.EOFException;
import java.io.InputStream;
import java.util.Objects;

public class ArraySource extends InputStream {

    public final byte[] array;

    private int position;
    private int available;

    public ArraySource(byte[] array) {
        this(array, 0, array.length);
    }

    public ArraySource(byte[] array, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, array.length);

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
        Objects.checkFromIndexSize(off, len, b.length);
        final int amount = Math.min(len, available);

        System.arraycopy(array, position, b, off, amount);

        position += amount;
        available -= amount;

        return amount;
    }

    @Override
    public long skip(long n) {
        if (n > 0 && available > 0) {
            final int amount = (int) Math.min(n, available);

            position += amount;
            available -= amount;

            return amount;
        } else {
            return 0;
        }
    }

}
