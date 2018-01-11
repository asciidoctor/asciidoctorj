package org.asciidoctor;

import org.asciidoctor.util.ClasspathResources;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WhenSlimTemplatesAreUsed {

    @Rule
    public ClasspathResources classpath = new ClasspathResources();

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    public void the_slim_paragraph_template_should_be_used_when_rendering_a_document_inline() throws Exception {

        Options options = OptionsBuilder.options().templateDir(classpath.getResource("src/custom-backends/slim")).toFile(false).headerFooter(false).get();

        String sourceDocument = "= Hello World\n" +
                "\n" +
                "This will be replaced by static content from the template";

        String renderContent = asciidoctor.render(sourceDocument, options);

        Element doc = Jsoup.parse(renderContent, "UTF-8");
        Element paragraph = doc.select("p").first();
        assertEquals("This is static content", paragraph.text());
    }
}
