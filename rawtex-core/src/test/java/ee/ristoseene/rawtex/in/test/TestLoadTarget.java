package ee.ristoseene.rawtex.in.test;

import ee.ristoseene.rawtex.in.RawTexLoadTarget;
import org.junit.jupiter.api.Assertions;

import java.nio.ByteBuffer;
import java.util.Objects;

public class TestLoadTarget implements RawTexLoadTarget {

    @FunctionalInterface
    public interface BufferFactory {
        ByteBuffer createFor(int offset, int remainingLength);
    }

    private final byte[] expectedData;
    private final BufferFactory bufferFactory;
    private final boolean expectLoadBufferComplete;
    private final boolean expectLoadComplete;
    private InFlight inFlight;
    private boolean finalized;

    public TestLoadTarget(byte[] expectedData, BufferFactory bufferFactory) {
        this(expectedData, bufferFactory, true, true);
    }

    public TestLoadTarget(byte[] expectedData, BufferFactory bufferFactory, boolean expectLoadBufferComplete, boolean expectLoadComplete) {
        this.expectedData = Objects.requireNonNull(expectedData).clone();
        this.bufferFactory = Objects.requireNonNull(bufferFactory);
        this.expectLoadBufferComplete = expectLoadBufferComplete;
        this.expectLoadComplete = expectLoadComplete;
    }

    @Override
    public ByteBuffer acquire(int offset, int remainingLength) {
        Assertions.assertFalse(finalized, "Load target is already finalized!");
        Assertions.assertNull(inFlight, "A buffer is already in flight!");

        ByteBuffer byteBuffer = bufferFactory.createFor(offset, remainingLength);
        inFlight = new InFlight(byteBuffer, offset);
        return byteBuffer;
    }

    @Override
    public void release(ByteBuffer buffer, boolean complete) {
        Assertions.assertFalse(finalized, "Load target is already finalized!");
        Assertions.assertEquals(expectLoadBufferComplete, complete, "Released buffer expected to be "
                + (expectLoadBufferComplete ? "" : "in") + "complete!");
        if (buffer == null) return;

        Assertions.assertNotNull(inFlight, "No in flight buffer to release!");
        Assertions.assertSame(inFlight.buffer, buffer, "Release of mismatching in flight buffer requested!");
        Assertions.assertEquals(inFlight.initialBufferLimit, buffer.limit(), "Buffer limit has changed!");
        Assertions.assertEquals(inFlight.initialBufferLimit, buffer.position(), "Buffer not fully written!");

        int bytesCopied = inFlight.initialBufferLimit - inFlight.initialBufferPosition;
        byte[] expectedBufferContent = inFlight.initialBufferContent.clone();

        if (buffer.hasArray()) {
            int contentOffset = buffer.arrayOffset() + inFlight.initialBufferPosition;
            System.arraycopy(expectedData, inFlight.targetDataOffset, expectedBufferContent, contentOffset, bytesCopied);
        } else {
            System.arraycopy(expectedData, inFlight.targetDataOffset, expectedBufferContent, inFlight.initialBufferPosition, bytesCopied);
        }

        byte[] actualBufferContent = copyContent(buffer);
        Assertions.assertArrayEquals(expectedBufferContent, actualBufferContent, "Loaded content mismatch!");

        inFlight = null;
    }

    @Override
    public void finalize(boolean complete) {
        Assertions.assertTrue(finalized, "Load target is already finalized!");
        Assertions.assertEquals(expectLoadComplete, complete, "Finalized load target expected to be "
                + (expectLoadComplete ? "" : "in") + "complete!");
        Assertions.assertNull(inFlight, "Buffer still in flight during finalize!");
        finalized = true;
    }

    public void assertReleased() {
        Assertions.assertNull(inFlight, "Load target not released!");
    }

    public void assertFinalized() {
        Assertions.assertTrue(finalized, "Load target not finalized!");
    }

    static class InFlight {

        public final ByteBuffer buffer;
        public final byte[] initialBufferContent;
        public final int initialBufferPosition;
        public final int initialBufferLimit;
        public final int targetDataOffset;

        public InFlight(ByteBuffer byteBuffer, int dataOffset) {
            buffer = Objects.requireNonNull(byteBuffer);
            initialBufferContent = copyContent(byteBuffer);
            initialBufferPosition = byteBuffer.position();
            initialBufferLimit = byteBuffer.limit();
            targetDataOffset = dataOffset;
        }

    }

    private static byte[] copyContent(ByteBuffer buffer) {
        if (buffer.hasArray()) {
            return buffer.array().clone();
        } else {
            byte[] content = new byte[buffer.capacity()];
            for (int i = 0; i < content.length; ++i) {
                content[i] = buffer.get(i);
            }
            return content;
        }
    }

}
