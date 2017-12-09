package org.asciidoctor.converter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A Java converter also has to be an OutputFormatWriter that writes the converted content to a stream.
 * This interface is only called if the caller asks to write the generated content to a file or a stream.
 * Otherwise the generated content, e.g. a {@link String} some other object representation will be returned
 * to the caller.
 */
public interface OutputFormatWriter<T> extends Converter<T> {

    void write(T output, OutputStream out) throws IOException;

}
