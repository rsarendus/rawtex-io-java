package ee.ristoseene.rawtex.io.deflate.in.test;

import java.util.Objects;
import java.util.zip.Deflater;

public class CloseableDeflater implements AutoCloseable {

    public final Deflater deflater;

    public CloseableDeflater(Deflater deflater) {
        this.deflater = Objects.requireNonNull(deflater);
    }

    @Override
    public void close() {
        deflater.end();
    }

}
