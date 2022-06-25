package ee.ristoseene.rawtex.io.core.in.test;

import org.junit.jupiter.api.Assertions;

import java.nio.ByteBuffer;
import java.util.Objects;

public class TestFixedLengthLoadTarget extends TestLoadTarget {

    @FunctionalInterface
    public interface BufferFactory {
        ByteBuffer createFor(int offset, int remainingLength);
    }

    private final byte[] expectedData;
    private final BufferFactory bufferFactory;
    private InFlight inFlight;

    public TestFixedLengthLoadTarget(byte[] expectedData, BufferFactory bufferFactory) {
        this(expectedData, bufferFactory, true, true);
    }

    public TestFixedLengthLoadTarget(byte[] expectedData, BufferFactory bufferFactory, boolean expectLoadBufferComplete, boolean expectLoadComplete) {
        super(expectLoadBufferComplete, expectLoadComplete);
        this.expectedData = Objects.requireNonNull(expectedData).clone();
        this.bufferFactory = Objects.requireNonNull(bufferFactory);
    }

    @Override
    protected ByteBuffer acquireImplementation(long offset, long remainingLength) {
        Assertions.assertNull(inFlight, "A buffer is already in flight!");

        if (offset > Integer.MAX_VALUE || remainingLength > Integer.MAX_VALUE) {
            throw new IllegalStateException("Value too large: " + offset + "/" + remainingLength);
        }

        ByteBuffer byteBuffer = bufferFactory.createFor((int) offset, (int) remainingLength);
        inFlight = new InFlight(byteBuffer, (int) offset);
        return byteBuffer;
    }

    @Override
    protected void releaseImplementation(ByteBuffer buffer, boolean complete) {
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
    protected void finalizeImplementation(boolean complete) {
        Assertions.assertNull(inFlight, "Buffer still in flight during finalize!");
    }

    @Override
    public void assertReleased() {
        Assertions.assertNull(inFlight, "Load target not released!");
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
