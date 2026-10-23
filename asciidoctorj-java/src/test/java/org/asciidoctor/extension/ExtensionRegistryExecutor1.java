package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

public class ExtensionRegistryExecutor1 implements ExtensionRegistry {

    static int callCount;

    static Asciidoctor asciidoctor;

    @Override
    public void register(Asciidoctor asciidoctor) {
        callCount++;
        if (ExtensionRegistryExecutor1.asciidoctor != null) {
            throw new IllegalStateException("Must be called by only one Asciidoctor instance");
        }
        ExtensionRegistryExecutor1.asciidoctor = asciidoctor;
    }
}
