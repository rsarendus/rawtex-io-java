package ee.ristoseene.rawtex.io.core.in;

import ee.ristoseene.rawtex.io.core.common.RawTexExtents;
import ee.ristoseene.rawtex.io.core.common.RawTexFormat;

import java.nio.ByteOrder;

/**
 * A factory for creating instances of {@link RawTexLoadTarget}.
 *
 * @see RawTexLoadTarget
 */
@FunctionalInterface
public interface RawTexLoadTargetFactory {

    /**
     * Returns a {@link RawTexLoadTarget} that is able to receive RAWTEX image data with the specified format, image
     * extents, source endianness and total length.
     *
     * @param format format of the image
     * @param extents extents of the image
     * @param endianness endianness of the image data to load
     * @param totalDataLength total length (in number of octets) of the RAWTEX image data to load
     *
     * @return an instance of {@link RawTexLoadTarget}
     *
     * @see RawTexFormat
     * @see RawTexExtents
     */
    RawTexLoadTarget create(RawTexFormat format, RawTexExtents extents, ByteOrder endianness, int totalDataLength);

}
