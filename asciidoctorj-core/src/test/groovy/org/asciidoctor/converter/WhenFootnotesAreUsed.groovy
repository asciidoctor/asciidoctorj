package org.asciidoctor.converter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.Footnote
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.jruby.ast.impl.FootnoteImpl
import spock.lang.Specification

import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.samePropertyValuesAs
import static org.junit.Assert.assertThat

/**
 * Tests that footnotes can be accessed from converter.
 *
 * Note that it is a current limitation of asciidoctor that footnotes are not
 * available until after they have been converted for the document.
 */
class WhenFootnotesAreUsed extends Specification {

    static final String CONVERTER_BACKEND = 'footnote'

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    private static List<Footnote> footnotesBeforeConvert
    private static List<Footnote> footnotesAfterConvert

    static class Converter extends StringConverter {
        Converter(String backend, Map<String, Object> opts) {
            super(backend, opts)
        }

        /*
         * For this conversion test we do not care about the conversion result,
         * we simply want to force the conversion to verify that footnotes
         * are populated.
         */

        @Override
        String convert(ContentNode node, String transform, Map<Object, Object> opts) {
            if (node instanceof Document) {
                def doc = (Document) node
                footnotesBeforeConvert = doc.catalog.footnotes
                doc.content
                footnotesAfterConvert = doc.catalog.footnotes
            } else if (node instanceof StructuralNode) {
                ((StructuralNode) node).content
            }
        }
    }

    def setup() {
        footnotesBeforeConvert = null
        footnotesAfterConvert = null
        asciidoctor.javaConverterRegistry().register(Converter, CONVERTER_BACKEND)
    }

    def convert(String document) {
        def options = Options.builder().backend(CONVERTER_BACKEND).build()
        asciidoctor.convert(document, options)
    }

    def footnote(Long index, String id, String text) {
        FootnoteImpl.getInstance(index, id, text)
    }

    def 'when there are no footnotes in source doc, footnotes should be empty'() {
        given:
        String document = 'no footnotes here'

        when:
        convert(document)

        then:
        footnotesBeforeConvert.isEmpty()
        footnotesAfterConvert.isEmpty()
    }

    def 'when a footnote is is in source doc, it should be accessible from converter'() {
        given:
        String document = 'Do footnotes work?footnote:fid[we shall find out!]'

        when:
        convert(document)

        then:
        footnotesBeforeConvert.isEmpty()
        assertThat(footnotesAfterConvert,
                contains(samePropertyValuesAs(footnote(1, 'fid', 'we shall find out!'))))
    }

    def 'when footnotes are in source doc, they should be accessible from the converter'() {
        given:
        String document = '''
This document has a variety of footnotes.

Ids can be assigned to footnotes.footnote:myid1[first footnote]
Footnotes can also be id-less.footnote:[second footnote]
An existing footnote can be referenced.footnote:myid1[]
'''
        when:
        convert(document)

        then:
        footnotesBeforeConvert.isEmpty()
        assertThat(footnotesAfterConvert,
                contains(samePropertyValuesAs(footnote(1, 'myid1', 'first footnote')),
                        samePropertyValuesAs(footnote(2, null, 'second footnote'))))
    }
}
