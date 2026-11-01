package org.asciidoctor.extension.spi;

import org.asciidoctor.Asciidoctor;

/**
 * Service provider interface to register extensions automatically.
 * <p>
 * The implementations are loaded with {@link java.util.ServiceLoader} when an {@link Asciidoctor} instance is created,
 * and must be declared in {@code META-INF/services/org.asciidoctor.extension.spi.ExtensionRegistry}.
 */
public interface ExtensionRegistry {

    /**
     * Registers extensions on the given instance.
     *
     * @param asciidoctor the instance being created.
     */
    void register(Asciidoctor asciidoctor);

}
