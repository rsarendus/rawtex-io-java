package ee.ristoseene.rawtex.io.deflate.in.test;

import ee.ristoseene.rawtex.io.deflate.common.format.DeflateCompressionFormatIndicator;
import ee.ristoseene.rawtex.io.deflate.in.data.InflaterAllocator;
import org.junit.jupiter.api.Assertions;

import java.util.Objects;
import java.util.Optional;
import java.util.zip.Inflater;

public class TestSimpleInflaterAllocator implements InflaterAllocator {

    private final boolean nowrap;
    private Inflater inflater;
    private boolean freed;

    public TestSimpleInflaterAllocator(boolean nowrap) {
        this.nowrap = nowrap;
    }

    public TestSimpleInflaterAllocator(DeflateCompressionFormatIndicator compressionFormatIndicator) {
        this(Objects.requireNonNull(compressionFormatIndicator).nowrap);
    }

    @Override
    public Inflater allocate() {
        Assertions.assertNull(inflater, "Inflater already allocated");
        return (inflater = new Inflater(nowrap));
    }

    @Override
    public void free(Inflater handle) {
        try {
            Assertions.assertNotNull(inflater, "Inflater not allocated yet");
            Assertions.assertSame(inflater, handle);
            freed = true;
        } finally {
            Optional.ofNullable(handle).ifPresent(Inflater::end);
        }
    }

    public void assertFreed() {
        Assertions.assertNotNull(inflater, "Inflater not even allocated");
        Assertions.assertTrue(freed, "Inflater not freed");
    }

}
