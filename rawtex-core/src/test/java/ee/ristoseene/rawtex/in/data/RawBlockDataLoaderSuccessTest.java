package ee.ristoseene.rawtex.in.data;

import ee.ristoseene.rawtex.common.internal.Endianness;
import ee.ristoseene.rawtex.common.test.TestBufferUtils;
import ee.ristoseene.rawtex.in.test.ByteArrayInputFactory;
import ee.ristoseene.rawtex.in.test.DirectBufferFactory;
import ee.ristoseene.rawtex.in.test.InputFactory;
import ee.ristoseene.rawtex.in.test.NonDirectBufferFactory;
import ee.ristoseene.rawtex.in.test.StreamInputFactory;
import ee.ristoseene.rawtex.in.test.TargetBufferFactory;
import ee.ristoseene.rawtex.in.test.TestLoadTarget;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.stream.Stream;

public class RawBlockDataLoaderSuccessTest {

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    public void testLoad8(ByteOrder inEndianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory, int blockCount) throws IOException {
        byte[] pixelData = TestBufferUtils.generateRandom8(blockCount);
        int length = blockCount * Byte.BYTES;

        InputStream in = inFactory.create(pixelData.clone());
        TestLoadTarget loadTarget = new TestLoadTarget(
                pixelData,
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new RawBlockDataLoader(
                () -> Byte.BYTES,
                Endianness.of(inEndianness)
        )
                .load(in, length, loadTarget, length);

        loadTarget.assertReleased();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    public void testLoad16(ByteOrder inEndianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory, int blockCount) throws IOException {
        short[] pixelData = TestBufferUtils.generateRandom16(blockCount);
        int length = blockCount * Short.BYTES;

        InputStream in = inFactory.create(TestBufferUtils.toBytes(pixelData, inEndianness));
        TestLoadTarget loadTarget = new TestLoadTarget(
                TestBufferUtils.toBytes(pixelData, outEndianness),
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new RawBlockDataLoader(
                () -> Short.BYTES,
                Endianness.of(inEndianness)
        )
                .load(in, length, loadTarget, length);

        loadTarget.assertReleased();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    public void testLoad32(ByteOrder inEndianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory, int blockCount) throws IOException {
        int[] pixelData = TestBufferUtils.generateRandom32(blockCount);
        int length = blockCount * Integer.BYTES;

        InputStream in = inFactory.create(TestBufferUtils.toBytes(pixelData, inEndianness));
        TestLoadTarget loadTarget = new TestLoadTarget(
                TestBufferUtils.toBytes(pixelData, outEndianness),
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new RawBlockDataLoader(
                () -> Integer.BYTES,
                Endianness.of(inEndianness)
        )
                .load(in, length, loadTarget, length);

        loadTarget.assertReleased();
    }

    @MethodSource("parameterCombinations")
    @ParameterizedTest(name = "in: [{0}, {1}], out: [{2}, {3}], block count: {4}")
    public void testLoad64(ByteOrder inEndianness, InputFactory inFactory, ByteOrder outEndianness, TargetBufferFactory outFactory, int blockCount) throws IOException {
        long[] pixelData = TestBufferUtils.generateRandom64(blockCount);
        int length = blockCount * Long.BYTES;

        InputStream in = inFactory.create(TestBufferUtils.toBytes(pixelData, inEndianness));
        TestLoadTarget loadTarget = new TestLoadTarget(
                TestBufferUtils.toBytes(pixelData, outEndianness),
                (o, l) -> outFactory.createFor(o, l).order(outEndianness)
        );

        new RawBlockDataLoader(
                () -> Long.BYTES,
                Endianness.of(inEndianness)
        )
                .load(in, length, loadTarget, length);

        loadTarget.assertReleased();
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
