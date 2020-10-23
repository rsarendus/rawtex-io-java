package ee.ristoseene.rawtex.in;

import ee.ristoseene.rawtex.common.RawTexFormat;

import java.nio.ByteOrder;

/**
 * A factory for creating instances of {@link RawTexDataLoader}.
 *
 * @see RawTexDataLoader
 */
@FunctionalInterface
public interface RawTexDataLoaderFactory {

    /**
     * Returns a {@link RawTexDataLoader} that is able to load RAWTEX image data with the specified format, source
     * endianness, number of data blocks and compression format.
     *
     * @param format format of the input data
     * @param endianness endianness of the input data
     * @param blockCount number of input data blocks
     * @param compressionFormat input data compression format string (e.g. {@code DEFLATE})
     *
     * @return an instance of {@link RawTexDataLoader}
     *
     * @see RawTexFormat
     */
    RawTexDataLoader create(RawTexFormat format, ByteOrder endianness, int blockCount, String compressionFormat);

}
