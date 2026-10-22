package org.asciidoctor.jruby.converter.internal;

import org.asciidoctor.converter.spi.ConverterRegistry;
import org.asciidoctor.jruby.AsciidoctorJRuby;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

public class ConverterRegistryExecutor {

    private AsciidoctorJRuby asciidoctor;

    public ConverterRegistryExecutor(AsciidoctorJRuby asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    @SuppressWarnings("deprecation")
    public void registerAllConverters() {
        Set<Class<?>> registeredClasses = new HashSet<>();
        for (ConverterRegistry converterRegistry : ServiceLoader.load(ConverterRegistry.class)) {
            registeredClasses.add(converterRegistry.getClass());
            converterRegistry.register(asciidoctor);
        }
        // Skip the implementations also declared with the new service
        for (ConverterRegistry converterRegistry : ServiceLoader.load(org.asciidoctor.jruby.converter.spi.ConverterRegistry.class)) {
            if (registeredClasses.add(converterRegistry.getClass())) {
                converterRegistry.register(asciidoctor);
            }
        }
    }
}
