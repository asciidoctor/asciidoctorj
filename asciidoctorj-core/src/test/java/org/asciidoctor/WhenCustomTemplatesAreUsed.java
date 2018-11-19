package org.asciidoctor;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WhenCustomTemplatesAreUsed {

    @ArquillianResource
    private ClasspathResources classpath;

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;
    
    @Test
    public void document_should_be_rendered_using_given_template_dir() {
        
        Options options = options().templateDir(classpath.getResource("src/custom-backends/haml/html5-tweaks")).toFile(false).get();
        String renderContent = asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"), options);
        
        Document doc = Jsoup.parse(renderContent, "UTF-8");
        Element paragraph = doc.select("div.content").first();
        assertThat(paragraph, notNullValue());
    }
    
}
