package ee.ristoseene.rawtex.io.core.in;

import ee.ristoseene.rawtex.io.core.common.exceptions.RawTexUnsupportedFormatException;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;

/**
 * A factory for creating instances of {@link RawTexFormatLoader}.
 *
 * @param <IF> the type of objects representing specific image formats
 *
 * @see RawTexFormatLoader
 * @see RawTexLoader
 */
@FunctionalInterface
public interface RawTexFormatLoaderFactory<IF> {

    /**
     * Returns a loader capable of loading specific {@code RAWTEX} binary formats with the specified
     * endianness, major version and minor version combination.
     *
     * @param endianness the endianness to support
     * @param majorVersion the major version to support
     * @param minorVersion the minor version to support
     *
     * @return a loader capable of loading specified {@code RAWTEX} binary formats
     *
     * @throws RawTexUnsupportedFormatException if the specified endianness and version combination is not supported
     */
    RawTexFormatLoader<IF> create(Endianness endianness, int majorVersion, int minorVersion) throws RawTexUnsupportedFormatException;

}
