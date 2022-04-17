package ee.ristoseene.rawtex.io.core.in;

import java.io.IOException;
import java.io.InputStream;

/**
 * A loader capable of loading data from an input stream into a {@link RawTexLoadTarget}.
 *
 * @see RawTexLoadTarget
 */
@FunctionalInterface
public interface RawTexDataLoader extends AutoCloseable {

    /**
     * Performs a load operation from the specified input stream into the specified destination.
     *
     * @param in the stream to perform the data load from
     * @param inputLength number of octets to read from the input stream {@code in}
     * @param loadTarget destination for the load operation
     * @param dataLength data length in number of octets
     *
     * @throws IOException if an I/O error occurs
     *
     * @see RawTexLoadTarget
     */
    void load(InputStream in, int inputLength, RawTexLoadTarget loadTarget, int dataLength) throws IOException;

    /**
     * Closes this data loader and releases any system resources associated with the loader.
     * <p>
     * The default implementation of this method is a NO-OP.
     *
     * @throws IOException if an error occurs on closing this resource
     */
    @Override
    default void close() throws IOException {}

}
