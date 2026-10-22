package org.asciidoctor.jruby.extension.internal;

import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

import java.util.ServiceLoader;

public class ExtensionRegistryExecutor {

    private AsciidoctorJRuby asciidoctor;

    public ExtensionRegistryExecutor(AsciidoctorJRuby asciidoctor) {
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
