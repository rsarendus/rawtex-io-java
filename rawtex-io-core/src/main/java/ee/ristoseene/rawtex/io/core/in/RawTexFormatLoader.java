package ee.ristoseene.rawtex.io.core.in;

import java.io.IOException;
import java.io.InputStream;

/**
 * A loader capable of loading specific {@code RAWTEX} binary formats.
 * Loading is delegated to an implementation of this interface by the {@link RawTexLoader}
 * right after the {@code RAWTEX} header (format indicator + major and minor version) has
 * been parsed.
 *
 * @param <IF> the type of objects representing specific image formats
 *
 * @see RawTexFormatLoaderFactory
 * @see RawTexLoader
 */
@FunctionalInterface
public interface RawTexFormatLoader<IF> {

    /**
     * Loads the entire body of {@code RAWTEX} input data from the specified input stream.
     *
     * @param in the input stream to load from, positioned right after the end of the {@code RAWTEX} header
     * @param loadTargetFactory factory that provides the specific {@link RawTexLoadTarget} instance
     *                          to load image data into
     *
     * @throws IOException if an I/O or a parsing error occurs
     *
     * @see RawTexLoadTargetFactory
     */
    void load(InputStream in, RawTexLoadTargetFactory<IF> loadTargetFactory) throws IOException;

}
