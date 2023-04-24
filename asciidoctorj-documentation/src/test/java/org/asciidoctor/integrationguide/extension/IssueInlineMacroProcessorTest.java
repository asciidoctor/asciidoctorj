package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;


public class IssueInlineMacroProcessorTest {

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
        File issueinlinemacro_adoc = //...
//end::include[]
                classpathResources.getResource("issue-inline-macro.adoc");
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
    public void should_create_anchor_elements_for_inline_macros_with_positional_attributes() {

        File issueinlinemacro_adoc = //...
                classpathResources.getResource("issue-inline-macro-positional.adoc");

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
