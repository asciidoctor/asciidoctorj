package org.asciidoctor.converter;

import java.io.File;
import java.io.OutputStream;

/**
 * A WritingConverter allows to convert Asciidoctor content to a binary representation.
 * In contrast to a simple {@link Converter} the convert method may return any content.
 * If the result should be written to a file the method {@link #write(T, java.io.OutputStream)} will be called.
 * Otherwise the result will be returned to the caller.
 */
public interface WritingConverter<T> extends Converter<T> {

    void write(T output, OutputStream out);

}
