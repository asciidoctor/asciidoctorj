package org.asciidoctor.ruby.converter.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ruby.converter.spi.ConverterRegistry;

import java.util.ServiceLoader;

public class ConverterRegistryExecutor {

    private Asciidoctor asciidoctor;

    public ConverterRegistryExecutor(Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    public void registerAllConverters() {
        ServiceLoader<ConverterRegistry> converterRegistryServiceLoader = ServiceLoader
                .load(ConverterRegistry.class);

        for (ConverterRegistry converterRegistry : converterRegistryServiceLoader) {
            converterRegistry.register(asciidoctor);
        }
    }
}
