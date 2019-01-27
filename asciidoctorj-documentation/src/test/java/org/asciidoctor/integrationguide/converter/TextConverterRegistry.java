package org.asciidoctor.integrationguide.converter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.asciidoctorj.converter.spi.ConverterRegistry;

public class TextConverterRegistry implements ConverterRegistry {
    @Override
    public void register(Asciidoctor asciidoctor) {

        asciidoctor.javaConverterRegistry().register(TextConverter.class);

    }
}
