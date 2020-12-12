package ee.ristoseene.rawtex.deflate.in;

import ee.ristoseene.rawtex.common.data.TransferBufferAllocator;
import ee.ristoseene.rawtex.common.internal.ArraySource;
import ee.ristoseene.rawtex.common.internal.Endianness;
import ee.ristoseene.rawtex.deflate.test.TestDeflateUtils;
import ee.ristoseene.rawtex.in.RawTexDataLoader;
import ee.ristoseene.rawtex.in.RawTexLoadTarget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.Stream;
import java.util.zip.Inflater;

@ExtendWith(MockitoExtension.class)
public class DeflateBlockDataLoaderFailureTest {

    @Mock
    private TransferBufferAllocator transferBufferAllocator;
    @Mock
    private Inflater inflater;
    @Mock
    private RawTexLoadTarget loadTarget;

    @MethodSource("nonByteBlockSizeAndEndiannessCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}")
    public void testLoadFailsOnDataLengthNotMultipleOfBlockSize(int blockSize, ByteOrder endianness) {
        RawTexDataLoader dataLoader = new DeflateBlockDataLoader(() -> blockSize, Endianness.of(endianness), transferBufferAllocator, inflater, true);
        InputStream in = Mockito.mock(InputStream.class);
        int inputLength = dataLengthToValidDeflatedLength(blockSize);
        int invalidDataLength = blockSize + 1;

        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, invalidDataLength)
        );

        Assertions.assertEquals(
                String.format("Data length (%d) is not a multiple of block size (%d)", invalidDataLength, blockSize),
                caughtException.getMessage()
        );
        Mockito.verifyNoInteractions(transferBufferAllocator, inflater, in, loadTarget);
    }

    private static Stream<Arguments> nonByteBlockSizeAndEndiannessCombinations() {
        return Stream.of(Short.BYTES, Integer.BYTES, Long.BYTES)
                .flatMap(blockSize -> Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                        .map(endianness -> Arguments.of(blockSize, endianness))
                );
    }

    @MethodSource("blockSizeAndEndiannessAndInputTypeCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}, input type: {2}")
    public void testLoadFailsOnMissingTargetBuffer(int blockSize, ByteOrder endianness, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new DeflateBlockDataLoader(() -> blockSize, Endianness.of(endianness), transferBufferAllocator, inflater, true);
        InputStream in = Mockito.mock(inputType);
        int inputLength = dataLengthToValidDeflatedLength(blockSize);
        byte[] readBuffer = new byte[inputLength];

        if (!(in instanceof ArraySource)) {
            Mockito.doReturn(readBuffer).when(transferBufferAllocator).allocate(Mockito.anyInt(), Mockito.anyInt());
            Mockito.doReturn(inputLength).when(in).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
        }
        Mockito.doReturn(null).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        NullPointerException caughtException = Assertions.assertThrows(
                NullPointerException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, blockSize)
        );

        Assertions.assertEquals("Target buffer missing", caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(inputLength);
        } else {
            Mockito.verify(transferBufferAllocator).allocate(1, inputLength);
            Mockito.verify(in).read(readBuffer, 0, inputLength);
            Mockito.verify(transferBufferAllocator).free(readBuffer);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize);
        Mockito.verify(loadTarget).release(null, false);
        Mockito.verifyNoMoreInteractions(transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("blockSizeAndEndiannessAndInputTypeCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}, input type: {2}")
    public void testLoadFailsOnReadOnlyTargetBuffer(int blockSize, ByteOrder endianness, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new DeflateBlockDataLoader(() -> blockSize, Endianness.of(endianness), transferBufferAllocator, inflater, true);
        InputStream in = Mockito.mock(inputType);
        int inputLength = dataLengthToValidDeflatedLength(blockSize);
        byte[] readBuffer = new byte[inputLength];

        if (!(in instanceof ArraySource)) {
            Mockito.doReturn(readBuffer).when(transferBufferAllocator).allocate(Mockito.anyInt(), Mockito.anyInt());
            Mockito.doReturn(inputLength).when(in).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
        }
        ByteBuffer readOnlyTarget = ByteBuffer.allocate(blockSize).order(endianness).asReadOnlyBuffer();
        Mockito.doReturn(readOnlyTarget).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, blockSize)
        );

        Assertions.assertEquals("Target buffer is read-only", caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(inputLength);
        } else {
            Mockito.verify(transferBufferAllocator).allocate(1, inputLength);
            Mockito.verify(in).read(readBuffer, 0, inputLength);
            Mockito.verify(transferBufferAllocator).free(readBuffer);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize);
        Mockito.verify(loadTarget).release(readOnlyTarget, false);
        Mockito.verifyNoMoreInteractions(transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("blockSizeAndEndiannessAndInputTypeCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}, input type: {2}")
    public void testLoadFailsOnInvalidTargetBufferLength(int blockSize, ByteOrder endianness, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new DeflateBlockDataLoader(() -> blockSize, Endianness.of(endianness), transferBufferAllocator, inflater, true);
        InputStream in = Mockito.mock(inputType);
        int inputLength = dataLengthToValidDeflatedLength(blockSize);
        byte[] readBuffer = new byte[inputLength];

        if (!(in instanceof ArraySource)) {
            Mockito.doReturn(readBuffer).when(transferBufferAllocator).allocate(Mockito.anyInt(), Mockito.anyInt());
            Mockito.doReturn(inputLength).when(in).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
        }
        int invalidTargetLength = blockSize - 1;
        ByteBuffer targetBuffer = ByteBuffer.allocate(invalidTargetLength).order(endianness);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, blockSize)
        );

        Assertions.assertEquals("Invalid target buffer length: " + invalidTargetLength, caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(inputLength);
        } else {
            Mockito.verify(transferBufferAllocator).allocate(1, inputLength);
            Mockito.verify(in).read(readBuffer, 0, inputLength);
            Mockito.verify(transferBufferAllocator).free(readBuffer);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize);
        Mockito.verify(loadTarget).release(targetBuffer, false);
        Mockito.verifyNoMoreInteractions(transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("blockSizeAndEndiannessAndInputTypeCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}, input type: {2}")
    public void testLoadFailsOnTooLongTargetBufferLength(int blockSize, ByteOrder endianness, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new DeflateBlockDataLoader(() -> blockSize, Endianness.of(endianness), transferBufferAllocator, inflater, true);
        InputStream in = Mockito.mock(inputType);
        int inputLength = dataLengthToValidDeflatedLength(blockSize);
        byte[] readBuffer = new byte[inputLength];

        if (!(in instanceof ArraySource)) {
            Mockito.doReturn(readBuffer).when(transferBufferAllocator).allocate(Mockito.anyInt(), Mockito.anyInt());
            Mockito.doReturn(inputLength).when(in).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
        }
        int tooLongTargetLength = blockSize * 2;
        ByteBuffer targetBuffer = ByteBuffer.allocate(tooLongTargetLength).order(endianness);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, blockSize)
        );

        Assertions.assertEquals("Invalid target buffer length: " + tooLongTargetLength, caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(inputLength);
        } else {
            Mockito.verify(transferBufferAllocator).allocate(1, inputLength);
            Mockito.verify(in).read(readBuffer, 0, inputLength);
            Mockito.verify(transferBufferAllocator).free(readBuffer);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize);
        Mockito.verify(loadTarget).release(targetBuffer, false);
        Mockito.verifyNoMoreInteractions(transferBufferAllocator, in, loadTarget);
    }

    private static Stream<Arguments> blockSizeAndEndiannessAndInputTypeCombinations() {
        return Stream.of(Byte.BYTES, Short.BYTES, Integer.BYTES, Long.BYTES)
                .flatMap(blockSize -> Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                        .flatMap(endianness -> Stream.of(ArraySource.class, InputStream.class)
                                .map(inputType -> Arguments.of(blockSize, endianness, inputType))
                        )
                );
    }

    private static int dataLengthToValidDeflatedLength(int dataLength) {
        return TestDeflateUtils.deflate(new byte[dataLength]).length;
    }

}
