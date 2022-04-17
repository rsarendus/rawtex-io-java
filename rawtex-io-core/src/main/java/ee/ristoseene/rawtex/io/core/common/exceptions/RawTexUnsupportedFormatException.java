package ee.ristoseene.rawtex.io.core.common.exceptions;

import java.io.IOException;

/**
 * Signals that an unexpected input or unsupported construct was encountered while parsing RAWTEX input.
 */
public class RawTexUnsupportedFormatException extends IOException {

    /**
     * Constructs a {@code RawTexUnsupportedFormatException} with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method)
     */
    public RawTexUnsupportedFormatException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code RawTexUnsupportedFormatException} with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method)
     * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method)
     */
    public RawTexUnsupportedFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@code RawTexUnsupportedFormatException} with the specified cause and a detail message derived from
     * the {@code cause}, if provided.
     *
     * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method)
     */
    public RawTexUnsupportedFormatException(Throwable cause) {
        super(cause);
    }

}
