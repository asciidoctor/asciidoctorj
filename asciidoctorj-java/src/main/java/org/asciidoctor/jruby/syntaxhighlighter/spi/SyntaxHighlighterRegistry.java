package org.asciidoctor.jruby.syntaxhighlighter.spi;

import org.asciidoctor.Asciidoctor;

public interface SyntaxHighlighterRegistry {
    void register(Asciidoctor asciidoctor);
}
