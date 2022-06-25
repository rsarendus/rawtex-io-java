package ee.ristoseene.rawtex.io.core.in.data;

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
import ee.ristoseene.rawtex.io.core.in.test.TestFixedLengthLoadTarget;
import ee.ristoseene.rawtex.io.core.in.test.TestLoadTarget;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.stream.Stream;

class RawBlockDataLoaderSuccessTest {

    private static final int TRANSFER_BUFFER_LENGTH = 137;

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    void testLoad8(Endianness endianness, InputFactory inFactory, ByteOrder outByteOrder, TargetBufferFactory outFactory, int blockCount) throws IOException {
        byte[] pixelData = TestBufferUtils.generateRandom8(blockCount);
        long length = blockCount * (long) Byte.BYTES;

        InputStream in = inFactory.create(pixelData.clone());
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestFixedLengthLoadTarget(
                pixelData,
                (o, l) -> outFactory.create(l).order(outByteOrder)
        );

        new RawBlockDataLoader(
                endianness,
                BlockSize.of(Byte.BYTES),
                transferBufferAllocator
        )
                .load(in, length, loadTarget, length);

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    void testLoad16(Endianness endianness, InputFactory inFactory, ByteOrder outByteOrder, TargetBufferFactory outFactory, int blockCount) throws IOException {
        short[] pixelData = TestBufferUtils.generateRandom16(blockCount);
        long length = blockCount * (long) Short.BYTES;

        InputStream in = inFactory.create(TestBufferUtils.toBytes(pixelData, endianness.byteOrder));
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestFixedLengthLoadTarget(
                TestBufferUtils.toBytes(pixelData, outByteOrder),
                (o, l) -> outFactory.create(l).order(outByteOrder)
        );

        new RawBlockDataLoader(
                endianness,
                BlockSize.of(Short.BYTES),
                transferBufferAllocator
        )
                .load(in, length, loadTarget, length);

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    void testLoad32(Endianness endianness, InputFactory inFactory, ByteOrder outByteOrder, TargetBufferFactory outFactory, int blockCount) throws IOException {
        int[] pixelData = TestBufferUtils.generateRandom32(blockCount);
        long length = blockCount * (long) Integer.BYTES;

        InputStream in = inFactory.create(TestBufferUtils.toBytes(pixelData, endianness.byteOrder));
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestFixedLengthLoadTarget(
                TestBufferUtils.toBytes(pixelData, outByteOrder),
                (o, l) -> outFactory.create(l).order(outByteOrder)
        );

        new RawBlockDataLoader(
                endianness,
                BlockSize.of(Integer.BYTES),
                transferBufferAllocator
        )
                .load(in, length, loadTarget, length);

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    void testLoad64(Endianness endianness, InputFactory inFactory, ByteOrder outByteOrder, TargetBufferFactory outFactory, int blockCount) throws IOException {
        long[] pixelData = TestBufferUtils.generateRandom64(blockCount);
        long length = blockCount * (long) Long.BYTES;

        InputStream in = inFactory.create(TestBufferUtils.toBytes(pixelData, endianness.byteOrder));
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestFixedLengthLoadTarget(
                TestBufferUtils.toBytes(pixelData, outByteOrder),
                (o, l) -> outFactory.create(l).order(outByteOrder)
        );

        new RawBlockDataLoader(
                endianness,
                BlockSize.of(Long.BYTES),
                transferBufferAllocator
        )
                .load(in, length, loadTarget, length);

        loadTarget.assertReleased();
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
                                        .flatMap(outFactory -> Stream.of(1, 123, 12345)
                                                .map(length -> Arguments.of(endianness, inFactory, outByteOrder, outFactory, length))
                                        )
                                )
                        )
                );
    }

}
