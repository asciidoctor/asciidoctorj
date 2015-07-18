package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class CopyrightFooterPostprocessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

    @Test
    public void should_render_comments_as_notes() {

//tag::include[]
        File doc = //...
//end::include[]
            classpathResources.getResource("comment.adoc");

//tag::include[]
        asciidoctor.javaExtensionRegistry().postprocessor(CopyrightFooterPostprocessor.class); // <1>

        String result =
                asciidoctor.convertFile(doc,
                        OptionsBuilder.options()
                                .headerFooter(true)                                            // <2>
                                .toFile(false));

        assertThat(result, containsString(CopyrightFooterPostprocessor.COPYRIGHT_NOTICE));
//end::include[]
    }

}
