package ee.ristoseene.rawtex.io.core.in;

import ee.ristoseene.rawtex.io.core.common.exceptions.RawTexUnsupportedFormatException;
import ee.ristoseene.rawtex.io.core.common.format.RawTexFormatIndicator;
import ee.ristoseene.rawtex.io.core.common.internal.CommonIO;
import ee.ristoseene.rawtex.io.core.in.internal.ArraySource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * The main entry-point of loading {@code RAWTEX} images.
 * The loader parses the {@code RAWTEX} header (format indicator + major and minor version)
 * and then delegates the loading process to a specific {@link RawTexFormatLoader} via
 * {@link RawTexFormatLoaderFactory}.
 *
 * @param <IF> the type of objects representing specific image formats
 */
public class RawTexLoader<IF> {

    private final RawTexFormatLoaderFactory<IF> formatLoaderFactory;

    /**
     * Constructs a {@code RawTexLoader} with the specified format loader factory.
     *
     * @param formatLoaderFactory a factory providing specific instances of {@link RawTexFormatLoader}
     *                            for loading specific {@code RAWTEX} binary formats
     *
     * @see RawTexFormatLoader
     * @see RawTexFormatLoaderFactory
     */
    public RawTexLoader(RawTexFormatLoaderFactory<IF> formatLoaderFactory) {
        this.formatLoaderFactory = Objects.requireNonNull(formatLoaderFactory, "Format loader factory not provided");
    }

    /**
     * Loads a {@code RAWTEX} image from the specified byte array.
     *
     * @param in a byte array containing the binary formatted {@code RAWTEX} image to load
     * @param loadTargetFactory a factory capable of providing an appropriate {@link RawTexLoadTarget}
     *                          for accepting the image data of the {@code RAWTEX} image to load
     *
     * @throws IOException if an I/O or a parsing error occurs
     *
     * @see RawTexLoadTarget
     * @see RawTexLoadTargetFactory
     */
    public void load(byte[] in, RawTexLoadTargetFactory<IF> loadTargetFactory) throws IOException {
        load(new ArraySource(in), loadTargetFactory);
    }

    /**
     * Loads a {@code RAWTEX} image from a portion of the specified byte array.
     *
     * @param in a byte array containing the binary formatted {@code RAWTEX} image to load
     * @param offset offset of the input data in the specified byte array {@code in}
     * @param length length of the input data in the specified byte array {@code in}
     * @param loadTargetFactory a factory capable of providing an appropriate {@link RawTexLoadTarget}
     *                          for accepting the image data of the {@code RAWTEX} image to load
     *
     * @throws IOException if an I/O or a parsing error occurs
     *
     * @see RawTexLoadTarget
     * @see RawTexLoadTargetFactory
     */
    public void load(byte[] in, int offset, int length, RawTexLoadTargetFactory<IF> loadTargetFactory) throws IOException {
        load(new ArraySource(in, offset, length), loadTargetFactory);
    }

    /**
     * Loads a {@code RAWTEX} image from the specified input stream.
     *
     * @param in a stream to load the binary formatted {@code RAWTEX} image from
     * @param loadTargetFactory a factory capable of providing an appropriate {@link RawTexLoadTarget}
     *                          for accepting the image data of the {@code RAWTEX} image to load
     *
     * @throws IOException if an I/O or a parsing error occurs
     *
     * @see RawTexLoadTarget
     * @see RawTexLoadTargetFactory
     */
    public void load(InputStream in, RawTexLoadTargetFactory<IF> loadTargetFactory) throws IOException {
        getFormatLoader(in).load(in, loadTargetFactory);
    }

    private RawTexFormatLoader<IF> getFormatLoader(InputStream in) throws IOException {
        final RawTexFormatIndicator formatIndicator = parseFormatIndicator(in);

        final int majorVersion = CommonIO.readOctet(in) & 0xff;
        final int minorVersion = CommonIO.readOctet(in) & 0xff;

        final RawTexFormatLoader<IF> versionLoader = formatLoaderFactory
                .create(formatIndicator.endianness, majorVersion, minorVersion);

        if (versionLoader != null) {
            return versionLoader;
        }

        throw new RawTexUnsupportedFormatException(String.format(
                "Unsupported format: \"%s\" (%s), version %d.%d",
                formatIndicator, formatIndicator.endianness, majorVersion, minorVersion
        ));
    }

    private static RawTexFormatIndicator parseFormatIndicator(InputStream in) throws IOException {
        final byte firstOctet = CommonIO.readOctet(in);
        RawTexFormatIndicator formatIndicator;

        if (RawTexFormatIndicator.LITTLE_ENDIAN.octetAt(0) == firstOctet) {
            formatIndicator = RawTexFormatIndicator.LITTLE_ENDIAN;
        } else if (RawTexFormatIndicator.BIG_ENDIAN.octetAt(0) == firstOctet) {
            formatIndicator = RawTexFormatIndicator.BIG_ENDIAN;
        } else {
            throw unrecognizedFormatIndicatorException();
        }

        final int expectedFormatIndicatorLength = formatIndicator.length();

        for (int i = 1; i < expectedFormatIndicatorLength; ++i) {
            if (formatIndicator.octetAt(i) != CommonIO.readOctet(in)) {
                throw unrecognizedFormatIndicatorException();
            }
        }

        return formatIndicator;
    }

    private static RawTexUnsupportedFormatException unrecognizedFormatIndicatorException() {
        return new RawTexUnsupportedFormatException("Unrecognized format indicator");
    }

}
