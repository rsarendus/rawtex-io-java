package ee.ristoseene.rawtex.io.core.in;

import ee.ristoseene.rawtex.io.core.common.format.BlockSize;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;

/**
 * A factory for creating instances of {@link RawTexDataLoader}.
 *
 * @param <IF> the type of objects representing specific image formats
 * @param <CF> the type of objects representing image data compression formats
 *
 * @see RawTexDataLoader
 */
@FunctionalInterface
public interface RawTexDataLoaderFactory<IF, CF> {

    /**
     * Returns a data loader that is able to load {@code RAWTEX} image data with the specified
     * endianness, block size, image format and compression format.
     *
     * @param endianness endianness of the input data
     * @param blockSize block size of the input data
     * @param imageFormat custom image format
     * @param compressionFormat custom compression format
     *
     * @return an instance of {@link RawTexDataLoader}
     */
    RawTexDataLoader create(Endianness endianness, BlockSize blockSize, IF imageFormat, CF compressionFormat);

}
