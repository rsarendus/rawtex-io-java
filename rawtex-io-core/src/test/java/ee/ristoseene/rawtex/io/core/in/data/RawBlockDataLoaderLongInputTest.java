package ee.ristoseene.rawtex.io.core.in.data;

import ee.ristoseene.rawtex.io.core.common.data.TransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.common.format.BlockSize;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;
import ee.ristoseene.rawtex.io.core.common.internal.CommonIO;
import ee.ristoseene.rawtex.io.core.common.test.BitTwiddlingUtils;
import ee.ristoseene.rawtex.io.core.common.test.LongToByteFunction;
import ee.ristoseene.rawtex.io.core.common.test.TestStaticTransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.common.test.TestTransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.in.RawTexDataLoader;
import ee.ristoseene.rawtex.io.core.in.RawTexLoadTarget;
import ee.ristoseene.rawtex.io.core.in.internal.ArraySource;
import ee.ristoseene.rawtex.io.core.in.test.DirectBufferFactory;
import ee.ristoseene.rawtex.io.core.in.test.NonDirectBufferFactory;
import ee.ristoseene.rawtex.io.core.in.test.TargetBufferFactory;
import ee.ristoseene.rawtex.io.core.in.test.TestLoadTarget;
import ee.ristoseene.rawtex.io.core.in.test.TestStreamingInputStream;
import ee.ristoseene.rawtex.io.core.in.test.TestStreamingLoadTarget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.function.Function;
import java.util.stream.Stream;

class RawBlockDataLoaderLongInputTest {

    private static final int TRANSFER_BUFFER_LENGTH = 1024 * 5 + 1;
    private static final int TARGET_BUFFER_LENGTH = 1024 * 8;

    @Tag("slow")
    @MethodSource("streamInputParameterCombinations")
    @ParameterizedTest(name = "in: {0}, out: [{1}, {2}]")
    void testLoad8(Endianness endianness, ByteOrder outByteOrder, TargetBufferFactory outFactory) throws IOException {
        LongToByteFunction longToByte = i -> (byte) (i & 0xffL);

        long dataLength = Integer.MAX_VALUE + 1L;
        InputStream in = new TestStreamingInputStream(dataLength, longToByte::apply);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestStreamingLoadTarget(
                outFactory.create(TARGET_BUFFER_LENGTH).order(outByteOrder),
                longToByte
        );

        new RawBlockDataLoader(
                endianness,
                BlockSize.of(Byte.BYTES),
                transferBufferAllocator
        )
                .load(in, dataLength, loadTarget, dataLength);

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @Tag("slow")
    @MethodSource("streamInputParameterCombinations")
    @ParameterizedTest(name = "in: {0}, out: [{1}, {2}]")
    void testLoad16(Endianness endianness, ByteOrder outByteOrder, TargetBufferFactory outFactory) throws IOException {
        Function<Endianness, LongToByteFunction> longToByteFactory = e -> i -> {
            final short shortValue = (short) ((i / Short.BYTES) & 0xffffL);
            return BitTwiddlingUtils.byteAt(shortValue, (int) (i % Short.BYTES), e);
        };

        long dataLength = (Integer.MAX_VALUE + 1L) * Short.BYTES;
        InputStream in = new TestStreamingInputStream(dataLength, longToByteFactory.apply(endianness)::apply);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestStreamingLoadTarget(
                outFactory.create(TARGET_BUFFER_LENGTH).order(outByteOrder),
                longToByteFactory.apply(Endianness.of(outByteOrder))
        );

        new RawBlockDataLoader(
                endianness,
                BlockSize.of(Short.BYTES),
                transferBufferAllocator
        )
                .load(in, dataLength, loadTarget, dataLength);

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @Tag("slow")
    @MethodSource("streamInputParameterCombinations")
    @ParameterizedTest(name = "in: {0}, out: [{1}, {2}]")
    void testLoad32(Endianness endianness, ByteOrder outByteOrder, TargetBufferFactory outFactory) throws IOException {
        Function<Endianness, LongToByteFunction> longToByteFactory = e -> i -> {
            final int intValue = (int) ((i / Integer.BYTES) & 0xffffffffL);
            return BitTwiddlingUtils.byteAt(intValue, (int) (i % Integer.BYTES), e);
        };

        long dataLength = (Integer.MAX_VALUE + 1L) * Integer.BYTES;
        InputStream in = new TestStreamingInputStream(dataLength, longToByteFactory.apply(endianness)::apply);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestStreamingLoadTarget(
                outFactory.create(TARGET_BUFFER_LENGTH).order(outByteOrder),
                longToByteFactory.apply(Endianness.of(outByteOrder))
        );

        new RawBlockDataLoader(
                endianness,
                BlockSize.of(Integer.BYTES),
                transferBufferAllocator
        )
                .load(in, dataLength, loadTarget, dataLength);

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @Tag("slow")
    @MethodSource("streamInputParameterCombinations")
    @ParameterizedTest(name = "in: {0}, out: [{1}, {2}]")
    void testLoad64(Endianness endianness, ByteOrder outByteOrder, TargetBufferFactory outFactory) throws IOException {
        Function<Endianness, LongToByteFunction> longToByteFactory = e -> i -> {
            final long longValue = i / Long.BYTES;
            return BitTwiddlingUtils.byteAt(longValue, (int) (i % Long.BYTES), e);
        };

        long dataLength = (Integer.MAX_VALUE + 1L) * Long.BYTES;
        InputStream in = new TestStreamingInputStream(dataLength, longToByteFactory.apply(endianness)::apply);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestStreamingLoadTarget(
                outFactory.create(TARGET_BUFFER_LENGTH).order(outByteOrder),
                longToByteFactory.apply(Endianness.of(outByteOrder))
        );

        new RawBlockDataLoader(
                endianness,
                BlockSize.of(Long.BYTES),
                transferBufferAllocator
        )
                .load(in, dataLength, loadTarget, dataLength);

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    static Stream<Arguments> streamInputParameterCombinations() {
        return Stream.of(Endianness.values())
                .flatMap(endianness -> Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                        .flatMap(outByteOrder -> Stream.of(new DirectBufferFactory(), new NonDirectBufferFactory())
                                .map(outFactory -> Arguments.of(endianness, outByteOrder, outFactory))
                        )
                );
    }

    @MethodSource("arraySourceInputParameterCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}")
    void testLoadFailsOnLongInputForArraySource(Endianness endianness, BlockSize blockSize) throws IOException {
        TransferBufferAllocator transferBufferAllocator = Mockito.mock(TransferBufferAllocator.class);
        RawTexDataLoader dataLoader = new RawBlockDataLoader(endianness, blockSize, transferBufferAllocator);
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        long dataLength = blockSize.truncate(Long.MAX_VALUE);

        ArraySource in = Mockito.mock(ArraySource.class);
        EOFException eofException = CommonIO.unexpectedEndOfInputException();
        Mockito.doThrow(eofException).when(in).ensureAvailableAndAdvance(Mockito.anyLong());

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> dataLoader.load(in, dataLength, loadTarget, dataLength)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verify(in, Mockito.times(1)).ensureAvailableAndAdvance(dataLength);
        Mockito.verifyNoInteractions(transferBufferAllocator, loadTarget);
        Mockito.verifyNoMoreInteractions(in);
    }

    static Stream<Arguments> arraySourceInputParameterCombinations() {
        return Stream.of(Endianness.values())
                .flatMap(endianness -> Stream.of(BlockSize.values())
                        .map(blockSize -> Arguments.of(endianness, blockSize))
                );
    }

}
