package org.asciidoctor.ruby.extension.internal;

import org.asciidoctor.ruby.AsciidoctorJ;
import org.asciidoctor.ruby.extension.spi.ExtensionRegistry;

import java.util.ServiceLoader;

public class ExtensionRegistryExecutor {

    private AsciidoctorJ asciidoctor;

    public ExtensionRegistryExecutor(AsciidoctorJ asciidoctor) {
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
