package ee.ristoseene.rawtex.io.core.common.image;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ImageExtentsTest {

    @Test
    void testImageExtents() {
        int width = 28593;
        int height = 398783;
        int depth = 2851;
        int layers = 285;
        int levels = 84;

        ImageExtents imageExtents = new ImageExtents(width, height, depth, layers, levels);

        Assertions.assertEquals(width, imageExtents.width);
        Assertions.assertEquals(height, imageExtents.height);
        Assertions.assertEquals(depth, imageExtents.depth);
        Assertions.assertEquals(layers, imageExtents.layers);
        Assertions.assertEquals(levels, imageExtents.levels);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -10, -2, -1, 0})
    void testNegativeOrZeroWidth(int width) {
        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageExtents(width, 1, 1, 1, 1)
        );
        Assertions.assertEquals(
                "Invalid image width: " + width,
                caughtException.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 10, Integer.MAX_VALUE})
    void testPositiveWidth(int width) {
        ImageExtents imageExtents = new ImageExtents(width, 1, 1, 1, 1);

        Assertions.assertEquals(width, imageExtents.width);
        Assertions.assertEquals(1, imageExtents.height);
        Assertions.assertEquals(1, imageExtents.depth);
        Assertions.assertEquals(1, imageExtents.layers);
        Assertions.assertEquals(1, imageExtents.levels);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -10, -2, -1, 0})
    void testNegativeOrZeroHeight(int height) {
        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageExtents(1, height, 1, 1, 1)
        );
        Assertions.assertEquals(
                "Invalid image height: " + height,
                caughtException.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 10, Integer.MAX_VALUE})
    void testPositiveHeight(int height) {
        ImageExtents imageExtents = new ImageExtents(1, height, 1, 1, 1);

        Assertions.assertEquals(1, imageExtents.width);
        Assertions.assertEquals(height, imageExtents.height);
        Assertions.assertEquals(1, imageExtents.depth);
        Assertions.assertEquals(1, imageExtents.layers);
        Assertions.assertEquals(1, imageExtents.levels);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -10, -2, -1, 0})
    void testNegativeOrZeroDepth(int depth) {
        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageExtents(1, 1, depth, 1, 1)
        );
        Assertions.assertEquals(
                "Invalid image depth: " + depth,
                caughtException.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 10, Integer.MAX_VALUE})
    void testPositiveDepth(int depth) {
        ImageExtents imageExtents = new ImageExtents(1, 1, depth, 1, 1);

        Assertions.assertEquals(1, imageExtents.width);
        Assertions.assertEquals(1, imageExtents.height);
        Assertions.assertEquals(depth, imageExtents.depth);
        Assertions.assertEquals(1, imageExtents.layers);
        Assertions.assertEquals(1, imageExtents.levels);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -10, -2, -1, 0})
    void testNegativeOrZeroLayers(int layers) {
        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageExtents(1, 1, 1, layers, 1)
        );
        Assertions.assertEquals(
                "Invalid number of image layers: " + layers,
                caughtException.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 10, Integer.MAX_VALUE})
    void testPositiveLayers(int layers) {
        ImageExtents imageExtents = new ImageExtents(1, 1, 1, layers, 1);

        Assertions.assertEquals(1, imageExtents.width);
        Assertions.assertEquals(1, imageExtents.height);
        Assertions.assertEquals(1, imageExtents.depth);
        Assertions.assertEquals(layers, imageExtents.layers);
        Assertions.assertEquals(1, imageExtents.levels);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -10, -2, -1, 0})
    void testNegativeOrZeroLevels(int levels) {
        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ImageExtents(1, 1, 1, 1, levels)
        );
        Assertions.assertEquals(
                "Invalid number of mipmap levels: " + levels,
                caughtException.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 10, Integer.MAX_VALUE})
    void testPositiveLevels(int levels) {
        ImageExtents imageExtents = new ImageExtents(1, 1, 1, 1, levels);

        Assertions.assertEquals(1, imageExtents.width);
        Assertions.assertEquals(1, imageExtents.height);
        Assertions.assertEquals(1, imageExtents.depth);
        Assertions.assertEquals(1, imageExtents.layers);
        Assertions.assertEquals(levels, imageExtents.levels);
    }

}
