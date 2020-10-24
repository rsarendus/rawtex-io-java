package ee.ristoseene.rawtex.in.test;

import java.nio.ByteBuffer;

public class NonDirectBufferFactory extends TargetBufferFactory {

    private static final String DESCRIPTION = "non-direct";

    public NonDirectBufferFactory() {
        super(DESCRIPTION);
    }

    public NonDirectBufferFactory(int maxBufferSize) {
        super(DESCRIPTION, maxBufferSize);
    }

    @Override
    protected ByteBuffer createBufferImplementation(int length) {
        return ByteBuffer.allocate(length);
    }

}
