package org.asciidoctor.converter;

import org.asciidoctor.ast.ContentNode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@ConverterFor(format = "42")
public class ObjectConverter
        extends AbstractConverter<ObjectConverter.ObjectConverterResult>
        implements OutputFormatWriter<ObjectConverter.ObjectConverterResult> {

    public static final String BACKEND = "42";
    public static final Integer FIXED_RESULT = Integer.valueOf(BACKEND);

    public ObjectConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
    }

    @Override
    public ObjectConverterResult convert(ContentNode node, String transform, Map<Object, Object> opts) {
        return new ObjectConverterResult(FIXED_RESULT);
    }

    @Override
    public void write(ObjectConverterResult output, OutputStream out) throws IOException {
        out.write(output.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static class ObjectConverterResult {
        private final int value;

        public ObjectConverterResult(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public String toString() {
            return String.valueOf(value);
        }
    }
}
