package ee.ristoseene.rawtex.io.core.in.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class TestStreamingInputStream extends InputStream {

    @FunctionalInterface
    public interface ByteResolver {
        byte resolveByte(long position) throws IOException;
    }

    private final ByteResolver byteResolver;
    private final Long length;
    private long position;

    public TestStreamingInputStream(ByteResolver byteResolver) {
        this.byteResolver = Objects.requireNonNull(byteResolver);
        this.length = null;
    }

    public TestStreamingInputStream(long length, ByteResolver byteResolver) {
        this.byteResolver = Objects.requireNonNull(byteResolver);
        this.length = length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || b.length - off < len) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else if (length == null || position < length) {
            final int readLength = (int) Math.min(len, (length != null ? length : Long.MAX_VALUE) - position);
            long localPosition = position;
            try {
                for (int i = 0; i < readLength; ++i) {
                    b[off + i] = byteResolver.resolveByte(localPosition++);
                }
            } finally {
                position = localPosition;
            }
            return readLength;
        } else {
            return -1;
        }
    }

    @Override
    public int read() throws IOException {
        if (length == null || position < length) {
            return (byteResolver.resolveByte(position++) & 0xff);
        } else {
            return -1;
        }
    }

}
