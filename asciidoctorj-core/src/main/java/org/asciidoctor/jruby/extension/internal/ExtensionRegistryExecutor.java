package org.asciidoctor.jruby.extension.internal;

import org.asciidoctor.extension.spi.ExtensionRegistry;
import org.asciidoctor.jruby.AsciidoctorJRuby;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

public class ExtensionRegistryExecutor {

    private AsciidoctorJRuby asciidoctor;

    public ExtensionRegistryExecutor(AsciidoctorJRuby asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    @SuppressWarnings("deprecation")
    public void registerAllExtensions() {
        Set<Class<?>> registeredClasses = new HashSet<>();
        for (ExtensionRegistry extensionRegistry : ServiceLoader.load(ExtensionRegistry.class)) {
            registeredClasses.add(extensionRegistry.getClass());
            extensionRegistry.register(asciidoctor);
        }
        // Skip the implementations also declared with the new service
        for (ExtensionRegistry extensionRegistry : ServiceLoader.load(org.asciidoctor.jruby.extension.spi.ExtensionRegistry.class)) {
            if (registeredClasses.add(extensionRegistry.getClass())) {
                extensionRegistry.register(asciidoctor);
            }
        }
    }
}
