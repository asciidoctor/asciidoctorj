package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class WhenABlockMacroProcessorCreatesAListTest {

    public static final String LISTMACRO_NAME = "list";
    public static final String UTF_8 = "UTF-8";
    public static final String OL = "ol";
    public static final String UL = "ul";
    public static final String LI = "li";
    public static final String FIRST_ITEM = "First item";
    public static final String SECOND_ITEM = "Second item";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private static final String DOCUMENT = """

            = Section Creation Test

            list::HelloWorld[]
            """;

    @ParameterizedTest(name = "the ordered list should appear in the resulting document (attributes in options: {0})")
    @ValueSource(booleans = {false, true})
    public void theOrderedListShouldAppearInTheResultingDocument(boolean attributesInOptions) {

        // given:
        asciidoctor.javaExtensionRegistry().blockMacro(LISTMACRO_NAME, new ListCreatorBlockMacro("olist", attributesInOptions));

        // when:
        Options standalone = Options.builder().safe(SafeMode.SAFE).toFile(false).standalone(false).build();
        String result = asciidoctor.convert(DOCUMENT, standalone);

        // then:
        Document htmlDocument = Jsoup.parse(result, UTF_8);

        assertThat(htmlDocument.select(OL).size()).isEqualTo(1);
        Element ol = htmlDocument.select(OL).first();
        assertThat(ol.attr("start")).isEqualTo("42");
        Elements items = ol.select(LI);
        assertThat(items.size()).isEqualTo(2);
        assertThat(items.get(0).text()).isEqualTo(FIRST_ITEM);
        assertThat(items.get(1).text()).isEqualTo(SECOND_ITEM);
    }

    @Test
    @DisplayName("the unordered list should appear in the resulting document")
    public void theUnorderedListShouldAppearInTheResultingDocument() {

        // given:
        asciidoctor.javaExtensionRegistry().blockMacro(LISTMACRO_NAME, new ListCreatorBlockMacro("ulist"));

        // when:
        Options options = Options.builder().safe(SafeMode.SAFE).toFile(false).standalone(false).build();
        String result = asciidoctor.convert(DOCUMENT, options);

        // then:
        Document htmlDocument = Jsoup.parse(result, UTF_8);

        assertThat(htmlDocument.select(UL).size()).isEqualTo(1);
        Element ul = htmlDocument.select(UL).first();
        Elements items = ul.select(LI);
        assertThat(items.size()).isEqualTo(2);
        assertThat(items.get(0).text()).isEqualTo(FIRST_ITEM);
        assertThat(items.get(1).text()).isEqualTo(SECOND_ITEM);
    }
}
