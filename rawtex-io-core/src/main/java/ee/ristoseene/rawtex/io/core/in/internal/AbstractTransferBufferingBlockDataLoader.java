package ee.ristoseene.rawtex.io.core.in.internal;

import ee.ristoseene.rawtex.io.core.common.data.TransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.common.format.BlockSize;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;

import java.util.Objects;

/**
 * An abstract base class for data loaders capable of loading data with specific endianness and block size,
 * and capable of using temporary byte arrays for data transfers.
 * <p>
 * This base class encapsulates no direct mutable state and supports thread-safe implementations
 * as long as the implementation of the {@link TransferBufferAllocator} assigned to the instances
 * of this class is thread-safe by itself.
 */
public abstract class AbstractTransferBufferingBlockDataLoader extends AbstractBlockDataLoader {

    /**
     * An allocator for temporary byte arrays for arbitrary data transfers.
     */
    protected final TransferBufferAllocator transferBufferAllocator;

    /**
     * Creates an {@code AbstractTransferBufferingBlockDataLoader} with the specified
     * endianness, block size and transfer buffer allocator.
     *
     * @param endianness endianness of the data to load
     * @param blockSize block size of the data to load
     * @param transferBufferAllocator an allocator for temporary byte arrays for arbitrary data transfers
     *
     * @throws NullPointerException if {@code endianness}, {@code blockSize} or {@code transferBufferAllocator}
     * is {@code null}
     */
    protected AbstractTransferBufferingBlockDataLoader(Endianness endianness, BlockSize blockSize, TransferBufferAllocator transferBufferAllocator) {
        super(endianness, blockSize);

        this.transferBufferAllocator = Objects.requireNonNull(transferBufferAllocator, "Transfer buffer allocator not provided");
    }

    /**
     * A convenience method for allocating a byte array with the specified minimum length
     * and maximum usable range, and ensuring that the obtained array is not {@code null}.
     * <p>
     * <b>A byte array obtained via this method, must be de-allocated after its use via
     * {@link #transferBufferAllocator}'s {@link TransferBufferAllocator#free(byte[]) free}
     * method!</b>
     *
     * @param minimumLength the minimum length of the requested byte array
     * @param maximumLength the maximum usable range in the requested byte array
     *
     * @return a byte array obtained from the {@link #transferBufferAllocator}
     *
     * @throws NullPointerException if {@link #transferBufferAllocator}'s
     * {@link TransferBufferAllocator#allocate(int, int) allocate} method
     * returns {@code null}
     *
     * @see TransferBufferAllocator#allocate(int, int) 
     */
    protected byte[] allocateTransferBuffer(int minimumLength, int maximumLength) {
        final byte[] transferBuffer = transferBufferAllocator.allocate(minimumLength, maximumLength);

        if (transferBuffer == null) {
            throw new NullPointerException("Transfer buffer is missing");
        }

        return transferBuffer;
    }

    /**
     * A convenience method for allocating a byte array with the minimum length of at least
     * the block size of the data to load and the maximum usable range equal to the specified
     * multiple of the block size of the data to load, and ensuring that the obtained array
     * is not {@code null}.
     * <p>
     * <b>A byte array obtained via this method, must be de-allocated after its use via
     * {@link #transferBufferAllocator}'s {@link TransferBufferAllocator#free(byte[]) free}
     * method!</b>
     *
     * @param maximumBlockCount the maximum number of data blocks that could fit into
     *                          the requested byte array
     *
     * @return a byte array obtained from the {@link #transferBufferAllocator}
     *
     * @throws NullPointerException if {@link #transferBufferAllocator}'s
     * {@link TransferBufferAllocator#allocate(int, int) allocate} method
     * returns {@code null}
     *
     * @see TransferBufferAllocator#allocate(int, int)
     */
    protected byte[] allocateTransferBufferForBlockWiseTransfers(int maximumBlockCount) {
        return allocateTransferBuffer(blockSize.octets, blockSize.multipleOf(maximumBlockCount));
    }

    /**
     * Ensures that the length of the specified byte array is not less than the specified minimum,
     * and returns the length of the array.
     *
     * @param transferBuffer the byte array whose length to validate and return
     * @param minimumLength the minimum length that the specified byte array must have
     *
     * @return the length of the specified byte array
     *
     * @throws IllegalStateException if the length of {@code transferBuffer} is less than {@code minimumLength}
     */
    protected static int validateTransferBufferAndReturnLength(byte[] transferBuffer, int minimumLength) {
        final int transferBufferLength = transferBuffer.length;

        if (transferBufferLength < minimumLength) {
            throw new IllegalStateException("Transfer buffer too short: " + transferBufferLength);
        }

        return transferBufferLength;
    }

    /**
     * Ensures that the specified byte array could fit at least one block of data,
     * and returns the maximum number of data blocks that the array could fit.
     *
     * @param transferBuffer the byte array whose length to validate
     *
     * @return the maximum number of data blocks that the array could fit
     *
     * @throws IllegalStateException if the length of {@code transferBuffer} is less than {@code minimumLength}
     */
    protected int validateTransferBufferForBlockWiseTransfersAndReturnBlockCount(byte[] transferBuffer) {
        return blockSize.quotientOf(validateTransferBufferAndReturnLength(transferBuffer, blockSize.octets));
    }

}
