package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

public class ExtensionRegistryExecutor2 implements ExtensionRegistry {

    static int callCount;

    static Asciidoctor asciidoctor;

    @Override
    public void register(Asciidoctor asciidoctor) {
        callCount++;
        if (ExtensionRegistryExecutor2.asciidoctor != null) {
            throw new IllegalStateException("Must be called by only one Asciidoctor instance");
        }
        ExtensionRegistryExecutor2.asciidoctor = asciidoctor;
    }
}
