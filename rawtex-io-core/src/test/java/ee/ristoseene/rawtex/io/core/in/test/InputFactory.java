package ee.ristoseene.rawtex.io.core.in.test;

import java.io.InputStream;
import java.util.Objects;

public abstract class InputFactory {

    private final String description;

    protected InputFactory(String description) {
        this.description = Objects.requireNonNull(description);
    }

    public abstract InputStream create(byte[] source, int offset, int length);

    public InputStream create(byte[] source) {
        return create(source, 0, source.length);
    }

    @Override
    public String toString() {
        return description;
    }

}
