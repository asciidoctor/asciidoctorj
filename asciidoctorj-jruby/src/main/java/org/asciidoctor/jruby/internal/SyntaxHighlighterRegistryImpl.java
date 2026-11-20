package org.asciidoctor.jruby.internal;

import org.asciidoctor.jruby.syntaxhighlighter.internal.SyntaxHighlighterProxy;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterRegistry;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;

public class SyntaxHighlighterRegistryImpl implements SyntaxHighlighterRegistry {

    private Ruby rubyRuntime;

    private JRubyAsciidoctor asciidoctor;

    public SyntaxHighlighterRegistryImpl(JRubyAsciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
        this.rubyRuntime = asciidoctor.getRubyRuntime();
    }

    @Override
    public void register(final Class<? extends SyntaxHighlighterAdapter> highlighterClass, String... names) {

        RubyClass clazz = SyntaxHighlighterProxy.register(asciidoctor, highlighterClass);

        getSyntaxHighlighterFactory()
            .callMethod("register", clazz, rubyRuntime.newString(names[0]));
    }

    private RubyModule getSyntaxHighlighterFactory() {
        return rubyRuntime.getModule("Asciidoctor")
            .getModule("SyntaxHighlighter");
    }

}