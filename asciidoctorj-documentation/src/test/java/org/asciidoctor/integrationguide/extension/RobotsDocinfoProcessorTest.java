package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class RobotsDocinfoProcessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

    @Test
    public void should_create_anchor_elements_for_inline_macros() {

//tag::include[]
        String src = "= Irrelevant content";

        asciidoctor.javaExtensionRegistry()
                .docinfoProcessor(RobotsDocinfoProcessor.class); // <1>

        String result = asciidoctor.convert(
                src,
                Options.builder()
                        .standalone(true)                        // <2>
                        .safe(SafeMode.SERVER)                   // <3>
                        .toFile(false)
                        .build());

        org.jsoup.nodes.Document document = Jsoup.parse(result); // <4>
        Element metaElement = document.head().children().last();
        assertThat(metaElement.tagName(), is("meta"));
        assertThat(metaElement.attr("name"), is("robots"));
        assertThat(metaElement.attr("content"), is("index,follow"));
//end::include[]
    }


}
