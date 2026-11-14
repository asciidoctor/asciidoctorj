package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.asciidoctor.test.extension.ClasspathHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WhenSlimTemplatesAreUsed {

    Asciidoctor asciidoctor = JRubyAsciidoctor.create();

    ClasspathHelper classpath = new ClasspathHelper(WhenSlimTemplatesAreUsed.class);

    @Test
    @DisplayName("the slim paragraph template should be used when rendering a document inline")
    void theSlimParagraphTemplateShouldBeUsedWhenRenderingADocumentInline() {
        // given:
        Options options = Options.builder()
                .templateDirs(classpath.getResource("src/custom-backends/slim"))
                .toFile(false)
                .standalone(false)
                .build();

        String sourceDocument = """

                = Hello World

                This will be replaced by static content from the template
                """;

        // when:
        String renderContent = asciidoctor.convert(sourceDocument, options);

        // then:
        Document doc = Jsoup.parse(renderContent, "UTF-8");
        Element paragraph = doc.select("p").first();
        assertThat(paragraph.text()).isEqualTo("This is static content");
    }

}
