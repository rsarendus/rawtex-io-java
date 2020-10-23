package ee.ristoseene.rawtex.common.internal;

import ee.ristoseene.rawtex.common.exceptions.RawTexUnsupportedFormatException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RawTexHeaderTest {

    @Test
    public void testParseEndiannessForBigEndianSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {'R', 'A', 'W', 'T', 'E', 'X'});

        Endianness result = RawTexHeader.parseEndianness(inputStream);

        Assertions.assertSame(Endianness.BIG_ENDIAN, result);
    }

    @Test
    public void testParseEndiannessForLittleEndianSucceeds() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[] {'r', 'a', 'w', 't', 'e', 'x'});

        Endianness result = RawTexHeader.parseEndianness(inputStream);

        Assertions.assertSame(Endianness.LITTLE_ENDIAN, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"RaWtEx", "rAwTeX", "RawTex", "RAW\0", "tex", "abc", "\0"})
    public void testParseEndiannessFailsForUnrelatedFormats(String formatIndicatorString) {
        InputStream inputStream = new ByteArrayInputStream(formatIndicatorString.getBytes(StandardCharsets.US_ASCII));

        RawTexUnsupportedFormatException caughtException = Assertions.assertThrows(
                RawTexUnsupportedFormatException.class,
                () -> RawTexHeader.parseEndianness(inputStream)
        );

        Assertions.assertEquals("Unrecognized format indicator", caughtException.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RAWTE", "RAWT", "RAW", "RA", "R", "rawte", "rawt", "raw", "ra", "r"})
    public void testParseEndiannessFailsOnUnexpectedEOF(String formatIndicatorString) {
        InputStream inputStream = new ByteArrayInputStream(formatIndicatorString.getBytes(StandardCharsets.US_ASCII));

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> RawTexHeader.parseEndianness(inputStream)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
    }

}
