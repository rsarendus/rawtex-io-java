package ee.ristoseene.rawtex.io.deflate.in.data;

import ee.ristoseene.rawtex.io.core.common.data.TransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.common.format.BlockSize;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;
import ee.ristoseene.rawtex.io.core.common.internal.CommonIO;
import ee.ristoseene.rawtex.io.core.common.test.BitTwiddlingUtils;
import ee.ristoseene.rawtex.io.core.common.test.LongToByteFunction;
import ee.ristoseene.rawtex.io.core.common.test.TestInputStreamUtils;
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
import ee.ristoseene.rawtex.io.deflate.common.format.DeflateCompressionFormatIndicator;
import ee.ristoseene.rawtex.io.deflate.in.test.CloseableDeflater;
import ee.ristoseene.rawtex.io.deflate.in.test.TestSimpleInflaterAllocator;
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
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Inflater;

class InflatingBlockDataLoaderLongInputTest {

    private static final int TRANSFER_BUFFER_LENGTH = 1024 * 5 + 1;
    private static final int TARGET_BUFFER_LENGTH = 1024 * 8;

    @Tag("slow")
    @MethodSource("streamInputParameterCombinations")
    @ParameterizedTest(name = "in: {0}, out: [{1}, {2}], compression format: {3}")
    void testLoad8(
            Endianness endianness, ByteOrder outByteOrder, TargetBufferFactory outFactory, DeflateCompressionFormatIndicator compressionFormatIndicator
    ) throws IOException {
        LongToByteFunction longToByte = i -> (byte) (i & 0xffL);

        long dataLength = Integer.MAX_VALUE + 1L;
        TestSimpleInflaterAllocator inflaterAllocator = new TestSimpleInflaterAllocator(compressionFormatIndicator);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, BlockSize.of(Byte.BYTES), inflaterAllocator, transferBufferAllocator);
        Function<Deflater, InputStream> inFactory = deflater -> new DeflaterInputStream(new TestStreamingInputStream(dataLength, longToByte::apply), deflater);
        TestLoadTarget loadTarget = new TestStreamingLoadTarget(
                outFactory.create(TARGET_BUFFER_LENGTH).order(outByteOrder),
                longToByte
        );

        try (CloseableDeflater closeableDeflater = new CloseableDeflater(
                new Deflater(Deflater.DEFAULT_COMPRESSION, compressionFormatIndicator.nowrap)
        )) {
            long inputLength = TestInputStreamUtils.getInputStreamLength(inFactory.apply(closeableDeflater.deflater));
            closeableDeflater.deflater.reset(); // Reset after using for acquiring input length

            dataLoader.load(inFactory.apply(closeableDeflater.deflater), inputLength, loadTarget, dataLength);
        }

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @Tag("slow")
    @MethodSource("streamInputParameterCombinations")
    @ParameterizedTest(name = "in: {0}, out: [{1}, {2}], compression format: {3}")
    void testLoad16(
            Endianness endianness, ByteOrder outByteOrder, TargetBufferFactory outFactory, DeflateCompressionFormatIndicator compressionFormatIndicator
    ) throws IOException {
        Function<Endianness, LongToByteFunction> longToByteFactory = e -> i -> {
            final short shortValue = (short) ((i / Short.BYTES) & 0xffffL);
            return BitTwiddlingUtils.byteAt(shortValue, (int) (i % Short.BYTES), e);
        };

        long dataLength = (Integer.MAX_VALUE + 1L) * Short.BYTES;
        TestSimpleInflaterAllocator inflaterAllocator = new TestSimpleInflaterAllocator(compressionFormatIndicator);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, BlockSize.of(Short.BYTES), inflaterAllocator, transferBufferAllocator);
        Function<Deflater, InputStream> inFactory = deflater -> new DeflaterInputStream(new TestStreamingInputStream(dataLength, longToByteFactory.apply(endianness)::apply), deflater);
        TestLoadTarget loadTarget = new TestStreamingLoadTarget(
                outFactory.create(TARGET_BUFFER_LENGTH).order(outByteOrder),
                longToByteFactory.apply(Endianness.of(outByteOrder))
        );

        try (CloseableDeflater closeableDeflater = new CloseableDeflater(
                new Deflater(Deflater.DEFAULT_COMPRESSION, compressionFormatIndicator.nowrap)
        )) {
            long inputLength = TestInputStreamUtils.getInputStreamLength(inFactory.apply(closeableDeflater.deflater));
            closeableDeflater.deflater.reset(); // Reset after using for acquiring input length

            dataLoader.load(inFactory.apply(closeableDeflater.deflater), inputLength, loadTarget, dataLength);
        }

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @Tag("slow")
    @MethodSource("streamInputParameterCombinations")
    @ParameterizedTest(name = "in: {0}, out: [{1}, {2}], compression format: {3}")
    void testLoad32(
            Endianness endianness, ByteOrder outByteOrder, TargetBufferFactory outFactory, DeflateCompressionFormatIndicator compressionFormatIndicator
    ) throws IOException {
        Function<Endianness, LongToByteFunction> longToByteFactory = e -> i -> {
            final int intValue = (int) ((i / Integer.BYTES) & 0xffffffffL);
            return BitTwiddlingUtils.byteAt(intValue, (int) (i % Integer.BYTES), e);
        };

        long dataLength = (Integer.MAX_VALUE + 1L) * Integer.BYTES;
        TestSimpleInflaterAllocator inflaterAllocator = new TestSimpleInflaterAllocator(compressionFormatIndicator);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, BlockSize.of(Integer.BYTES), inflaterAllocator, transferBufferAllocator);
        Function<Deflater, InputStream> inFactory = deflater -> new DeflaterInputStream(new TestStreamingInputStream(dataLength, longToByteFactory.apply(endianness)::apply), deflater);
        TestLoadTarget loadTarget = new TestStreamingLoadTarget(
                outFactory.create(TARGET_BUFFER_LENGTH).order(outByteOrder),
                longToByteFactory.apply(Endianness.of(outByteOrder))
        );

        try (CloseableDeflater closeableDeflater = new CloseableDeflater(
                new Deflater(Deflater.DEFAULT_COMPRESSION, compressionFormatIndicator.nowrap)
        )) {
            long inputLength = TestInputStreamUtils.getInputStreamLength(inFactory.apply(closeableDeflater.deflater));
            closeableDeflater.deflater.reset(); // Reset after using for acquiring input length

            dataLoader.load(inFactory.apply(closeableDeflater.deflater), inputLength, loadTarget, dataLength);
        }

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @Tag("slow")
    @MethodSource("streamInputParameterCombinations")
    @ParameterizedTest(name = "in: {0}, out: [{1}, {2}], compression format: {3}")
    void testLoad64(
            Endianness endianness, ByteOrder outByteOrder, TargetBufferFactory outFactory, DeflateCompressionFormatIndicator compressionFormatIndicator
    ) throws IOException {
        Function<Endianness, LongToByteFunction> longToByteFactory = e -> i -> {
            final long longValue = i / Long.BYTES;
            return BitTwiddlingUtils.byteAt(longValue, (int) (i % Long.BYTES), e);
        };

        long dataLength = (Integer.MAX_VALUE + 1L) * Long.BYTES;
        TestSimpleInflaterAllocator inflaterAllocator = new TestSimpleInflaterAllocator(compressionFormatIndicator);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, BlockSize.of(Long.BYTES), inflaterAllocator, transferBufferAllocator);
        Function<Deflater, InputStream> inFactory = deflater -> new DeflaterInputStream(new TestStreamingInputStream(dataLength, longToByteFactory.apply(endianness)::apply), deflater);
        TestLoadTarget loadTarget = new TestStreamingLoadTarget(
                outFactory.create(TARGET_BUFFER_LENGTH).order(outByteOrder),
                longToByteFactory.apply(Endianness.of(outByteOrder))
        );

        try (CloseableDeflater closeableDeflater = new CloseableDeflater(
                new Deflater(Deflater.DEFAULT_COMPRESSION, compressionFormatIndicator.nowrap))
        ) {
            long inputLength = TestInputStreamUtils.getInputStreamLength(inFactory.apply(closeableDeflater.deflater));
            closeableDeflater.deflater.reset(); // Reset after using for acquiring input length

            dataLoader.load(inFactory.apply(closeableDeflater.deflater), inputLength, loadTarget, dataLength);
        }

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    static Stream<Arguments> streamInputParameterCombinations() {
        return Stream.of(Endianness.values())
                .flatMap(endianness -> Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                        .flatMap(outByteOrder -> Stream.of(new DirectBufferFactory(), new NonDirectBufferFactory())
                                .flatMap(outFactory -> Stream.of(DeflateCompressionFormatIndicator.values())
                                        .map(compressionFormat -> Arguments.of(endianness, outByteOrder, outFactory, compressionFormat))
                                )
                        )
                );
    }

    @MethodSource("arraySourceInputParameterCombinations")
    @ParameterizedTest(name = "endianness: {0}, block size: {1}")
    void testLoadFailsOnLongInputForArraySource(Endianness endianness, BlockSize blockSize) throws IOException {
        InflaterAllocator inflaterAllocator = Mockito.mock(InflaterAllocator.class);
        TransferBufferAllocator transferBufferAllocator = Mockito.mock(TransferBufferAllocator.class);
        RawTexDataLoader dataLoader = new InflatingBlockDataLoader(endianness, blockSize, inflaterAllocator, transferBufferAllocator);
        RawTexLoadTarget loadTarget = Mockito.mock(RawTexLoadTarget.class);
        long dataLength = blockSize.truncate(Long.MAX_VALUE);

        Inflater inflater = Mockito.mock(Inflater.class);
        Mockito.doReturn(inflater).when(inflaterAllocator).allocate();
        Mockito.doReturn(true).when(inflater).needsInput();

        ArraySource in = Mockito.mock(ArraySource.class);
        EOFException eofException = CommonIO.unexpectedEndOfInputException();
        Mockito.doThrow(eofException).when(in).ensureAvailableAndAdvance(Mockito.anyLong());

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> dataLoader.load(in, dataLength, loadTarget, dataLength)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verify(inflaterAllocator, Mockito.times(1)).allocate();
        Mockito.verify(inflater, Mockito.times(1)).needsDictionary();
        Mockito.verify(inflater, Mockito.times(1)).needsInput();
        Mockito.verify(in, Mockito.times(1)).ensureAvailableAndAdvance(dataLength);
        Mockito.verify(inflaterAllocator, Mockito.times(1)).free(inflater);
        Mockito.verifyNoMoreInteractions(in, inflater, inflaterAllocator);
        Mockito.verifyNoInteractions(transferBufferAllocator, loadTarget);
    }

    static Stream<Arguments> arraySourceInputParameterCombinations() {
        return Stream.of(Endianness.values())
                .flatMap(endianness -> Stream.of(BlockSize.values())
                        .map(blockSize -> Arguments.of(endianness, blockSize))
                );
    }

}
