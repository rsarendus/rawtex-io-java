package ee.ristoseene.rawtex.io.core.common.octets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

class OctetSequenceTest {

    @Test
    void testEmpty() {
        Assertions.assertNotNull(OctetSequence.EMPTY);
        Assertions.assertEquals(0, OctetSequence.EMPTY.length());
        Assertions.assertArrayEquals(new byte[0], OctetSequence.EMPTY.toByteArray());
    }

    @Test
    void testToByteArray() {
        OctetSequence octetSequence = Mockito.mock(OctetSequence.class);
        Mockito.doReturn(3).when(octetSequence).length();
        Mockito.doReturn((byte) 5).when(octetSequence).octetAt(0);
        Mockito.doReturn((byte) 3).when(octetSequence).octetAt(1);
        Mockito.doReturn((byte) 7).when(octetSequence).octetAt(2);
        Mockito.doCallRealMethod().when(octetSequence).toByteArray();

        byte[] result = octetSequence.toByteArray();

        Assertions.assertArrayEquals(new byte[] {5, 3, 7}, result);
        Mockito.verify(octetSequence).length();
        Mockito.verify(octetSequence).octetAt(0);
        Mockito.verify(octetSequence).octetAt(1);
        Mockito.verify(octetSequence).octetAt(2);
        Mockito.verify(octetSequence).toByteArray();
        Mockito.verifyNoMoreInteractions(octetSequence);
    }

    @Test
    void testEqualsWithBothArgumentsNull() {
        boolean result = OctetSequence.equals(null, null);
        Assertions.assertTrue(result);
    }

    @Test
    void testEqualsWithBothArgumentsSame() {
        OctetSequence octetSequence = Mockito.mock(OctetSequence.class);

        boolean result = OctetSequence.equals(octetSequence, octetSequence);

        Assertions.assertTrue(result);
        Mockito.verifyNoInteractions(octetSequence);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void testEqualsWithOneArgumentNull(int nullArgument) {
        OctetSequence octetSequence = Mockito.mock(OctetSequence.class);
        OctetSequence octetSequence1 = (nullArgument == 1) ? null : octetSequence;
        OctetSequence octetSequence2 = (nullArgument == 1) ? octetSequence : null;

        boolean result = OctetSequence.equals(octetSequence1, octetSequence2);

        Assertions.assertFalse(result);
        Mockito.verifyNoInteractions(octetSequence);
    }

    @Test
    void testEqualsWithDifferingLengths() {
        OctetSequence octetSequence1 = Mockito.mock(OctetSequence.class);
        Mockito.doReturn(1).when(octetSequence1).length();
        OctetSequence octetSequence2 = Mockito.mock(OctetSequence.class);
        Mockito.doReturn(2).when(octetSequence2).length();

        boolean result = OctetSequence.equals(octetSequence1, octetSequence2);

        Assertions.assertFalse(result);
        Mockito.verify(octetSequence1).length();
        Mockito.verify(octetSequence2).length();
        Mockito.verifyNoMoreInteractions(octetSequence1, octetSequence2);
    }

    @Test
    void testEqualsWithDifferingContents() {
        OctetSequence octetSequence1 = Mockito.mock(OctetSequence.class);
        Mockito.doReturn(3).when(octetSequence1).length();
        Mockito.doReturn((byte) 23).when(octetSequence1).octetAt(0);
        Mockito.doReturn((byte) 45).when(octetSequence1).octetAt(1);

        OctetSequence octetSequence2 = Mockito.mock(OctetSequence.class);
        Mockito.doReturn(3).when(octetSequence2).length();
        Mockito.doReturn((byte) 23).when(octetSequence2).octetAt(0);
        Mockito.doReturn((byte) 11).when(octetSequence2).octetAt(1);

        boolean result = OctetSequence.equals(octetSequence1, octetSequence2);

        Assertions.assertFalse(result);
        Mockito.verify(octetSequence1).length();
        Mockito.verify(octetSequence2).length();
        Mockito.verify(octetSequence1).octetAt(0);
        Mockito.verify(octetSequence2).octetAt(0);
        Mockito.verify(octetSequence1).octetAt(1);
        Mockito.verify(octetSequence2).octetAt(1);
        Mockito.verifyNoMoreInteractions(octetSequence1, octetSequence2);
    }

    @Test
    void testEqualsWithEqualContents() {
        OctetSequence octetSequence1 = Mockito.mock(OctetSequence.class);
        Mockito.doReturn(3).when(octetSequence1).length();
        Mockito.doReturn((byte) 171).when(octetSequence1).octetAt(0);
        Mockito.doReturn((byte) 22).when(octetSequence1).octetAt(1);
        Mockito.doReturn((byte) 65).when(octetSequence1).octetAt(2);

        OctetSequence octetSequence2 = Mockito.mock(OctetSequence.class);
        Mockito.doReturn(3).when(octetSequence2).length();
        Mockito.doReturn((byte) 171).when(octetSequence2).octetAt(0);
        Mockito.doReturn((byte) 22).when(octetSequence2).octetAt(1);
        Mockito.doReturn((byte) 65).when(octetSequence2).octetAt(2);

        boolean result = OctetSequence.equals(octetSequence1, octetSequence2);

        Assertions.assertTrue(result);
        Mockito.verify(octetSequence1).length();
        Mockito.verify(octetSequence2).length();
        Mockito.verify(octetSequence1).octetAt(0);
        Mockito.verify(octetSequence2).octetAt(0);
        Mockito.verify(octetSequence1).octetAt(1);
        Mockito.verify(octetSequence2).octetAt(1);
        Mockito.verify(octetSequence1).octetAt(2);
        Mockito.verify(octetSequence2).octetAt(2);
        Mockito.verifyNoMoreInteractions(octetSequence1, octetSequence2);
    }

}
