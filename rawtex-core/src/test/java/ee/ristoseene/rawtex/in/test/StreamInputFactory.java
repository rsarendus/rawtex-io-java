package ee.ristoseene.rawtex.in.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class StreamInputFactory extends InputFactory {

    public StreamInputFactory() {
        super("input stream");
    }

    @Override
    public InputStream create(byte[] source, int offset, int length) {
        return new ByteArrayInputStream(source, offset, length);
    }

}
