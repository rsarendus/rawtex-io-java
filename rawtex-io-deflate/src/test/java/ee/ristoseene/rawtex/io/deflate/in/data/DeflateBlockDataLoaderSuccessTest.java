package ee.ristoseene.rawtex.io.deflate.in.data;

import ee.ristoseene.rawtex.io.core.common.internal.Endianness;
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
import ee.ristoseene.rawtex.io.deflate.common.test.TestDeflateUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.stream.Stream;

public class DeflateBlockDataLoaderSuccessTest {

    private static final int TRANSFER_BUFFER_LENGTH = 137;

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    public void testLoad8(ByteOrder inEndianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory, int blockCount) throws IOException {
        byte[] pixelData = TestBufferUtils.generateRandom8(blockCount);
        byte[] deflatedData = TestDeflateUtils.deflate(pixelData);
        int dataLength = blockCount * Byte.BYTES;

        InputStream in = inFactory.create(deflatedData);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestLoadTarget(
                pixelData,
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new DeflateBlockDataLoader(
                () -> Byte.BYTES,
                Endianness.of(inEndianness),
                transferBufferAllocator
        )
                .load(in, deflatedData.length, loadTarget, dataLength);

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    public void testLoad16(ByteOrder inEndianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory, int blockCount) throws IOException {
        short[] pixelData = TestBufferUtils.generateRandom16(blockCount);
        byte[] deflatedData = TestDeflateUtils.deflate(TestBufferUtils.toBytes(pixelData, inEndianness));
        int dataLength = blockCount * Short.BYTES;

        InputStream in = inFactory.create(deflatedData);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestLoadTarget(
                TestBufferUtils.toBytes(pixelData, outEndianness),
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new DeflateBlockDataLoader(
                () -> Short.BYTES,
                Endianness.of(inEndianness),
                transferBufferAllocator
        )
                .load(in, deflatedData.length, loadTarget, dataLength);

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    public void testLoad32(ByteOrder inEndianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory, int blockCount) throws IOException {
        int[] pixelData = TestBufferUtils.generateRandom32(blockCount);
        byte[] deflatedData = TestDeflateUtils.deflate(TestBufferUtils.toBytes(pixelData, inEndianness));
        int dataLength = blockCount * Integer.BYTES;

        InputStream in = inFactory.create(deflatedData);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestLoadTarget(
                TestBufferUtils.toBytes(pixelData, outEndianness),
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new DeflateBlockDataLoader(
                () -> Integer.BYTES,
                Endianness.of(inEndianness),
                transferBufferAllocator
        )
                .load(in, deflatedData.length, loadTarget, dataLength);

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    public void testLoad64(ByteOrder inEndianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory, int blockCount) throws IOException {
        long[] pixelData = TestBufferUtils.generateRandom64(blockCount);
        byte[] deflatedData = TestDeflateUtils.deflate(TestBufferUtils.toBytes(pixelData, inEndianness));
        int dataLength = blockCount * Long.BYTES;

        InputStream in = inFactory.create(deflatedData);
        TestTransferBufferAllocator transferBufferAllocator = new TestStaticTransferBufferAllocator(TRANSFER_BUFFER_LENGTH, TRANSFER_BUFFER_LENGTH);
        TestLoadTarget loadTarget = new TestLoadTarget(
                TestBufferUtils.toBytes(pixelData, outEndianness),
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new DeflateBlockDataLoader(
                () -> Long.BYTES,
                Endianness.of(inEndianness),
                transferBufferAllocator
        )
                .load(in, deflatedData.length, loadTarget, dataLength);

        loadTarget.assertReleased();
        transferBufferAllocator.assertFreed();
    }

    private static Stream<Arguments> parameterCombinations() {
        return Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                .flatMap(inEndianness -> Stream.of(new ByteArrayInputFactory(), new StreamInputFactory())
                        .flatMap(inFactory -> Stream.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)
                                .flatMap(outEndianness -> Stream.of(
                                        new DirectBufferFactory(), new DirectBufferFactory(0x18), new DirectBufferFactory(0x1ff0),
                                        new NonDirectBufferFactory(), new NonDirectBufferFactory(0x18), new NonDirectBufferFactory(0x1ff0)
                                        )
                                        .flatMap(outFactory -> Stream.of(1, 123, 12345)
                                                .map(length -> Arguments.of(inEndianness, inFactory, outEndianness, outFactory, length))
                                        )
                                )
                        )
                );
    }

}
