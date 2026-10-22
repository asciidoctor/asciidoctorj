package org.asciidoctor.core.api.syntaxhighlighter;

import org.asciidoctor.core.api.CoreAsciidoctor;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterRegistry;

/**
 * The {@link SyntaxHighlighterRegistry} of the AsciidoctorJ API: the syntax highlighters are registered with the
 * Asciidoctor instance.
 */
public class SyntaxHighlighterRegistryImpl implements SyntaxHighlighterRegistry {
    private final CoreAsciidoctor asciidoctor;

    public SyntaxHighlighterRegistryImpl(CoreAsciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    @Override
    public void register(Class<? extends SyntaxHighlighterAdapter> sourceHighlighter, String... names) {
        for (String name : names) {
            asciidoctor.getSyntaxHighlighters().put(name, sourceHighlighter);
        }
    }
}
