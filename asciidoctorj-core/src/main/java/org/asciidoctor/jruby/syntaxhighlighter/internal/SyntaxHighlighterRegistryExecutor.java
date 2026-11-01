package org.asciidoctor.jruby.syntaxhighlighter.internal;

import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.syntaxhighlighter.spi.SyntaxHighlighterRegistry;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

public class SyntaxHighlighterRegistryExecutor {

    private AsciidoctorJRuby asciidoctor;

    public SyntaxHighlighterRegistryExecutor(AsciidoctorJRuby asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    @SuppressWarnings("deprecation")
    public void registerAllSyntaxHighlighter() {
        Set<Class<?>> registeredClasses = new HashSet<>();
        for (SyntaxHighlighterRegistry syntaxHighlighterRegistry : ServiceLoader.load(SyntaxHighlighterRegistry.class)) {
            registeredClasses.add(syntaxHighlighterRegistry.getClass());
            syntaxHighlighterRegistry.register(asciidoctor);
        }
        // Skip the implementations also declared with the new service
        for (SyntaxHighlighterRegistry syntaxHighlighterRegistry : ServiceLoader.load(org.asciidoctor.jruby.syntaxhighlighter.spi.SyntaxHighlighterRegistry.class)) {
            if (registeredClasses.add(syntaxHighlighterRegistry.getClass())) {
                syntaxHighlighterRegistry.register(asciidoctor);
            }
        }
    }
}
