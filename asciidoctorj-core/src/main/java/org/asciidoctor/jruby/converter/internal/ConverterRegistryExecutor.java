package org.asciidoctor.jruby.converter.internal;

import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.jruby.converter.spi.ConverterRegistry;

import java.util.ServiceLoader;

public class ConverterRegistryExecutor {

    private AsciidoctorJRuby asciidoctor;

    public ConverterRegistryExecutor(AsciidoctorJRuby asciidoctor) {
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
