package ee.ristoseene.rawtex.io.deflate.in.data;

import ee.ristoseene.rawtex.io.core.common.format.BlockSize;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;
import ee.ristoseene.rawtex.io.core.common.test.TestBufferUtils;
import ee.ristoseene.rawtex.io.core.common.test.TestStaticTransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.common.test.TestTransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.in.test.ByteArrayInputFactory;
import ee.ristoseene.rawtex.io.core.in.test.DirectBufferFactory;
import ee.ristoseene.rawtex.io.core.in.test.InputFactory;
import ee.ristoseene.rawtex.io.core.in.test.NonDirectBufferFactory;
import ee.ristoseene.rawtex.io.core.in.test.StreamInputFactory;
import ee.ristoseene.rawtex.io.core.in.test.TargetBufferFactory;
import ee.ristoseene.rawtex.io.core.in.test.TestLoadTarget;
import ee.ristoseene.rawtex.io.deflate.common.format.DeflateCompressionFormatIndicator;
import ee.ristoseene.rawtex.io.deflate.common.test.TestDeflateUtils;
import ee.ristoseene.rawtex.io.deflate.in.test.TestSimpleInflaterAllocator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.stream.Stream;

class InflatingBlockDataLoaderSuccessTest {

    private static final int TRANSFER_BUFFER_LENGTH = 137;

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], compression format: {4}, block count: {5}")
    void testLoad8(
            Endianness endianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory,
            DeflateCompressionFormatIndicator compressionFormatIndicator, int blockCount
    ) throws IOException {
        byte[] pixelData = TestBufferUtils.generateRandom8(blockCount);
        byte[] deflatedData = TestDeflateUtils.deflate(compressionFormatIndicator, pixelData);
        int dataLength = blockCount * Byte.BYTES;

        InputStream in = inFactory.create(deflatedData);
        TestSimpleInflaterAllocator inflaterAllocator = new TestSimpleInflaterAllocator(compressionFormatIndicator);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestLoadTarget(
                pixelData,
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new InflatingBlockDataLoader(
                endianness,
                BlockSize.of(Byte.BYTES),
                inflaterAllocator,
                transferBufferAllocator
        )
                .load(in, deflatedData.length, loadTarget, dataLength);

        loadTarget.assertReleased();
        inflaterAllocator.assertFreed();
        transferBufferAllocator.assertFreed();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], compression format: {4}, block count: {5}")
    void testLoad16(
            Endianness endianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory,
            DeflateCompressionFormatIndicator compressionFormatIndicator, int blockCount
    ) throws IOException {
        short[] pixelData = TestBufferUtils.generateRandom16(blockCount);
        byte[] deflatedData = TestDeflateUtils.deflate(compressionFormatIndicator, TestBufferUtils.toBytes(pixelData, endianness.byteOrder));
        int dataLength = blockCount * Short.BYTES;

        InputStream in = inFactory.create(deflatedData);
        TestSimpleInflaterAllocator inflaterAllocator = new TestSimpleInflaterAllocator(compressionFormatIndicator);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestLoadTarget(
                TestBufferUtils.toBytes(pixelData, outEndianness),
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new InflatingBlockDataLoader(
                endianness,
                BlockSize.of(Short.BYTES),
                inflaterAllocator,
                transferBufferAllocator
        )
                .load(in, deflatedData.length, loadTarget, dataLength);

        loadTarget.assertReleased();
        inflaterAllocator.assertFreed();
        transferBufferAllocator.assertFreed();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], compression format: {4}, block count: {5}")
    void testLoad32(
            Endianness endianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory,
            DeflateCompressionFormatIndicator compressionFormatIndicator, int blockCount
    ) throws IOException {
        int[] pixelData = TestBufferUtils.generateRandom32(blockCount);
        byte[] deflatedData = TestDeflateUtils.deflate(compressionFormatIndicator, TestBufferUtils.toBytes(pixelData, endianness.byteOrder));
        int dataLength = blockCount * Integer.BYTES;

        InputStream in = inFactory.create(deflatedData);
        TestSimpleInflaterAllocator inflaterAllocator = new TestSimpleInflaterAllocator(compressionFormatIndicator);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestLoadTarget(
                TestBufferUtils.toBytes(pixelData, outEndianness),
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new InflatingBlockDataLoader(
                endianness,
                BlockSize.of(Integer.BYTES),
                inflaterAllocator,
                transferBufferAllocator
        )
                .load(in, deflatedData.length, loadTarget, dataLength);

        loadTarget.assertReleased();
        inflaterAllocator.assertFreed();
        transferBufferAllocator.assertFreed();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], compression format: {4}, block count: {5}")
    void testLoad64(
            Endianness endianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory,
            DeflateCompressionFormatIndicator compressionFormatIndicator, int blockCount
    ) throws IOException {
        long[] pixelData = TestBufferUtils.generateRandom64(blockCount);
        byte[] deflatedData = TestDeflateUtils.deflate(compressionFormatIndicator, TestBufferUtils.toBytes(pixelData, endianness.byteOrder));
        int dataLength = blockCount * Long.BYTES;

        InputStream in = inFactory.create(deflatedData);
        TestSimpleInflaterAllocator inflaterAllocator = new TestSimpleInflaterAllocator(compressionFormatIndicator);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestLoadTarget(
                TestBufferUtils.toBytes(pixelData, outEndianness),
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new InflatingBlockDataLoader(
                endianness,
                BlockSize.of(Long.BYTES),
                inflaterAllocator,
                transferBufferAllocator
        )
                .load(in, deflatedData.length, loadTarget, dataLength);

        loadTarget.assertReleased();
        inflaterAllocator.assertFreed();
        transferBufferAllocator.assertFreed();
    }

    static Stream<Arguments> parameterCombinations() {
        return Stream.of(Endianness.values())
                .flatMap(endianness -> Stream.of(new ByteArrayInputFactory(), new StreamInputFactory())
                        .flatMap(inFactory -> Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                                .flatMap(outByteOrder -> Stream.of(
                                        new DirectBufferFactory(), new DirectBufferFactory(0x18), new DirectBufferFactory(0x1ff0),
                                        new NonDirectBufferFactory(), new NonDirectBufferFactory(0x18), new NonDirectBufferFactory(0x1ff0)
                                        )
                                        .flatMap(outFactory -> Stream.of(DeflateCompressionFormatIndicator.values())
                                                .flatMap(compressionFormat -> Stream.of(1, 123, 12345)
                                                        .map(length -> Arguments.of(endianness, inFactory, outByteOrder, outFactory, compressionFormat, length))
                                                )
                                        )
                                )
                        )
                );
    }

}
