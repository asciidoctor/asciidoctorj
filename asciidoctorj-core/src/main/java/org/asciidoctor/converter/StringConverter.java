package org.asciidoctor.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * The base class for simple converters that convert to strings.
 * If the result should be written to a stream or file it is encoded with the {@code UTF-8} encoding.
 */
public abstract class StringConverter extends AbstractConverter<String> {

    public StringConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
    }

    @Override
    public void write(String output, OutputStream out) throws IOException {
        if (output != null) {
            out.write(output.getBytes(Charset.forName("UTF-8")));
        }
    }
}
