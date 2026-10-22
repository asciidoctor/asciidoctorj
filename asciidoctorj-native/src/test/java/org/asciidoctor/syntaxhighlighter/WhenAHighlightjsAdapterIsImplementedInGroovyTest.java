package org.asciidoctor.syntaxhighlighter;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// NOTE converted from Groovy (the name of the test is kept)
public class WhenAHighlightjsAdapterIsImplementedInGroovyTest {

    public static final String NAME_SYNTAXHIGHLIGHTER = "highlight4J";
    public static final String EMPTY_STRING = "";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private String doc = """
            = Test Document
            :nofooter:

            [source,java]
            ----
            System.out.println("Hello World");
            ----

            [source,go]
            ----
            func main() {
              fmt.Println("Hello World")
            }
            ----
            """;

    @Test
    @DisplayName("should highlight with highlightjs")
    public void shouldHighlightWithHighlightjs() {

        // given:

        asciidoctor.syntaxHighlighterRegistry().register(HighlightJsHighlighter.class, NAME_SYNTAXHIGHLIGHTER);

        // when:
        String html = asciidoctor.convert(doc, Options.builder()
                .safe(SafeMode.UNSAFE)
                .standalone(true)
                .attributes(Attributes.builder().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER).build())
                .build());

        // then:
        Document document = Jsoup.parse(html);
        assertThat(document.head().html()).contains(HighlightJsHighlighter.DOCINFO_HEADER);
        assertThat(strip(document.body().html())).contains(strip(HighlightJsHighlighter.DOCINFO_FOOTER));
        assertThat(document.body().html()).contains("<pre class=\"highlightjs highlight\">");
    }

    private String strip(String s) {
        return s.replace("\n", EMPTY_STRING).replace(" ", EMPTY_STRING);
    }

    @Test
    @DisplayName("should autoregister from extension")
    public void shouldAutoregisterFromExtension() {

        // when:
        asciidoctor.convert(doc, Options.builder()
                .safe(SafeMode.UNSAFE)
                .standalone(true)
                .attributes(Attributes.builder().sourceHighlighter(HighlightJsExtension.NAME_HIGHLIGHTER).build())
                .build());

        // then:
        assertThat(ObservableHighlightJsHighlighter.called).isEqualTo(2);
    }
}
