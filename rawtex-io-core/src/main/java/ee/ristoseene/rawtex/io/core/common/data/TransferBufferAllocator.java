package ee.ristoseene.rawtex.io.core.common.data;

/**
 * An interface representing an allocator for temporary transfer buffers.
 */
@FunctionalInterface
public interface TransferBufferAllocator {

    /**
     * Returns a byte array of at least {@code minimumLength} bytes long, meant for temporary usage.
     * The content of the returned array is <b>not</b> guaranteed to be zero initialized.
     *
     * @param minimumLength minimum length of the requested byte array
     * @param maximumLength maximum usable range in the requested byte array
     *                      (an implementation is allowed to return a longer array)
     *
     * @return a byte array of at least {@code minimumLength} bytes long
     */
    byte[] allocate(int minimumLength, int maximumLength);

    /**
     * Notifies that the specified byte array will not be used any more by the caller and the implementation may now
     * re-use the array if applicable.
     * <p>
     * <b>NB:</b> After a successful call to this method, any further interaction with the specified array is undefined!
     * <p>
     * The default implementation of this method is a NO-OP.
     *
     * @param handle a handle to a byte array that was previously acquired via a successful call to
     *               {@link TransferBufferAllocator#allocate(int, int)}
     */
    default void free(byte[] handle) {}

}
