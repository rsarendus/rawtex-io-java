package ee.ristoseene.rawtex.io.core.common.format;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

class EndiannessTest {

    @Test
    void testBigEndian() {
        Assertions.assertSame(ByteOrder.BIG_ENDIAN, Endianness.BIG_ENDIAN.byteOrder);
        Assertions.assertSame(Endianness.BIG_ENDIAN, Endianness.of(ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testLittleEndian() {
        Assertions.assertSame(ByteOrder.LITTLE_ENDIAN, Endianness.LITTLE_ENDIAN.byteOrder);
        Assertions.assertSame(Endianness.LITTLE_ENDIAN, Endianness.of(ByteOrder.LITTLE_ENDIAN));
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void testRead8FromStreamSucceeds(Endianness endianness) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {7});

        byte result = endianness.read8(inputStream);

        Assertions.assertEquals((byte) 7, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void testRead8FromStreamFails(Endianness endianness) {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> endianness.read8(inputStream)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
    }

    @Test
    void testRead16BigEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2});

        short result = Endianness.BIG_ENDIAN.read16(inputStream);

        Assertions.assertEquals((short) 0x0102, result);
    }

    @Test
    void testRead16LittleEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2});

        short result = Endianness.LITTLE_ENDIAN.read16(inputStream);

        Assertions.assertEquals((short) 0x0201, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void testRead16FromStreamFails(Endianness endianness) {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1});

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> endianness.read16(inputStream)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
    }

    @Test
    void testRead32BigEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3, 4});

        int result = Endianness.BIG_ENDIAN.read32(inputStream);

        Assertions.assertEquals(0x01020304, result);
    }

    @Test
    void testRead32LittleEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3, 4});

        int result = Endianness.LITTLE_ENDIAN.read32(inputStream);

        Assertions.assertEquals(0x04030201, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void testRead32FromStreamFails(Endianness endianness) {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3});

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> endianness.read32(inputStream)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
    }

    @Test
    void testRead64BigEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6, 7, 8});

        long result = Endianness.BIG_ENDIAN.read64(inputStream);

        Assertions.assertEquals(0x0102030405060708L, result);
    }

    @Test
    void testRead64LittleEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6, 7, 8});

        long result = Endianness.LITTLE_ENDIAN.read64(inputStream);

        Assertions.assertEquals(0x0807060504030201L, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void testRead64FromStreamFails(Endianness endianness) {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6, 7});

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> endianness.read64(inputStream)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void testRead8FromArraySucceeds(Endianness endianness) {
        byte[] inputArray = {3, 7};

        byte result = endianness.read8(inputArray, 1);

        Assertions.assertEquals((byte) 7, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void testRead8FromArrayFails(Endianness endianness) {
        byte[] inputArray = {3};

        Assertions.assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> endianness.read8(inputArray, 1)
        );
    }

    @Test
    void testRead16BigEndianFromArraySucceeds() {
        byte[] inputArray = {0, 1, 2};

        short result = Endianness.BIG_ENDIAN.read16(inputArray, 1);

        Assertions.assertEquals((short) 0x0102, result);
    }

    @Test
    void testRead16LittleEndianFromArraySucceeds() {
        byte[] inputArray = {0, 1, 2};

        short result = Endianness.LITTLE_ENDIAN.read16(inputArray, 1);

        Assertions.assertEquals((short) 0x0201, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void testRead16FromArrayFails(Endianness endianness) {
        byte[] inputArray = {0, 1};

        Assertions.assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> endianness.read16(inputArray, 1)
        );
    }

    @Test
    void testRead32BigEndianFromArraySucceeds() {
        byte[] inputArray = {0, 1, 2, 3, 4};

        int result = Endianness.BIG_ENDIAN.read32(inputArray, 1);

        Assertions.assertEquals(0x01020304, result);
    }

    @Test
    void testRead32LittleEndianFromArraySucceeds() {
        byte[] inputArray = {0, 1, 2, 3, 4};

        int result = Endianness.LITTLE_ENDIAN.read32(inputArray, 1);

        Assertions.assertEquals(0x04030201, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void testRead32FromArrayFails(Endianness endianness) {
        byte[] inputArray = {0, 1, 2, 3};

        Assertions.assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> endianness.read32(inputArray, 1)
        );
    }

    @Test
    void testRead64BigEndianFromArraySucceeds() {
        byte[] inputArray = {0, 1, 2, 3, 4, 5, 6, 7, 8};

        long result = Endianness.BIG_ENDIAN.read64(inputArray, 1);

        Assertions.assertEquals(0x0102030405060708L, result);
    }

    @Test
    void testRead64LittleEndianFromArraySucceeds() {
        byte[] inputArray = {0, 1, 2, 3, 4, 5, 6, 7, 8};

        long result = Endianness.LITTLE_ENDIAN.read64(inputArray, 1);

        Assertions.assertEquals(0x0807060504030201L, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void testRead64FromArrayFails(Endianness endianness) {
        byte[] inputArray = {0, 1, 2, 3, 4, 5, 6, 7};

        Assertions.assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> endianness.read64(inputArray, 1)
        );
    }

}
