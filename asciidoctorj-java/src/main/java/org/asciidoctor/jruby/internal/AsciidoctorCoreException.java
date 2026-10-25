package org.asciidoctor.jruby.internal;

import org.asciidoctor.AsciidoctorException;

/**
 * The exception thrown when a conversion fails (kept in its original package for compatibility with AsciidoctorJ 3,
 * applications should catch {@link AsciidoctorException}).
 */
public class AsciidoctorCoreException extends AsciidoctorException {

    public AsciidoctorCoreException(Throwable e) {
        super(e);
    }

    public AsciidoctorCoreException(String message, Throwable e) {
        super(message, e);
    }
}
