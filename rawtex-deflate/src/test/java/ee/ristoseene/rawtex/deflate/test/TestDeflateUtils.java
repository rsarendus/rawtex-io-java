package ee.ristoseene.rawtex.deflate.test;

import java.util.Arrays;
import java.util.zip.Deflater;

public final class TestDeflateUtils {

    public static byte[] deflate(byte[] data) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
        byte[] outBuffer = new byte[Math.max(data.length * 2, 1024)];

        try {
            deflater.setInput(data);
            deflater.finish();

            int outLength = deflater.deflate(outBuffer);
            return Arrays.copyOf(outBuffer, outLength);
        } finally {
            deflater.end();
        }
    }

    private TestDeflateUtils() {}

}
