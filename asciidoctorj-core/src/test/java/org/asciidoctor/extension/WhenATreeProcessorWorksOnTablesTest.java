package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenATreeProcessorWorksOnTablesTest {

    private static final String EMPTY_DOCUMENT = "= Document without table";

    public static final String TABLE_BODY = "tbody";

    public static final String TABLE_HEADER = "thead";

    public static final String TABLE_FOOTER = "tfoot";

    public static final String TD = "td";

    public static final String TH = "th";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    @DisplayName("then the extension should be able to create tables")
    public void thenTheExtensionShouldBeAbleToCreateTables() {

        // given: an empty document and a tree processor that creates a simple table
        asciidoctor.javaExtensionRegistry().treeprocessor(TableCreatorTreeProcessor.class);

        // when: the document is rendered
        String content = asciidoctor.convert(EMPTY_DOCUMENT, Options.builder().standalone(false).build());

        // then: the resulting document has a table
        org.jsoup.nodes.Document document = Jsoup.parse(content);

        assertThat(document.getElementsByTag(TABLE_BODY).size()).isEqualTo(1);

        // and: this table has one cell with the text A1

        org.jsoup.nodes.Element table = document.getElementsByTag(TABLE_BODY).get(0);
        assertThat(table.getElementsByTag(TD).size()).isEqualTo(1);
        assertThat(table.getElementsByTag(TD).get(0).text()).isEqualTo("A1");

        // and: this table has a header row

        org.jsoup.nodes.Element tableHeader = document.getElementsByTag(TABLE_HEADER).get(0);
        assertThat(tableHeader.getElementsByTag(TH).size()).isEqualTo(1);
        assertThat(tableHeader.getElementsByTag(TH).get(0).text()).isEqualTo("A");

        // and: this table has a header footer

        org.jsoup.nodes.Element tableFooter = document.getElementsByTag(TABLE_FOOTER).get(0);
        assertThat(tableFooter.getElementsByTag(TD).size()).isEqualTo(1);
        assertThat(tableFooter.getElementsByTag(TD).get(0).text()).isEqualTo("Sum of A");

    }

    @Test
    @DisplayName("then it should be able to update the text of a cell")
    public void thenItShouldBeAbleToUpdateTheTextOfACell() {

        // given: an document with a table and a tree processor that updates tables
        asciidoctor.javaExtensionRegistry().treeprocessor(TableUpdaterTreeProcessor.class);

        // when: the document is rendered
        String content = asciidoctor.convert("""
                === Test

                |===
                | Hello | World
                | World | Hello\s
                |===

                """, Options.builder().standalone(false).build());

        org.jsoup.nodes.Document htmlDocument = Jsoup.parse(content);

        // then:
        Elements replacedElements = htmlDocument.select("td p strong");
        assertThat(replacedElements.size()).isEqualTo(4);
        assertThat(replacedElements.stream().allMatch(it -> it.text().equals("Replaced"))).isTrue();
    }
}
