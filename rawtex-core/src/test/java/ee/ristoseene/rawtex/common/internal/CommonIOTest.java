package ee.ristoseene.rawtex.common.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class CommonIOTest {

    @ParameterizedTest
    @ValueSource(bytes = {0, 1, 0x7f, (byte) 0x80, (byte) 0xff})
    public void testReadOctetSucceeds(int byteValue) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        Mockito.doReturn(byteValue & 0xff).when(inputStream).read();

        byte result = CommonIO.readOctet(inputStream);

        Assertions.assertEquals(byteValue, result);
        Mockito.verify(inputStream, Mockito.times(1)).read();
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -2, -1})
    public void testReadOctetFailsWhenInputStreamReturnsNegativeValue(int returnValue) throws IOException {
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
    public void testReadOctetFailsWhenInputStreamThrowsException() throws IOException {
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
    @ValueSource(ints = {-10, -2, -1, 0})
    public void testReadOctetsSucceedsForNonPositiveLengthToRead(int lengthToRead) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);

        CommonIO.readOctets(inputStream, new byte[0], 0, lengthToRead);

        Mockito.verifyNoInteractions(inputStream);
    }

    @Test
    public void testReadOctetsSucceedsWhenInputStreamReadsWholeLengthInOneInvocation() throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = new byte[] {-1, -1, -1, -1, -1};

        Mockito.doAnswer(invocationOnInputStreamReadIntoArray(3)).when(inputStream).read(buffer, 1, 3);
        CommonIO.readOctets(inputStream, buffer, 1, 3);

        Assertions.assertArrayEquals(new byte[] {-1, 1, 2, 3, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 3);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @Test
    public void testReadOctetsSucceedsWhenInputStreamReadsWholeLengthInMultipleInvocations() throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = new byte[] {-1, -1, -1, -1, -1, -1, -1, -1};

        Mockito.doAnswer(invocationOnInputStreamReadIntoArray(2)).when(inputStream).read(buffer, 1, 6);
        Mockito.doAnswer(invocationOnInputStreamReadIntoArray(1)).when(inputStream).read(buffer, 3, 4);
        Mockito.doAnswer(invocationOnInputStreamReadIntoArray(3)).when(inputStream).read(buffer, 4, 3);
        CommonIO.readOctets(inputStream, buffer, 1, 6);

        Assertions.assertArrayEquals(new byte[] {-1, 1, 2, 1, 1, 2, 3, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 6);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 3, 4);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 4, 3);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -2, -1})
    public void testReadOctetsFailsWhenInputStreamReturnsNegativeValueOnFirstInvocation(int readLength) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = new byte[] {-1, -1, -1};

        Mockito.doAnswer(invocationOnInputStreamReadIntoArray(readLength)).when(inputStream).read(buffer, 1, 1);
        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> CommonIO.readOctets(inputStream, buffer, 1, 1)
        );

        Assertions.assertArrayEquals(new byte[] {-1, -1, -1}, buffer);
        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 1);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -2, -1})
    public void testReadOctetsFailsWhenInputStreamReturnsNegativeValueOnNonFirstInvocation(int readLength) throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        byte[] buffer = new byte[] {-1, -1, -1, -1, -1};

        Mockito.doAnswer(invocationOnInputStreamReadIntoArray(2)).when(inputStream).read(buffer, 1, 3);
        Mockito.doAnswer(invocationOnInputStreamReadIntoArray(readLength)).when(inputStream).read(buffer, 3, 1);
        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> CommonIO.readOctets(inputStream, buffer, 1, 3)
        );

        Assertions.assertArrayEquals(new byte[] {-1, 1, 2, -1, -1}, buffer);
        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 3);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 3, 1);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @Test
    public void testReadOctetsFailsWhenInputStreamThrowsExceptionOnFirstInvocation() throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        IOException exceptionToThrow = new IOException("A message");
        byte[] buffer = new byte[] {-1, -1, -1};

        Mockito.doThrow(exceptionToThrow).when(inputStream).read(buffer, 1, 1);
        IOException caughtException = Assertions.assertThrows(
                IOException.class,
                () -> CommonIO.readOctets(inputStream, buffer, 1, 1)
        );

        Assertions.assertSame(exceptionToThrow, caughtException);
        Assertions.assertArrayEquals(new byte[] {-1, -1, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 1);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @Test
    public void testReadOctetsFailsWhenInputStreamThrowsExceptionOnNonFirstInvocation() throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        IOException exceptionToThrow = new IOException("A message");
        byte[] buffer = new byte[] {-1, -1, -1, -1, -1};

        Mockito.doAnswer(invocationOnInputStreamReadIntoArray(2)).when(inputStream).read(buffer, 1, 3);
        Mockito.doThrow(exceptionToThrow).when(inputStream).read(buffer, 3, 1);
        IOException caughtException = Assertions.assertThrows(
                IOException.class,
                () -> CommonIO.readOctets(inputStream, buffer, 1, 3)
        );

        Assertions.assertSame(exceptionToThrow, caughtException);
        Assertions.assertArrayEquals(new byte[] {-1, 1, 2, -1, -1}, buffer);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 1, 3);
        Mockito.verify(inputStream, Mockito.times(1)).read(buffer, 3, 1);
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @Test
    public void testUnexpectedEndOfInputException() {
        EOFException result = CommonIO.unexpectedEndOfInputException();
        Assertions.assertEquals("Unexpected end of input", result.getMessage());
    }

    private static Answer<Integer> invocationOnInputStreamReadIntoArray(final int length) {
        return invocationOnMock -> {
            final byte[] array = invocationOnMock.getArgument(0, byte[].class);
            final int offset = invocationOnMock.getArgument(1, Integer.class);

            for (int i = 0; i < length; ++i) {
                array[offset + i] = (byte) (1 + i);
            }

            return length;
        };
    }

}
