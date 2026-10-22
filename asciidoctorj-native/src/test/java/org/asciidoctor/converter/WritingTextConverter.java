package org.asciidoctor.converter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@ConverterFor(value = WritingTextConverter.DEFAULT_FORMAT, suffix = ".txt")
public class WritingTextConverter extends TextConverter implements OutputFormatWriter<String> {

    static final String DEFAULT_FORMAT = "writabletext";

    static File targetFile;

    static boolean called = false;

    public WritingTextConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
    }

    @Override
    public void write(String output, OutputStream out) throws IOException {
        called = true;
        out.write(output.getBytes());
    }
}
