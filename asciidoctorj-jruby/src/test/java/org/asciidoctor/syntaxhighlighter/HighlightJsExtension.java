package org.asciidoctor.syntaxhighlighter;

import org.asciidoctor.Asciidoctor;

public class HighlightJsExtension implements org.asciidoctor.jruby.syntaxhighlighter.spi.SyntaxHighlighterRegistry {

    static String NAME_HIGHLIGHTER = "autoregisteredhighlightjs";

    @Override
    public void register(Asciidoctor asciidoctor) {
        asciidoctor.getSyntaxHighlighterRegistry()
            .register(ObservableHighlightJsHighlighter.class, NAME_HIGHLIGHTER);
    }

}
