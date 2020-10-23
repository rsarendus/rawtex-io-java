package ee.ristoseene.rawtex.in;

import ee.ristoseene.rawtex.common.exceptions.RawTexUnsupportedFormatException;
import ee.ristoseene.rawtex.common.internal.Endianness;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class RawTexLoaderTest {

    @Test
    public void testConstructorFailsForMissingDataLoaderFactory() {
        NullPointerException caughtException = Assertions.assertThrows(
                NullPointerException.class,
                () -> new RawTexLoader(null)
        );

        Assertions.assertEquals("Data loader factory not provided", caughtException.getMessage());
    }

    @Test
    public void testConstructorFailsForNullVersionLoader() {
        RawTexDataLoaderFactory dataLoaderFactory = Mockito.mock(RawTexDataLoaderFactory.class);

        NullPointerException caughtException = Assertions.assertThrows(
                NullPointerException.class,
                () -> new RawTexLoader(dataLoaderFactory, (RawTexVersionLoader) null)
        );

        Assertions.assertEquals("Version loader cannot be null", caughtException.getMessage());
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("namedLoadTesters")
    public void testLoadFailsOnEmptyInput(LoadCall loadTester, String name) {
        RawTexDataLoaderFactory dataLoaderFactory = Mockito.mock(RawTexDataLoaderFactory.class);
        RawTexVersionLoader versionLoader = Mockito.mock(RawTexVersionLoader.class);

        RawTexLoader loader = new RawTexLoader(dataLoaderFactory, versionLoader);
        RawTexLoadTargetFactory loadTargetFactory = Mockito.mock(RawTexLoadTargetFactory.class);
        byte[] input = {};

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verifyNoInteractions(dataLoaderFactory, versionLoader, loadTargetFactory);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("namedLoadTesters")
    public void testLoadFailsOnInvalidInput(LoadCall loadTester, String name) {
        RawTexDataLoaderFactory dataLoaderFactory = Mockito.mock(RawTexDataLoaderFactory.class);
        RawTexVersionLoader versionLoader = Mockito.mock(RawTexVersionLoader.class);

        RawTexLoader loader = new RawTexLoader(dataLoaderFactory, versionLoader);
        RawTexLoadTargetFactory loadTargetFactory = Mockito.mock(RawTexLoadTargetFactory.class);
        byte[] input = {'i', 'n', 'v', 'a', 'l', 'i', 'd'};

        RawTexUnsupportedFormatException caughtException = Assertions.assertThrows(
                RawTexUnsupportedFormatException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertEquals("Unrecognized format indicator", caughtException.getMessage());
        Mockito.verifyNoInteractions(dataLoaderFactory, versionLoader, loadTargetFactory);
    }

    private static Stream<Arguments> namedLoadTesters() {
        return Stream.of(
                Arguments.of((LoadCall) RawTexLoader::load, "load(byte[], ...)"),
                Arguments.of((LoadCall) (loader, input, loadTargetFactory) -> {
                        byte[] extendedInput = new byte[7 + input.length + 3];
                        System.arraycopy(input, 0, extendedInput, 7, input.length);
                        loader.load(extendedInput, 7, input.length, loadTargetFactory);
                }, "load(byte[], int, int, ...)"),
                Arguments.of((LoadCall) (loader, input, loadTargetFactory) -> {
                    try (InputStream in = new ByteArrayInputStream(input)) {
                        loader.load(in, loadTargetFactory);
                    }
                }, "load(InputStream, ...)")
        );
    }

    @ParameterizedTest(name = "{0} {2}")
    @MethodSource("namedLoadTestersWithFormatIndicators")
    public void testLoadFailsOnIncompleteFormatIndicator(String formatIndicator, LoadCall loadTester, String name) {
        RawTexDataLoaderFactory dataLoaderFactory = Mockito.mock(RawTexDataLoaderFactory.class);
        RawTexVersionLoader versionLoader = Mockito.mock(RawTexVersionLoader.class);

        RawTexLoader loader = new RawTexLoader(dataLoaderFactory, versionLoader);
        RawTexLoadTargetFactory loadTargetFactory = Mockito.mock(RawTexLoadTargetFactory.class);
        byte[] input = formatIndicator.substring(0, formatIndicator.length() - 1).getBytes(StandardCharsets.US_ASCII);

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verifyNoInteractions(dataLoaderFactory, versionLoader, loadTargetFactory);
    }

    @ParameterizedTest(name = "{0} {2}")
    @MethodSource("namedLoadTestersWithFormatIndicators")
    public void testLoadFailsOnIncompleteVersion(String formatIndicator, LoadCall loadTester, String name) {
        RawTexDataLoaderFactory dataLoaderFactory = Mockito.mock(RawTexDataLoaderFactory.class);
        RawTexVersionLoader versionLoader = Mockito.mock(RawTexVersionLoader.class);

        RawTexLoader loader = new RawTexLoader(dataLoaderFactory, versionLoader);
        RawTexLoadTargetFactory loadTargetFactory = Mockito.mock(RawTexLoadTargetFactory.class);
        byte[] input = (formatIndicator + '\0').getBytes(StandardCharsets.US_ASCII);

        EOFException caughtException = Assertions.assertThrows(
                EOFException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertEquals("Unexpected end of input", caughtException.getMessage());
        Mockito.verifyNoInteractions(dataLoaderFactory, versionLoader, loadTargetFactory);
    }

    @ParameterizedTest(name = "{0} {2}")
    @MethodSource("namedLoadTestersWithFormatIndicators")
    public void testLoadFailsWhenVersionNotSupported(String formatIndicator, LoadCall loadTester, String name) {
        RawTexDataLoaderFactory dataLoaderFactory = Mockito.mock(RawTexDataLoaderFactory.class);
        RawTexVersionLoader versionLoader = Mockito.mock(RawTexVersionLoader.class);
        Mockito.doReturn(false).when(versionLoader).supportsVersion(Mockito.anyInt(), Mockito.anyInt());

        RawTexLoader loader = new RawTexLoader(dataLoaderFactory, versionLoader);
        RawTexLoadTargetFactory loadTargetFactory = Mockito.mock(RawTexLoadTargetFactory.class);
        byte[] input = (formatIndicator + '\u0003' + '\u0007').getBytes(StandardCharsets.US_ASCII);

        RawTexUnsupportedFormatException caughtException = Assertions.assertThrows(
                RawTexUnsupportedFormatException.class,
                () -> loadTester.callLoad(loader, input, loadTargetFactory)
        );

        Assertions.assertEquals("Unsupported version: 3.7", caughtException.getMessage());
        Mockito.verify(versionLoader, Mockito.times(1)).supportsVersion(3, 7);
        Mockito.verifyNoMoreInteractions(versionLoader);
        Mockito.verifyNoInteractions(dataLoaderFactory, loadTargetFactory);
    }

    @ParameterizedTest(name = "{0} {2}")
    @MethodSource("namedLoadTestersWithFormatIndicators")
    public void testLoadSucceeds(String formatIndicator, LoadCall loadTester, String name) throws IOException {
        RawTexDataLoaderFactory dataLoaderFactory = Mockito.mock(RawTexDataLoaderFactory.class);
        RawTexVersionLoader versionLoader1 = Mockito.mock(RawTexVersionLoader.class);
        Mockito.doReturn(false).when(versionLoader1).supportsVersion(Mockito.anyInt(), Mockito.anyInt());
        RawTexVersionLoader versionLoader2 = Mockito.mock(RawTexVersionLoader.class);
        Mockito.doReturn(true).when(versionLoader2).supportsVersion(Mockito.anyInt(), Mockito.anyInt());
        RawTexVersionLoader versionLoader3 = Mockito.mock(RawTexVersionLoader.class);
        Endianness expectedEndianness = "RAWTEX".equals(formatIndicator) ? Endianness.BIG_ENDIAN : Endianness.LITTLE_ENDIAN;

        RawTexLoader loader = new RawTexLoader(dataLoaderFactory, versionLoader1, versionLoader2, versionLoader3);
        RawTexLoadTargetFactory loadTargetFactory = Mockito.mock(RawTexLoadTargetFactory.class);
        byte[] input = (formatIndicator + '\u0003' + '\u0007').getBytes(StandardCharsets.US_ASCII);

        loadTester.callLoad(loader, input, loadTargetFactory);

        Mockito.verify(versionLoader1, Mockito.times(1)).supportsVersion(3, 7);
        Mockito.verify(versionLoader2, Mockito.times(1)).supportsVersion(3, 7);
        Mockito.verify(versionLoader2, Mockito.times(1))
                .load(Mockito.any(InputStream.class), Mockito.same(expectedEndianness), Mockito.same(loadTargetFactory), Mockito.same(dataLoaderFactory));
        Mockito.verifyNoMoreInteractions(versionLoader1, versionLoader2);
        Mockito.verifyNoInteractions(dataLoaderFactory, versionLoader3, loadTargetFactory);
    }

    private static Stream<Arguments> namedLoadTestersWithFormatIndicators() {
        return Stream.of("RAWTEX", "rawtex")
                .flatMap(formatIndicator -> namedLoadTesters()
                        .map(arguments -> new ArrayList<>(Arrays.asList(arguments.get())))
                        .peek(list -> list.add(0, formatIndicator))
                        .map(list -> Arguments.of(list.toArray()))
                );
    }

    interface LoadCall {
        void callLoad(RawTexLoader loader, byte[] input, RawTexLoadTargetFactory loadTargetFactory) throws IOException;
    }

}
