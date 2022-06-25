package ee.ristoseene.rawtex.io.core.common.format;

/**
 * An enum representing the supported range of block sizes.
 */
public enum BlockSize {

    /**
     * The singleton instance representing the block size of 1 octet long
     * (a block representing an 8-bit value).
     */
    OCTETS_1(1) {

        @Override
        public final int multipleOf(int value) {
            return value;
        }

        @Override
        public final long multipleOf(long value) {
            return value;
        }

        @Override
        public final int quotientOf(int value) {
            return value;
        }

        @Override
        public final long quotientOf(long value) {
            return value;
        }

        @Override
        public final int remainderOf(int value) {
            return 0;
        }

        @Override
        public final long remainderOf(long value) {
            return 0L;
        }

        @Override
        public final int truncate(int value) {
            return value;
        }

        @Override
        public final long truncate(long value) {
            return value;
        }

    },

    /**
     * The singleton instance representing the block size of 2 octets long
     * (a block representing a 16-bit value).
     */
    OCTETS_2(2) {

        @Override
        public final int multipleOf(int value) {
            return (value << 1);
        }

        @Override
        public final long multipleOf(long value) {
            return (value << 1);
        }

        @Override
        public final int quotientOf(int value) {
            return (value >>> 1);
        }

        @Override
        public final long quotientOf(long value) {
            return (value >>> 1);
        }

        @Override
        public final int remainderOf(int value) {
            return (value & 0b1);
        }

        @Override
        public final long remainderOf(long value) {
            return (value & 0b1L);
        }

        @Override
        public final int truncate(int value) {
            return (value & (~0b1));
        }

        @Override
        public final long truncate(long value) {
            return (value & (~0b1L));
        }

    },

    /**
     * The singleton instance representing the block size of 4 octets long
     * (a block representing a 32-bit value).
     */
    OCTETS_4(4) {

        @Override
        public final int multipleOf(int value) {
            return (value << 2);
        }

        @Override
        public final long multipleOf(long value) {
            return (value << 2);
        }

        @Override
        public final int quotientOf(int value) {
            return (value >>> 2);
        }

        @Override
        public final long quotientOf(long value) {
            return (value >>> 2);
        }

        @Override
        public final int remainderOf(int value) {
            return (value & 0b11);
        }

        @Override
        public final long remainderOf(long value) {
            return (value & 0b11L);
        }

        @Override
        public final int truncate(int value) {
            return (value & (~0b11));
        }

        @Override
        public final long truncate(long value) {
            return (value & (~0b11L));
        }

    },

    /**
     * The singleton instance representing the block size of 8 octets long
     * (a block representing a 64-bit value).
     */
    OCTETS_8(8) {

        @Override
        public final int multipleOf(int value) {
            return (value << 3);
        }

        @Override
        public final long multipleOf(long value) {
            return (value << 3);
        }

        @Override
        public final int quotientOf(int value) {
            return (value >>> 3);
        }

        @Override
        public final long quotientOf(long value) {
            return (value >>> 3);
        }

        @Override
        public final int remainderOf(int value) {
            return (value & 0b111);
        }

        @Override
        public final long remainderOf(long value) {
            return (value & 0b111L);
        }

        @Override
        public final int truncate(int value) {
            return (value & (~0b111));
        }

        @Override
        public final long truncate(long value) {
            return (value & (~0b111L));
        }

    };

    /**
     * The size of the block in number of octets.
     */
    public final int octets;

    BlockSize(int octets) {
        this.octets = octets;
    }

    /**
     * Calculates the product of a non-negative {@code int} value multiplied by the block size
     * ({@code value * blockSize.octets}).
     * <p>
     * <b>This operation is undefined for negative values!</b>
     * <p>
     * <b>This operation is undefined for values greater than
     * {@link Integer#MAX_VALUE} {@code / blockSize.octets}!</b>
     *
     * @param value a non-negative {@code int} value to multiply by the block size
     *
     * @return the product of {@code value} multiplied by the block size
     */
    public abstract int multipleOf(int value);

    /**
     * Calculates the product of a non-negative {@code long} value multiplied by the block size
     * ({@code value * blockSize.octets}).
     * <p>
     * <b>This operation is undefined for negative values!</b>
     * <p>
     * <b>This operation is undefined for values greater than
     * {@link Long#MAX_VALUE} {@code / blockSize.octets}!</b>
     *
     * @param value a non-negative {@code long} value to multiply by the block size
     *
     * @return the product of {@code value} multiplied by the block size
     */
    public abstract long multipleOf(long value);

    /**
     * Calculates the quotient of a non-negative {@code int} value divided by the block size
     * ({@code value / blockSize.octets}).
     * <p>
     * <b>This operation is undefined for negative values!</b>
     *
     * @param value a non-negative {@code int} value to divide by the block size
     *
     * @return the quotient of {@code value} divided by the block size
     */
    public abstract int quotientOf(int value);

    /**
     * Calculates the quotient of a non-negative {@code long} value divided by the block size
     * ({@code value / blockSize.octets}).
     * <p>
     * <b>This operation is undefined for negative values!</b>
     *
     * @param value a non-negative {@code long} value to divide by the block size
     *
     * @return the quotient of {@code value} divided by the block size
     */
    public abstract long quotientOf(long value);

    /**
     * Calculates the remainder of a non-negative {@code int} value divided by the block size
     * ({@code value % blockSize.octets}).
     * <p>
     * <b>This operation is undefined for negative values!</b>
     *
     * @param value a non-negative {@code int} value to divide by the block size
     *
     * @return the remainder of {@code value} divided by the block size
     */
    public abstract int remainderOf(int value);

    /**
     * Calculates the remainder of a non-negative {@code long} value divided by the block size
     * ({@code value % blockSize.octets}).
     * <p>
     * <b>This operation is undefined for negative values!</b>
     *
     * @param value a non-negative {@code long} value to divide by the block size
     *
     * @return the remainder of {@code value} divided by the block size
     */
    public abstract long remainderOf(long value);

    /**
     * Truncates a non-negative {@code int} value into a multiple of the block size
     * ({@code value - (value % blockSize.octets)}).
     * <p>
     * <b>This operation is undefined for negative values!</b>
     *
     * @param value a non-negative {@code int} value to truncate
     *
     * @return the truncation of {@code value}, a multiple of the block size
     */
    public abstract int truncate(int value);

    /**
     * Truncates a non-negative {@code long} value into a multiple of the block size
     * ({@code value - (value % blockSize.octets)}).
     * <p>
     * <b>This operation is undefined for negative values!</b>
     *
     * @param value a non-negative {@code long} value to truncate
     *
     * @return the truncation of {@code value}, a multiple of the block size
     */
    public abstract long truncate(long value);

    /**
     * Obtains an instance of {@link BlockSize} with the specified number of octets.
     *
     * @param octets the number of octets of the requested block size
     *
     * @return the block size of the specified number of octets, not {@code null}
     *
     * @throws IllegalArgumentException if the specified number of octets
     * does not represent a valid block size
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
     * Obtains an instance of {@link BlockSize} representing the specified power of two.
     *
     * @param exponent the power of two exponent of the requested block size
     *
     * @return the block size of the specified power of two, not {@code null}
     *
     * @throws IllegalArgumentException if the specified power of two exponent
     * does not represent a valid block size
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
