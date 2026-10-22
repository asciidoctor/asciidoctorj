package org.asciidoctor.jruby.syntaxhighlighter.internal;

import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;
import org.asciidoctor.jruby.syntaxhighlighter.spi.SyntaxHighlighterRegistry;

import java.util.ServiceLoader;

public class SyntaxHighlighterRegistryExecutor {

    private AsciidoctorJRuby asciidoctor;

    public SyntaxHighlighterRegistryExecutor(AsciidoctorJRuby asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    public void registerAllSyntaxHighlighter() {
        ServiceLoader<SyntaxHighlighterRegistry> serviceLoader = ServiceLoader
                .load(SyntaxHighlighterRegistry.class);
        
        for (SyntaxHighlighterRegistry extensionRegistry : serviceLoader) {
            extensionRegistry.register(asciidoctor);
        }
    }
}
