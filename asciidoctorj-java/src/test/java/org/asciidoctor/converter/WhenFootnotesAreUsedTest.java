package org.asciidoctor.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Footnote;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that footnotes can be accessed from converter.
 *
 * Note that it is a current limitation of asciidoctor that footnotes are not
 * available until after they have been converted for the document.
 */
public class WhenFootnotesAreUsedTest {

    static final String CONVERTER_BACKEND = "footnote";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private static List<Footnote> footnotesBeforeConvert;
    private static List<Footnote> footnotesAfterConvert;

    public static class Converter extends StringConverter {
        public Converter(String backend, Map<String, Object> opts) {
            super(backend, opts);
        }

        /*
         * For this conversion test we do not care about the conversion result,
         * we simply want to force the conversion to verify that footnotes
         * are populated.
         */

        @Override
        public String convert(ContentNode node, String transform, Map<Object, Object> opts) {
            if (node instanceof Document) {
                Document doc = (Document) node;
                footnotesBeforeConvert = doc.getCatalog().getFootnotes();
                Object content = doc.getContent();
                footnotesAfterConvert = doc.getCatalog().getFootnotes();
                return (String) content;
            } else if (node instanceof StructuralNode) {
                return (String) ((StructuralNode) node).getContent();
            }
            return null;
        }
    }

    @BeforeEach
    public void setup() {
        footnotesBeforeConvert = null;
        footnotesAfterConvert = null;
        asciidoctor.javaConverterRegistry().register(Converter.class, CONVERTER_BACKEND);
    }

    private String convert(String document) {
        Options options = Options.builder().backend(CONVERTER_BACKEND).build();
        return asciidoctor.convert(document, options);
    }

    @Test
    @DisplayName("when there are no footnotes in source doc, footnotes should be empty")
    public void whenThereAreNoFootnotesInSourceDocFootnotesShouldBeEmpty() {
        // given:
        String document = "no footnotes here";

        // when:
        convert(document);

        // then:
        assertThat(footnotesBeforeConvert).isEmpty();
        assertThat(footnotesAfterConvert).isEmpty();
    }

    // NOTE the footnotes are compared by their properties (AsciidoctorJ creates the expected footnotes with its
    // implementation class)
    @Test
    @DisplayName("when a footnote is is in source doc, it should be accessible from converter")
    public void whenAFootnoteIsInSourceDocItShouldBeAccessibleFromConverter() {
        // given:
        String document = "Do footnotes work?footnote:fid[we shall find out!]";

        // when:
        convert(document);

        // then:
        assertThat(footnotesBeforeConvert).isEmpty();
        assertThat(footnotesAfterConvert)
                .extracting(Footnote::getIndex, Footnote::getId, Footnote::getText)
                .containsExactly(tuple(1L, "fid", "we shall find out!"));
    }

    @Test
    @DisplayName("when footnotes are in source doc, they should be accessible from the converter")
    public void whenFootnotesAreInSourceDocTheyShouldBeAccessibleFromTheConverter() {
        // given:
        String document = """

                This document has a variety of footnotes.

                Ids can be assigned to footnotes.footnote:myid1[first footnote]
                Footnotes can also be id-less.footnote:[second footnote]
                An existing footnote can be referenced.footnote:myid1[]
                """;
        // when:
        convert(document);

        // then:
        assertThat(footnotesBeforeConvert).isEmpty();
        assertThat(footnotesAfterConvert)
                .extracting(Footnote::getIndex, Footnote::getId, Footnote::getText)
                .containsExactly(tuple(1L, "myid1", "first footnote"), tuple(2L, null, "second footnote"));
    }
}
