package org.asciidoctor.syntaxhighlighter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Stream;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class WhenASyntaxHighlighterIsRegisteredTest {

    public static final String NAME_SYNTAXHIGHLIGHTER = "test";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    public static class TestHighlighter implements SyntaxHighlighterAdapter {

        public TestHighlighter(String name, String backend, Map<String, Object> options) {
            if (!NAME_SYNTAXHIGHLIGHTER.equals(name)) {
                throw new IllegalArgumentException("Expected highlighter name " + NAME_SYNTAXHIGHLIGHTER + ", got " + name);
            }
            if (!"html5".equals(backend)) {
                throw new IllegalArgumentException("Expected backend html5, got " + backend);
            }
            Document document = (Document) options.get("document");
            if (!"Test Document".equals(document.getDoctitle())) {
                throw new IllegalArgumentException("Expected doctitle 'Test Document', got '" + document.getDoctitle() + "'");
            }
        }

        @Override
        public boolean hasDocInfo(LocationType location) {
            return true;
        }

        @Override
        public String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
            switch (location) {
                case HEADER:
                    return "<meta name=\"testhighlighter\" content=\"HeaderDocInfo\">";
                case FOOTER:
                    return "<div class=\"testhighlighter\">FooterDocInfo</div>";
                default:
                    throw new IllegalArgumentException("Unexpected location: " + location);
            }
        }
    }

    public static class HeaderOnlyHighlighter extends TestHighlighter {
        public HeaderOnlyHighlighter(String name, String backend, Map<String, Object> options) {
            super(name, backend, options);
        }

        @Override
        public boolean hasDocInfo(LocationType location) {
            return location == LocationType.HEADER;
        }
    }

    public static class FooterOnlyHighlighter extends TestHighlighter {
        public FooterOnlyHighlighter(String name, String backend, Map<String, Object> options) {
            super(name, backend, options);
        }

        @Override
        public boolean hasDocInfo(LocationType location) {
            return location == LocationType.FOOTER;
        }
    }

    public static class HeaderAndFooterHighlighter extends TestHighlighter {
        public HeaderAndFooterHighlighter(String name, String backend, Map<String, Object> options) {
            super(name, backend, options);
        }

        @Override
        public boolean hasDocInfo(LocationType location) {
            return true;
        }
    }

    public static class NoneHighlighter extends TestHighlighter {
        public NoneHighlighter(String name, String backend, Map<String, Object> options) {
            super(name, backend, options);
        }

        @Override
        public boolean hasDocInfo(LocationType location) {
            return false;
        }
    }

    private static final String DOC = """
            = Test Document

            [source,java]
            ----
            System.out.println("Hello World");
            ----""";

    static Stream<Arguments> highlighters() {
        return Stream.of(
                Arguments.of(HeaderOnlyHighlighter.class, true, false),
                Arguments.of(FooterOnlyHighlighter.class, false, true),
                Arguments.of(HeaderAndFooterHighlighter.class, true, true),
                Arguments.of(NoneHighlighter.class, false, false));
    }

    @ParameterizedTest(name = "{0} should create header docinfo {1} and footer docinfo {2}")
    @MethodSource("highlighters")
    public void shouldCreateHeaderAndFooterDocinfo(Class<? extends SyntaxHighlighterAdapter> highlighter,
            boolean expectHeader, boolean expectFooter) {

        // expect:
        asciidoctor.syntaxHighlighterRegistry().register(highlighter, NAME_SYNTAXHIGHLIGHTER);
        String html = asciidoctor.convert(DOC, Options.builder()
                .safe(SafeMode.UNSAFE)
                .standalone(true)
                .attributes(
                        Attributes.builder().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER).build())
                .build());

        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        Element headerDocInfo = doc.head().getElementsByTag("meta").stream()
                .filter(it -> it.attr("name").equals("testhighlighter")).findFirst().orElse(null);

        assertThat(headerDocInfo != null).isEqualTo(expectHeader);

        boolean hasFooterDocInfo = !doc.body().select("div.testhighlighter").isEmpty();

        assertThat(hasFooterDocInfo).isEqualTo(expectFooter);
    }

}
