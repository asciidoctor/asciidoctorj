package org.asciidoctor.syntaxhighlighter;

public interface SyntaxHighlighterRegistry {

    void register(Class<? extends SyntaxHighlighterAdapter> sourceHighlighter , String... names);

}
