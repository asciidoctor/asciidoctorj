package org.asciidoctor.converter;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that refs can be accessed from converter.
 */
public class CatalogOfRefsAreAvailableTest {

    static final String CONVERTER_BACKEND = "refs";
    static final String NODE_NAME_SECTION = "section";
    static final String NODE_NAME_PARAGRAPH = "paragraph";
    static final String NODE_NAME_ULIST = "ulist";
    static final String NODE_NAME_INLINE_ANCHOR = "inline_anchor";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private static Map<String, Object> refs;

    public static class Converter extends StringConverter {
        public Converter(String backend, Map<String, Object> opts) {
            super(backend, opts);
        }

        /*
         * For this conversion test we do not care about the conversion result,
         * we simply want to to verify that refs are available and as expected.
         */

        @Override
        public String convert(ContentNode node, String transform, Map<Object, Object> opts) {
            if (node instanceof Document) {
                Document doc = (Document) node;
                refs = doc.getCatalog().getRefs();
            } else if (node instanceof StructuralNode) {
                return (String) ((StructuralNode) node).getContent();
            }
            return null;
        }
    }

    @BeforeEach
    public void setup() {
        refs = null;
        asciidoctor.javaConverterRegistry().register(Converter.class, CONVERTER_BACKEND);
    }

    private String convert(String document) {
        return asciidoctor.convert(document, Options.builder().backend(CONVERTER_BACKEND).build());
    }

    @Test
    @DisplayName("when there are no refs in source doc, refs should be empty")
    public void whenThereAreNoRefsInSourceDocRefsShouldBeEmpty() {
        // given:
        String document = "no refs here";

        // when:
        convert(document);

        // then:
        assertThat(refs.entrySet()).isEmpty();
    }

    @Test
    @DisplayName("when a ref is is in source doc, it should be accessible from converter")
    public void whenARefIsInSourceDocItShouldBeAccessibleFromConverter() {
        // given:
        String idSectionA = "_a_section";
        String document = "== A Section";

        // when:
        convert(document);

        // then:
        assertThat(refs.keySet()).contains(idSectionA);
        assertHasIdPointingToNodeNamed(idSectionA, NODE_NAME_SECTION);
    }

    @Test
    @DisplayName("when refs are in source doc, they should be accessible from the converter")
    public void whenRefsAreInSourceDocTheyShouldBeAccessibleFromTheConverter() {
        // given:
        String idSectionA = "_section_a";
        String idSectionB = "override-section-b";
        String idSectionC = "section-c-primary";
        String idPara1 = "para1";
        String idPara2 = "para2";
        String idInline = "inlineref";
        String idList = "list";
        String idInList = "in-list";
        String document = """

                This document has a variety of ref ids.

                == Section A

                A section, by default, is assigned an id.

                [[%s,Override default section id]]
                == Section B

                == Section C[[section-c2]][[section-c3]] [[%s]]

                Section C heading has multiple anchors, note that the secondary
                anchors do not seem to show up in refs.

                [[%s]]
                My paragraph

                [#%s]
                Shorthand syntax

                An [#%s]*quoted text*

                [[%s,some ref text]]
                * item1
                * [[%s]] item2
                * item3

                // This ref will be excluded as it points to nothing
                [[reftonada]]
                """.formatted(idSectionB, idSectionC, idPara1, idPara2, idInline, idList, idInList);
        // when:
        convert(document);

        // then:
        assertThat(refs.keySet()).containsExactlyInAnyOrder(idSectionA, idSectionB, idSectionC, idPara1, idPara2, idList, idInList);
        assertHasIdPointingToNodeNamed(idSectionA, NODE_NAME_SECTION);
        assertHasIdPointingToNodeNamed(idSectionB, NODE_NAME_SECTION);
        assertHasIdPointingToNodeNamed(idSectionC, NODE_NAME_SECTION);
        assertHasIdPointingToNodeNamed(idPara1, NODE_NAME_PARAGRAPH);
        assertHasIdPointingToNodeNamed(idPara2, NODE_NAME_PARAGRAPH);
        assertHasIdPointingToNodeNamed(idList, NODE_NAME_ULIST);
        assertHasIdPointingToNodeNamed(idInList, NODE_NAME_INLINE_ANCHOR);
    }

    private static void assertHasIdPointingToNodeNamed(final String id, final String expectedNodeName) {
        assertThat(refs).as("ref id %s", id).containsKey(id);
        assertThat(((ContentNode) refs.get(id)).getNodeName())
                .as("ref id %s pointing to node named %s", id, expectedNodeName)
                .isEqualTo(expectedNodeName);
    }

}
