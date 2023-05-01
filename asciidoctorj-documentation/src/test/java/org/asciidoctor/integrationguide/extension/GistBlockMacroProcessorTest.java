package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class GistBlockMacroProcessorTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;


    @Test
    public void should_create_script_element_for_block_macro(@ClasspathResource("gist-macro.adoc") File gistMacroDocument) {

//tag::include[]
        File gistmacro_adoc = //...
//end::include[]
                gistMacroDocument;

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
    public void should_create_script_element_for_block_macro_with_positional_attributes(@ClasspathResource("gist-macro-attributes.adoc") File gistmacro_adoc) {

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
