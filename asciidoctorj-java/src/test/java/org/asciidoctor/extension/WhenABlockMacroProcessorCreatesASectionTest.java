package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenABlockMacroProcessorCreatesASectionTest {

    public static final String BLOCKMACRO_NAME = "section";
    public static final String UTF_8 = "UTF-8";
    public static final String H2 = "h2";
    public static final String HELLO_WORLD = "1. HelloWorld";
    public static final String SECT1_SELECTOR = "div.sect1";
    public static final String PARAGRAPH_SELECTOR = "div.paragraph";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private static final String DOCUMENT = """

            = Section Creation Test

            section::HelloWorld[]
            """;

    @Test
    @DisplayName("the section should appear in the resulting document")
    public void theSectionShouldAppearInTheResultingDocument() {

        // given:
        asciidoctor.javaExtensionRegistry().blockMacro(BLOCKMACRO_NAME, SectionCreatorBlockMacro.class);

        // when:
        Options options = Options.builder().safe(SafeMode.SAFE).toFile(false).standalone(true).build();
        String result = asciidoctor.convert(DOCUMENT, options);

        // then:
        Document htmlDocument = Jsoup.parse(result, UTF_8);

        assertThat(htmlDocument.select(H2).text()).isEqualTo(HELLO_WORLD);
        assertThat(htmlDocument.select(SECT1_SELECTOR).select(PARAGRAPH_SELECTOR).text()).isEqualTo(SectionCreatorBlockMacro.CONTENT);
    }

    @Test
    @DisplayName("the section should appear in the resulting document when the extension is registered with an extension group")
    public void theSectionShouldAppearInTheResultingDocumentWhenTheExtensionIsRegisteredWithAnExtensionGroup() {

        // given:
        asciidoctor.createGroup().blockMacro(BLOCKMACRO_NAME, SectionCreatorBlockMacro.class).register();

        // when:
        Options options = Options.builder().safe(SafeMode.SAFE).toFile(false).standalone(true).build();
        String result = asciidoctor.convert(DOCUMENT, options);

        // then:
        Document htmlDocument = Jsoup.parse(result, UTF_8);

        assertThat(htmlDocument.select(H2).text()).isEqualTo(HELLO_WORLD);
        assertThat(htmlDocument.select(SECT1_SELECTOR).select(PARAGRAPH_SELECTOR).text()).isEqualTo(SectionCreatorBlockMacro.CONTENT);
    }
}
