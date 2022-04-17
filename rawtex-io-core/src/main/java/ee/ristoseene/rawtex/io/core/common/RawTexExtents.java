package ee.ristoseene.rawtex.io.core.common;

/**
 * An entity that encapsulates the extents of an image.
 */
public class RawTexExtents {

    /**
     * Width of an image.
     */
    public final int width;

    /**
     * Height of an image.
     */
    public final int height;

    /**
     * Depth of an image.
     */
    public final int depth;

    /**
     * Number of image layers.
     */
    public final int layers;

    /**
     * Number of mipmap levels.
     */
    public final int levels;

    /**
     * Constructs an instance of {@code RawTexExtents} with the specified width, height, depth, number of image layers
     * and number of mipmap levels.
     * <p>
     * <b>NB:</b> No sanity checks are performed on the input values!
     *
     * @param width image width
     * @param height image height
     * @param depth image depth
     * @param layers number of image layers
     * @param levels number of mipmap levels
     */
    public RawTexExtents(
            int width,
            int height,
            int depth,
            int layers,
            int levels
    ) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.layers = layers;
        this.levels = levels;
    }

}
