package ee.ristoseene.rawtex.in;

import ee.ristoseene.rawtex.common.RawTexFormat;
import ee.ristoseene.rawtex.common.internal.Endianness;

/**
 * A factory for creating instances of {@link RawTexDataLoader}.
 *
 * @see RawTexDataLoader
 */
@FunctionalInterface
public interface RawTexDataLoaderFactory {

    /**
     * Returns a {@link RawTexDataLoader} that is able to load RAWTEX image data with the specified format, source
     * endianness and compression format.
     *
     * @param format format of the input data
     * @param endianness endianness of the input data
     * @param compressionFormat input data compression format string (e.g. {@code DEFLATE}),
     *                          or {@code null} if input data is not compressed
     *
     * @return an instance of {@link RawTexDataLoader}
     *
     * @see RawTexFormat
     */
    RawTexDataLoader create(RawTexFormat format, Endianness endianness, String compressionFormat);

}
