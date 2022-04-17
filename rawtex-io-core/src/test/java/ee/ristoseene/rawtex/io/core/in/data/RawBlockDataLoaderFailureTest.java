package ee.ristoseene.rawtex.io.core.in.data;

import ee.ristoseene.rawtex.io.core.common.data.TransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.common.format.BlockSize;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;
import ee.ristoseene.rawtex.io.core.in.RawTexDataLoader;
import ee.ristoseene.rawtex.io.core.in.RawTexLoadTarget;
import ee.ristoseene.rawtex.io.core.in.internal.ArraySource;
import ee.ristoseene.rawtex.io.core.in.test.DirectBufferFactory;
import ee.ristoseene.rawtex.io.core.in.test.NonDirectBufferFactory;
import ee.ristoseene.rawtex.io.core.in.test.TargetBufferFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class RawBlockDataLoaderFailureTest {

    @Mock
    private TransferBufferAllocator transferBufferAllocator;
    @Mock
    private RawTexLoadTarget loadTarget;

    @MethodSource("endiannessAndBlockSizeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}")
    void testLoadFailsOnInputLengthAndDataLengthMismatch(Endianness endianness, BlockSize blockSize) {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(endianness, blockSize, transferBufferAllocator);
        InputStream in = Mockito.mock(InputStream.class);
        int invalidInputLength = blockSize.octets + 1;

        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> dataLoader.load(in, invalidInputLength, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals(
                String.format("Input length (%d) does not match data length (%d)", invalidInputLength, blockSize.octets),
                caughtException.getMessage()
        );
        Mockito.verifyNoInteractions(transferBufferAllocator, in, loadTarget);
    }

    static Stream<Arguments> endiannessAndBlockSizeCombinations() {
        return Stream.of(Endianness.values())
                .flatMap(endianness -> Stream.of(BlockSize.values())
                        .map(blockSize -> Arguments.of(endianness, blockSize))
                );
    }

    @MethodSource("endiannessAndNonByteBlockSizeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}")
    void testLoadFailsOnDataLengthNotMultipleOfBlockSize(Endianness endianness, BlockSize blockSize) {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(endianness, blockSize, transferBufferAllocator);
        InputStream in = Mockito.mock(InputStream.class);
        int invalidDataLength = blockSize.octets + 1;

        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> dataLoader.load(in, invalidDataLength, loadTarget, invalidDataLength)
        );

        Assertions.assertEquals(
                String.format("Data length (%d) is not a valid multiple of block size (%d)", invalidDataLength, blockSize.octets),
                caughtException.getMessage()
        );
        Mockito.verifyNoInteractions(transferBufferAllocator, in, loadTarget);
    }

    static Stream<Arguments> endiannessAndNonByteBlockSizeCombinations() {
        return Stream.of(Endianness.values())
                .flatMap(endianness -> Stream.of(BlockSize.values())
                        .filter(blockSize -> blockSize.octets != Byte.BYTES)
                        .map(blockSize -> Arguments.of(endianness, blockSize))
                );
    }

    @MethodSource("endiannessAndBlockSizeAndInputTypeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, input type: {2}")
    void testLoadFailsOnMissingTargetBuffer(Endianness endianness, BlockSize blockSize, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(endianness, blockSize, transferBufferAllocator);
        InputStream in = Mockito.mock(inputType);

        Mockito.doReturn(null).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        NullPointerException caughtException = Assertions.assertThrows(
                NullPointerException.class,
                () -> dataLoader.load(in, blockSize.octets, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Target buffer is missing", caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(blockSize.octets);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize.octets);
        Mockito.verifyNoMoreInteractions(transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndInputTypeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, input type: {2}")
    void testLoadFailsOnReadOnlyTargetBuffer(Endianness endianness, BlockSize blockSize, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(endianness, blockSize, transferBufferAllocator);
        InputStream in = Mockito.mock(inputType);

        ByteBuffer readOnlyTarget = ByteBuffer.allocate(blockSize.octets).order(endianness.byteOrder).asReadOnlyBuffer();
        Mockito.doReturn(readOnlyTarget).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, blockSize.octets, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Target buffer is read-only", caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(blockSize.octets);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize.octets);
        Mockito.verify(loadTarget).release(readOnlyTarget, false);
        Mockito.verifyNoMoreInteractions(transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndInputTypeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, input type: {2}")
    void testLoadFailsOnInvalidTargetBufferLength(Endianness endianness, BlockSize blockSize, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(endianness, blockSize, transferBufferAllocator);
        InputStream in = Mockito.mock(inputType);

        int invalidTargetLength = blockSize.octets - 1;
        ByteBuffer targetBuffer = ByteBuffer.allocate(invalidTargetLength).order(endianness.byteOrder);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, blockSize.octets, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Invalid target buffer length: " + invalidTargetLength, caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(blockSize.octets);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize.octets);
        Mockito.verify(loadTarget).release(targetBuffer, false);
        Mockito.verifyNoMoreInteractions(transferBufferAllocator, in, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndInputTypeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, input type: {2}")
    void testLoadFailsOnTooLongTargetBufferLength(Endianness endianness, BlockSize blockSize, Class<? extends InputStream> inputType) throws IOException {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(endianness, blockSize, transferBufferAllocator);
        InputStream in = Mockito.mock(inputType);

        int tooLongTargetLength = blockSize.octets * 2;
        ByteBuffer targetBuffer = ByteBuffer.allocate(tooLongTargetLength).order(endianness.byteOrder);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        IllegalStateException caughtException = Assertions.assertThrows(
                IllegalStateException.class,
                () -> dataLoader.load(in, blockSize.octets, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Invalid target buffer length: " + tooLongTargetLength, caughtException.getMessage());
        if (in instanceof ArraySource) {
            Mockito.verify((ArraySource) in).ensureAvailableAndAdvance(blockSize.octets);
        }
        Mockito.verify(loadTarget).acquire(0, blockSize.octets);
        Mockito.verify(loadTarget).release(targetBuffer, false);
        Mockito.verifyNoMoreInteractions(transferBufferAllocator, in, loadTarget);
    }

    static Stream<Arguments> endiannessAndBlockSizeAndInputTypeCombinations() {
        return Stream.of(Endianness.values())
                .flatMap(endianness -> Stream.of(BlockSize.values())
                        .flatMap(blockSize -> Stream.of(ArraySource.class, InputStream.class)
                                .map(inputType -> Arguments.of(endianness, blockSize, inputType))
                        )
                );
    }

    @MethodSource("endiannessAndBlockSizeCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}")
    void testLoadFromArrayFailsWithEOF(Endianness endianness, BlockSize blockSize) {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(endianness, blockSize, transferBufferAllocator);
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = new ArraySource(new byte[blockSize.octets - 1]);

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> dataLoader.load(in, blockSize.octets, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verifyNoInteractions(transferBufferAllocator, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndByteOrderAndFactoryCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, out: [{2}, {3}]")
    void testLoadFromStreamFailsWithEOF(Endianness endianness, BlockSize blockSize, ByteOrder outByteOrder, TargetBufferFactory outFactory) {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(endianness, blockSize, transferBufferAllocator);
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = new ByteArrayInputStream(new byte[blockSize.octets - 1]);

        ByteBuffer targetBuffer = outFactory.createFor(0, blockSize.octets).order(outByteOrder);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        byte[] transferBuffer = null;
        if (needsTransferBuffer(endianness, blockSize, targetBuffer)) {
            transferBuffer = new byte[blockSize.octets];
            Mockito.doReturn(transferBuffer).when(transferBufferAllocator).allocate(Mockito.anyInt(), Mockito.anyInt());
        }

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> dataLoader.load(in, blockSize.octets, loadTarget, blockSize.octets)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verify(loadTarget).acquire(0, blockSize.octets);
        Mockito.verify(loadTarget).release(targetBuffer, false);
        if (transferBuffer != null) {
            int minimumTransferBufferLength = getMinimumTransferBufferLength(endianness, blockSize, targetBuffer);
            Mockito.verify(transferBufferAllocator).allocate(minimumTransferBufferLength, blockSize.octets);
            Mockito.verify(transferBufferAllocator).free(transferBuffer);
        }
        Mockito.verifyNoMoreInteractions(transferBufferAllocator, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndByteOrderAndFactoryCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, out: [{2}, {3}]")
    void testLoadFailsWithNullTransferBuffer(Endianness endianness, BlockSize blockSize, ByteOrder outByteOrder, TargetBufferFactory outFactory) throws IOException {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(endianness, blockSize, transferBufferAllocator);
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = new ByteArrayInputStream(new byte[blockSize.octets]);

        ByteBuffer targetBuffer = outFactory.createFor(0, blockSize.octets).order(outByteOrder);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        if (needsTransferBuffer(endianness, blockSize, targetBuffer)) {
            Mockito.doReturn(null).when(transferBufferAllocator).allocate(Mockito.anyInt(), Mockito.anyInt());

            NullPointerException caughtException = Assertions.assertThrows(
                    NullPointerException.class,
                    () -> dataLoader.load(in, blockSize.octets, loadTarget, blockSize.octets)
            );

            Assertions.assertEquals("Transfer buffer is missing", caughtException.getMessage());
            Mockito.verify(loadTarget).acquire(0, blockSize.octets);
            Mockito.verify(loadTarget).release(targetBuffer, false);
            int minimumTransferBufferLength = getMinimumTransferBufferLength(endianness, blockSize, targetBuffer);
            Mockito.verify(transferBufferAllocator).allocate(minimumTransferBufferLength, blockSize.octets);
        } else {
            dataLoader.load(in, blockSize.octets, loadTarget, blockSize.octets);

            Mockito.verify(loadTarget).acquire(0, blockSize.octets);
            Mockito.verify(loadTarget).release(targetBuffer, true);
        }

        Mockito.verifyNoMoreInteractions(transferBufferAllocator, loadTarget);
    }

    @MethodSource("endiannessAndBlockSizeAndByteOrderAndFactoryCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}, out: [{2}, {3}]")
    void testLoadFailsWithTooShortTransferBuffer(Endianness endianness, BlockSize blockSize, ByteOrder outByteOrder, TargetBufferFactory outFactory) throws IOException {
        RawTexDataLoader dataLoader = new RawBlockDataLoader(endianness, blockSize, transferBufferAllocator);
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        InputStream in = new ByteArrayInputStream(new byte[blockSize.octets]);

        ByteBuffer targetBuffer = outFactory.createFor(0, blockSize.octets).order(outByteOrder);
        Mockito.doReturn(targetBuffer).when(loadTarget).acquire(Mockito.anyInt(), Mockito.anyInt());

        if (needsTransferBuffer(endianness, blockSize, targetBuffer)) {
            int minimumTransferBufferLength = getMinimumTransferBufferLength(endianness, blockSize, targetBuffer);
            byte[] transferBuffer = new byte[minimumTransferBufferLength - 1];
            Mockito.doReturn(transferBuffer).when(transferBufferAllocator).allocate(Mockito.anyInt(), Mockito.anyInt());

            IllegalStateException caughtException = Assertions.assertThrows(
                    IllegalStateException.class,
                    () -> dataLoader.load(in, blockSize.octets, loadTarget, blockSize.octets)
            );

            Assertions.assertEquals("Transfer buffer too short: " + (minimumTransferBufferLength - 1), caughtException.getMessage());
            Mockito.verify(loadTarget).acquire(0, blockSize.octets);
            Mockito.verify(loadTarget).release(targetBuffer, false);
            Mockito.verify(transferBufferAllocator).allocate(minimumTransferBufferLength, blockSize.octets);
            Mockito.verify(transferBufferAllocator).free(transferBuffer);
        } else {
            dataLoader.load(in, blockSize.octets, loadTarget, blockSize.octets);

            Mockito.verify(loadTarget).acquire(0, blockSize.octets);
            Mockito.verify(loadTarget).release(targetBuffer, true);
        }

        Mockito.verifyNoMoreInteractions(transferBufferAllocator, loadTarget);
    }

    static boolean needsTransferBuffer(Endianness endianness, BlockSize blockSize, ByteBuffer targetBuffer) {
        return (blockSize != BlockSize.OCTETS_1 && targetBuffer.order() != endianness.byteOrder) || !targetBuffer.hasArray();
    }

    static int getMinimumTransferBufferLength(Endianness endianness, BlockSize blockSize, ByteBuffer targetBuffer) {
        return (blockSize != BlockSize.OCTETS_1 && targetBuffer.order() != endianness.byteOrder) ? blockSize.octets : 1;
    }

    static Stream<Arguments> endiannessAndBlockSizeAndByteOrderAndFactoryCombinations() {
        return Stream.of(Endianness.values())
                .flatMap(endianness -> Stream.of(BlockSize.values())
                        .flatMap(blockSize -> Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                                .flatMap(outByteOrder -> Stream.of(new DirectBufferFactory(), new NonDirectBufferFactory())
                                        .map(outFactory -> Arguments.of(endianness, blockSize, outByteOrder, outFactory))
                                )
                        )
                );
    }

}
