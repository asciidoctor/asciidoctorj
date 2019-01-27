package org.asciidoctor.ruby.converter.spi;

import org.asciidoctor.Asciidoctor;

public interface ConverterRegistry {
    void register(Asciidoctor asciidoctor);
}
