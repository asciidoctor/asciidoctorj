package org.asciidoctor.syntaxhighlighter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenAServerSideSyntaxHighlighterIsRegisteredTest {

    public static final String NAME_SYNTAXHIGHLIGHTER = "test";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    public static class TestHighlighter implements SyntaxHighlighterAdapter, Highlighter {

        @Override
        public boolean hasDocInfo(LocationType location) {
            return false;
        }

        @Override
        public String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
            throw new IllegalArgumentException("Unexpected call");
        }

        @Override
        public HighlightResult highlight(Block node, String source, String lang, Map<String, Object> options) {
            return new HighlightResult(
                    source.replace("System", "<span class=\"class-name\">System</span>"),
                    1);
        }
    }


    @Test
    @DisplayName("should highlight source")
    public void shouldHighlightSource() {

        // given:
        String doc = """
                = Test Document

                [source,java]
                ----
                System.out.println("Hello World");
                ----
                """;

        asciidoctor.syntaxHighlighterRegistry().register(TestHighlighter.class, NAME_SYNTAXHIGHLIGHTER);

        // when:
        String html = asciidoctor.convert(doc, Options.builder()
                .safe(SafeMode.UNSAFE)
                .standalone(false)
                .attributes(Attributes.builder().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER).build())
                .build());

        org.jsoup.nodes.Document document = Jsoup.parse(html);

        // then:
        Elements elements = document.select("div.listingblock div.content pre.test.highlight code span.class-name");
        assertThat(elements.size()).isEqualTo(1);
        assertThat(elements.text()).isEqualTo("System");
    }
}
