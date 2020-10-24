package ee.ristoseene.rawtex.in.test;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.OptionalInt;

public abstract class TargetBufferFactory implements TestLoadTarget.BufferFactory {

    private final String description;
    private final OptionalInt maxBufferSize;

    protected TargetBufferFactory(String description) {
        this.description = Objects.requireNonNull(description);
        this.maxBufferSize = OptionalInt.empty();
    }

    protected TargetBufferFactory(String description, int maxBufferSize) {
        this.description = Objects.requireNonNull(description);
        this.maxBufferSize = OptionalInt.of(maxBufferSize);
    }

    protected abstract ByteBuffer createBufferImplementation(int length);

    @Override
    public ByteBuffer createFor(int offset, int remainingLength) {
        if (maxBufferSize.isPresent()) {
            return createBufferImplementation(Math.min(maxBufferSize.getAsInt(), remainingLength));
        } else {
            return createBufferImplementation(remainingLength);
        }
    }

    @Override
    public String toString() {
        if (maxBufferSize.isPresent()) {
            return description + " (max length: " + maxBufferSize.getAsInt() + ")";
        } else {
            return description;
        }
    }

}
