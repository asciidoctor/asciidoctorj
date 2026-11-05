package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;


import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(AsciidoctorExtension.class)
public class WhenSourceHighlightingIsUsedTest {

    private static final String DOCUMENT = "[source,java]\n" +
            "----\n" +
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    println(\"Hello World\")\n" +
            "  }\n" +
            "}\n" +
            "----\n";

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @Test

    @Disabled("Rouge is a Ruby gem, which is not supported by this implementation")
    public void should_render_with_rouge() {
        String html = asciidoctor.convert(DOCUMENT,
                sourceHighlighter("rouge"));

        Document doc = Jsoup.parse(html);

        assertThat(doc.select("pre.rouge span.kd").size()).as("No elements were highlighted").isGreaterThan(0);
        assertThat(html).as("CSS was not added").contains("pre.rouge .kd");
    }

    @Test

    @Disabled("CodeRay is a Ruby gem, which is not supported by this implementation")
    public void should_render_with_coderay() {
        String html = asciidoctor.convert(DOCUMENT,
                sourceHighlighter("coderay"));

        Document doc = Jsoup.parse(html);

        assertThat(doc.select("pre.CodeRay span.class").size()).as("No elements were highlighted").isGreaterThan(0);
        assertThat(html).as("CSS was not added").contains(".CodeRay .class");
    }

    private static Options sourceHighlighter(String sourceHighlighter) {
        return Options.builder()
                .standalone(true)
                .safe(SafeMode.UNSAFE)
                .attributes(Attributes.builder()
                        .sourceHighlighter(sourceHighlighter)
                        .build())
                .build();
    }
}
