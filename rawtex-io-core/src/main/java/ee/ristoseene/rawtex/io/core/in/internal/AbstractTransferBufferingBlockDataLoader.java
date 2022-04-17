package ee.ristoseene.rawtex.io.core.in.internal;

import ee.ristoseene.rawtex.io.core.common.data.TransferBufferAllocator;
import ee.ristoseene.rawtex.io.core.common.format.BlockSize;
import ee.ristoseene.rawtex.io.core.common.format.Endianness;

import java.util.Objects;

public abstract class AbstractTransferBufferingBlockDataLoader extends AbstractBlockDataLoader {

    protected final TransferBufferAllocator transferBufferAllocator;

    protected AbstractTransferBufferingBlockDataLoader(Endianness endianness, BlockSize blockSize, TransferBufferAllocator transferBufferAllocator) {
        super(endianness, blockSize);

        this.transferBufferAllocator = Objects.requireNonNull(transferBufferAllocator, "Transfer buffer allocator not provided");
    }

    protected byte[] allocateTransferBuffer(int minimumLength, int maximumLength) {
        final byte[] transferBuffer = transferBufferAllocator.allocate(minimumLength, maximumLength);

        if (transferBuffer == null) {
            throw new NullPointerException("Transfer buffer is missing");
        }

        return transferBuffer;
    }

    protected byte[] allocateTransferBufferForBlockWiseTransfers(int blockCount) {
        return allocateTransferBuffer(blockSize.octets, blockSize.multipleOf(blockCount));
    }

    protected static int validateTransferBufferAndReturnLength(byte[] transferBuffer, int minimumLength) {
        final int transferBufferLength = transferBuffer.length;

        if (transferBufferLength < minimumLength) {
            throw new IllegalStateException("Transfer buffer too short: " + transferBufferLength);
        }

        return transferBufferLength;
    }

    protected int validateTransferBufferForBlockWiseTransfersAndReturnBlockCount(byte[] transferBuffer) {
        return blockSize.quotientOf(validateTransferBufferAndReturnLength(transferBuffer, blockSize.octets));
    }

}
