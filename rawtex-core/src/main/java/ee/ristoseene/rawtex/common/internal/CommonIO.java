package ee.ristoseene.rawtex.common.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public final class CommonIO {

    public static byte readOctet(InputStream in) throws IOException {
        final int octetValue = in.read();

        if (octetValue < 0) {
            throw unexpectedEndOfInputException();
        }

        return (byte) octetValue;
    }

    public static EOFException unexpectedEndOfInputException() {
        return new EOFException("Unexpected end of input");
    }

    private CommonIO() {}

}
