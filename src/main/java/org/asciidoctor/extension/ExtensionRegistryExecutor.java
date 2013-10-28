package org.asciidoctor.extension;

import java.util.ServiceLoader;

import org.asciidoctor.Asciidoctor;

public class ExtensionRegistryExecutor {

    private static ServiceLoader<ExtensionRegistry> extensionRegistryServiceLoader = ServiceLoader
            .load(ExtensionRegistry.class);

    private Asciidoctor asciidoctor;

    public ExtensionRegistryExecutor(Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    public void registerAllExtensions() {
        for (ExtensionRegistry extensionRegistry : extensionRegistryServiceLoader) {
            extensionRegistry.register(asciidoctor);
        }
    }
}
