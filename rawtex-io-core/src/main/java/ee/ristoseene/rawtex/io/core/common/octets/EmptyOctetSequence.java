package ee.ristoseene.rawtex.io.core.common.octets;

/**
 * A special implementation of {@link OctetSequence} representing a sequence of zero octets.
 */
final class EmptyOctetSequence implements OctetSequence {

    /**
     * An instance of zero-length byte array.
     */
    public static final byte[] ZERO_LENGTH_ARRAY = new byte[0];

    /**
     * Throws {@link IndexOutOfBoundsException}.
     *
     * @param index the index of the octet value to be returned
     *
     * @return nothing
     *
     * @throws IndexOutOfBoundsException always
     */
    @Override
    public byte octetAt(int index) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
    }

    /**
     * Returns {@code 0}.
     *
     * @return {@code 0}
     */
    @Override
    public int length() {
        return 0;
    }

    /**
     * Returns a zero-length byte array.
     *
     * @return a zero-length byte array
     */
    @Override
    public byte[] toByteArray() {
        return ZERO_LENGTH_ARRAY;
    }

    /**
     * Constructs an empty octet sequence.
     */
    EmptyOctetSequence() {}

}
