package org.asciidoctor.extension;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.io.File;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

public class WhenExtensionIsRegistered {

    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

    @Test
    public void a_preprocessor_should_be_executed_before_document_is_rendered() {
        asciidoctor.preprocessor(FrontMatterPreprocessorExtension.class);

        String content = asciidoctor.renderFile(new File(
                "target/test-classes/render-with-front-matter.adoc"),
                new Options());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class",
                "content").first();
        assertThat(contentElement.text(), is("---\n"
                + "[tags: [announcement, website]]\n" + "---"));

    }

}
