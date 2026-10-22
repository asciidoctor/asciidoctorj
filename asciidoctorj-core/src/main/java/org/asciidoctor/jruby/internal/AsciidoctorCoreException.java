package org.asciidoctor.jruby.internal;

import org.asciidoctor.AsciidoctorException;

public class AsciidoctorCoreException extends AsciidoctorException {

    public AsciidoctorCoreException(Throwable e) {
        super(e);
    }

    public AsciidoctorCoreException(String message, Throwable e) {
        super(message, e);
    }

}
