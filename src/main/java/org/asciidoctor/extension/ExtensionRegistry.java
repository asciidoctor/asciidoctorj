package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;

public interface ExtensionRegistry {

    void register(Asciidoctor asciidoctor);
    
}
