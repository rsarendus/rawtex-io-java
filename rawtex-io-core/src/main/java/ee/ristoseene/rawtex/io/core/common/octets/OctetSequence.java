package ee.ristoseene.rawtex.io.core.common.octets;

/**
 * An interface representing a readable sequence of octets.
 * <p>
 * This interface does not refine the general contracts of the {@link Object#equals(Object) equals}
 * and {@link Object#hashCode() hashCode} methods.
 * The result of comparing two objects that implement {@code OctetSequence} is not guaranteed to be defined.
 * Use static {@link #equals(OctetSequence, OctetSequence)} method instead for comparing equality of the contents
 * of two octet sequences.
 */
public interface OctetSequence {

    /**
     * Returns the octet value at the specified index.
     * An index ranges from {@code 0} to {@code length() - 1}.
     *
     * @param index the index of the octet value to be returned
     *
     * @return the specified octet value
     *
     * @throws IndexOutOfBoundsException if {@code index} is negative or not less than {@code length()}
     */
    byte octetAt(int index);

    /**
     * Returns the length of this octet sequence.
     * The length is the number of octets in the sequence.
     *
     * @return the number of octets in this sequence
     */
    int length();

    /**
     * Converts this octet sequence to a byte array.
     *
     * @return a byte array whose length is the length of this octet sequence and
     * whose contents are initialized to contain the sequence of bytes represented by this octet sequence
     */
    default byte[] toByteArray() {
        final int length = length();

        if (length > 0) {
            final byte[] byteArray = new byte[length];

            for (int i = 0; i < length; ++i) {
                byteArray[i] = octetAt(i);
            }

            return byteArray;
        } else {
            return EmptyOctetSequence.ZERO_LENGTH_ARRAY;
        }
    }

    /**
     * The singleton instance of a zero-length octet sequence.
     */
    OctetSequence EMPTY = new EmptyOctetSequence();

    /**
     * Returns {@code true} if the octet sequences are equal to each other and {@code false} otherwise.
     * Octet sequences are considered equal if the arguments represent the same object, if both arguments
     * are {@code null} or if the octet sequences represented by the arguments have equal length and their
     * contents are equal.
     *
     * @param a the first octet sequence to be compared for equality
     * @param b the second octet sequence to be compared for equality
     *
     * @return {@code true} if the arguments are equal to each other and {@code false} otherwise
     */
    static boolean equals(OctetSequence a, OctetSequence b) {
        if (a == b) {
            return true;
        } else if (a == null || b == null) {
            return false;
        }

        final int length = a.length();

        if (b.length() != length) {
            return false;
        }

        for (int i = 0; i < length; ++i) {
            if (a.octetAt(i) != b.octetAt(i)) {
                return false;
            }
        }

        return true;
    }

}
