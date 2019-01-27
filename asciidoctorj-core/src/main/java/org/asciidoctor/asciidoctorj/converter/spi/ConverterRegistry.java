package org.asciidoctor.asciidoctorj.converter.spi;

import org.asciidoctor.Asciidoctor;

public interface ConverterRegistry {
    void register(Asciidoctor asciidoctor);
}
