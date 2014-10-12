package org.asciidoctor;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.util.ClasspathResources;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Rule;
import org.junit.Test;

public class WhenCustomTemplatesAreUsed {

    @Rule
    public ClasspathResources classpath = new ClasspathResources();
    
    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();
    
    @Test
    public void document_should_be_rendered_using_given_template_dir() {
        
        Options options = options().templateDir(classpath.getResource("src/custom-backends/haml/html5-tweaks")).toFile(false).get();
        String renderContent = asciidoctor.renderFile(classpath.getResource("rendersample.asciidoc"), options);
        
        Document doc = Jsoup.parse(renderContent, "UTF-8");
        Element paragraph = doc.select("div.content").first();
        assertThat(paragraph, notNullValue());
    }
    
}
