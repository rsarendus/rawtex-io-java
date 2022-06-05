package ee.ristoseene.rawtex.io.core.common.format;

import ee.ristoseene.rawtex.io.core.common.octets.OctetSequence;

import java.util.Objects;

/**
 * An enum representing {@code RAWTEX} format indicators.
 */
public enum RawTexFormatIndicator implements OctetSequence {

    /**
     * The singleton instance representing the big-endian {@code RAWTEX} binary format.
     * <p>
     * The octets of the format indicator form an upper case ASCII string "{@code RAWTEX}"
     * (52<sub>h</sub> 41<sub>h</sub> 57<sub>h</sub> 54<sub>h</sub> 45<sub>h</sub> 58<sub>h</sub>).
     */
    BIG_ENDIAN("RAWTEX", Endianness.BIG_ENDIAN),

    /**
     * The singleton instance representing the little-endian {@code RAWTEX} binary format.
     * <p>
     * The octets of the format indicator form a lower case ASCII string "{@code rawtex}"
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
     * {@inheritDoc}
     *
     * @param index {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public final byte octetAt(int index) {
        return octets[index];
    }

    /**
     * Returns the length of this format indicator.
     *
     * @return the number of octets in this format indicator
     */
    @Override
    public final int length() {
        return octets.length;
    }

    /**
     * Converts this format indicator to a byte array.
     *
     * @return a byte array whose length is the length of this format indicator and
     * whose contents are initialized to contain the sequence of bytes represented by this format indicator
     */
    @Override
    public final byte[] toByteArray() {
        return octets.clone();
    }

    /**
     * Returns the textual representation of this format indicator.
     *
     * @return the textual representation of this format indicator
     */
    @Override
    public final String toString() {
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
     * @throws IllegalArgumentException if no format indicator
     * corresponds to the specified textual form
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
