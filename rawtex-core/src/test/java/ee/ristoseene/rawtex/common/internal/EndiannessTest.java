package ee.ristoseene.rawtex.common.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public class EndiannessTest {

    @Test
    public void testOfBigEndian() {
        Endianness result = Endianness.of(ByteOrder.BIG_ENDIAN);

        Assertions.assertSame(Endianness.BIG_ENDIAN, result);
        Assertions.assertSame(ByteOrder.BIG_ENDIAN, result.byteOrder);
    }

    @Test
    public void testOfLittleEndian() {
        Endianness result = Endianness.of(ByteOrder.LITTLE_ENDIAN);

        Assertions.assertSame(Endianness.LITTLE_ENDIAN, result);
        Assertions.assertSame(ByteOrder.LITTLE_ENDIAN, result.byteOrder);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    public void testRead8FromStreamSucceeds(Endianness endianness) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {7});

        byte result = endianness.read8(inputStream);

        Assertions.assertEquals((byte) 7, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    public void testRead8FromStreamFails(Endianness endianness) {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> endianness.read8(inputStream)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
    }

    @Test
    public void testRead16BigEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2});

        short result = Endianness.BIG_ENDIAN.read16(inputStream);

        Assertions.assertEquals((short) 0x0102, result);
    }

    @Test
    public void testRead16LittleEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2});

        short result = Endianness.LITTLE_ENDIAN.read16(inputStream);

        Assertions.assertEquals((short) 0x0201, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    public void testRead16FromStreamFails(Endianness endianness) {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1});

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> endianness.read16(inputStream)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
    }

    @Test
    public void testRead32BigEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3, 4});

        int result = Endianness.BIG_ENDIAN.read32(inputStream);

        Assertions.assertEquals(0x01020304, result);
    }

    @Test
    public void testRead32LittleEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3, 4});

        int result = Endianness.LITTLE_ENDIAN.read32(inputStream);

        Assertions.assertEquals(0x04030201, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    public void testRead32FromStreamFails(Endianness endianness) {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3});

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> endianness.read32(inputStream)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
    }

    @Test
    public void testRead64BigEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6, 7, 8});

        long result = Endianness.BIG_ENDIAN.read64(inputStream);

        Assertions.assertEquals(0x0102030405060708L, result);
    }

    @Test
    public void testRead64LittleEndianFromStreamSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6, 7, 8});

        long result = Endianness.LITTLE_ENDIAN.read64(inputStream);

        Assertions.assertEquals(0x0807060504030201L, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    public void testRead64FromStreamFails(Endianness endianness) {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6, 7});

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> endianness.read64(inputStream)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    public void testRead8FromArraySucceeds(Endianness endianness) {
        byte[] inputArray = {3, 7};

        byte result = endianness.read8(inputArray, 1);

        Assertions.assertEquals((byte) 7, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    public void testRead8FromArrayFails(Endianness endianness) {
        byte[] inputArray = {3};

        ArrayIndexOutOfBoundsException caughtException = Assertions.assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> endianness.read8(inputArray, 1)
        );
    }

    @Test
    public void testRead16BigEndianFromArraySucceeds() throws IOException {
        byte[] inputArray = {0, 1, 2};

        short result = Endianness.BIG_ENDIAN.read16(inputArray, 1);

        Assertions.assertEquals((short) 0x0102, result);
    }

    @Test
    public void testRead16LittleEndianFromArraySucceeds() throws IOException {
        byte[] inputArray = {0, 1, 2};

        short result = Endianness.LITTLE_ENDIAN.read16(inputArray, 1);

        Assertions.assertEquals((short) 0x0201, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    public void testRead16FromArrayFails(Endianness endianness) {
        byte[] inputArray = {0, 1};

        ArrayIndexOutOfBoundsException caughtException = Assertions.assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> endianness.read16(inputArray, 1)
        );
    }

    @Test
    public void testRead32BigEndianFromArraySucceeds() throws IOException {
        byte[] inputArray = {0, 1, 2, 3, 4};

        int result = Endianness.BIG_ENDIAN.read32(inputArray, 1);

        Assertions.assertEquals(0x01020304, result);
    }

    @Test
    public void testRead32LittleEndianFromArraySucceeds() throws IOException {
        byte[] inputArray = {0, 1, 2, 3, 4};

        int result = Endianness.LITTLE_ENDIAN.read32(inputArray, 1);

        Assertions.assertEquals(0x04030201, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    public void testRead32FromArrayFails(Endianness endianness) {
        byte[] inputArray = {0, 1, 2, 3};

        ArrayIndexOutOfBoundsException caughtException = Assertions.assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> endianness.read32(inputArray, 1)
        );
    }

    @Test
    public void testRead64BigEndianFromArraySucceeds() throws IOException {
        byte[] inputArray = {0, 1, 2, 3, 4, 5, 6, 7, 8};

        long result = Endianness.BIG_ENDIAN.read64(inputArray, 1);

        Assertions.assertEquals(0x0102030405060708L, result);
    }

    @Test
    public void testRead64LittleEndianFromArraySucceeds() throws IOException {
        byte[] inputArray = {0, 1, 2, 3, 4, 5, 6, 7, 8};

        long result = Endianness.LITTLE_ENDIAN.read64(inputArray, 1);

        Assertions.assertEquals(0x0807060504030201L, result);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    public void testRead64FromArrayFails(Endianness endianness) {
        byte[] inputArray = {0, 1, 2, 3, 4, 5, 6, 7};

        ArrayIndexOutOfBoundsException caughtException = Assertions.assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> endianness.read64(inputArray, 1)
        );
    }

}
