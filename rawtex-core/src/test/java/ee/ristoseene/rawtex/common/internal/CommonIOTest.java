package ee.ristoseene.rawtex.common.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    public void testReadOctetFails() throws IOException {
        InputStream inputStream = Mockito.mock(InputStream.class);
        Mockito.doReturn(-1).when(inputStream).read();

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> CommonIO.readOctet(inputStream)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verify(inputStream, Mockito.times(1)).read();
        Mockito.verifyNoMoreInteractions(inputStream);
    }

    @Test
    public void testUnexpectedEndOfInputException() {
        EOFException result = CommonIO.unexpectedEndOfInputException();
        Assertions.assertEquals("Unexpected end of input", result.getMessage());
    }

}
