package ee.ristoseene.rawtex.io.core.in.test;

import java.nio.ByteBuffer;
import java.util.Objects;

public abstract class TargetBufferFactory {

    private final String description;
    private final Integer maxBufferSize;

    protected TargetBufferFactory(String description) {
        this.description = Objects.requireNonNull(description);
        this.maxBufferSize = null;
    }

    protected TargetBufferFactory(String description, int maxBufferSize) {
        this.description = Objects.requireNonNull(description);
        this.maxBufferSize = maxBufferSize;
    }

    protected abstract ByteBuffer createBufferImplementation(int length);

    public ByteBuffer create(int maxLength) {
        if (maxBufferSize != null) {
            return createBufferImplementation(Math.min(maxBufferSize, maxLength));
        } else {
            return createBufferImplementation(maxLength);
        }
    }

    @Override
    public String toString() {
        if (maxBufferSize != null) {
            return description + " (max length: " + maxBufferSize + ")";
        } else {
            return description;
        }
    }

}
