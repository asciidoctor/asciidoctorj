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
public class IssueInlineMacroProcessorTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;


    @Test
    public void should_create_anchor_elements_for_inline_macros(
            @ClasspathResource("issue-inline-macro.adoc") File issueinlinemacro) {

//tag::include[]
        File issueinlinemacro_adoc = //...
//end::include[]
                issueinlinemacro;

//tag::include[]
        asciidoctor.javaExtensionRegistry().inlineMacro(IssueInlineMacroProcessor.class);       // <1>

        String result = asciidoctor.convertFile(issueinlinemacro_adoc, Options.builder().toFile(false).build());

        assertThat(
                result,
                containsString(
                        "<a href=\"https://github.com/asciidoctor/asciidoctorj/issues/333\"")); // <2>

        assertThat(
                result,
                containsString(                                                                 // <2>
                        "<a href=\"https://github.com/asciidoctor/asciidoctorj-groovy-dsl/issues/2\""));

//end::include[]
    }

    @Test
    public void should_create_anchor_elements_for_inline_macros_with_positional_attributes(
            @ClasspathResource("issue-inline-macro-positional.adoc") File issueinlinemacro) {

        File issueinlinemacro_adoc = //...
                issueinlinemacro;

        asciidoctor.javaExtensionRegistry().inlineMacro(IssueInlineMacroPositionalAttributesProcessor.class);

        String result = asciidoctor.convertFile(issueinlinemacro_adoc, Options.builder().toFile(false).build());

        assertThat(
                result,
                containsString(
                        "<a href=\"https://github.com/asciidoctor/asciidoctorj/issues/334\"")); // <2>

        assertThat(
                result,
                containsString(                                                                 // <2>
                        "<a href=\"https://github.com/asciidoctor/asciidoctorj-groovy-dsl/issues/3\""));

    }

}
