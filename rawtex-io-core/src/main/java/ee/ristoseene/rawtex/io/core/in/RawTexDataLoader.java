package ee.ristoseene.rawtex.io.core.in;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface representing a loader capable of loading data from an input stream into a {@link RawTexLoadTarget}.
 *
 * @see RawTexLoadTarget
 */
@FunctionalInterface
public interface RawTexDataLoader {

    /**
     * Performs a loading operation from the specified input stream into the specified destination.
     *
     * @param in the input stream to load the data from, not {@code null}
     * @param inputLength the number of octets to read from the input stream {@code in}
     * @param loadTarget destination for the loading operation, not {@code null}
     * @param dataLength total length of the data to load, in number of octets
     *
     * @throws IOException if an I/O error occurs
     *
     * @see RawTexLoadTarget
     */
    void load(InputStream in, long inputLength, RawTexLoadTarget loadTarget, long dataLength) throws IOException;

}
