package ee.ristoseene.rawtex.io.core.in;

import ee.ristoseene.rawtex.io.core.common.exceptions.RawTexUnsupportedFormatException;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;
import ee.ristoseene.rawtex.io.core.common.format.RawTexFormatIndicator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class RawTexLoaderTest {

    @Test
    void testConstructorFailsForMissingFormatLoaderFactory() {
        NullPointerException caughtException = Assertions.assertThrows(
                NullPointerException.class,
                () -> new RawTexLoader<>(null)
        );

        Assertions.assertEquals("Format loader factory not provided", caughtException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("namedLoadTesters")
    void testLoadFailsOnEmptyInput(LoadCall loadTester) {
        RawTexFormatLoaderFactory<DummyFormat> versionLoaderFactory = mockFormatLoaderFactory();

        RawTexLoader<DummyFormat> loader = new RawTexLoader<>(versionLoaderFactory);
        RawTexLoadTargetFactory<DummyFormat> loadTargetFactory = mockLoadTargetFactory();
        byte[] input = {};

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verifyNoInteractions(versionLoaderFactory, loadTargetFactory);
    }

    static Stream<LoadCall> namedLoadTesters() {
        return Stream.of(
                new NamedLoadTester("load(byte[], ...)", RawTexLoader::load),
                new NamedLoadTester("load(byte[], int, int, ...)", (loader, input, loadTargetFactory) -> {
                    byte[] extendedInput = new byte[7 + input.length + 3];
                    System.arraycopy(input, 0, extendedInput, 7, input.length);
                    loader.load(extendedInput, 7, input.length, loadTargetFactory);
                }),
                new NamedLoadTester("load(InputStream, ...)", (loader, input, loadTargetFactory) -> {
                    try (InputStream in = new ByteArrayInputStream(input)) {
                        loader.load(in, loadTargetFactory);
                    }
                })
        );
    }

    @ParameterizedTest(name = "\"{0}\" {1}")
    @MethodSource("namedLoadTestersWithInvalidFormatIndicators")
    void testLoadFailsOnInvalidFormatIndicator(String formatIndicator, LoadCall loadTester) {
        RawTexFormatLoaderFactory<DummyFormat> versionLoaderFactory = mockFormatLoaderFactory();

        RawTexLoader<DummyFormat> loader = new RawTexLoader<>(versionLoaderFactory);
        RawTexLoadTargetFactory<DummyFormat> loadTargetFactory = mockLoadTargetFactory();
        byte[] input = formatIndicator.getBytes(StandardCharsets.UTF_8);

        RawTexUnsupportedFormatException caughtException = Assertions.assertThrows(
                RawTexUnsupportedFormatException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertEquals("Unrecognized format indicator", caughtException.getMessage());
        Mockito.verifyNoInteractions(versionLoaderFactory, loadTargetFactory);
    }

    static Stream<Arguments> namedLoadTestersWithInvalidFormatIndicators() {
        return Stream.of("RaWtEx", "rAwTeX", "RawTex", "Raw Tex", "raw tex", "RAW\0", "tex", "abc", "\0")
                .flatMap(formatIndicator -> namedLoadTesters()
                        .map(loadCall -> Arguments.of(formatIndicator, loadCall))
                );
    }

    @ParameterizedTest(name = "\"{0}\" {1}")
    @MethodSource("namedLoadTestersWithIncompleteFormatIndicators")
    void testLoadFailsOnIncompleteFormatIndicator(String formatIndicator, LoadCall loadTester) {
        RawTexFormatLoaderFactory<DummyFormat> versionLoaderFactory = mockFormatLoaderFactory();

        RawTexLoader<DummyFormat> loader = new RawTexLoader<>(versionLoaderFactory);
        RawTexLoadTargetFactory<DummyFormat> loadTargetFactory = mockLoadTargetFactory();
        byte[] input = formatIndicator.substring(0, formatIndicator.length() - 1).getBytes(StandardCharsets.US_ASCII);

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verifyNoInteractions(versionLoaderFactory, loadTargetFactory);
    }

    static Stream<Arguments> namedLoadTestersWithIncompleteFormatIndicators() {
        return Stream.of(RawTexFormatIndicator.values())
                .flatMap(formatIndicator -> IntStream.range(1, formatIndicator.length())
                        .mapToObj(endIndex -> formatIndicator.toString().substring(0, endIndex))
                        .flatMap(formatIndicatorString -> namedLoadTesters()
                                .map(loadCall -> Arguments.of(formatIndicatorString, loadCall))
                        )
                );
    }

    @ParameterizedTest(name = "\"{0}\" {1}")
    @MethodSource("namedLoadTestersWithFormatIndicators")
    void testLoadFailsOnIncompleteVersion(String formatIndicator, LoadCall loadTester) {
        RawTexFormatLoaderFactory<DummyFormat> versionLoaderFactory = mockFormatLoaderFactory();

        RawTexLoader<DummyFormat> loader = new RawTexLoader<>(versionLoaderFactory);
        RawTexLoadTargetFactory<DummyFormat> loadTargetFactory = mockLoadTargetFactory();
        byte[] input = (formatIndicator + '\0').getBytes(StandardCharsets.US_ASCII);

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verifyNoInteractions(versionLoaderFactory, loadTargetFactory);
    }

    @ParameterizedTest(name = "\"{0}\" {1}")
    @MethodSource("namedLoadTestersWithFormatIndicators")
    void testLoadFailsWhenVersionLoaderFactoryThrowsException(String formatIndicator, LoadCall loadTester) throws IOException {
        RawTexFormatLoaderFactory<DummyFormat> versionLoaderFactory = mockFormatLoaderFactory();
        RawTexUnsupportedFormatException exceptionToThrow = new RawTexUnsupportedFormatException("A message");
        Mockito.doThrow(exceptionToThrow).when(versionLoaderFactory).create(Mockito.any(Endianness.class), Mockito.anyInt(), Mockito.anyInt());
        Endianness expectedEndianness = RawTexFormatIndicator.of(formatIndicator).endianness;

        RawTexLoader<DummyFormat> loader = new RawTexLoader<>(versionLoaderFactory);
        RawTexLoadTargetFactory<DummyFormat> loadTargetFactory = mockLoadTargetFactory();
        byte[] input = (formatIndicator + '\u0003' + '\u0007').getBytes(StandardCharsets.US_ASCII);

        RawTexUnsupportedFormatException caughtException = Assertions.assertThrows(
                RawTexUnsupportedFormatException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertSame(exceptionToThrow, caughtException);
        Mockito.verify(versionLoaderFactory, Mockito.times(1)).create(expectedEndianness, 3, 7);
        Mockito.verifyNoMoreInteractions(versionLoaderFactory);
        Mockito.verifyNoInteractions(loadTargetFactory);
    }

    @ParameterizedTest(name = "\"{0}\" {1}")
    @MethodSource("namedLoadTestersWithFormatIndicators")
    void testLoadFailsWhenVersionLoaderFactoryReturnsNull(String formatIndicator, LoadCall loadTester) throws IOException {
        RawTexFormatLoaderFactory<DummyFormat> versionLoaderFactory = mockFormatLoaderFactory();
        Mockito.doReturn(null).when(versionLoaderFactory).create(Mockito.any(Endianness.class), Mockito.anyInt(), Mockito.anyInt());
        Endianness expectedEndianness = RawTexFormatIndicator.of(formatIndicator).endianness;

        RawTexLoader<DummyFormat> loader = new RawTexLoader<>(versionLoaderFactory);
        RawTexLoadTargetFactory<DummyFormat> loadTargetFactory = mockLoadTargetFactory();
        byte[] input = (formatIndicator + '\u0003' + '\u0007').getBytes(StandardCharsets.US_ASCII);

        RawTexUnsupportedFormatException caughtException = Assertions.assertThrows(
                RawTexUnsupportedFormatException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertEquals(
                String.format("Unsupported format: \"%s\" (%s), version 3.7", formatIndicator, expectedEndianness),
                caughtException.getMessage()
        );
        Mockito.verify(versionLoaderFactory, Mockito.times(1)).create(expectedEndianness, 3, 7);
        Mockito.verifyNoMoreInteractions(versionLoaderFactory);
        Mockito.verifyNoInteractions(loadTargetFactory);
    }

    @ParameterizedTest(name = "\"{0}\" {1}")
    @MethodSource("namedLoadTestersWithFormatIndicators")
    void testLoadFailsWhenVersionLoaderThrowsException(String formatIndicator, LoadCall loadTester) throws IOException {
        RawTexFormatLoaderFactory<DummyFormat> versionLoaderFactory = mockFormatLoaderFactory();
        RawTexFormatLoader<DummyFormat> versionLoader = mockFormatLoader();
        Mockito.doReturn(versionLoader).when(versionLoaderFactory).create(Mockito.any(Endianness.class), Mockito.anyInt(), Mockito.anyInt());
        IOException exceptionToThrow = new IOException("A message");
        Mockito.doThrow(exceptionToThrow).when(versionLoader).load(Mockito.any(InputStream.class), Mockito.any());
        Endianness expectedEndianness = RawTexFormatIndicator.of(formatIndicator).endianness;

        RawTexLoader<DummyFormat> loader = new RawTexLoader<>(versionLoaderFactory);
        RawTexLoadTargetFactory<DummyFormat> loadTargetFactory = mockLoadTargetFactory();
        byte[] input = (formatIndicator + '\u0003' + '\u0007').getBytes(StandardCharsets.US_ASCII);

        IOException caughtException = Assertions.assertThrows(
                IOException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertSame(exceptionToThrow, caughtException);
        Mockito.verify(versionLoaderFactory, Mockito.times(1)).create(expectedEndianness, 3, 7);
        Mockito.verify(versionLoader, Mockito.times(1)).load(Mockito.any(InputStream.class), Mockito.same(loadTargetFactory));
        Mockito.verifyNoMoreInteractions(versionLoaderFactory, versionLoader);
        Mockito.verifyNoInteractions(loadTargetFactory);
    }

    @ParameterizedTest(name = "\"{0}\" {1}")
    @MethodSource("namedLoadTestersWithFormatIndicators")
    void testLoadSucceeds(String formatIndicator, LoadCall loadTester) throws IOException {
        RawTexFormatLoaderFactory<DummyFormat> versionLoaderFactory = mockFormatLoaderFactory();
        RawTexFormatLoader<DummyFormat> versionLoader = mockFormatLoader();
        Mockito.doReturn(versionLoader).when(versionLoaderFactory).create(Mockito.any(Endianness.class), Mockito.anyInt(), Mockito.anyInt());
        Endianness expectedEndianness = RawTexFormatIndicator.of(formatIndicator).endianness;

        RawTexLoader<DummyFormat> loader = new RawTexLoader<>(versionLoaderFactory);
        RawTexLoadTargetFactory<DummyFormat> loadTargetFactory = mockLoadTargetFactory();
        byte[] input = (formatIndicator + '\u0003' + '\u0007').getBytes(StandardCharsets.US_ASCII);

        loadTester.callLoad(loader, input, loadTargetFactory);

        Mockito.verify(versionLoaderFactory, Mockito.times(1)).create(expectedEndianness, 3, 7);
        Mockito.verify(versionLoader, Mockito.times(1)).load(Mockito.any(InputStream.class), Mockito.same(loadTargetFactory));
        Mockito.verifyNoMoreInteractions(versionLoaderFactory, versionLoader);
        Mockito.verifyNoInteractions(loadTargetFactory);
    }

    static Stream<Arguments> namedLoadTestersWithFormatIndicators() {
        return Stream.of(RawTexFormatIndicator.values())
                .map(RawTexFormatIndicator::toString)
                .flatMap(formatIndicator -> namedLoadTesters()
                        .map(loadCall -> Arguments.of(formatIndicator, loadCall))
                );
    }

    interface LoadCall {
        void callLoad(RawTexLoader<DummyFormat> loader, byte[] input, RawTexLoadTargetFactory<DummyFormat> loadTargetFactory) throws IOException;
    }

    static class NamedLoadTester implements LoadCall {

        private final String name;
        private final LoadCall loadCall;

        public NamedLoadTester(String name, LoadCall loadCall) {
            this.name = Objects.requireNonNull(name);
            this.loadCall = Objects.requireNonNull(loadCall);
        }

        public void callLoad(RawTexLoader<DummyFormat> loader, byte[] input, RawTexLoadTargetFactory<DummyFormat> loadTargetFactory) throws IOException {
            loadCall.callLoad(loader, input, loadTargetFactory);
        }

        @Override
        public String toString() {
            return name;
        }

    }

    @SuppressWarnings("unchecked")
    static RawTexFormatLoaderFactory<DummyFormat> mockFormatLoaderFactory() {
        return (RawTexFormatLoaderFactory<DummyFormat>) Mockito.mock(RawTexFormatLoaderFactory.class);
    }

    @SuppressWarnings("unchecked")
    static RawTexLoadTargetFactory<DummyFormat> mockLoadTargetFactory() {
        return (RawTexLoadTargetFactory<DummyFormat>) Mockito.mock(RawTexLoadTargetFactory.class);
    }

    @SuppressWarnings("unchecked")
    static RawTexFormatLoader<DummyFormat> mockFormatLoader() {
        return (RawTexFormatLoader<DummyFormat>) Mockito.mock(RawTexFormatLoader.class);
    }

    static class DummyFormat {}

}
