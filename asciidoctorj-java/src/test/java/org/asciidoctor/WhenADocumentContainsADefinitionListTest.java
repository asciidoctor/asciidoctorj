package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.converter.StringConverter;
import org.asciidoctor.extension.Treeprocessor;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenADocumentContainsADefinitionListTest {

    static final String TAG_DD = "dd";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    @DisplayName("the definition list should be loaded")
    public void theDefinitionListShouldBeLoaded() {

        // given:
        String document = """
                = Test document

                Item A::
                Item B:: A description

                Item C:: Another description
                """;

        // when:
        Document documentNode = asciidoctor.load(document, Options.builder().standalone(false).build());

        // then:
        DescriptionList list = (DescriptionList) documentNode.getBlocks().get(0);

        assertThat(list.getItems() != null || list.getItems().size() == 2).isTrue();

        assertThat(list.getItems().get(0)).isInstanceOf(DescriptionListEntry.class);

        assertThat(list.getItems().get(0).getTerms().get(0).getText()).isEqualTo("Item A");
        assertThat(list.getItems().get(0).getTerms().get(0)).isInstanceOf(ListItem.class);

        assertThat(list.getItems().get(0).getTerms().get(1).getText()).isEqualTo("Item B");
        assertThat(list.getItems().get(0).getTerms().get(1)).isInstanceOf(ListItem.class);

        assertThat(list.getItems().get(0).getDescription()).isInstanceOf(ListItem.class);
        assertThat(list.getItems().get(0).getDescription().hasText()).isTrue();
        assertThat(list.getItems().get(0).getDescription().getText()).isEqualTo("A description");

        assertThat(list.getItems().get(1)).isInstanceOf(DescriptionListEntry.class);

        assertThat(list.getItems().get(1).getTerms().get(0).getText()).isEqualTo("Item C");

        assertThat(list.getItems().get(1).getDescription()).isInstanceOf(ListItem.class);
        assertThat(list.getItems().get(1).getDescription().hasText()).isTrue();
        assertThat(list.getItems().get(1).getDescription().getText()).isEqualTo("Another description");
    }

    public static class TestConverter extends StringConverter {

        static final String NAME = "test";

        static final String NEWLINE = "\n";
        static final String NEWLINE_2 = NEWLINE.repeat(2);

        public TestConverter(String backend, Map<String, Object> opts) {
            super(backend, opts);
        }

        @Override
        public String convert(ContentNode node, String transform, Map<Object, Object> opts) {
            if (transform == null) {
                transform = node.getNodeName();
            }

            switch (transform) {
                case "embedded":
                    return convertEmbedded((Document) node);
                case "dlist":
                    return convertDlist((DescriptionList) node);
                default:
                    throw new IllegalArgumentException("No method convert" + transform);
            }
        }

        String convertEmbedded(Document node) {
            String result = node.getDoctitle().toUpperCase();
            String content = (String) node.getContent();
            if (content != null && content.trim().length() > 0) {
                result += NEWLINE_2 + content;
            }
            return result;
        }

        String convertDlist(DescriptionList dlist) {
            return dlist.getItems().stream().map(listItem -> {
                String items = listItem.getTerms().stream().map(ListItem::getText).collect(Collectors.joining(", "));
                String description = listItem.getDescription() == null ? null : listItem.getDescription().getText();
                return items + NEWLINE + "----" + NEWLINE + (description == null ? "" : description + NEWLINE);
            }).collect(Collectors.joining(NEWLINE_2));

        }

    }


    @Test
    @DisplayName("a document with a description list that contains no description should be converted")
    public void aDocumentWithADescriptionListThatContainsNoDescriptionShouldBeConverted() {

        // given:
        String document = """
                = Test document

                Item A::
                """;

        // when:
        asciidoctor.javaConverterRegistry().register(TestConverter.class, TestConverter.NAME);
        String result = asciidoctor.convert(document, Options.builder().backend(TestConverter.NAME).standalone(false).build());

        // then:
        assertThat(result).isEqualTo("""
                TEST DOCUMENT

                Item A
                ----
                """);
    }

    @Test
    @DisplayName("a document with a description list should be converted")
    public void aDocumentWithADescriptionListShouldBeConverted() {

        // given:
        String document = """
                = Test document

                Item A::
                Item B::
                  A very nice description
                """;

        // when:
        asciidoctor.javaConverterRegistry().register(TestConverter.class, TestConverter.NAME);
        String result = asciidoctor.convert(document, Options.builder().backend(TestConverter.NAME).standalone(false).build());

        // then:
        assertThat(result).isEqualTo("""
                TEST DOCUMENT

                Item A, Item B
                ----
                A very nice description
                """);
    }

    @Test
    @DisplayName("a processor should be able to set the description of a description list")
    public void aProcessorShouldBeAbleToSetTheDescriptionOfADescriptionList() {

        // given:
        String document = """
                = Test document

                Item A::
                  Replace me
                """;
        String newDescription = "A nice description";

        // when:
        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
            @Override
            public Document process(Document doc) {
                DescriptionList dl = (DescriptionList) doc.getBlocks().get(0);
                DescriptionListEntry dlEntry = dl.getItems().get(0);
                dlEntry.setDescription(createListItem(dl, newDescription));
                return doc;
            }
        });
        String result = asciidoctor.convert(document, Options.builder().standalone(false).build());

        // then:
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(result);
        assertThat(htmlDoc.getElementsByTag(TAG_DD).size()).isEqualTo(1);
        assertThat(htmlDoc.getElementsByTag(TAG_DD).get(0).text()).isEqualTo(newDescription);
    }
}
