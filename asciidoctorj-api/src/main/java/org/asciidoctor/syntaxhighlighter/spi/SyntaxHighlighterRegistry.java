package org.asciidoctor.syntaxhighlighter.spi;

import org.asciidoctor.Asciidoctor;

/**
 * Service provider interface to register syntax highlighters automatically.
 * <p>
 * The implementations are loaded with {@link java.util.ServiceLoader} when an {@link Asciidoctor} instance is created,
 * and must be declared in {@code META-INF/services/org.asciidoctor.syntaxhighlighter.spi.SyntaxHighlighterRegistry}.
 */
public interface SyntaxHighlighterRegistry {

    /**
     * Registers syntax highlighters on the given instance.
     *
     * @param asciidoctor the instance being created.
     */
    void register(Asciidoctor asciidoctor);

}
