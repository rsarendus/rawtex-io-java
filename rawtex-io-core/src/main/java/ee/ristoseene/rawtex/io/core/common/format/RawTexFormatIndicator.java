package ee.ristoseene.rawtex.io.core.common.format;

import ee.ristoseene.rawtex.io.core.common.octets.OctetSequence;

import java.util.Objects;

/**
 * An enum representing {@code RAWTEX} format indicators.
 */
public enum RawTexFormatIndicator implements OctetSequence {

    /**
     * Big-endian format indicator, represented by the upper case ASCII string "{@code RAWTEX}"
     * (52<sub>h</sub> 41<sub>h</sub> 57<sub>h</sub> 54<sub>h</sub> 45<sub>h</sub> 58<sub>h</sub>).
     */
    BIG_ENDIAN("RAWTEX", Endianness.BIG_ENDIAN),

    /**
     * Little-endian format indicator, represented by the lower case ASCII string "{@code rawtex}"
     * (72<sub>h</sub> 61<sub>h</sub> 77<sub>h</sub> 74<sub>h</sub> 65<sub>h</sub> 78<sub>h</sub>).
     */
    LITTLE_ENDIAN("rawtex", Endianness.LITTLE_ENDIAN);

    /**
     * Endianness of the format.
     */
    public final Endianness endianness;

    private final byte[] octets;
    private final String string;

    RawTexFormatIndicator(String formatIndicatorString, Endianness endianness) {
        this.endianness = Objects.requireNonNull(endianness);
        this.string = Objects.requireNonNull(formatIndicatorString);

        final int length = formatIndicatorString.length();
        octets = new byte[length];

        for (int i = 0; i < length; ++i) {
            octets[i] = (byte) formatIndicatorString.charAt(i);
        }
    }

    /**
     * Returns the octet value at the specified index.
     * An index ranges from {@code 0} to {@code length() - 1}.
     *
     * @param index the index of the octet value to be returned
     *
     * @return the specified octet value
     *
     * @throws IndexOutOfBoundsException if {@code index} is negative or not less than {@code length()}
     */
    @Override
    public byte octetAt(int index) {
        return octets[index];
    }

    /**
     * Returns the length of this format indicator.
     *
     * @return the number of octets in this format indicator
     */
    @Override
    public int length() {
        return octets.length;
    }

    /**
     * Converts this format indicator to a byte array.
     *
     * @return a byte array whose length is the length of this format indicator and
     * whose contents are initialized to contain the sequence of bytes represented by this format indicator
     */
    @Override
    public byte[] toByteArray() {
        return octets.clone();
    }

    /**
     * Returns the textual representation of this format indicator.
     *
     * @return the textual representation of this format indicator
     */
    @Override
    public String toString() {
        return string;
    }

    /**
     * Obtains a format indicator from endianness.
     *
     * @param endianness endianness of the format indicator
     *
     * @return the format indicator, not {@code null}
     *
     * @throws IllegalArgumentException if no format indicator
     * corresponds to the specified endianness
     */
    public static RawTexFormatIndicator of(Endianness endianness) {
        switch (endianness) {

            case BIG_ENDIAN:
                return BIG_ENDIAN;

            case LITTLE_ENDIAN:
                return LITTLE_ENDIAN;

            default:
                throw new IllegalArgumentException("Unsupported endianness: " + endianness);

        }
    }

    /**
     * Obtains a format indicator from its textual representation.
     *
     * @param formatIndicatorString textual representation of the format indicator
     *
     * @return the format indicator, not {@code null}
     *
     * @throws IllegalArgumentException if not format indicator
     * corresponds to the specified text
     */
    public static RawTexFormatIndicator of(String formatIndicatorString) {
        if (LITTLE_ENDIAN.string.equals(formatIndicatorString)) {
            return LITTLE_ENDIAN;
        } else if (BIG_ENDIAN.string.equals(formatIndicatorString)) {
            return BIG_ENDIAN;
        } else {
            throw new IllegalArgumentException("Unrecognized format indicator: " + formatIndicatorString);
        }
    }

}
