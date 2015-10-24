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
public class LsIncludeProcessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

    @Test
    public void should_create_anchor_elements_for_inline_macros() {

//tag::include[]
        File lsinclude_adoc = //...
//end::include[]
                classpathResources.getResource("ls-include.adoc");
//tag::include[]

        String firstFileName = new File(".").listFiles()[0].getName();

        asciidoctor.javaExtensionRegistry().includeProcessor(LsIncludeProcessor.class);       // <1>

        String result = asciidoctor.convertFile(lsinclude_adoc, OptionsBuilder.options().toFile(false));

        assertThat(
                result,
                containsString(firstFileName));
//end::include[]
    }


}
