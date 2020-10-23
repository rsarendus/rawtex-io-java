package ee.ristoseene.rawtex.in;

import ee.ristoseene.rawtex.common.internal.Endianness;

import java.io.IOException;
import java.io.InputStream;

/**
 * A loader capable of loading (a) specific version(s) of the RAWTEX binary format.
 */
public interface RawTexVersionLoader {

    /**
     * Loads the entire body of RAWTEX input data from the specified input stream.
     *
     * @param in the stream to perform the load from, positioned right after the end of the RAWTEX header
     * @param endianness endianness of the input data, determined from the RAWTEX format indicator
     * @param loadTargetFactory factory that provides the specific {@link RawTexLoadTarget} instance to load image data into
     * @param dataLoaderFactory factory that provides the specific {@link RawTexDataLoader} instance to perform the load operation of the image data
     *
     * @throws IOException if an I/O or a parsing error occurs
     *
     * @see RawTexLoadTargetFactory
     * @see RawTexDataLoaderFactory
     */
    void load(InputStream in, Endianness endianness, RawTexLoadTargetFactory loadTargetFactory, RawTexDataLoaderFactory dataLoaderFactory) throws IOException;

    /**
     * Returns whether this version loader supports the specified major-minor version combination.
     *
     * @param major major version to check against
     * @param minor minor version to check against
     *
     * @return {@code true} if the specified version is supported, otherwise {@code false}
     */
    boolean supportsVersion(int major, int minor);

}
