package ee.ristoseene.rawtex.io.core.common.test;

import org.mockito.Mockito;
import org.mockito.stubbing.Stubber;

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

    private TestInputStreamUtils() {
        throw new UnsupportedOperationException();
    }

}
