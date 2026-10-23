package org.asciidoctor.converter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.converter.spi.ConverterRegistry;

public class TextConverterRegistryExecutor implements ConverterRegistry {

    @Override
    public void register(Asciidoctor asciidoctor) {
        asciidoctor.javaConverterRegistry().register(TextConverter.class, "extensiontext");
    }

}
