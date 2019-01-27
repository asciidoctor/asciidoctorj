package org.asciidoctor.converter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.asciidoctorj.converter.spi.ConverterRegistry;

public class TextConverterRegistryExecutor implements ConverterRegistry {

    @Override
    public void register(Asciidoctor asciidoctor) {
        asciidoctor.javaConverterRegistry().register(TextConverter.class, "extensiontext");
    }

}
