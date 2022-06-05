package ee.ristoseene.rawtex.io.core.common.data;

/**
 * An interface representing an allocator for temporary transfer buffers.
 */
@FunctionalInterface
public interface TransferBufferAllocator {

    /**
     * Returns a byte array of at least {@code minimumLength} bytes long, meant for temporary usage.
     * The maximum requested length is just a hint, indicating the maximum usable range by the caller,
     * but the implementation is allowed to return either a shorter or a longer array,
     * as long as the minimum requested length is honored.
     * <p>
     * <b>The content of the returned array is not guaranteed to be zero-initialized!</b>
     *
     * @param minimumLength the minimum length of the requested byte array
     * @param maximumLength the maximum usable range in the requested byte array
     *
     * @return a byte array of at least {@code minimumLength} bytes long
     */
    byte[] allocate(int minimumLength, int maximumLength);

    /**
     * Notifies that the specified byte array will not be used any more by the caller
     * and the implementation may now re-use it if applicable.
     * <p>
     * <b>After a successful call to this method, any further interaction with the specified array is undefined!</b>
     * <p>
     * The default implementation of this method is a NO-OP.
     *
     * @param handle a handle to a byte array that was previously acquired via a successful call to
     *               {@link TransferBufferAllocator#allocate(int, int)}
     */
    default void free(byte[] handle) {}

}
