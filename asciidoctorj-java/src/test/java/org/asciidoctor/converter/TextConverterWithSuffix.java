package org.asciidoctor.converter;


import java.util.Map;

// ConverterFor.format should have precedence over ConverterFor.value
// ConverterFor.suffix should not be used if setOutfileSuffix is called in constructor
@ConverterFor(format = TextConverterWithSuffix.DEFAULT_FORMAT, suffix = ".txt")
public class TextConverterWithSuffix extends TextConverter {

    public static final String DEFAULT_FORMAT = "annotatedtext";

    public TextConverterWithSuffix(String backend, Map<String, Object> opts) {
        super(backend, opts);
        setOutfileSuffix(".text");
    }
}
