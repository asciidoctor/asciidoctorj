package org.asciidoctor.converter.spi;

import org.asciidoctor.Asciidoctor;

/**
 * Service provider interface to register converters automatically.
 * <p>
 * The implementations are loaded with {@link java.util.ServiceLoader} when an {@link Asciidoctor} instance is created,
 * and must be declared in {@code META-INF/services/org.asciidoctor.converter.spi.ConverterRegistry}.
 */
public interface ConverterRegistry {

    /**
     * Registers converters on the given instance.
     *
     * @param asciidoctor the instance being created.
     */
    void register(Asciidoctor asciidoctor);

}
