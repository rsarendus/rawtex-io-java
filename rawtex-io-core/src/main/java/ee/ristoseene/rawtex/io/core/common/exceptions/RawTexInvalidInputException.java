package ee.ristoseene.rawtex.io.core.common.exceptions;

import java.io.IOException;

/**
 * Signals that the RAWTEX input data is not valid.
 */
public class RawTexInvalidInputException extends IOException {

    /**
     * Constructs a {@code RawTexInvalidInputException} with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method)
     */
    public RawTexInvalidInputException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code RawTexInvalidInputException} with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method)
     * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method)
     */
    public RawTexInvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@code RawTexInvalidInputException} with the specified cause and a detail message derived from
     * the {@code cause}, if provided.
     *
     * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method)
     */
    public RawTexInvalidInputException(Throwable cause) {
        super(cause);
    }

}
