package ee.ristoseene.rawtex.io.core.in;

import ee.ristoseene.rawtex.io.core.common.image.ImageExtents;

import java.nio.ByteOrder;

/**
 * A factory for creating instances of {@link RawTexLoadTarget}.
 *
 * @param <IF> the type of objects representing specific image formats
 *
 * @see RawTexLoadTarget
 */
@FunctionalInterface
public interface RawTexLoadTargetFactory<IF> {

    /**
     * Returns a {@link RawTexLoadTarget} that is able to receive {@code RAWTEX} image data with the specified
     * format, image extents, source endianness and total length.
     *
     * @param imageFormat format of the image
     * @param imageExtents extents of the image
     * @param byteOrder byte order of the image data to load
     * @param totalDataLength total length (in number of octets) of the {@code RAWTEX} image data to load
     *
     * @return an instance of {@link RawTexLoadTarget}
     *
     * @see java.nio.ByteBuffer#order()
     * @see java.nio.ByteBuffer#order(ByteOrder) 
     */
    RawTexLoadTarget create(IF imageFormat, ImageExtents imageExtents, ByteOrder byteOrder, long totalDataLength);

}
