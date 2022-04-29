package ee.ristoseene.rawtex.io.core.common.format;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class RawTexFormatIndicatorTest {

    @Test
    void testBigEndianFormatIndicator() {
        RawTexFormatIndicator formatIndicator = RawTexFormatIndicator.BIG_ENDIAN;

        Assertions.assertNotNull(formatIndicator);
        Assertions.assertSame(Endianness.BIG_ENDIAN, formatIndicator.endianness);
        Assertions.assertEquals("RAWTEX", formatIndicator.toString());

        Assertions.assertEquals(6, formatIndicator.length());
        Assertions.assertEquals((byte) 'R', formatIndicator.octetAt(0));
        Assertions.assertEquals((byte) 'A', formatIndicator.octetAt(1));
        Assertions.assertEquals((byte) 'W', formatIndicator.octetAt(2));
        Assertions.assertEquals((byte) 'T', formatIndicator.octetAt(3));
        Assertions.assertEquals((byte) 'E', formatIndicator.octetAt(4));
        Assertions.assertEquals((byte) 'X', formatIndicator.octetAt(5));
    }

    @Test
    void testLittleEndianFormatIndicator() {
        RawTexFormatIndicator formatIndicator = RawTexFormatIndicator.LITTLE_ENDIAN;

        Assertions.assertNotNull(formatIndicator);
        Assertions.assertSame(Endianness.LITTLE_ENDIAN, formatIndicator.endianness);
        Assertions.assertEquals("rawtex", formatIndicator.toString());

        Assertions.assertEquals(6, formatIndicator.length());
        Assertions.assertEquals((byte) 'r', formatIndicator.octetAt(0));
        Assertions.assertEquals((byte) 'a', formatIndicator.octetAt(1));
        Assertions.assertEquals((byte) 'w', formatIndicator.octetAt(2));
        Assertions.assertEquals((byte) 't', formatIndicator.octetAt(3));
        Assertions.assertEquals((byte) 'e', formatIndicator.octetAt(4));
        Assertions.assertEquals((byte) 'x', formatIndicator.octetAt(5));
    }

    @ParameterizedTest
    @EnumSource(RawTexFormatIndicator.class)
    void testByteAtThrowsExceptionForNegativeIndex(RawTexFormatIndicator formatIndicator) {
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> formatIndicator.octetAt(-1)
        );
    }

    @ParameterizedTest
    @EnumSource(RawTexFormatIndicator.class)
    void testByteAtThrowsExceptionForOverflowingIndex(RawTexFormatIndicator formatIndicator) {
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> formatIndicator.octetAt(formatIndicator.length())
        );
    }

    @ParameterizedTest
    @EnumSource(RawTexFormatIndicator.class)
    void testToByteArrayReturnsFormatIndicatorBytes(RawTexFormatIndicator formatIndicator) {
        byte[] result = formatIndicator.toByteArray();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(formatIndicator.length(), result.length);

        for (int i = 0; i < formatIndicator.length(); ++i) {
            Assertions.assertEquals(formatIndicator.octetAt(i), result[i]);
        }
    }

    @Test
    void testOfFromEndiannessSucceedsForBigEndian() {
        RawTexFormatIndicator formatIndicator = RawTexFormatIndicator.of(Endianness.BIG_ENDIAN);
        Assertions.assertSame(RawTexFormatIndicator.BIG_ENDIAN, formatIndicator);
    }

    @Test
    void testOfFromEndiannessSucceedsForLittleEndian() {
        RawTexFormatIndicator formatIndicator = RawTexFormatIndicator.of(Endianness.LITTLE_ENDIAN);
        Assertions.assertSame(RawTexFormatIndicator.LITTLE_ENDIAN, formatIndicator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"RaWtEx", "rAwTeX", "RawTex", "Raw Tex", "raw tex", "RAW\0", "tex", "abc", "\0"})
    void testOfFromFormatIndicatorStringFailsForUnrelatedFormats(String formatIndicatorString) {
        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> RawTexFormatIndicator.of(formatIndicatorString)
        );

        Assertions.assertEquals(
                String.format("Unrecognized format indicator: %s", formatIndicatorString),
                caughtException.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"RAWTE", "RAWT", "RAW", "RA", "R", "rawte", "rawt", "raw", "ra", "r"})
    void testOfFromFormatIndicatorStringFailsForFormatsCutShort(String formatIndicatorString) {
        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> RawTexFormatIndicator.of(formatIndicatorString)
        );

        Assertions.assertEquals(
                String.format("Unrecognized format indicator: %s", formatIndicatorString),
                caughtException.getMessage()
        );
    }

    @Test
    void testOfFromFormatIndicatorStringSucceedsForBigEndian() {
        RawTexFormatIndicator formatIndicator = RawTexFormatIndicator.of("RAWTEX");
        Assertions.assertSame(RawTexFormatIndicator.BIG_ENDIAN, formatIndicator);
    }

    @Test
    void testOfFromFormatIndicatorStringSucceedsForLittleEndian() {
        RawTexFormatIndicator formatIndicator = RawTexFormatIndicator.of("rawtex");
        Assertions.assertSame(RawTexFormatIndicator.LITTLE_ENDIAN, formatIndicator);
    }

}
