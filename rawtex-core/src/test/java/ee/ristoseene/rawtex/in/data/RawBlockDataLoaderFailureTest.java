package ee.ristoseene.rawtex.in.data;

import ee.ristoseene.rawtex.common.internal.ArraySource;
import ee.ristoseene.rawtex.common.internal.Endianness;
import ee.ristoseene.rawtex.in.RawTexDataLoader;
import ee.ristoseene.rawtex.in.RawTexLoadTarget;
import ee.ristoseene.rawtex.in.test.DirectBufferFactory;
import ee.ristoseene.rawtex.in.test.NonDirectBufferFactory;
import ee.ristoseene.rawtex.in.test.TargetBufferFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.Stream;

public class RawBlockDataLoaderFailureTest {

    @MethodSource("blockSizeAndEndiannessCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}")
    public void testInvalidMaxTransferBufferSize(int blockSize, ByteOrder endianness) {
        int invalidMaxTransferBufferSize = blockSize - 1;

        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new RawBlockDataLoader(() -> blockSize, Endianness.of(endianness), invalidMaxTransferBufferSize)
        );

        Assertions.assertEquals("Invalid maximum transfer buffer size: " + invalidMaxTransferBufferSize, caughtException.getMessage());
    }

    @MethodSource("blockSizeAndEndiannessCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}")
    public void testInvalidPreAllocatedTransferBuffer(int blockSize, ByteOrder endianness) {
        int invalidTransferBufferSize = blockSize - 1;
        byte[] invalidTransferBuffer = new byte[invalidTransferBufferSize];

        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new RawBlockDataLoader(() -> blockSize, Endianness.of(endianness), invalidTransferBuffer)
        );

        Assertions.assertEquals("Invalid pre-allocated transfer buffer size: " + invalidTransferBufferSize, caughtException.getMessage());
    }

    @MethodSource("blockSizeAndEndiannessCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}")
    public void testLoadFailsOnInputLengthAndDataLengthMismatch(int blockSize, ByteOrder endianness) {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(() -> blockSize, Endianness.of(endianness));
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = Mockito.mock(InputStream.class);
        int invalidInputLength = blockSize + 1;

        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> dataLoader.load(in, invalidInputLength, loadTarget, blockSize)
        );

        Assertions.assertEquals(
                String.format("Input length (%d) does not match data length (%d)", invalidInputLength, blockSize),
                caughtException.getMessage()
        );
        Mockito.verifyNoInteractions(loadTarget, in);
    }

    private static Stream<Arguments> blockSizeAndEndiannessCombinations() {
        return Stream.of(Byte.BYTES, Short.BYTES, Integer.BYTES, Long.BYTES)
                .flatMap(blockSize -> Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                        .map(endianness -> Arguments.of(blockSize, endianness))
                );
    }

    @MethodSource("nonByteBlockSizeAndEndiannessCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}")
    public void testLoadFailsOnDataLengthNotMultipleOfBlockSize(int blockSize, ByteOrder endianness) {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(() -> blockSize, Endianness.of(endianness));
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = Mockito.mock(InputStream.class);
        int invalidDataLength = blockSize + 1;

        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> dataLoader.load(in, invalidDataLength, loadTarget, invalidDataLength)
        );

        Assertions.assertEquals(
                String.format("Data length (%d) is not a multiple of block size (%d)", invalidDataLength, blockSize),
                caughtException.getMessage()
        );
        Mockito.verifyNoInteractions(loadTarget, in);
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
        RawTexDataLoader dataLoader = new RawBlockDataLoader(() -> blockSize, Endianness.of(endianness));
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = Mockito.mock(inputType);

        Mockito.doReturn(null).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        NullPointerException caughtException = Assertions.assertThrows(
                NullPointerException.class,
                () -> dataLoader.load(in, blockSize, loadTarget, blockSize)
        );

        Assertions.assertEquals("Target buffer missing", caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(blockSize);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize);
        Mockito.verify(loadTarget).release(null, false);
        Mockito.verifyNoMoreInteractions(in, loadTarget);
    }

    @MethodSource("blockSizeAndEndiannessAndInputTypeCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}, input type: {2}")
    public void testLoadFailsOnReadOnlyTargetBuffer(int blockSize, ByteOrder endianness, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(() -> blockSize, Endianness.of(endianness));
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = Mockito.mock(inputType);

        ByteBuffer readOnlyTarget = ByteBuffer.allocate(blockSize).order(endianness).asReadOnlyBuffer();
        Mockito.doReturn(readOnlyTarget).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, blockSize, loadTarget, blockSize)
        );

        Assertions.assertEquals("Target buffer is read-only", caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(blockSize);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize);
        Mockito.verify(loadTarget).release(readOnlyTarget, false);
        Mockito.verifyNoMoreInteractions(in, loadTarget);
    }

    @MethodSource("blockSizeAndEndiannessAndInputTypeCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}, input type: {2}")
    public void testLoadFailsOnInvalidTargetBufferLength(int blockSize, ByteOrder endianness, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(() -> blockSize, Endianness.of(endianness));
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = Mockito.mock(inputType);

        int invalidTargetLength = blockSize - 1;
        ByteBuffer targetBuffer = ByteBuffer.allocate(invalidTargetLength).order(endianness);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, blockSize, loadTarget, blockSize)
        );

        Assertions.assertEquals("Invalid target buffer length: " + invalidTargetLength, caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(blockSize);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize);
        Mockito.verify(loadTarget).release(targetBuffer, false);
        Mockito.verifyNoMoreInteractions(in, loadTarget);
    }

    @MethodSource("blockSizeAndEndiannessAndInputTypeCombinations")
    @ParameterizedTest(name = "block size: {0}, endianness: {1}, input type: {2}")
    public void testLoadFailsOnTooLongTargetBufferLength(int blockSize, ByteOrder endianness, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(() -> blockSize, Endianness.of(endianness));
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = Mockito.mock(inputType);

        int tooLongTargetLength = blockSize * 2;
        ByteBuffer targetBuffer = ByteBuffer.allocate(tooLongTargetLength).order(endianness);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, blockSize, loadTarget, blockSize)
        );

        Assertions.assertEquals("Invalid target buffer length: " + tooLongTargetLength, caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(blockSize);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize);
        Mockito.verify(loadTarget).release(targetBuffer, false);
        Mockito.verifyNoMoreInteractions(in, loadTarget);
    }

    private static Stream<Arguments> blockSizeAndEndiannessAndInputTypeCombinations() {
        return Stream.of(Byte.BYTES, Short.BYTES, Integer.BYTES, Long.BYTES)
                .flatMap(blockSize -> Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                        .flatMap(endianness -> Stream.of(ArraySource.class, InputStream.class)
                                .map(inputType -> Arguments.of(blockSize, endianness, inputType))
                        )
                );
    }

    @MethodSource("blockSizeAndEndiannessCombinations")
    @ParameterizedTest(name = "block size: {0}, in: [{1}]")
    public void testLoadFromArrayFailsWithEOF(int blockSize, ByteOrder inEndianness) {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(() -> blockSize, Endianness.of(inEndianness));
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = new ArraySource(new byte[blockSize - 1]);

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> dataLoader.load(in, blockSize, loadTarget, blockSize)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verifyNoInteractions(loadTarget);
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "block size: {0}, in: [{1}], out: [{2}, {3}]")
    public void testLoadStreamFailsWithEOF(int blockSize, ByteOrder inEndianness, ByteOrder outEndianness, TargetBufferFactory outFactory) {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(() -> blockSize, Endianness.of(inEndianness));
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = new ByteArrayInputStream(new byte[blockSize - 1]);

        ByteBuffer targetBuffer = outFactory.createFor(0, blockSize).order(outEndianness);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> dataLoader.load(in, blockSize, loadTarget, blockSize)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verify(loadTarget).acquire(0, blockSize);
        Mockito.verify(loadTarget).release(targetBuffer, false);
        Mockito.verifyNoMoreInteractions(loadTarget);
    }

    private static Stream<Arguments> parameterCombinations() {
        return Stream.of(Byte.BYTES, Short.BYTES, Integer.BYTES, Long.BYTES)
                .flatMap(blockSize -> Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                        .flatMap(inEndianness -> Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                                .flatMap(outEndianness -> Stream.of(new DirectBufferFactory(), new NonDirectBufferFactory())
                                        .map(outFactory -> Arguments.of(blockSize, inEndianness, outEndianness, outFactory))
                                )
                        )
                );
    }

}
