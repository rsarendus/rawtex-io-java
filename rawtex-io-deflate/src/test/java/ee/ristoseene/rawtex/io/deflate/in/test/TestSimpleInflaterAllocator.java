package ee.ristoseene.rawtex.io.deflate.in.test;

import ee.ristoseene.rawtex.io.deflate.in.data.InflaterAllocator;
import org.junit.jupiter.api.Assertions;

import java.util.Optional;
import java.util.zip.Inflater;

public class TestSimpleInflaterAllocator implements InflaterAllocator {

    private Inflater inflater;
    private boolean freed;

    @Override
    public Inflater allocate(boolean nowrap) {
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
