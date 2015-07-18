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
public class GistBlockMacroProcessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

    @Test
    public void should_create_script_element_for_block_macro() {

//tag::include[]
        File gistmacro_adoc = //...
//end::include[]
                classpathResources.getResource("gist-macro.adoc");
//tag::include[]
        asciidoctor.javaExtensionRegistry().blockMacro(GistBlockMacroProcessor.class);      // <1>

        String result = asciidoctor.convertFile(gistmacro_adoc, OptionsBuilder.options().toFile(false));

        assertThat(
                result,
                containsString(
                        "<script src=\"https://gist.github.com/myaccount/1234abcd.js\">")); // <2>
//end::include[]
    }


}
