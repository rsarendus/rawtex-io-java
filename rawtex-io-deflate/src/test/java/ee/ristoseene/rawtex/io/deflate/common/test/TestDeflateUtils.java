package ee.ristoseene.rawtex.io.deflate.common.test;

import ee.ristoseene.rawtex.io.deflate.common.format.DeflateCompressionFormatIndicator;

import java.util.Arrays;
import java.util.zip.Deflater;

public final class TestDeflateUtils {

    public static byte[] deflate(DeflateCompressionFormatIndicator compressionFormatIndicator, byte[] data) {
        return deflate(compressionFormatIndicator.nowrap, data);
    }

    public static byte[] deflate(boolean nowrap, byte[] data) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, nowrap);

        try {
            byte[] outBuffer = new byte[Math.max(data.length, 8)];
            int outLength = 0;

            deflater.setInput(data);
            deflater.finish();

            while (true) {
                outLength += deflater.deflate(outBuffer, outLength, outBuffer.length - outLength);

                if (deflater.finished()) {
                    break;
                } else {
                    outBuffer = Arrays.copyOf(outBuffer, outBuffer.length * 2);
                }
            }

            return Arrays.copyOf(outBuffer, outLength);
        } finally {
            deflater.end();
        }
    }

    private TestDeflateUtils() {
        throw new UnsupportedOperationException();
    }

}
