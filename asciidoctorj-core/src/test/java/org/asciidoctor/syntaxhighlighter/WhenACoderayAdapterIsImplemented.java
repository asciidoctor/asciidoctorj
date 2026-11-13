package org.asciidoctor.syntaxhighlighter;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class WhenACoderayAdapterIsImplemented {

    public static final String NAME_SYNTAXHIGHLIGHTER = "coderay4J";

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Disabled("until the latest release also puts the link to the CSS into the header")
    @Test
    void should_highlight_with_coderay() {
        String doc = """
                = Test Document
                :nofooter:

                [source,java]
                ----
                System.out.println("Hello World"); // <1>
                ----

                [source,go,linenums]
                ----
                func main() {
                  fmt.Println("Hello World") // <1>
                }
                ----
                """;

        asciidoctor.syntaxHighlighterRegistry().register(CoderayHighlighter.class, NAME_SYNTAXHIGHLIGHTER);

        String htmlJava = asciidoctor.convert(doc, Options.builder()
                .safe(SafeMode.UNSAFE)
                .standalone(true)
                .attributes(Attributes.builder()
                        .sourceHighlighter(NAME_SYNTAXHIGHLIGHTER)
                        .linkCss(true)
                        .build())
                .build());

        String htmlRuby = asciidoctor.convert(doc, Options.builder()
                .safe(SafeMode.UNSAFE)
                .standalone(true)
                .attributes(Attributes.builder()
                        .sourceHighlighter("coderay")
                        .linkCss(true)
                        .build())
                .build());

        assertThat(htmlJava).isEqualTo(htmlRuby);
    }
}
