package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.Asciidoctor;
//tag::include-syntaxhighlighter-registry[]
import org.asciidoctor.jruby.syntaxhighlighter.spi.SyntaxHighlighterRegistry;

public class HighlightJsExtension implements SyntaxHighlighterRegistry { // <1>
    @Override
    public void register(Asciidoctor asciidoctor) { // <2>
        asciidoctor.syntaxHighlighterRegistry()     // <3>
            .register(HighlightJsHighlighter.class, "autoloadedHighlightJs");
    }
}
//end::include-syntaxhighlighter-registry[]
