package org.asciidoctor;

import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Converter templates (Tilt, here Slim) are not supported by this implementation")
@ExtendWith(ClasspathExtension.class)
public class WhenSlimTemplatesAreUsedTest {

    Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    @DisplayName("the slim paragraph template should be used when rendering a document inline")
    void theSlimParagraphTemplateShouldBeUsedWhenRenderingADocumentInline(@ClasspathResource("src/custom-backends/slim") File slimTemplates) {
        // given:
        Options options = Options.builder()
                .templateDirs(slimTemplates)
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
