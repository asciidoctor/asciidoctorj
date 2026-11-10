package org.asciidoctor.extension;

import static org.asciidoctor.util.OptionsTestHelper.emptyOptions;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenAstIsIteratedTest {

    Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    static final String DOCUMENT = """

            = Hello World

            This is a test document

            == This is the first section

            This is a paragraph

            == Another section

            A list with items

            * One
            * Two
            * Three
            """;


    @Test
    @DisplayName("getDocument should always return the same instance")
    public void getDocumentShouldAlwaysReturnTheSameInstance() {
        // when:
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        List<StructuralNode> allBlocks = document.findBy(new HashMap<>());

        // then:
        assertThat(document).isSameAs(document.getDocument());
        assertThat(allBlocks.stream().allMatch(block -> document == block.getDocument())).isTrue();
    }

    @Test
    @DisplayName("getParent should always return the same instance")
    public void getParentShouldAlwaysReturnTheSameInstance() {
        // when:
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        List<StructuralNode> allBlocks = document.findBy(new HashMap<>());

        // then: Every block of the document should return the same node as the parent from getParent()
        assertThat(allBlocks.stream().allMatch(
                block -> block.getBlocks().stream().allMatch(child -> block == child.getParent()))).isTrue();
    }

}
