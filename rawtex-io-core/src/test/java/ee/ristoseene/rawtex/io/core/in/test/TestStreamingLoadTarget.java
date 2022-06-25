package ee.ristoseene.rawtex.io.core.in.test;

import ee.ristoseene.rawtex.io.core.common.test.LongToByteFunction;
import ee.ristoseene.rawtex.io.core.common.test.TestBufferUtils;
import org.junit.jupiter.api.Assertions;

import java.nio.ByteBuffer;
import java.util.Objects;

public class TestStreamingLoadTarget extends TestLoadTarget {

    private final ByteBuffer streamingBuffer;
    private final LongToByteFunction expectedBufferContentProducer;
    private InFlight inFlight;

    public TestStreamingLoadTarget(ByteBuffer streamingBuffer) {
        this(streamingBuffer, null, true, true);
    }

    public TestStreamingLoadTarget(ByteBuffer streamingBuffer, LongToByteFunction expectedBufferContentProducer) {
        this(streamingBuffer, expectedBufferContentProducer, true, true);
    }

    public TestStreamingLoadTarget(ByteBuffer streamingBuffer, LongToByteFunction expectedBufferContentProducer, boolean expectLoadBufferComplete, boolean expectLoadComplete) {
        super(expectLoadBufferComplete, expectLoadComplete);
        this.streamingBuffer = Objects.requireNonNull(streamingBuffer);
        this.expectedBufferContentProducer = expectedBufferContentProducer;
    }

    @Override
    protected ByteBuffer acquireImplementation(long offset, long remainingLength) {
        Assertions.assertNull(inFlight, "A buffer is already in flight!");
        final int lengthToAcquire = (int) Math.min(streamingBuffer.capacity(), remainingLength);

        if (expectedBufferContentProducer != null) {
            TestBufferUtils.zeroFillBuffer(streamingBuffer, 0, lengthToAcquire);
        }

        inFlight = new InFlight(offset, lengthToAcquire);
        streamingBuffer.position(0).limit(lengthToAcquire);
        return streamingBuffer;
    }

    @Override
    protected void releaseImplementation(ByteBuffer buffer, boolean complete) {
        Assertions.assertNotNull(inFlight, "No in flight buffer to release!");
        Assertions.assertSame(streamingBuffer, buffer, "Mismatching buffer to release!");

        if (expectedBufferContentProducer != null) {
            verifyBufferContent();
        }

        inFlight = null;
    }

    @Override
    protected void finalizeImplementation(boolean complete) {
        Assertions.assertNull(inFlight, "Buffer still in flight during finalize!");
    }

    @Override
    public void assertReleased() {
        Assertions.assertNull(inFlight, "Load target not released!");
    }

    private void verifyBufferContent() {
        final long dataOffset = inFlight.dataOffset;
        final int transferLength = inFlight.transferLength;

        if (streamingBuffer.hasArray()) {
            // Read the backing array of the buffer directly for more performance
            final int arrayOffset = streamingBuffer.arrayOffset();
            final byte[] array = streamingBuffer.array();

            for (int i = 0; i < transferLength; ++i) {
                verifyValueAtOffset(dataOffset + i, array[arrayOffset + i]);
            }
        } else {
            // Copy data out from the buffer in chunks for more performance
            final byte[] array = new byte[Math.min(transferLength, 1024)];
            streamingBuffer.clear();

            for (int i = 0; i < transferLength; i += array.length) {
                final int localTransferLength = Math.min(array.length, transferLength - i);
                streamingBuffer.get(array, 0, localTransferLength);
                final long localDataOffset = dataOffset + i;

                for (int j = 0; j < localTransferLength; ++j) {
                    verifyValueAtOffset(localDataOffset + j, array[j]);
                }
            }
        }
    }

    private void verifyValueAtOffset(long offset, byte value) {
        final byte expectedValue = expectedBufferContentProducer.apply(offset);
        Assertions.assertEquals(expectedValue, value, "Loaded content mismatch!");
    }

    static class InFlight {

        public final long dataOffset;
        public final int transferLength;

        public InFlight(long dataOffset, int transferLength) {
            this.dataOffset = dataOffset;
            this.transferLength = transferLength;
        }

    }

}
