package org.asciidoctor.core;

/**
 * Raised when the processor fails to load or convert a document.
 */
public class AsciidoctorException extends RuntimeException {
    public AsciidoctorException(String message) {
        super(message);
    }

    public AsciidoctorException(String message, Throwable cause) {
        super(message, cause);
    }
}
