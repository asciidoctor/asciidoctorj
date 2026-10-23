package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenATableIsLoadedTest {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    @DisplayName("colspan greater 1 should be passed correctly to the nodes")
    public void colspanGreater1ShouldBePassedCorrectlyToTheNodes() {

        // given:
        String document = """
                = Test document

                |===
                | Column A | Column B | Column C

                3.+|Same cell content in columns 1, 2, and 3
                |===
                """;

        // when:
        Document documentNode = asciidoctor.load(document, Options.builder().standalone(false).build());
        Table tableNode = (Table) documentNode.getBlocks().get(0);

        // then:
        assertThat(tableNode.getBody().get(0).getCells().get(0).getColspan()).isEqualTo(3);
    }


    @Test
    @DisplayName("normal columns should be passed as colspan 0 to the nodes")
    public void normalColumnsShouldBePassedAsColspan0ToTheNodes() {

        // given:
        String document = """
                = Test document

                |===
                | Column A

                |One cell content in columns 1
                |===
                """;

        // when:
        Document documentNode = asciidoctor.load(document, Options.builder().standalone(false).build());
        Table tableNode = (Table) documentNode.getBlocks().get(0);

        // then:
        assertThat(tableNode.getBody().get(0).getCells().get(0).getColspan()).isEqualTo(0);
    }

    @Test
    @DisplayName("asking a table cell for its inner document when it does not have one should return null")
    public void askingATableCellForItsInnerDocumentWhenItDoesNotHaveOneShouldReturnNull() {

        // given:
        String document = """
                = Test document

                [cols="40,60"]
                |===
                | Source | Output

                a|

                The first content cell

                a|

                The second content cell

                |===
                """;

        // when:
        Document documentNode = asciidoctor.load(document, Options.builder().standalone(false).build());
        Table tableNode = (Table) documentNode.getBlocks().get(0);

        // then:
        assertThat(tableNode.getHeader().get(0).getCells().get(0).getInnerDocument()).isNull();
        assertThat(tableNode.getHeader().get(0).getCells().get(1).getInnerDocument()).isNull();
    }
}
