package ee.ristoseene.rawtex.common.internal;

import ee.ristoseene.rawtex.common.exceptions.RawTexUnsupportedFormatException;

import java.io.IOException;
import java.io.InputStream;

public final class RawTexHeader {

    private static final byte[] BIG_ENDIAN_FORMAT_INDICATOR = {'R', 'A', 'W', 'T', 'E', 'X'};
    private static final byte[] LITTLE_ENDIAN_FORMAT_INDICATOR = {'r', 'a', 'w', 't', 'e', 'x'};
    private static final int FORMAT_INDICATOR_LENGTH = 6;

    public static Endianness parseEndianness(InputStream in) throws IOException {
        byte octetValue = CommonIO.readOctet(in);
        byte[] referenceFormat;
        Endianness endianness;

        if (LITTLE_ENDIAN_FORMAT_INDICATOR[0] == octetValue) {
            referenceFormat = LITTLE_ENDIAN_FORMAT_INDICATOR;
            endianness = Endianness.LITTLE_ENDIAN;
        } else if (BIG_ENDIAN_FORMAT_INDICATOR[0] == octetValue) {
            referenceFormat = BIG_ENDIAN_FORMAT_INDICATOR;
            endianness = Endianness.BIG_ENDIAN;
        } else {
            throw unsupportedFormatException();
        }

        for (int i = 1; i < FORMAT_INDICATOR_LENGTH; ++i) {
            if (referenceFormat[i] != CommonIO.readOctet(in)) {
                throw unsupportedFormatException();
            }
        }

        return endianness;
    }

    private static RawTexUnsupportedFormatException unsupportedFormatException() {
        return new RawTexUnsupportedFormatException("Unrecognized format indicator");
    }

    private RawTexHeader() {}

}
