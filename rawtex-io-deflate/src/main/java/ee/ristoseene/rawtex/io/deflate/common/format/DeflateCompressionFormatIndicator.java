package ee.ristoseene.rawtex.io.deflate.common.format;

import ee.ristoseene.rawtex.io.core.common.octets.OctetSequence;

import java.util.Objects;

/**
 * An enum representing {@code DEFLATE} compression format indicators.
 */
public enum DeflateCompressionFormatIndicator implements OctetSequence {

    /**
     * The singleton instance representing raw unwrapped {@code DEFLATE} streams.
     * <p>
     * The octets of the format indicator form an upper case ASCII string "{@code DEFLATE}"
     * (44<sub>h</sub> 45<sub>h</sub> 46<sub>h</sub> 4C<sub>h</sub> 41<sub>h</sub> 54<sub>h</sub> 45<sub>h</sub>).
     */
    DEFLATE("DEFLATE", true),

    /**
     * The singleton instance representing {@code zlib}-formatted {@code DEFLATE} streams.
     * The {@code zlib} format has a two-byte header to identify the stream and to provide decoding information,
     * and a four-byte trailer for integrity checking of the uncompressed data after decoding.
     * <p>
     * The octets of the format indicator form a lower case ASCII string "{@code zlib}"
     * (7A<sub>h</sub> 6C<sub>h</sub> 69<sub>h</sub> 62<sub>h</sub>).
     */
    ZLIB("zlib", false);

    /**
     * Whether to omit additional headers and checksum fields from around {@code DEFLATE} streams.
     *
     * @see java.util.zip.Deflater#Deflater(int, boolean)
     * @see java.util.zip.Inflater#Inflater(boolean)
     */
    public final boolean nowrap;

    private final byte[] octets;
    private final String string;

    DeflateCompressionFormatIndicator(String formatIndicatorString, boolean nowrap) {
        this.string = Objects.requireNonNull(formatIndicatorString);
        this.nowrap = nowrap;

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
     * Obtains a format indicator from its textual representation.
     *
     * @param formatIndicatorString textual representation of the format indicator
     *
     * @return the format indicator, not {@code null}
     *
     * @throws IllegalArgumentException if no format indicator
     * corresponds to the specified textual form
     */
    public static DeflateCompressionFormatIndicator of(String formatIndicatorString) {
        if (DEFLATE.string.equals(formatIndicatorString)) {
            return DEFLATE;
        } else if (ZLIB.string.equals(formatIndicatorString)) {
            return ZLIB;
        } else {
            throw new IllegalArgumentException("Unrecognized format indicator: " + formatIndicatorString);
        }
    }

}
