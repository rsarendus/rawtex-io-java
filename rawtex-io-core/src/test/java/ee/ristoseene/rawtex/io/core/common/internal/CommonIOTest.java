package ee.ristoseene.rawtex.io.core.common.internal;

import ee.ristoseene.rawtex.io.core.common.test.TestBufferUtils;
import ee.ristoseene.rawtex.io.core.common.test.TestInputStreamUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.stubbing.Stubber;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class CommonIOTest {

    @ParameterizedTest
    @ValueSource(bytes = {0, 1, 0x7f, (byte) 0x80, (byte) 0xff})
    void testReadOctetSucceeds(int byteValue) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        Mockito.doReturn(byteValue & 0xff).when(inputStream).read();

        byte result = CommonIO.readOctet(inputStream);

        Assertions.assertEquals(byteValue, result);
        Mockito.verify(inputStream, Mockito.times(1)).read();
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -10, -2, -1})
    void testReadOctetFailsWhenInputStreamReturnsNegativeValue(int returnValue) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        Mockito.doReturn(returnValue).when(inputStream).read();

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> CommonIO.readOctet(inputStream)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verify(inputStream, Mockito.times(1)).read();
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @Test
    void testReadOctetFailsWhenInputStreamThrowsException() throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        IOException exceptionToThrow = new IOException("A message");
        Mockito.doThrow(exceptionToThrow).when(inputStream).read();

        IOException caughtException = Assertions.assertThrows(
                IOException.class,
                () -> CommonIO.readOctet(inputStream)
        );

        Assertions.assertSame(exceptionToThrow, caughtException);
        Mockito.verify(inputStream, Mockito.times(1)).read();
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void testReadOctetsSucceedsWhenInputStreamReadsPositiveNumberOfOctets(int readLength) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = {-1, -1, -1, -1, -1, -1};

        doAnswerInputStreamReadIntoByteArray(readLength).when(inputStream)
                .read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());

        int result = CommonIO.readOctets(inputStream, buffer, 2, 3);

        Assertions.assertEquals(readLength, result);
        Assertions.assertArrayEquals(
                TestBufferUtils.createByteArray(6, (byte) -1, 2, IntStream.rangeClosed(1, readLength)),
                buffer
        );
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 2, 3);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -10, -2, -1})
    void testReadOctetsFailsWhenInputStreamReturnsNegativeValue(int returnValue) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = {-1, -1, -1, -1};

        Mockito.doReturn(returnValue).when(inputStream).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> CommonIO.readOctets(inputStream, buffer, 1, 2)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Assertions.assertArrayEquals(new byte[] {-1, -1, -1, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 2);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @Test
    void testReadOctetsFailsWhenInputStreamThrowsException() throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        IOException exceptionToThrow = new IOException("A message");
        byte[] buffer = {-1, -1, -1};

        Mockito.doThrow(exceptionToThrow).when(inputStream).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());

        IOException caughtException = Assertions.assertThrows(
                IOException.class,
                () -> CommonIO.readOctets(inputStream, buffer, 1, 1)
        );

        Assertions.assertSame(exceptionToThrow, caughtException);
        Assertions.assertArrayEquals(new byte[] {-1, -1, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 1);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @MethodSource("oneToThreeAndByteValuesCombinations")
    void testReadOctetsSucceedsWhenInputStreamReadsNoOctetsOnFirstTryButSucceedsOnNext(int maxOctetsToRead, int byteValue) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = {-1, -1, -1, -1, -1, -1};

        Mockito.doReturn(0).when(inputStream).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doReturn(byteValue).when(inputStream).read();

        int result = CommonIO.readOctets(inputStream, buffer, 2, maxOctetsToRead);

        Assertions.assertEquals(1, result);
        Assertions.assertArrayEquals(new byte[] {-1, -1, (byte) byteValue, -1, -1, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 2, maxOctetsToRead);
        Mockito.verify(inputStream, Mockito.times(1)).read();
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    static Stream<Arguments> oneToThreeAndByteValuesCombinations() {
        return IntStream.rangeClosed(1, 3).boxed()
                .flatMap(i -> IntStream.of(0, 1, 0x7f, 0x80, 0xff)
                        .mapToObj(j -> Arguments.of(i, j))
                );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void testReadOctetsFailsWhenInputStreamReadsNoOctetsOnFirstTryAndThrowsExceptionOnNext(int maxOctetsToRead) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        IOException exceptionToThrow = new IOException("A message");
        byte[] buffer = {-1, -1, -1, -1, -1};

        Mockito.doReturn(0).when(inputStream).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doThrow(exceptionToThrow).when(inputStream).read();

        IOException caughtException = Assertions.assertThrows(
                IOException.class,
                () -> CommonIO.readOctets(inputStream, buffer, 1, maxOctetsToRead)
        );

        Assertions.assertSame(exceptionToThrow, caughtException);
        Assertions.assertArrayEquals(new byte[] {-1, -1, -1, -1, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, maxOctetsToRead);
        Mockito.verify(inputStream, Mockito.times(1)).read();
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -10, -2, -1, 0})
    void testReadOctetsHasNoEffectWhenStreamReturnsZeroAndOctetsToReadIsNotPositive(int maxOctetsToRead) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = {-1, -1, -1};

        Mockito.doReturn(0).when(inputStream).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());

        int result = CommonIO.readOctets(inputStream, buffer, 1, maxOctetsToRead);

        Assertions.assertEquals(0, result);
        Assertions.assertArrayEquals(new byte[] {-1, -1, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, maxOctetsToRead);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void testReadNOctetsSucceedsWhenInputStreamReadsWholeLengthInOneInvocation(int octetsToRead) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = new byte[] {-1, -1, -1, -1, -1, -1};

        doAnswerInputStreamReadIntoByteArray(octetsToRead).when(inputStream)
                .read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());

        CommonIO.readNOctets(inputStream, buffer, 2, octetsToRead);

        Assertions.assertArrayEquals(
                TestBufferUtils.createByteArray(6, (byte) -1, 2, IntStream.rangeClosed(1, octetsToRead)),
                buffer
        );
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 2, octetsToRead);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @Test
    void testReadNOctetsSucceedsWhenInputStreamReadsWholeLengthInMultipleInvocations() throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = new byte[] {-1, -1, -1, -1, -1, -1, -1, -1};

        doAnswerInputStreamReadIntoByteArray(2).when(inputStream).read(buffer, 1, 6);
        doAnswerInputStreamReadIntoByteArray(1).when(inputStream).read(buffer, 3, 4);
        doAnswerInputStreamReadIntoByteArray(3).when(inputStream).read(buffer, 4, 3);
        CommonIO.readNOctets(inputStream, buffer, 1, 6);

        Assertions.assertArrayEquals(new byte[] {-1, 1, 2, 1, 1, 2, 3, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 6);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 3, 4);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 4, 3);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -10, -2, -1})
    void testReadNOctetsFailsWhenInputStreamReturnsNegativeValueOnFirstInvocation(int readLength) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = new byte[] {-1, -1, -1};

        Mockito.doReturn(readLength).when(inputStream).read(buffer, 1, 1);
        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> CommonIO.readNOctets(inputStream, buffer, 1, 1)
        );

        Assertions.assertArrayEquals(new byte[] {-1, -1, -1}, buffer);
        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 1);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -2, -1})
    void testReadNOctetsFailsWhenInputStreamReturnsNegativeValueOnNonFirstInvocation(int readLength) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = new byte[] {-1, -1, -1, -1, -1};

        doAnswerInputStreamReadIntoByteArray(2).when(inputStream).read(buffer, 1, 3);
        Mockito.doReturn(readLength).when(inputStream).read(buffer, 3, 1);
        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> CommonIO.readNOctets(inputStream, buffer, 1, 3)
        );

        Assertions.assertArrayEquals(new byte[] {-1, 1, 2, -1, -1}, buffer);
        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 3);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 3, 1);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @Test
    void testReadNOctetsFailsWhenInputStreamThrowsExceptionOnFirstInvocation() throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        IOException exceptionToThrow = new IOException("A message");
        byte[] buffer = new byte[] {-1, -1, -1};

        Mockito.doThrow(exceptionToThrow).when(inputStream).read(buffer, 1, 1);
        IOException caughtException = Assertions.assertThrows(
                IOException.class,
                () -> CommonIO.readNOctets(inputStream, buffer, 1, 1)
        );

        Assertions.assertSame(exceptionToThrow, caughtException);
        Assertions.assertArrayEquals(new byte[] {-1, -1, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 1);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @Test
    void testReadNOctetsFailsWhenInputStreamThrowsExceptionOnNonFirstInvocation() throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        IOException exceptionToThrow = new IOException("A message");
        byte[] buffer = new byte[] {-1, -1, -1, -1, -1};

        doAnswerInputStreamReadIntoByteArray(2).when(inputStream).read(buffer, 1, 3);
        Mockito.doThrow(exceptionToThrow).when(inputStream).read(buffer, 3, 1);
        IOException caughtException = Assertions.assertThrows(
                IOException.class,
                () -> CommonIO.readNOctets(inputStream, buffer, 1, 3)
        );

        Assertions.assertSame(exceptionToThrow, caughtException);
        Assertions.assertArrayEquals(new byte[] {-1, 1, 2, -1, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 3);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 3, 1);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -10, -2, -1, 0})
    void testReadNOctetsHasNoEffectWhenStreamReturnsZeroAndOctetsToReadIsNotPositive(int octetsToRead) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = {-1, -1, -1};

        Mockito.doReturn(0).when(inputStream).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());

        CommonIO.readNOctets(inputStream, buffer, 1, octetsToRead);

        Assertions.assertArrayEquals(new byte[] {-1, -1, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, octetsToRead);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @Test
    void testUnexpectedEndOfInputException() {
        EOFException result = CommonIO.unexpectedEndOfInputException();
        Assertions.assertEquals("Unexpected end of input", result.getMessage());
    }

    private static Stubber doAnswerInputStreamReadIntoByteArray(final int length) {
        return TestInputStreamUtils.doAnswerInputStreamReadIntoByteArray(() -> IntStream
                .rangeClosed(1, length)
        );
    }

}
