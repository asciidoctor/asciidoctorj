package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Issue: https://github.com/asciidoctor/asciidoctorj/issues/448
public class WhenAnInlineMacroProcessorProcessesATableTest {

    public static final String UTF8 = "UTF-8";
    public static final String HREF = "href";
    public static final String ANCHOR_TAG = "a";
    public static final String TARGET = "gittutorial.html";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private static final String INLINE_MACRO_DOCUMENT = """
            = Test document

            |===
            | You can find infos on man:gittutorial[7] or man:git[8, 1].
            |===

            """;

    private static final String INLINE_MACRO_DOCUMENT_ASCIIDOC_STYLE = """
            = Test document

            |===
            a| You can find infos on man:gittutorial[7] or man:git[8, 1].
            |===

            """;


    @Test
    @DisplayName("a InlineMacroProcessor should be able to process table cells")
    public void aInlineMacroProcessorShouldBeAbleToProcessTableCells() {
        // when:
        asciidoctor.javaExtensionRegistry().inlineMacro(AnnotatedLongInlineMacroProcessor.class);
        String result = asciidoctor.convert(INLINE_MACRO_DOCUMENT, Options.builder().standalone(false).build());

        // then:
        Document doc = Jsoup.parse(result, UTF8);
        Element link = doc.getElementsByTag(ANCHOR_TAG).first();
        assertThat(link.attr(HREF)).isEqualTo(TARGET);
    }

    @Test
    @DisplayName("a InlineMacroProcessor should be able to process table cells with asciidoc style")
    public void aInlineMacroProcessorShouldBeAbleToProcessTableCellsWithAsciidocStyle() {
        // when:
        asciidoctor.javaExtensionRegistry().inlineMacro(AnnotatedLongInlineMacroProcessor.class);
        String result = asciidoctor.convert(INLINE_MACRO_DOCUMENT_ASCIIDOC_STYLE, Options.builder().standalone(false).build());

        // then:
        Document doc = Jsoup.parse(result, UTF8);
        Element link = doc.getElementsByTag(ANCHOR_TAG).first();
        assertThat(link.attr(HREF)).isEqualTo(TARGET);
    }

}
