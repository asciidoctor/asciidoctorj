package org.asciidoctor.converter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.Footnote
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.jruby.ast.impl.FootnoteImpl
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith
import spock.lang.Specification

import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.empty
import static org.hamcrest.Matchers.samePropertyValuesAs
import static org.junit.Assert.assertThat

/**
 * Tests that footnotes can be accessed from converter.
 *
 * Note that it is a current limitation of asciidoctor that footnotes are not available until
 * after they have been converted for the document.
 */
@RunWith(ArquillianSputnik)
class WhenFootnotesAreUsed extends Specification {
    static final String CONVERTER_BACKEND = 'footnote'

    @ArquillianResource
    private Asciidoctor asciidoctor
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
                footnotesBeforeConvert = doc.catalog.footnotes.collect()
                doc.content
                footnotesAfterConvert = doc.catalog.footnotes.collect()
            }
            else if (node instanceof StructuralNode) {
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
        asciidoctor.convert(document, OptionsBuilder.options().backend(CONVERTER_BACKEND))
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
        assertThat(footnotesBeforeConvert, empty())
        assertThat(footnotesAfterConvert, empty())
    }

    def 'when a footnote is is in source doc, it should be accessible from converter'() {
        given:
        String document = 'Do footnotes work?footnote:fid[we shall find out!]'

        when:
        convert(document)

        then:
        assertThat(footnotesBeforeConvert, empty())
        assertThat(footnotesAfterConvert,
                contains(samePropertyValuesAs(footnote(1,'fid','we shall find out!'))))
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
        assertThat(footnotesBeforeConvert, empty())
        assertThat(footnotesAfterConvert,
                contains(samePropertyValuesAs(footnote(1,'myid1','first footnote')),
                         samePropertyValuesAs(footnote(2, null, 'second footnote'))))
    }
}
