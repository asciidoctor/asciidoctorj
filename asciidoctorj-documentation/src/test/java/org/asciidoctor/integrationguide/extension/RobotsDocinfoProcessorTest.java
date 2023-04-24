package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.util.ClasspathHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class RobotsDocinfoProcessorTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

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
