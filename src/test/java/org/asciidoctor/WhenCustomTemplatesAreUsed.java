package org.asciidoctor;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.asciidoctor.OptionsBuilder.options;

import java.io.File;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

public class WhenCustomTemplatesAreUsed {

    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();
    
    @Test
    public void document_should_be_rendered_using_given_template_dir() {
        
        Options options = options().templateDir(new File("target/test-classes/src/custom-backends/haml/html5-tweaks")).get();
        String renderContent = asciidoctor.renderFile(new File("target/test-classes/rendersample.asciidoc"), options);
        
        Document doc = Jsoup.parse(renderContent, "UTF-8");
        Element paragraph = doc.select("div.content").first();
        assertThat(paragraph, notNullValue());
    }
    
}
