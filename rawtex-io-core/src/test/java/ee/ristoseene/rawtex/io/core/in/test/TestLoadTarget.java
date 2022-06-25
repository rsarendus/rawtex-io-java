package ee.ristoseene.rawtex.io.core.in.test;

import ee.ristoseene.rawtex.io.core.in.RawTexLoadTarget;
import org.junit.jupiter.api.Assertions;

import java.nio.ByteBuffer;

public abstract class TestLoadTarget implements RawTexLoadTarget {

    private final boolean expectLoadBufferComplete;
    private final boolean expectLoadComplete;
    private boolean finalized;

    protected TestLoadTarget(boolean expectLoadBufferComplete, boolean expectLoadComplete) {
        this.expectLoadBufferComplete = expectLoadBufferComplete;
        this.expectLoadComplete = expectLoadComplete;
    }

    protected abstract ByteBuffer acquireImplementation(long offset, long remainingLength);
    protected abstract void releaseImplementation(ByteBuffer buffer, boolean complete);
    protected abstract void finalizeImplementation(boolean complete);

    @Override
    public ByteBuffer acquire(long offset, long remainingLength) {
        Assertions.assertFalse(finalized, "Load target is already finalized!");
        return acquireImplementation(offset, remainingLength);
    }

    @Override
    public void release(ByteBuffer buffer, boolean complete) {
        Assertions.assertFalse(finalized, "Load target is already finalized!");
        Assertions.assertEquals(expectLoadBufferComplete, complete, "Released buffer expected to be "
                + (expectLoadBufferComplete ? "" : "in") + "complete!");

        releaseImplementation(buffer, complete);
    }

    @Override
    public void finalize(boolean complete) {
        Assertions.assertTrue(finalized, "Load target is already finalized!");
        Assertions.assertEquals(expectLoadComplete, complete, "Finalized load target expected to be "
                + (expectLoadComplete ? "" : "in") + "complete!");

        finalizeImplementation(complete);

        finalized = true;
    }

    public abstract void assertReleased();

    public void assertFinalized() {
        Assertions.assertTrue(finalized, "Load target not finalized!");
    }

}
