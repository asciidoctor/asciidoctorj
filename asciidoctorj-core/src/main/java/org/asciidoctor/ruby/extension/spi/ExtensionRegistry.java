package org.asciidoctor.ruby.extension.spi;

import org.asciidoctor.Asciidoctor;

public interface ExtensionRegistry {
    void register(Asciidoctor asciidoctor);
}
