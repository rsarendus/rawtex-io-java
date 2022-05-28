package ee.ristoseene.rawtex.io.deflate.in.data;

import java.util.zip.Inflater;

/**
 * An interface representing an allocator for temporary instances of {@link Inflater}.
 *
 * @see ee.ristoseene.rawtex.io.deflate.common.format.DeflateCompressionFormatIndicator#nowrap
 * @see Inflater#Inflater(boolean)
 */
@FunctionalInterface
public interface InflaterAllocator {

    /**
     * Returns an inflater, meant for temporary usage.
     * The returned inflater is guaranteed to be in a consistent state for processing a new set of input data.
     *
     * @return an inflater, ready for processing a new set of input data
     */
    Inflater allocate();

    /**
     * Notifies that the specified inflater will not be used any more by the caller and the implementation
     * may now destroy it or re-use it as appropriate.
     * <p>
     * <b>After a successful call to this method, any further interaction with the specified
     * inflater is undefined!</b>
     * <p>
     * The default implementation of this method calls {@link Inflater#end()} on {@code handle}.
     *
     * @param handle a handle to an inflater that was previously acquired via a successful call
     *               to {@link #allocate()}
     */
    default void free(Inflater handle) {
        if (handle != null) handle.end();
    }

}
