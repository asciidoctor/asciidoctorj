package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WhenDocumentIsLoaded {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    public void should_return_empty_when_document_is_empty() {
        assertEmptySources(loadDocument(""));
    }

    private static void assertEmptySources(Document document) {
        String source = document.getSource();
        assertThat(source).isEmpty();
        List<String> sourceLines = document.getSourceLines();
        assertThat(sourceLines).isEmpty();
    }

    @Test
    public void should_return_source_and_source_lines() {
        final String asciidoc = asciidocSample();

        Document document = loadDocument(asciidoc);

        String source = document.getSource();
        assertThat(source).isEqualTo(asciidoc.trim());
        List<String> sourceLines = document.getSourceLines();
        assertThat(sourceLines)
                .containsExactly("= Document Title",
                        "",
                        "== Section A",
                        "",
                        "Section A paragraph.",
                        "",
                        "=== Section A Subsection",
                        "",
                        "Section A 'subsection' paragraph.");
    }

    @Test
    public void should_return_source_and_source_lines_without_trailing() {
        final String asciidoc = "= Document Title\n\n" +
                "== Section\n\n" +
                "Hello\t  \n";

        Document document = loadDocument(asciidoc);

        String source = document.getSource();
        assertThat(source).isEqualTo("= Document Title\n\n== Section\n\nHello");
        List<String> sourceLines = document.getSourceLines();
        assertThat(sourceLines)
                .containsExactly("= Document Title",
                        "",
                        "== Section",
                        "",
                        "Hello");
    }

    @Test
    public void should_return_source_and_source_lines_without_resolving_attributes() {
        final String asciidoc = "= Document Title\n" +
                ":an-attribute: a-value\n\n" +
                "This is {an-attribute}";

        Document document = loadDocument(asciidoc);

        String source = document.getSource();
        assertThat(source).isEqualTo("= Document Title\n:an-attribute: a-value\n\nThis is {an-attribute}");
        List<String> sourceLines = document.getSourceLines();
        assertThat(sourceLines)
                .containsExactly("= Document Title",
                        ":an-attribute: a-value",
                        "",
                        "This is {an-attribute}");
    }

    @Test
    public void should_return_source_and_source_lines_without_resolving_includes() {
        final String asciidoc = "= Document Title\n\n" +
                "== Section\n\n" +
                "include::partial.adoc[]";

        Document document = loadDocument(asciidoc);

        String source = document.getSource();
        assertThat(source).isEqualTo("= Document Title\n\n== Section\n\ninclude::partial.adoc[]");
        List<String> sourceLines = document.getSourceLines();
        assertThat(sourceLines)
                .containsExactly("= Document Title",
                        "",
                        "== Section",
                        "",
                        "include::partial.adoc[]");
    }

    private Document loadDocument(String source) {
        Attributes attributes = Attributes.builder().build();
        Options options = Options.builder().attributes(attributes).build();
        return asciidoctor.load(source, options);
    }

    static String asciidocSample() {
        return "= Document Title\n\n" +
                "== Section A\n\n" +
                "Section A paragraph.\n\n" +
                "=== Section A Subsection\n\n" +
                "Section A 'subsection' paragraph.\n\n";
    }
}
