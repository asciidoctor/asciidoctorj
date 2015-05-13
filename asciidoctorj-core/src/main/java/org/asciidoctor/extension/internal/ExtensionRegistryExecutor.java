package org.asciidoctor.extension.internal;

import java.util.ServiceLoader;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.spi.ExtensionRegistry;

public class ExtensionRegistryExecutor {

    private Asciidoctor asciidoctor;

    public ExtensionRegistryExecutor(Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    public void registerAllExtensions() {
        ServiceLoader<ExtensionRegistry> extensionRegistryServiceLoader = ServiceLoader
                .load(ExtensionRegistry.class);
        
        for (ExtensionRegistry extensionRegistry : extensionRegistryServiceLoader) {
            extensionRegistry.register(asciidoctor);
        }
    }
}
