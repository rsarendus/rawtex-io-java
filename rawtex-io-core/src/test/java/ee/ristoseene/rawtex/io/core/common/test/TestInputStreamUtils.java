package ee.ristoseene.rawtex.io.core.common.test;

import org.mockito.Mockito;
import org.mockito.stubbing.Stubber;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public final class TestInputStreamUtils {

    public static Stubber doAnswerInputStreamReadIntoByteArray(final byte[] bytes) {
        return Mockito.doAnswer(invocationOnMock -> {
            final byte[] array = invocationOnMock.getArgument(0, byte[].class);
            final int offset = (invocationOnMock.getArguments().length > 1)
                    ? invocationOnMock.getArgument(1, Integer.class)
                    : 0;

            System.arraycopy(bytes, 0, array, offset, bytes.length);

            return bytes.length;
        });
    }

    public static Stubber doAnswerInputStreamReadIntoByteArray(final Supplier<IntStream> byteStreamSupplier) {
        return Mockito.doAnswer(invocationOnMock -> {
            final byte[] array = invocationOnMock.getArgument(0, byte[].class);
            final int offset = (invocationOnMock.getArguments().length > 1)
                    ? invocationOnMock.getArgument(1, Integer.class)
                    : 0;

            final AtomicInteger bytesRead = new AtomicInteger(0);

            byteStreamSupplier.get().forEach(i -> {
                array[offset + bytesRead.getAndIncrement()] = (byte) (i & 0xff);
            });

            return bytesRead.intValue();
        });
    }

    public static long getInputStreamLength(InputStream inputStream) throws IOException {
        long length = 0L;

        do {
            final long skipped = inputStream.skip(Long.MAX_VALUE);

            if (skipped > 0L) {
                length += skipped;
            } else if (skipped < 0L) {
                throw new IllegalStateException("Invalid number of bytes skipped: " + skipped);
            } else if (inputStream.read() < 0) {
                break;
            } else {
                ++length;
            }

            if (length < 0L) {
                throw new IllegalStateException("Length not representable using a 64-bit signed integer");
            }
        } while (true);

        return length;
    }

    private TestInputStreamUtils() {
        throw new UnsupportedOperationException();
    }

}
