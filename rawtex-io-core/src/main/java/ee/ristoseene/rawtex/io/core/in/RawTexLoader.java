package ee.ristoseene.rawtex.io.core.in;

import ee.ristoseene.rawtex.io.core.common.exceptions.RawTexUnsupportedFormatException;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;
import ee.ristoseene.rawtex.io.core.common.internal.CommonIO;
import ee.ristoseene.rawtex.io.core.common.internal.RawTexHeader;
import ee.ristoseene.rawtex.io.core.in.internal.ArraySource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * A configurable loader for loading RAWTEX images.
 */
public class RawTexLoader {

    private final RawTexDataLoaderFactory dataLoaderFactory;
    private final RawTexVersionLoader[] versionLoaders;

    /**
     * Constructs a {@code RawTexLoader} with the specified data loader factory and version loaders.
     *
     * @param dataLoaderFactory a factory providing specific instances of {@link RawTexDataLoader} for loading RAWTEX image data
     * @param versionLoaders a list of loaders capable of loading specific versions of binary formatted RAWTEX images
     *
     * @see RawTexDataLoader
     * @see RawTexDataLoaderFactory
     * @see RawTexVersionLoader
     */
    public RawTexLoader(RawTexDataLoaderFactory dataLoaderFactory, RawTexVersionLoader... versionLoaders) {
        this.dataLoaderFactory = Objects.requireNonNull(dataLoaderFactory, "Data loader factory not provided");
        for (RawTexVersionLoader versionLoader : versionLoaders) {
            Objects.requireNonNull(versionLoader, "Version loader cannot be null");
        }
        this.versionLoaders = versionLoaders.clone();
    }

    /**
     * Loads a RAWTEX image from the specified byte array.
     *
     * @param in a byte array containing the binary formatted RAWTEX image to load
     * @param loadTargetFactory a factory capable of providing an appropriate {@link RawTexLoadTarget} for accepting the image data of the RAWTEX image to load
     *
     * @throws IOException if an I/O or a parsing error occurs
     *
     * @see RawTexLoadTarget
     * @see RawTexLoadTargetFactory
     */
    public void load(byte[] in, RawTexLoadTargetFactory loadTargetFactory) throws IOException {
        load(new ArraySource(in), loadTargetFactory);
    }

    /**
     * Loads a RAWTEX image from a portion of the specified byte array.
     *
     * @param in a byte array containing the binary formatted RAWTEX image to load
     * @param offset offset of the input data in the specified byte array
     * @param length length of the input data in the specified byte array
     * @param loadTargetFactory a factory capable of providing an appropriate {@link RawTexLoadTarget} for accepting the image data of the RAWTEX image to load
     *
     * @throws IOException if an I/O or a parsing error occurs
     *
     * @see RawTexLoadTarget
     * @see RawTexLoadTargetFactory
     */
    public void load(byte[] in, int offset, int length, RawTexLoadTargetFactory loadTargetFactory) throws IOException {
        load(new ArraySource(in, offset, length), loadTargetFactory);
    }

    /**
     * Loads a RAWTEX image from the specified input stream.
     *
     * @param in a stream to load the binary formatted RAWTEX image from
     * @param loadTargetFactory a factory capable of providing an appropriate {@link RawTexLoadTarget} for accepting the image data of the RAWTEX image to load
     *
     * @throws IOException if an I/O or a parsing error occurs
     *
     * @see RawTexLoadTarget
     * @see RawTexLoadTargetFactory
     */
    public void load(InputStream in, RawTexLoadTargetFactory loadTargetFactory) throws IOException {
        final Endianness endianness = RawTexHeader.parseEndianness(in);

        final int majorVersion = CommonIO.readOctet(in) & 0xff;
        final int minorVersion = CommonIO.readOctet(in) & 0xff;

        for (RawTexVersionLoader versionLoader : versionLoaders) {
            if (versionLoader.supportsVersion(majorVersion, minorVersion)) {
                versionLoader.load(in, endianness, loadTargetFactory, dataLoaderFactory);
                return;
            }
        }

        throw new RawTexUnsupportedFormatException(String.format(
                "Unsupported version: %d.%d", majorVersion, minorVersion
        ));
    }

}
