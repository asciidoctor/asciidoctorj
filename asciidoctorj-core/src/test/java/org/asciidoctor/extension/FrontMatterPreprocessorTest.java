package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class FrontMatterPreprocessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @Test
    public void shouldRemoveFrontMatter() {
        // Given: the FrontMatterPreprocessor
        asciidoctor.javaExtensionRegistry().preprocessor(FrontMatterPreprocessor.class);

        // And a document with a front matter
        String document = "---\n" +
                "layout: info\n" +
                "permalink: /sample/\n" +
                "---\n" +
                "= Sample Page\n" +
                ":url-asciidoctor: http://asciidoctor.org\n" +
                "\n" +
                "This is a sample page composed in AsciiDoc.\n" +
                "Jekyll converts it to HTML using {url-asciidoctor}[Asciidoctor].\n" +
                "\n" +
                "[source,ruby]\n" +
                "puts \"Hello, World!\"";

        // If I parse the document
        Document ast = asciidoctor.load(document, Options.builder()
                .headerFooter(true)
                .toFile(false)
                .build());

        // Then: the preprocessor has set the attribute
        assertThat(ast.getAttribute("front-matter")).isEqualTo(Arrays.asList(
                "layout: info",
                "permalink: /sample/"
        ));

        // And: the processor has removed the front-matter from the converted document
        String html = ast.convert();
        assertThat(html).doesNotContain("permalink");
        assertThat(html).doesNotContain("layout");
        org.jsoup.nodes.Document htmlTree = Jsoup.parse(html);
        Element body = htmlTree.body();
        assertThat(body.getElementsByTag("h1").first().text())
                .isEqualTo("Sample Page");

        assertThat(body.getElementsByTag("p").first().text())
                .startsWith("This is a sample page");
    }
}
