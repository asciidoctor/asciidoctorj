package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SectionImplTest {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();


    @Test
    public void should_return_valid_sectnum_when_sectionNumbers_are_enabled() {
        final String source = sectionsSample();

        Document document = loadDocument(source, true);

        Section node1 = findSectionNode(document, 1);
        assertThat(node1.getSectnum()).isEqualTo("1.");
        assertThat(node1.isNumbered()).isTrue();

        Section node2 = findSectionNode(document, 2);
        assertThat(node2.getSectnum()).isEqualTo("1.1.");
        assertThat(node2.isNumbered()).isTrue();
    }

    @Test
    public void should_return_invalid_sectnum_when_sectionNumbers_are_not_enabled() {
        final String source = sectionsSample();

        Document document = loadDocument(source, false);

        Section node1 = findSectionNode(document, 1);
        assertThat(node1.getSectnum()).isEqualTo(".");
        assertThat(node1.isNumbered()).isFalse();

        Section node2 = findSectionNode(document, 2);
        assertThat(node2.getSectnum()).isEqualTo("..");
        assertThat(node2.isNumbered()).isFalse();
    }

    @Test
    public void should_return_sectnum_with_custom_delimiter_when_sectionNumbers_are_enabled() {
        final String source = sectionsSample();

        Document document = loadDocument(source, true);

        Section node1 = findSectionNode(document, 1);
        assertThat(node1.getSectnum("_")).isEqualTo("1_");

        Section node2 = findSectionNode(document, 2);
        assertThat(node2.getSectnum("*")).isEqualTo("1*1*");
    }

    @Test
    public void should_return_sectnum_with_custom_delimiter_when_sectionNumbers_are_not_enabled() {
        final String source = sectionsSample();

        Document document = loadDocument(source, false);

        Section node1 = findSectionNode(document, 1);
        assertThat(node1.getSectnum("_")).isEqualTo("_");

        Section node2 = findSectionNode(document, 2);
        assertThat(node2.getSectnum("*")).isEqualTo("**");
    }

    private Document loadDocument(String source, boolean sectionNumbers) {
        Attributes attributes = Attributes.builder().sectionNumbers(sectionNumbers).build();
        Options options = Options.builder().attributes(attributes).build();
        return asciidoctor.load(source, options);
    }

    private Section findSectionNode(Document document, int level) {
        return (Section) document.findBy(Collections.singletonMap("context", ":section"))
                .stream()
                .filter(n -> n.getLevel() == level)
                .findFirst()
                .get();
    }

    static String sectionsSample() {
        return "= Document Title\n\n" +
                "== Section A\n\n" +
                "Section A paragraph.\n\n" +
                "=== Section A Subsection\n\n" +
                "Section A 'subsection' paragraph.\n\n";
    }

    @Test
    public void should_return_isNumbered_true_when_doctype_is_article_and_sectnums_is_set() {
        assertDocTypeAndSectnums("article", true);
    }

    @Test
    public void should_return_isNumbered_false_when_doctype_is_article_and_sectnums_is_not_set() {
        assertDocTypeAndSectnums("article", false);
    }

    @Test
    public void should_return_isNumbered_true_when_doctype_is_book_and_sectnums_is_set() {
        assertDocTypeAndSectnums("book", true);
    }

    @Test
    public void should_return_isNumbered_false_when_doctype_is_book_and_sectnums_is_not_set() {
        assertDocTypeAndSectnums("book", false);
    }

    private void assertDocTypeAndSectnums(String doctype, boolean sectnums) {
        String content = buildContent(doctype, sectnums);

        Document document = asciidoctor.load(content, Options.builder().build());

        SectionImpl section = (SectionImpl) document.getBlocks().get(0);
        assertThat(section.isNumbered()).isEqualTo(sectnums);
    }

    private static String buildContent(String doctype, boolean sectnums) {
        return "= Sample Document\n" +
                ":doctype: " + doctype + "\n" +
                (sectnums ? ":sectnums:" : "") +
                "\n\n" +
                "== Test\n" +
                "\n" +
                "Test";
    }
}
