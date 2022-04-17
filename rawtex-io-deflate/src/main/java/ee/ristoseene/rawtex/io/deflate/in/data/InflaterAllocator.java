package ee.ristoseene.rawtex.io.deflate.in.data;

import java.util.zip.Inflater;

/**
 * An interface representing an allocator for temporary instances of {@link Inflater}.
 */
@FunctionalInterface
public interface InflaterAllocator {

    /**
     * Returns an instance of {@link Inflater}, meant for temporary usage.
     * The returned {@link Inflater} is guaranteed to be in a consistent state for processing a new set of input data.
     *
     * @param nowrap if {@code true} then no additional headers nor checksum fields are used around the DEFLATE stream
     *
     * @return an instance of {@link Inflater}, ready for processing a new set of input data
     *
     * @see Inflater#Inflater(boolean)
     */
    Inflater allocate(boolean nowrap);

    /**
     * Notifies that the specified {@link Inflater} will not be used any more by the caller and the implementation
     * may now destroy it or re-use it as appropriate.
     * <p>
     * <b>NB:</b> After a successful call to this method, any further interaction with the specified instance
     * of {@link Inflater} is undefined!
     * <p>
     * The default implementation of this method calls {@link Inflater#end()} on {@code handle}.
     *
     * @param handle a handle to an instance of {@link Inflater} that was previously acquired via a successful call to
     *               {@link InflaterAllocator#allocate(boolean)}
     */
    default void free(Inflater handle) {
        if (handle != null) handle.end();
    }

}
