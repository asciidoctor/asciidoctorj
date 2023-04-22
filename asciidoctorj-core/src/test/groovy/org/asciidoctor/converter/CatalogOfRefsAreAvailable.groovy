package org.asciidoctor.converter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.hamcrest.BaseMatcher
import spock.lang.Specification

import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.empty
import static org.junit.Assert.assertThat

/**
 * Tests that refs can be accessed from converter.
 */
class CatalogOfRefsAreAvailable extends Specification {

    static final String CONVERTER_BACKEND = 'refs'
    static final String NODE_NAME_SECTION = 'section'
    static final String NODE_NAME_PARAGRAPH = 'paragraph'
    static final String NODE_NAME_ULIST = 'ulist'
    static final String NODE_NAME_INLINE_ANCHOR = 'inline_anchor'

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    private static Map<String, Object> refs

    static class Converter extends StringConverter {
        Converter(String backend, Map<String, Object> opts) {
            super(backend, opts)
        }

        /*
         * For this conversion test we do not care about the conversion result,
         * we simply want to to verify that refs are available and as expected.
         */

        @Override
        String convert(ContentNode node, String transform, Map<Object, Object> opts) {
            if (node instanceof Document) {
                def doc = (Document) node
                refs = doc.catalog.refs
            } else if (node instanceof StructuralNode) {
                ((StructuralNode) node).content
            }
        }
    }

    def setup() {
        refs = null
        asciidoctor.javaConverterRegistry().register(Converter, CONVERTER_BACKEND)
    }

    def convert(String document) {
        asciidoctor.convert(document, OptionsBuilder.options().backend(CONVERTER_BACKEND))
    }

    def 'when there are no refs in source doc, refs should be empty'() {
        given:
        String document = 'no refs here'

        when:
        convert(document)

        then:
        assertThat(refs.entrySet(), empty())
    }

    def 'when a ref is is in source doc, it should be accessible from converter'() {
        given:
        def idSectionA = '_a_section'
        String document = '== A Section'

        when:
        convert(document)

        then:
        assertThat(refs.keySet(), contains(idSectionA))
        assertThat(refs, hasIdPointingToNodeNamed(idSectionA, NODE_NAME_SECTION))
    }

    def 'when refs are in source doc, they should be accessible from the converter'() {
        given:
        def idSectionA = '_section_a'
        def idSectionB = 'override-section-b'
        def idSectionC = 'section-c-primary'
        def idPara1 = 'para1'
        def idPara2 = 'para2'
        def idInline = 'inlineref'
        def idList = 'list'
        def idInList = 'in-list'
        String document = """
This document has a variety of ref ids.

== Section A

A section, by default, is assigned an id.

[[${idSectionB},Override default section id]]
== Section B

== Section C[[section-c2]][[section-c3]] [[${idSectionC}]]

Section C heading has multiple anchors, note that the secondary
anchors do not seem to show up in refs.

[[${idPara1}]]
My paragraph

[#${idPara2}]
Shorthand syntax

An [#${idInline}]*quoted text*

[[${idList},some ref text]]
* item1
* [[${idInList}]] item2
* item3

// This ref will be excluded as it points to nothing
[[reftonada]]
"""
        when:
        convert(document)

        then:
        assertThat(refs.keySet(), containsInAnyOrder(idSectionA, idSectionB, idSectionC,
                idPara1, idPara2, idList, idInList))
        assertThat(refs, hasIdPointingToNodeNamed(idSectionA, NODE_NAME_SECTION))
        assertThat(refs, hasIdPointingToNodeNamed(idSectionB, NODE_NAME_SECTION))
        assertThat(refs, hasIdPointingToNodeNamed(idSectionC, NODE_NAME_SECTION))
        assertThat(refs, hasIdPointingToNodeNamed(idPara1, NODE_NAME_PARAGRAPH))
        assertThat(refs, hasIdPointingToNodeNamed(idPara2, NODE_NAME_PARAGRAPH))
        assertThat(refs, hasIdPointingToNodeNamed(idList, NODE_NAME_ULIST))
        assertThat(refs, hasIdPointingToNodeNamed(idInList, NODE_NAME_INLINE_ANCHOR))
    }

    private hasIdPointingToNodeNamed(final id, final expectedNodeName) {
        [
            matches: { refs ->
                if (refs.get(id)) {
                    refs.get(id).nodeName == expectedNodeName
                }
            },
            describeTo: { description ->
                description.appendText('ref id ')
                        .appendValue(id)
                        .appendText(' pointing to node named ')
                        .appendValue(expectedNodeName)
            },
            describeMismatch: { refs, description ->
                if (!refs.get(id)) {
                    description.appendText('did not find id ')
                            .appendValue(id)
                            .appendText(' in refs')
                } else {
                    description.appendText('instead found id pointing to ')
                            .appendValue(refs.get(id).nodeName)
                }
            }
        ] as BaseMatcher<Map<String, Object>>
    }

}
