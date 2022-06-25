package ee.ristoseene.rawtex.io.core.common.format;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

class BlockSizeTest {

    @Test
    void testOctets1() {
        Assertions.assertEquals(1, BlockSize.OCTETS_1.octets);
        Assertions.assertSame(BlockSize.OCTETS_1, BlockSize.of(1));
        Assertions.assertSame(BlockSize.OCTETS_1, BlockSize.fromPowerOfTwo(0));
    }

    @Test
    void testOctets2() {
        Assertions.assertEquals(2, BlockSize.OCTETS_2.octets);
        Assertions.assertSame(BlockSize.OCTETS_2, BlockSize.of(2));
        Assertions.assertSame(BlockSize.OCTETS_2, BlockSize.fromPowerOfTwo(1));
    }

    @Test
    void testOctets4() {
        Assertions.assertEquals(4, BlockSize.OCTETS_4.octets);
        Assertions.assertSame(BlockSize.OCTETS_4, BlockSize.of(4));
        Assertions.assertSame(BlockSize.OCTETS_4, BlockSize.fromPowerOfTwo(2));
    }

    @Test
    void testOctets8() {
        Assertions.assertEquals(8, BlockSize.OCTETS_8.octets);
        Assertions.assertSame(BlockSize.OCTETS_8, BlockSize.of(8));
        Assertions.assertSame(BlockSize.OCTETS_8, BlockSize.fromPowerOfTwo(3));
    }

    @ParameterizedTest(name = "block size: {0}, value: {1}")
    @MethodSource("blockSizeAndNonNegativeIntegerCombinations")
    void testMultipleOfNonNegativeInteger(BlockSize blockSize, int value) {
        int expectedResult = value * blockSize.octets;

        int result = blockSize.multipleOf(value);
        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest(name = "block size: {0}, value: {1}")
    @MethodSource("blockSizeAndNonNegativeIntegerCombinations")
    void testQuotientOfNonNegativeInteger(BlockSize blockSize, int value) {
        int expectedResult = value / blockSize.octets;

        int result = blockSize.quotientOf(value);
        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest(name = "block size: {0}, value: {1}")
    @MethodSource("blockSizeAndNonNegativeIntegerCombinations")
    void testRemainderOfNonNegativeInteger(BlockSize blockSize, int value) {
        int expectedResult = value % blockSize.octets;

        int result = blockSize.remainderOf(value);
        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest(name = "block size: {0}, value: {1}")
    @MethodSource("blockSizeAndNonNegativeIntegerCombinations")
    void testTruncateNonNegativeInteger(BlockSize blockSize, int value) {
        int expectedResult = value - (value % blockSize.octets);

        int result = blockSize.truncate(value);
        Assertions.assertEquals(expectedResult, result);
    }

    static IntStream nonNegativeIntegersToTest() {
        return IntStream.of(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 15, 16, 17, 25, 100, 111);
    }

    static Stream<Arguments> blockSizeAndNonNegativeIntegerCombinations() {
        return Stream.of(BlockSize.values())
                .flatMap(blockSize -> nonNegativeIntegersToTest()
                        .mapToObj(i -> Arguments.of(blockSize, i))
                );
    }

    @ParameterizedTest(name = "block size: {0}, value: {1}")
    @MethodSource("blockSizeAndNonNegativeLongIntegerCombinations")
    void testMultipleOfNonNegativeLongInteger(BlockSize blockSize, long value) {
        long expectedResult = value * blockSize.octets;

        long result = blockSize.multipleOf(value);
        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest(name = "block size: {0}, value: {1}")
    @MethodSource("blockSizeAndNonNegativeLongIntegerCombinations")
    void testQuotientOfNonNegativeLongInteger(BlockSize blockSize, long value) {
        long expectedResult = value / blockSize.octets;

        long result = blockSize.quotientOf(value);
        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest(name = "block size: {0}, value: {1}")
    @MethodSource("blockSizeAndNonNegativeLongIntegerCombinations")
    void testRemainderOfNonNegativeLongInteger(BlockSize blockSize, long value) {
        long expectedResult = value % blockSize.octets;

        long result = blockSize.remainderOf(value);
        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest(name = "block size: {0}, value: {1}")
    @MethodSource("blockSizeAndNonNegativeLongIntegerCombinations")
    void testTruncateNonNegativeLongInteger(BlockSize blockSize, long value) {
        long expectedResult = value - (value % blockSize.octets);

        long result = blockSize.truncate(value);
        Assertions.assertEquals(expectedResult, result);
    }

    static LongStream nonNegativeLongIntegersToTest() {
        return LongStream.concat(
                nonNegativeIntegersToTest().asLongStream(),
                LongStream.of(Integer.MAX_VALUE, Integer.MAX_VALUE + 1L, Integer.MAX_VALUE * 17L)
        );
    }

    static Stream<Arguments> blockSizeAndNonNegativeLongIntegerCombinations() {
        return Stream.of(BlockSize.values())
                .flatMap(blockSize -> nonNegativeLongIntegersToTest()
                        .mapToObj(i -> Arguments.of(blockSize, i))
                );
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 3, 5, 6, 7, 9, 16})
    void testOfWithInvalidNumberOfOctetsFails(int octets) {
        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> BlockSize.of(octets)
        );
        Assertions.assertEquals(
                String.format("Invalid block size: %d", octets),
                caughtException.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 4, 5, 6, 7, 8, 9, 10})
    void testFromPowerOfTwoWithInvalidExponentFails(int exponent) {
        IllegalArgumentException caughtException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> BlockSize.fromPowerOfTwo(exponent)
        );
        Assertions.assertEquals(
                String.format("Invalid block size exponent: %d", exponent),
                caughtException.getMessage()
        );
    }

}
