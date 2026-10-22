package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.Asciidoctor

class HighlightJsExtension implements org.asciidoctor.jruby.syntaxhighlighter.spi.SyntaxHighlighterRegistry {

    static String NAME_HIGHLIGHTER = 'autoregisteredhighlightjs'

    @Override
    void register(Asciidoctor asciidoctor) {
        asciidoctor.syntaxHighlighterRegistry()
            .register(ObservableHighlightJsHighlighter, NAME_HIGHLIGHTER)
    }

}
