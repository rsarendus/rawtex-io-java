package ee.ristoseene.rawtex.io.core.in;

import java.nio.ByteBuffer;

/**
 * An interface representing the destination for {@code RAWTEX} image data loading operations.
 */
public interface RawTexLoadTarget {

    /**
     * Returns a buffer into which {@code RAWTEX} image data can be written to.
     * The acquired {@link ByteBuffer} will be written to starting from {@link ByteBuffer#position()} and no more than
     * {@link ByteBuffer#remaining()} octets will be written.
     *
     * @param offset offset (in number of octets) of the {@code RAWTEX} image data that will be written into
     *               the acquired buffer
     * @param remainingLength total number of remaining {@code RAWTEX} image data octets to load
     *
     * @return the buffer to load image data into, not {@code null}
     *
     * @see ByteBuffer
     */
    ByteBuffer acquire(int offset, int remainingLength);

    /**
     * Notifies that loading data into the remaining length of the specified buffer has either been completed
     * successfully or aborted. The implementation may now perform any clean-up or finalization, if necessary.
     * <p>
     * <b>After a successful call to this method, any further interaction with the specified {@link ByteBuffer}
     * is undefined!</b>
     * <p>
     * The default implementation of this method is a NO-OP.
     *
     * @param buffer a handle to the buffer that was previously acquired via the {@link #acquire(int, int) acquire}
     *               method
     * @param complete {@code true} if the remaining length of the specified buffer has been fully written to,
     *                 otherwise {@code false}
     */
    default void release(ByteBuffer buffer, boolean complete) {}

    /**
     * Notifies that load into this load target has either been completed successfully or aborted.
     * The implementation may now perform any clean-up or finalization, if necessary.
     * <p>
     * <b>After a successful call to this method, any further interaction via this interface is undefined!</b>
     * <p>
     * The default implementation of this method is a NO-OP.
     *
     * @param complete {@code true} if image data has been successfully loaded into this load target in its entirety,
     *                 otherwise {@code false}
     */
    default void finalize(boolean complete) {}

}
