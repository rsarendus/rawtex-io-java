package ee.ristoseene.rawtex.io.core.common.data;

/**
 * An interface representing the provider of temporary transfer buffers.
 */
@FunctionalInterface
public interface TransferBufferAllocator {

    /**
     * Returns a byte array of at least {@code minRequired} bytes long, meant for temporary usage.
     * The content of the returned array is <b>not</b> guaranteed to be zero initialized.
     *
     * @param minRequired minimum length of the requested byte array
     * @param maxRequired maximum usable length by the caller (returning a longer byte array has no positive
     *                    effect on the caller)
     *
     * @return a byte array of at least {@code minRequired} bytes long
     */
    byte[] allocate(int minRequired, int maxRequired);

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
