package org.asciidoctor.internal;

public class AsciidoctorCoreException extends RuntimeException {

    public AsciidoctorCoreException(Throwable e) {
        super(e);
    }

    public AsciidoctorCoreException(String message, Throwable e) {
        super(message, e);
    }

}
