package ee.ristoseene.rawtex.io.deflate.in.data;

import ee.ristoseene.rawtex.io.core.common.data.TransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.common.format.BlockSize;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;
import ee.ristoseene.rawtex.io.core.in.RawTexDataLoader;
import ee.ristoseene.rawtex.io.core.in.RawTexLoadTarget;
import ee.ristoseene.rawtex.io.core.in.internal.ArraySource;
import ee.ristoseene.rawtex.io.deflate.common.test.TestDeflateUtils;
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
import java.util.stream.Stream;
import java.util.zip.Inflater;

@ExtendWith(MockitoExtension.class)
class InflatingBlockDataLoaderFailureTest {

    @Mock
    private TransferBufferAllocator transferBufferAllocator;
    @Mock
    private InflaterAllocator inflaterAllocator;
    @Mock
    private RawTexLoadTarget loadTarget;

    @MethodSource("endiannessAndNonByteBlockSizeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}")
    void testLoadFailsOnDataLengthNotMultipleOfBlockSize(Endianness endianness, BlockSize blockSize) {
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, blockSize, inflaterAllocator, transferBufferAllocator);
        InputStream in = Mockito.mock(InputStream.class);
        int inputLength = dataLengthToValidDeflatedLength(blockSize.octets);
        int invalidDataLength = blockSize.octets + 1;

        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, invalidDataLength)
        );

        Assertions.assertEquals(
                String.format("Data length (%d) is not a valid multiple of block size (%d)", invalidDataLength, blockSize.octets),
                caughtException.getMessage()
        );
        Mockito.verifyNoInteractions(inflaterAllocator, transferBufferAllocator, in, loadTarget);
    }

    static Stream<Arguments> endiannessAndNonByteBlockSizeCombinations() {
        return Stream.of(Endianness.values())
                .flatMap(endianness -> Stream.of(BlockSize.values())
                        .filter(blockSize -> blockSize.octets != Byte.BYTES)
                        .map(blockSize -> Arguments.of(endianness, blockSize))
                );
    }

    @MethodSource("endiannessAndNonByteBlockSizeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}")
    void testLoadFailsOnTooShortInputLength(Endianness endianness, BlockSize blockSize) {
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, blockSize, inflaterAllocator, transferBufferAllocator);
        InputStream in = Mockito.mock(InputStream.class);
        int invalidInputLength = 0;

        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> dataLoader.load(in, invalidInputLength, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals(
                String.format("Invalid input length: %d", invalidInputLength),
                caughtException.getMessage()
        );
        Mockito.verifyNoInteractions(inflaterAllocator, transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndInputTypeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, input type: {2}")
    void testLoadFailsOnMissingInflater(Endianness endianness, BlockSize blockSize, Class<? extends InputStream> inputType) {
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, blockSize, inflaterAllocator, transferBufferAllocator);
        InputStream in = Mockito.mock(inputType);
        int inputLength = dataLengthToValidDeflatedLength(blockSize.octets);

        Mockito.doReturn(null).when(inflaterAllocator).allocate();

        NullPointerException caughtException = Assertions.assertThrows(
                NullPointerException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Inflater is missing", caughtException.getMessage());
        Mockito.verify(inflaterAllocator).allocate();
        Mockito.verifyNoMoreInteractions(inflaterAllocator, transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndInputTypeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, input type: {2}")
    void testLoadFailsOnInflaterNeedsDictionary(Endianness endianness, BlockSize blockSize, Class<? extends InputStream> inputType) {
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, blockSize, inflaterAllocator, transferBufferAllocator);
        InputStream in = Mockito.mock(inputType);
        int inputLength = dataLengthToValidDeflatedLength(blockSize.octets);
        Inflater inflater = Mockito.mock(Inflater.class);

        Mockito.doReturn(inflater).when(inflaterAllocator).allocate();
        Mockito.doReturn(true).when(inflater).needsDictionary();

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Inflater is in an unexpected state", caughtException.getMessage());
        Mockito.verify(inflaterAllocator).allocate();
        Mockito.verify(inflater).needsDictionary();
        Mockito.verify(inflaterAllocator).free(inflater);
        Mockito.verifyNoMoreInteractions(inflaterAllocator, inflater, transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndInputTypeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, input type: {2}")
    void testLoadFailsOnInflaterDoesNotNeedInput(Endianness endianness, BlockSize blockSize, Class<? extends InputStream> inputType) {
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, blockSize, inflaterAllocator, transferBufferAllocator);
        InputStream in = Mockito.mock(inputType);
        int inputLength = dataLengthToValidDeflatedLength(blockSize.octets);
        Inflater inflater = Mockito.mock(Inflater.class);

        Mockito.doReturn(inflater).when(inflaterAllocator).allocate();
        Mockito.doReturn(false).when(inflater).needsDictionary();
        Mockito.doReturn(false).when(inflater).needsInput();

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Inflater is in an unexpected state", caughtException.getMessage());
        Mockito.verify(inflaterAllocator).allocate();
        Mockito.verify(inflater).needsDictionary();
        Mockito.verify(inflater).needsInput();
        Mockito.verify(inflaterAllocator).free(inflater);
        Mockito.verifyNoMoreInteractions(inflaterAllocator, inflater, transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndInputTypeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, input type: {2}")
    void testLoadFailsOnMissingTargetBuffer(Endianness endianness, BlockSize blockSize, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, blockSize, inflaterAllocator, transferBufferAllocator);
        InputStream in = Mockito.mock(inputType);
        int inputLength = dataLengthToValidDeflatedLength(blockSize.octets);
        Inflater inflater = Mockito.mock(Inflater.class);
        byte[] readBuffer = new byte[inputLength];

        Mockito.doReturn(inflater).when(inflaterAllocator).allocate();
        Mockito.doReturn(true).when(inflater).needsInput();
        if (!(in instanceof ArraySource)) {
            Mockito.doReturn(readBuffer).when(transferBufferAllocator).allocate(Mockito.anyInt(), Mockito.anyInt());
            Mockito.doReturn(inputLength).when(in).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
        }
        Mockito.doReturn(null).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        NullPointerException caughtException = Assertions.assertThrows(
                NullPointerException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Target buffer is missing", caughtException.getMessage());
        Mockito.verify(inflaterAllocator).allocate();
        Mockito.verify(inflater).needsDictionary();
        Mockito.verify(inflater).needsInput();
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(inputLength);
            Mockito.verify(inflater).setInput(null, 0, inputLength);
        } else {
            Mockito.verify(transferBufferAllocator).allocate(1, inputLength);
            Mockito.verify(in).read(readBuffer, 0, inputLength);
            Mockito.verify(inflater).setInput(readBuffer, 0, inputLength);
            Mockito.verify(transferBufferAllocator).free(readBuffer);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize.octets);
        Mockito.verify(inflater).reset();
        Mockito.verify(inflaterAllocator).free(inflater);
        Mockito.verifyNoMoreInteractions(inflaterAllocator, inflater, transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndInputTypeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, input type: {2}")
    void testLoadFailsOnReadOnlyTargetBuffer(Endianness endianness, BlockSize blockSize, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, blockSize, inflaterAllocator, transferBufferAllocator);
        InputStream in = Mockito.mock(inputType);
        int inputLength = dataLengthToValidDeflatedLength(blockSize.octets);
        Inflater inflater = Mockito.mock(Inflater.class);
        byte[] readBuffer = new byte[inputLength];

        Mockito.doReturn(inflater).when(inflaterAllocator).allocate();
        Mockito.doReturn(true).when(inflater).needsInput();
        if (!(in instanceof ArraySource)) {
            Mockito.doReturn(readBuffer).when(transferBufferAllocator).allocate(Mockito.anyInt(), Mockito.anyInt());
            Mockito.doReturn(inputLength).when(in).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
        }
        ByteBuffer readOnlyTarget = ByteBuffer.allocate(blockSize.octets).order(endianness.byteOrder).asReadOnlyBuffer();
        Mockito.doReturn(readOnlyTarget).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Target buffer is read-only", caughtException.getMessage());
        Mockito.verify(inflaterAllocator).allocate();
        Mockito.verify(inflater).needsDictionary();
        Mockito.verify(inflater).needsInput();
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(inputLength);
            Mockito.verify(inflater).setInput(null, 0, inputLength);
        } else {
            Mockito.verify(transferBufferAllocator).allocate(1, inputLength);
            Mockito.verify(in).read(readBuffer, 0, inputLength);
            Mockito.verify(inflater).setInput(readBuffer, 0, inputLength);
            Mockito.verify(transferBufferAllocator).free(readBuffer);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize.octets);
        Mockito.verify(loadTarget).release(readOnlyTarget, false);
        Mockito.verify(inflater).reset();
        Mockito.verify(inflaterAllocator).free(inflater);
        Mockito.verifyNoMoreInteractions(inflaterAllocator, inflater, transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndInputTypeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, input type: {2}")
    void testLoadFailsOnInvalidTargetBufferLength(Endianness endianness, BlockSize blockSize, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, blockSize, inflaterAllocator, transferBufferAllocator);
        InputStream in = Mockito.mock(inputType);
        int inputLength = dataLengthToValidDeflatedLength(blockSize.octets);
        Inflater inflater = Mockito.mock(Inflater.class);
        byte[] readBuffer = new byte[inputLength];

        Mockito.doReturn(inflater).when(inflaterAllocator).allocate();
        Mockito.doReturn(true).when(inflater).needsInput();
        if (!(in instanceof ArraySource)) {
            Mockito.doReturn(readBuffer).when(transferBufferAllocator).allocate(Mockito.anyInt(), Mockito.anyInt());
            Mockito.doReturn(inputLength).when(in).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
        }
        int invalidTargetLength = blockSize.octets - 1;
        ByteBuffer targetBuffer = ByteBuffer.allocate(invalidTargetLength).order(endianness.byteOrder);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Invalid target buffer length: " + invalidTargetLength, caughtException.getMessage());
        Mockito.verify(inflaterAllocator).allocate();
        Mockito.verify(inflater).needsDictionary();
        Mockito.verify(inflater).needsInput();
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(inputLength);
            Mockito.verify(inflater).setInput(null, 0, inputLength);
        } else {
            Mockito.verify(transferBufferAllocator).allocate(1, inputLength);
            Mockito.verify(in).read(readBuffer, 0, inputLength);
            Mockito.verify(inflater).setInput(readBuffer, 0, inputLength);
            Mockito.verify(transferBufferAllocator).free(readBuffer);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize.octets);
        Mockito.verify(loadTarget).release(targetBuffer, false);
        Mockito.verify(inflater).reset();
        Mockito.verify(inflaterAllocator).free(inflater);
        Mockito.verifyNoMoreInteractions(inflaterAllocator, inflater, transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndInputTypeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, input type: {2}")
    void testLoadFailsOnTooLongTargetBufferLength(Endianness endianness, BlockSize blockSize, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, blockSize, inflaterAllocator, transferBufferAllocator);
        InputStream in = Mockito.mock(inputType);
        int inputLength = dataLengthToValidDeflatedLength(blockSize.octets);
        Inflater inflater = Mockito.mock(Inflater.class);
        byte[] readBuffer = new byte[inputLength];

        Mockito.doReturn(inflater).when(inflaterAllocator).allocate();
        Mockito.doReturn(true).when(inflater).needsInput();
        if (!(in instanceof ArraySource)) {
            Mockito.doReturn(readBuffer).when(transferBufferAllocator).allocate(Mockito.anyInt(), Mockito.anyInt());
            Mockito.doReturn(inputLength).when(in).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
        }
        int tooLongTargetLength = blockSize.octets * 2;
        ByteBuffer targetBuffer = ByteBuffer.allocate(tooLongTargetLength).order(endianness.byteOrder);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, inputLength, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Invalid target buffer length: " + tooLongTargetLength, caughtException.getMessage());
        Mockito.verify(inflaterAllocator).allocate();
        Mockito.verify(inflater).needsDictionary();
        Mockito.verify(inflater).needsInput();
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(inputLength);
            Mockito.verify(inflater).setInput(null, 0, inputLength);
        } else {
            Mockito.verify(transferBufferAllocator).allocate(1, inputLength);
            Mockito.verify(in).read(readBuffer, 0, inputLength);
            Mockito.verify(inflater).setInput(readBuffer, 0, inputLength);
            Mockito.verify(transferBufferAllocator).free(readBuffer);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize.octets);
        Mockito.verify(loadTarget).release(targetBuffer, false);
        Mockito.verify(inflater).reset();
        Mockito.verify(inflaterAllocator).free(inflater);
        Mockito.verifyNoMoreInteractions(inflaterAllocator, inflater, transferBufferAllocator, in, loadTarget);
    }

    static Stream<Arguments> endiannessAndBlockSizeAndInputTypeCombinations() {
        return Stream.of(Endianness.values())
                .flatMap(endianness -> Stream.of(BlockSize.values())
                        .flatMap(blockSize -> Stream.of(ArraySource.class, InputStream.class)
                                .map(inputType -> Arguments.of(endianness, blockSize, inputType))
                        )
                );
    }

    static int dataLengthToValidDeflatedLength(int dataLength) {
        return TestDeflateUtils.deflate(true, new byte[dataLength]).length;
    }

}
