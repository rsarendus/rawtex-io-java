package ee.ristoseene.rawtex.io.core.in.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.EOFException;
import java.io.IOException;

class ArraySourceTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10})
    void testArrayConstructor(int arrayLength) throws EOFException {
        byte[] array = new byte[arrayLength];

        ArraySource arraySource = new ArraySource(array);

        Assertions.assertSame(array, arraySource.array);
        Assertions.assertEquals(0, arraySource.ensureAvailableAndAdvance(0L));
        Assertions.assertEquals(arrayLength, arraySource.available());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10})
    void testArrayAndOffsetAndLengthConstructor(int arrayLength) throws EOFException {
        byte[] array = new byte[3 + arrayLength + 7];

        ArraySource arraySource = new ArraySource(array, 2, arrayLength);

        Assertions.assertSame(array, arraySource.array);
        Assertions.assertEquals(2, arraySource.ensureAvailableAndAdvance(0L));
        Assertions.assertEquals(arrayLength, arraySource.available());
    }

    @Test
    void testNegativeConstructorOffsetFails() {
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> new ArraySource(new byte[1], -1, 1)
        );
    }

    @Test
    void testOverflowingConstructorOffsetFails() {
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> new ArraySource(new byte[1], 1, 1)
        );
    }

    @Test
    void testNegativeConstructorLengthFails() {
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> new ArraySource(new byte[1], 0, -1)
        );
    }

    @Test
    void testOverflowingConstructorLengthFails() {
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> new ArraySource(new byte[2], 1, 2)
        );
    }

    @Test
    void testEnsureAvailableAndAdvance() throws EOFException {
        ArraySource arraySource = new ArraySource(new byte[5]);

        Assertions.assertEquals(5, arraySource.available());
        Assertions.assertEquals(0, arraySource.ensureAvailableAndAdvance(3L));

        Assertions.assertEquals(2, arraySource.available());
        Assertions.assertEquals(3, arraySource.ensureAvailableAndAdvance(1L));

        Assertions.assertEquals(1, arraySource.available());
        Assertions.assertEquals(4, arraySource.ensureAvailableAndAdvance(0L));

        Assertions.assertEquals(1, arraySource.available());
        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> arraySource.ensureAvailableAndAdvance(2)
        );
        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Assertions.assertEquals(1, arraySource.available());
    }

    @Test
    void testRead() {
        ArraySource arraySource = new ArraySource(new byte[] {2, 127, 1, (byte) 0xff});

        Assertions.assertEquals(4, arraySource.available());
        Assertions.assertEquals(2, arraySource.read());

        Assertions.assertEquals(3, arraySource.available());
        Assertions.assertEquals(127, arraySource.read());

        Assertions.assertEquals(2, arraySource.available());
        Assertions.assertEquals(1, arraySource.read());

        Assertions.assertEquals(1, arraySource.available());
        Assertions.assertEquals(0xff, arraySource.read());

        Assertions.assertEquals(0, arraySource.available());
        Assertions.assertEquals(-1, arraySource.read());
        Assertions.assertEquals(0, arraySource.available());
    }

    @Test
    void testReadIntoArray() throws IOException {
        ArraySource arraySource = new ArraySource(new byte[] {1, 2, 3, 4, 5});
        byte[] buffer;

        buffer = new byte[5];
        Assertions.assertEquals(5, arraySource.available());
        Assertions.assertEquals(3, arraySource.read(buffer, 1, 3));
        Assertions.assertArrayEquals(new byte[] {0, 1, 2, 3, 0}, buffer);

        buffer = new byte[1];
        Assertions.assertEquals(2, arraySource.available());
        Assertions.assertEquals(1, arraySource.read(buffer));
        Assertions.assertArrayEquals(new byte[] {4}, buffer);

        buffer = new byte[3];
        Assertions.assertEquals(1, arraySource.available());
        Assertions.assertEquals(1, arraySource.read(buffer, 1, 2));
        Assertions.assertArrayEquals(new byte[] {0, 5, 0}, buffer);

        buffer = new byte[1];
        Assertions.assertEquals(0, arraySource.available());
        Assertions.assertEquals(-1, arraySource.read(buffer));
        Assertions.assertArrayEquals(new byte[] {0}, buffer);

        Assertions.assertEquals(0, arraySource.available());
    }

    @Test
    void testSkip() {
        ArraySource arraySource = new ArraySource(new byte[5]);

        Assertions.assertEquals(5, arraySource.available());
        Assertions.assertEquals(3L, arraySource.skip(3L));

        Assertions.assertEquals(2, arraySource.available());
        Assertions.assertEquals(1L, arraySource.skip(1L));

        Assertions.assertEquals(1, arraySource.available());
        Assertions.assertEquals(1L, arraySource.skip(2L));

        Assertions.assertEquals(0, arraySource.available());
        Assertions.assertEquals(0L, arraySource.skip(1L));

        Assertions.assertEquals(0, arraySource.available());
    }

}
