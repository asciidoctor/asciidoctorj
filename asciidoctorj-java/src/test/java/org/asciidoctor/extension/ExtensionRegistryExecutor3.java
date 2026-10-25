package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

@SuppressWarnings("deprecation")
public class ExtensionRegistryExecutor3 implements ExtensionRegistry {

    static int callCount;

    @Override
    public void register(Asciidoctor asciidoctor) {
        callCount++;
    }
}
