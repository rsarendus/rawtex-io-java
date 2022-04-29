package ee.ristoseene.rawtex.io.core.common.image;

/**
 * An entity that encapsulates the extents of an image.
 */
public final class ImageExtents {

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
     * Constructs an instance of {@code RawTexExtents} with the specified width, height, depth,
     * number of image layers and number of mipmap levels.
     *
     * @param width image width
     * @param height image height
     * @param depth image depth
     * @param layers number of image layers
     * @param levels number of mipmap levels
     *
     * @throws IllegalArgumentException if {@code width}, {@code height}, {@code depth},
     * {@code layers} or {@code levels} is less than {@code 1}
     */
    public ImageExtents(int width, int height, int depth, int layers, int levels) {
        if (width < 1) {
            throw new IllegalArgumentException("Invalid image width: " + width);
        } else if (height < 1) {
            throw new IllegalArgumentException("Invalid image height: " + height);
        } else if (depth < 1) {
            throw new IllegalArgumentException("Invalid image depth: " + depth);
        } else if (layers < 1) {
            throw new IllegalArgumentException("Invalid number of image layers: " + layers);
        } else if (levels < 1) {
            throw new IllegalArgumentException("Invalid number of mipmap levels: " + levels);
        }

        this.width = width;
        this.height = height;
        this.depth = depth;
        this.layers = layers;
        this.levels = levels;
    }

}
