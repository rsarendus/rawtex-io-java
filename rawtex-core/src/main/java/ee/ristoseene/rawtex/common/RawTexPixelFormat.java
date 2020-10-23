package ee.ristoseene.rawtex.common;

/**
 * A generic format for image data that is uniformly divisible into pixels that span (multiple of) whole octets.
 *
 * @see RawTexFormat
 */
public interface RawTexPixelFormat extends RawTexFormat {

    /**
     * Returns the size of a single pixel in blocks.
     *
     * @return the number of blocks per pixel
     *
     * @see RawTexFormat#getOctetsPerBlock()
     */
    int getBlocksPerPixel();

    /**
     * Returns the size of a single pixel in octets (8-bit bytes).
     *
     * @return the number of octets per pixel
     */
    int getOctetsPerPixel();

}
