package ee.ristoseene.rawtex.io.deflate.common.format;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class DeflateCompressionFormatIndicatorTest {

    @Test
    void testRawDeflateFormatIndicator() {
        DeflateCompressionFormatIndicator formatIndicator = DeflateCompressionFormatIndicator.DEFLATE;

        Assertions.assertNotNull(formatIndicator);
        Assertions.assertTrue(formatIndicator.nowrap);
        Assertions.assertEquals("DEFLATE", formatIndicator.toString());

        Assertions.assertEquals(7, formatIndicator.length());
        Assertions.assertEquals((byte) 'D', formatIndicator.octetAt(0));
        Assertions.assertEquals((byte) 'E', formatIndicator.octetAt(1));
        Assertions.assertEquals((byte) 'F', formatIndicator.octetAt(2));
        Assertions.assertEquals((byte) 'L', formatIndicator.octetAt(3));
        Assertions.assertEquals((byte) 'A', formatIndicator.octetAt(4));
        Assertions.assertEquals((byte) 'T', formatIndicator.octetAt(5));
        Assertions.assertEquals((byte) 'E', formatIndicator.octetAt(6));
    }

    @Test
    void testWrappedZlibFormatIndicator() {
        DeflateCompressionFormatIndicator formatIndicator = DeflateCompressionFormatIndicator.ZLIB;

        Assertions.assertNotNull(formatIndicator);
        Assertions.assertFalse(formatIndicator.nowrap);
        Assertions.assertEquals("zlib", formatIndicator.toString());

        Assertions.assertEquals(4, formatIndicator.length());
        Assertions.assertEquals((byte) 'z', formatIndicator.octetAt(0));
        Assertions.assertEquals((byte) 'l', formatIndicator.octetAt(1));
        Assertions.assertEquals((byte) 'i', formatIndicator.octetAt(2));
        Assertions.assertEquals((byte) 'b', formatIndicator.octetAt(3));
    }

    @ParameterizedTest
    @EnumSource(DeflateCompressionFormatIndicator.class)
    void testByteAtThrowsExceptionForNegativeIndex(DeflateCompressionFormatIndicator formatIndicator) {
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> formatIndicator.octetAt(-1)
        );
    }

    @ParameterizedTest
    @EnumSource(DeflateCompressionFormatIndicator.class)
    void testByteAtThrowsExceptionForOverflowingIndex(DeflateCompressionFormatIndicator formatIndicator) {
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> formatIndicator.octetAt(formatIndicator.length())
        );
    }

    @ParameterizedTest
    @EnumSource(DeflateCompressionFormatIndicator.class)
    void testToByteArrayReturnsFormatIndicatorBytes(DeflateCompressionFormatIndicator formatIndicator) {
        byte[] result = formatIndicator.toByteArray();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(formatIndicator.length(), result.length);

        for (int i = 0; i < formatIndicator.length(); ++i) {
            Assertions.assertEquals(formatIndicator.octetAt(i), result[i]);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"DeFlAtE", "dEfLaTe", "ZlIb", "zLiB", "DEF LATE", "z-lib", "DEF\0", "abc", "\0"})
    void testOfFromFormatIndicatorStringFailsForUnrelatedFormats(String formatIndicatorString) {
        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DeflateCompressionFormatIndicator.of(formatIndicatorString)
        );

        Assertions.assertEquals(
                String.format("Unrecognized format indicator: %s", formatIndicatorString),
                caughtException.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEFLAT", "DEFLA", "DEFL", "DEF", "DE", "D", "zli", "zl", "z"})
    void testOfFromFormatIndicatorStringFailsForFormatsCutShort(String formatIndicatorString) {
        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DeflateCompressionFormatIndicator.of(formatIndicatorString)
        );

        Assertions.assertEquals(
                String.format("Unrecognized format indicator: %s", formatIndicatorString),
                caughtException.getMessage()
        );
    }

    @Test
    void testOfFromFormatIndicatorStringSucceedsForDeflate() {
        DeflateCompressionFormatIndicator formatIndicator = DeflateCompressionFormatIndicator.of("DEFLATE");
        Assertions.assertSame(DeflateCompressionFormatIndicator.DEFLATE, formatIndicator);
    }

    @Test
    void testOfFromFormatIndicatorStringSucceedsForzlib() {
        DeflateCompressionFormatIndicator formatIndicator = DeflateCompressionFormatIndicator.of("zlib");
        Assertions.assertSame(DeflateCompressionFormatIndicator.ZLIB, formatIndicator);
    }

}
