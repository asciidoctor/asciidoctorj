package org.asciidoctor;

import static org.asciidoctor.util.OptionsTestHelper.emptyOptions;
import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Table;
import org.asciidoctor.extension.Treeprocessor;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that the unsubstituted text can be retrieved from nodes
 */
public class WhenTheSourceShouldBeAccessedTest {

    public static final String SOURCE = "This paragraph should show {foo}";
    public static final String CONVERTED = "This paragraph should show bar";
    public static final String P = "p";
    public static final String TD = "td";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    @DisplayName("it should be possible to get the raw text from a paragraph")
    public void itShouldBePossibleToGetTheRawTextFromAParagraph() {

        // given:
        String document = """

                = Test
                :foo: bar

                == Section

                This paragraph should show {foo}

                """;
        // when:
        Document doc = asciidoctor.load(document, emptyOptions());
        Block block = (Block) doc.getBlocks().get(0).getBlocks().get(0);

        // then:
        assertThat(block.getSource()).isEqualTo(SOURCE);
        assertThat(block.getContent()).isEqualTo(CONVERTED);
    }

    @Test
    @DisplayName("it should be possible to get the raw text from a list item")
    public void itShouldBePossibleToGetTheRawTextFromAListItem() {

        // given:
        String document = """

                = Test
                :foo: bar

                == Section

                * This list item should show {foo}
                  and should continue here
                * This does not interest at all

                """;
        // when:
        Document doc = asciidoctor.load(document, emptyOptions());
        List list = (List) doc.getBlocks().get(0).getBlocks().get(0);
        ListItem listItem = (ListItem) list.getItems().get(0);

        // then:
        assertThat(listItem.getSource()).isEqualTo("""
                This list item should show {foo}
                and should continue here""");
        assertThat(listItem.getText()).isEqualTo("""
                This list item should show bar
                and should continue here""");
        assertThat(list.getBlocks().size()).isEqualTo(list.getItems().size());
        assertThat(((ListItem) list.getItems().get(0)).getSource()).isEqualTo(((ListItem) list.getBlocks().get(0)).getSource());
    }

    @Test
    @DisplayName("it should be possible to get the raw text from a description list item")
    public void itShouldBePossibleToGetTheRawTextFromADescriptionListItem() {

        // given:
        String document = """

                = Test
                :foo: bar

                == Section

                This::
                  list item should show {foo}
                  and should continue here
                That::
                  does not interest at all

                """;
        // when:
        Document doc = asciidoctor.load(document, emptyOptions());
        DescriptionList list = (DescriptionList) doc.getBlocks().get(0).getBlocks().get(0);
        DescriptionListEntry listItem = list.getItems().get(0);

        // then:
        assertThat(listItem.getDescription().getSource()).isEqualTo("""
                list item should show {foo}
                and should continue here""");
        assertThat(listItem.getDescription().getText()).isEqualTo("""
                list item should show bar
                and should continue here""");
        assertThat(list.getBlocks().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("it should be possible to get the raw text from a table cell")
    public void itShouldBePossibleToGetTheRawTextFromATableCell() {

        // given:
        String document = """

                = Test
                :foo: bar

                == Section

                |===
                | Hello {foo}
                |===

                """;
        // when:
        Document doc = asciidoctor.load(document, emptyOptions());
        Table table = (Table) doc.getBlocks().get(0).getBlocks().get(0);
        Cell cell = table.getBody().get(0).getCells().get(0);

        // then:
        assertThat(cell.getSource()).isEqualTo("Hello {foo}");
        assertThat(cell.getText()).isEqualTo("Hello bar");
    }


    @Test
    @DisplayName("it should be possible to set the raw text of a paragraph")
    public void itShouldBePossibleToSetTheRawTextOfAParagraph() {

        // given:
        String document = """

                = Test
                :foo: bar

                == Section

                xxx

                """;
        // when:
        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
            @Override
            public Document process(Document doc) {
                ((Block) doc.getBlocks().get(0).getBlocks().get(0)).setSource(SOURCE);
                return doc;
            }
        });
        String html = asciidoctor.convert(document, Options.builder().standalone(false).build());
        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        // then:
        assertThat(doc.select(P).text()).isEqualTo(CONVERTED);
    }

    @Test
    @DisplayName("it should be possible to set the source of a list item")
    public void itShouldBePossibleToSetTheSourceOfAListItem() {

        // given:
        String document = """

                = Test
                :foo: bar

                == Section

                * xxx
                * This does not interest at all

                """;
        // when:
        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
            @Override
            public Document process(Document doc) {
                ((ListItem) ((List) doc.getBlocks().get(0).getBlocks().get(0)).getItems().get(0)).setSource(SOURCE);
                return doc;
            }
        });
        String html = asciidoctor.convert(document, Options.builder().standalone(false).build());
        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        // then:
        assertThat(doc.select(P).get(0).text()).isEqualTo(CONVERTED);
        assertThat(doc.select(P).get(1).text()).isEqualTo("This does not interest at all");
    }

    @Test
    @DisplayName("it should be possible to set the raw text of a table cell")
    public void itShouldBePossibleToSetTheRawTextOfATableCell() {

        // given:
        String document = """

                = Test
                :foo: bar

                == Section

                |===
                | xxx
                |===

                """;
        // when:
        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
            @Override
            public Document process(Document doc) {
                Table table = (Table) doc.getBlocks().get(0).getBlocks().get(0);
                Cell cell = table.getBody().get(0).getCells().get(0);
                cell.setSource(SOURCE);
                return doc;
            }
        });
        String html = asciidoctor.convert(document, Options.builder().standalone(false).build());
        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        // then:
        assertThat(doc.select(TD).text()).isEqualTo(CONVERTED);
    }

}
