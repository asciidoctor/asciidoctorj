package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry

class ExtensionRegistryExecutor2 implements ExtensionRegistry {

    static int callCount

    static Asciidoctor asciidoctor

    @Override
    void register(Asciidoctor asciidoctor) {
        callCount++
        if (this.asciidoctor != null) {
            throw new IllegalStateException('Must be called by only one Asciidoctor instance')
        }
        this.asciidoctor = asciidoctor
    }
}
