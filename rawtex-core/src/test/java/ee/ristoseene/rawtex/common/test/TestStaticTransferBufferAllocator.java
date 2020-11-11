package ee.ristoseene.rawtex.common.test;

import org.junit.jupiter.api.Assertions;

public class TestStaticTransferBufferAllocator extends TestTransferBufferAllocator {

    private final byte[][] staticBuffers;

    public TestStaticTransferBufferAllocator(int... staticBufferSizes) {
        staticBuffers = new byte[staticBufferSizes.length][];

        for (int i = 0; i < staticBufferSizes.length; ++i) {
            staticBuffers[i] = new byte[staticBufferSizes[i]];
        }
    }

    @Override
    protected byte[] allocateInternal(int minRequired, int maxRequired) {
        boolean tooSmall = false;

        for (byte[] staticBuffer : staticBuffers) {
            if (buffersInUse.contains(staticBuffer)) {
                continue;
            }

            if (staticBuffer.length < minRequired) {
                tooSmall = true;
                continue;
            }

            return staticBuffer;
        }

        Assertions.fail(tooSmall
                ? "Required minimum size is greater than available!"
                : "Buffer(s) already in use!"
        );

        return null;
    }

}
