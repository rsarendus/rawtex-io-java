package ee.ristoseene.rawtex.io.core.in;

import java.nio.ByteBuffer;

/**
 * An interface representing the destination for RAWTEX image data load operation.
 */
public interface RawTexLoadTarget {

    /**
     * Returns an instance of {@link ByteBuffer} into which RAWTEX image data can be written to.
     * The acquired {@link ByteBuffer} will be written to starting from {@link ByteBuffer#position()} and no more than
     * {@link ByteBuffer#remaining()} octets will be written.
     *
     * @param offset offset (in number of octets) into the RAWTEX image data to be loaded into the requested {@link ByteBuffer}
     * @param remainingLength total number of remaining RAWTEX image data octets to load
     *
     * @return an instance of {@link ByteBuffer} to load image data into
     *
     * @see ByteBuffer
     */
    ByteBuffer acquire(int offset, int remainingLength);

    /**
     * Notifies that load into the remaining length of the specified {@link ByteBuffer} has either been completed
     * successfully or aborted, and the implementation may now perform any clean-up or finalization, if necessary.
     * <p>
     * <b>NB:</b> After a successful call to this method, any further interaction with the specified {@link ByteBuffer}
     * is undefined!
     * <p>
     * The default implementation of this method is a NO-OP.
     *
     * @param buffer a handle to an instance of {@link ByteBuffer} that was previously acquired via a successful call to {@link RawTexLoadTarget#acquire(int, int)}
     * @param complete {@code true} if the remaining length of the specified {@link ByteBuffer} has been fully written to, otherwise {@code false}
     */
    default void release(ByteBuffer buffer, boolean complete) {}

    /**
     * Notifies that load into this {@code RawTexLoadTarget} has either been completed successfully or aborted,
     * and the implementation may now perform any clean-up or finalization, if necessary.
     * <p>
     * <b>NB:</b> After a successful call to this method, any further interaction via this interface is undefined!
     * <p>
     * The default implementation of this method is a NO-OP.
     *
     * @param complete {@code true} if image data has been successfully loaded into this {@code RawTexLoadTarget} in its entirety, otherwise {@code false}
     */
    default void finalize(boolean complete) {}

}
