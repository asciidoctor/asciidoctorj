package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;


public class GistBlockMacroProcessorTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

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
