package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
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

        String result = asciidoctor.convertFile(gistmacro_adoc, Options.builder().toFile(false).build());

        assertThat(
                result,
                containsString(
                        "<script src=\"https://gist.github.com/myaccount/1234abcd.js\">")); // <2>
//end::include[]
    }

    @Test
    public void should_create_script_element_for_block_macro_with_positional_attributes() {

        File gistmacro_adoc = classpathResources.getResource("gist-macro-attributes.adoc");
        asciidoctor.javaExtensionRegistry().blockMacro(GistBlockMacroPositionalAttributesProcessor.class);

        String result = asciidoctor.convertFile(gistmacro_adoc, Options.builder().toFile(false).build());

        assertThat(
                result,
                containsString(
                        "<script src=\"https://gist.github.com/myaccount/1234abcd.js\"/></script>"));
        assertThat(
                result,
                containsString(
                        "<script src=\"https://gitlab.com/-/snippets/2228798.js\"></script>"));
        assertThat(
                result,
                containsString(
                        "<script src=\"https://gitlab.com/gitlab-org/gitlab-foss/-/snippets/1717978.js\"></script>"));
        assertThat(
                result,
                containsString(
                        "<script src=\"https://gitlab.com/gitlab-org/gitlab-foss/-/snippets/1717979.js\"></script>"));
    }


}
