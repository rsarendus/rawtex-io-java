package ee.ristoseene.rawtex.io.core.common.octets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmptyOctetSequenceTest {

    @ParameterizedTest
    @ValueSource(ints = {-10, -2, -1, 0, 1, 2, 3, 10})
    void testOctetAtThrowsIndexOutOfBoundsException(int index) {
        OctetSequence octetSequence = new EmptyOctetSequence();

        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> octetSequence.octetAt(index)
        );
    }

    @Test
    void testLengthReturnsZero() {
        OctetSequence octetSequence = new EmptyOctetSequence();

        int result = octetSequence.length();
        Assertions.assertEquals(0, result);
    }

    @Test
    void testToByteArrayReturnsZeroLengthByteArray() {
        OctetSequence octetSequence = new EmptyOctetSequence();

        byte[] result = octetSequence.toByteArray();
        Assertions.assertArrayEquals(new byte[0], result);
    }

}
