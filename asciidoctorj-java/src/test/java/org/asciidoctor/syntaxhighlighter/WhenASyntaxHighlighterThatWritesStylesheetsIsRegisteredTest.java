package org.asciidoctor.syntaxhighlighter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class WhenASyntaxHighlighterThatWritesStylesheetsIsRegisteredTest {

    public static final String NAME_SYNTAXHIGHLIGHTER = "test";

    public static final String CONTENT_CSS = "FOOBAR";
    public static final String FILENAME_CSS = "highlighter.css";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @TempDir
    public File tempDir;

    public static class TestHighlighter implements SyntaxHighlighterAdapter, StylesheetWriter {

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
            return false;
        }

        @Override
        public String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
            throw new IllegalArgumentException("Unexpected call");
        }

        @Override
        public boolean isWriteStylesheet(Document doc) {
            return doc.findBy(Map.of("context", ":listing")).stream()
                    .anyMatch(it -> "java".equals(it.getAttributes().get("language")));
        }

        @Override
        public void writeStylesheet(Document doc, File toDir) {
            try {
                Files.writeString(new File(toDir, FILENAME_CSS).toPath(), CONTENT_CSS);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }


    @ParameterizedTest(name = "should write css to disk for {0}: {1}")
    @CsvSource({
            "java, true",
            "go, false"
    })
    public void shouldWriteCssToDisk(String lang, boolean exists) throws IOException {

        // given:
        String doc = """
                = Test Document

                [source,%s]
                ----
                System.out.println("Hello World");
                ----
                """.formatted(lang);

        File toDir = tempDir;
        asciidoctor.syntaxHighlighterRegistry().register(TestHighlighter.class, NAME_SYNTAXHIGHLIGHTER);

        // when:
        asciidoctor.convert(doc, Options.builder()
                .safe(SafeMode.UNSAFE)
                .standalone(true)
                .toFile(new File(toDir, "syntaxhighlighterwithwritestylesheet.html"))
                .attributes(
                        Attributes.builder().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER)
                                .copyCss(true)
                                .linkCss(true).build())
                .build());

        // then:
        File css = new File(toDir, FILENAME_CSS);
        assertThat(css.exists() && Files.readString(css.toPath()).equals(CONTENT_CSS)).isEqualTo(exists);
    }
}
