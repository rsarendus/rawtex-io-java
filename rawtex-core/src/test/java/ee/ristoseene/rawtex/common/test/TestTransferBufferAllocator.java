package ee.ristoseene.rawtex.common.test;

import ee.ristoseene.rawtex.common.data.TransferBufferAllocator;
import org.junit.jupiter.api.Assertions;

import java.util.HashSet;
import java.util.Set;

public abstract class TestTransferBufferAllocator implements TransferBufferAllocator {

    protected final Set<byte[]> buffersInUse = new HashSet<>();

    protected abstract byte[] allocateInternal(int minRequired, int maxRequired);
    protected void freeInternal(byte[] handle) {}

    @Override
    public byte[] allocate(int minRequired, int maxRequired) {
        byte[] buffer = allocateInternal(minRequired, maxRequired);

        if (!buffersInUse.add(buffer)) {
            Assertions.fail("Buffer is already in use!");
        }

        return buffer;
    }

    @Override
    public void free(byte[] handle) {
        if (!buffersInUse.remove(handle)) {
            Assertions.fail(buffersInUse.isEmpty()
                    ? "No buffers currently in use!"
                    : "Free of mismatching buffer requested!"
            );
        }

        freeInternal(handle);
    }

    public void assertFreed() {
        Assertions.assertTrue(buffersInUse.isEmpty(), () -> buffersInUse.size() + " buffer(s) not freed!");
    }

}
