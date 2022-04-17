package ee.ristoseene.rawtex.io.core.in.test;

import ee.ristoseene.rawtex.io.core.common.internal.ArraySource;

import java.io.InputStream;

public class ByteArrayInputFactory extends InputFactory {

    public ByteArrayInputFactory() {
        super("byte array");
    }

    @Override
    public InputStream create(byte[] source, int offset, int length) {
        return new ArraySource(source, offset, length);
    }

}
