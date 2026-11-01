package org.asciidoctor;

/**
 * Thrown when Asciidoctor fails to load or convert a document.
 * <p>
 * Applications should catch this exception rather than the exception of an implementation.
 */
public class AsciidoctorException extends RuntimeException {

    public AsciidoctorException(String message) {
        super(message);
    }

    public AsciidoctorException(Throwable cause) {
        super(cause);
    }

    public AsciidoctorException(String message, Throwable cause) {
        super(message, cause);
    }

}
