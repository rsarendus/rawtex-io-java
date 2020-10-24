package ee.ristoseene.rawtex.common;

/**
 * A generic format for image data consisting of a uniform stream of indivisible endianness-dependent blocks.
 */
public interface RawTexFormat {

    /**
     * Returns the size of a single block in number of octets (8-bit bytes).
     * <p>
     * <b>NB:</b> Block size must be a power of two!
     *
     * @return the number of octets per block
     */
    int getOctetsPerBlock();

}
