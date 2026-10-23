package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenATreeProcessorCreatesNodesTest {

    public static final String NEW_CODE = "puts \"World\"";

    public static final String OLD_CODE = "puts \"Hello\"";

    public static final String ATTR_VALUE_LANGUAGE_RUBY = "language-ruby";

    public static final String ELEM_CODE = "code";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    // Issue: https://github.com/asciidoctor/asciidoctorj/issues/513
    @Test
    @DisplayName("it should be able to copy attributes from another node")
    public void itShouldBeAbleToCopyAttributesFromAnotherNode() {

        // given: A Asciidoctor document with one Ruby source block
        String document = """
                == Test doc

                [source,ruby]
                ----
                puts "Hello"
                ----
                """;

        // and: a Treeprocessor that creates an additional source block passing the same attributes
        Treeprocessor tp = new Treeprocessor() {
            @Override
            public Document process(Document doc) {
                StructuralNode newSourceBlock = createBlock(doc.getBlocks().get(0), "listing", NEW_CODE, doc.getBlocks().get(0).getBlocks().get(0).getAttributes());
                // The style 'source' is visible in the attributes, but for new blocks it has to be set individually as a object property.
                newSourceBlock.setStyle("source");
                doc.getBlocks().get(0).append(newSourceBlock);
                // NOTE the Groovy method returned the result of append (null)
                return null;
            }
        };
        asciidoctor.javaExtensionRegistry().treeprocessor(tp);

        // when: The document is converted
        String html = asciidoctor.convert(document, Options.builder().standalone(true).build());

        // then: The second source block uses the same value of the language attribute and highlight as Ruby
        org.jsoup.nodes.Document htmlDocument = Jsoup.parse(html);
        Elements elements = htmlDocument.getElementsByTag(ELEM_CODE);
        assertThat(elements.size()).isEqualTo(2);
        assertThat(elements.get(0).hasClass(ATTR_VALUE_LANGUAGE_RUBY)).isTrue();
        assertThat(elements.get(0).text()).isEqualTo(OLD_CODE);
        assertThat(elements.get(1).hasClass(ATTR_VALUE_LANGUAGE_RUBY)).isTrue();
        assertThat(elements.get(1).text()).isEqualTo(NEW_CODE);
    }

}
