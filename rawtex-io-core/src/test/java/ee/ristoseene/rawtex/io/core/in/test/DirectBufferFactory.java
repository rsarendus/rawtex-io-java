package ee.ristoseene.rawtex.io.core.in.test;

import java.nio.ByteBuffer;

public class DirectBufferFactory extends TargetBufferFactory {

    private static final String DESCRIPTION = "direct";

    public DirectBufferFactory() {
        super(DESCRIPTION);
    }

    public DirectBufferFactory(int maxBufferSize) {
        super(DESCRIPTION, maxBufferSize);
    }

    @Override
    protected ByteBuffer createBufferImplementation(int length) {
        return ByteBuffer.allocateDirect(length);
    }

}
