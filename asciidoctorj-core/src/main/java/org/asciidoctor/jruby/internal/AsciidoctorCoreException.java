package org.asciidoctor.jruby.internal;

/**
 * The exception thrown when a conversion fails (kept in its original package for compatibility with AsciidoctorJ 3,
 * since the API doesn't define an exception).
 */
public class AsciidoctorCoreException extends RuntimeException {

    public AsciidoctorCoreException(Throwable e) {
        super(e);
    }

    public AsciidoctorCoreException(String message, Throwable e) {
        super(message, e);
    }
}
