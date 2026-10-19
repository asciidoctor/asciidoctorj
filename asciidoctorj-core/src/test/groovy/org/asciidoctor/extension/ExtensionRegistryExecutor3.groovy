package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry

class ExtensionRegistryExecutor3 implements ExtensionRegistry {

    static int callCount

    @Override
    void register(Asciidoctor asciidoctor) {
        callCount++
    }
}
