package ee.ristoseene.rawtex.io.core.common.format;

public enum BlockSize {

    OCTETS_1(1) {

        @Override
        public final int multipleOf(int value) {
            return value;
        }

        @Override
        public final int quotientOf(int value) {
            return value;
        }

        @Override
        public final int remainderOf(int value) {
            return 0;
        }

        @Override
        public final int truncate(int value) {
            return value;
        }

    },

    OCTETS_2(2) {

        @Override
        public final int multipleOf(int value) {
            return (value << 1);
        }

        @Override
        public final int quotientOf(int value) {
            return (value >>> 1);
        }

        @Override
        public final int remainderOf(int value) {
            return (value & 0b1);
        }

        @Override
        public final int truncate(int value) {
            return (value & (~0b1));
        }

    },

    OCTETS_4(4) {

        @Override
        public final int multipleOf(int value) {
            return (value << 2);
        }

        @Override
        public final int quotientOf(int value) {
            return (value >>> 2);
        }

        @Override
        public final int remainderOf(int value) {
            return (value & 0b11);
        }

        @Override
        public final int truncate(int value) {
            return (value & (~0b11));
        }

    },

    OCTETS_8(8) {

        @Override
        public final int multipleOf(int value) {
            return (value << 3);
        }

        @Override
        public final int quotientOf(int value) {
            return (value >>> 3);
        }

        @Override
        public final int remainderOf(int value) {
            return (value & 0b111);
        }

        @Override
        public final int truncate(int value) {
            return (value & (~0b111));
        }

    };

    /**
     * Block size in number of octets.
     */
    public final int octets;

    BlockSize(int octets) {
        this.octets = octets;
    }

    /**
     * Calculates the product of a non-negative integer multiplied by block size
     * ({@code value * blockSizeInOctets}).
     * <p>
     * <b>NB:</b> This operation is undefined for negative integers!
     *
     * @param value a non-negative integer to multiply by block size
     *
     * @return the product of {@code value} multiplied by block size
     */
    public abstract int multipleOf(int value);

    /**
     * Calculates the quotient of a non-negative integer divided by block size
     * ({@code value / blockSizeInOctets}).
     * <p>
     * <b>NB:</b> This operation is undefined for negative integers!
     *
     * @param value a non-negative integer to divide by block size
     *
     * @return the quotient of {@code value} divided by block size
     */
    public abstract int quotientOf(int value);

    /**
     * Calculates the remainder of a non-negative integer divided by block size
     * ({@code value % blockSizeInOctets}).
     * <p>
     * <b>NB:</b> This operation is undefined for negative integers!
     *
     * @param value a non-negative integer to divide by block size
     *
     * @return the remainder of {@code value} divided by block size
     */
    public abstract int remainderOf(int value);

    /**
     * Truncates a non-negative integer
     * <p>
     * <b>NB:</b> This operation is undefined for negative integers!
     *
     * @param value a non-negative integer to truncate
     *
     * @return the truncation of {@code value}
     */
    public abstract int truncate(int value);

    /**
     * Returns an instance of {@link BlockSize} representing the specified number of octets.
     *
     * @param octets the number of octets of the requested {@link BlockSize}
     *
     * @return an instance of {@link BlockSize} representing the specified number of octets
     *
     * @throws IllegalArgumentException if the specified number of octets does not represent a valid {@link BlockSize}
     */
    public static BlockSize of(int octets) {
        switch (octets) {

            case 1:
                return OCTETS_1;

            case 2:
                return OCTETS_2;

            case 4:
                return OCTETS_4;

            case 8:
                return OCTETS_8;

            default:
                throw new IllegalArgumentException("Invalid block size: " + octets);

        }
    }

    /**
     * Returns an instance of {@link BlockSize} representing the specified power of two.
     *
     * @param exponent the power of two exponent of the requested {@link BlockSize}
     *
     * @return an instance of {@link BlockSize} representing the specified power of two
     *
     * @throws IllegalArgumentException if the specified power of two exponent does not represent a valid {@link BlockSize}
     */
    public static BlockSize fromPowerOfTwo(int exponent) {
        switch (exponent) {

            case 0b00:
                return OCTETS_1;

            case 0b01:
                return OCTETS_2;

            case 0b10:
                return OCTETS_4;

            case 0b11:
                return OCTETS_8;

            default:
                throw new IllegalArgumentException("Invalid block size exponent: " + exponent);

        }
    }

}
