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
public class IssueInlineMacroProcessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

    @Test
    public void should_create_anchor_elements_for_inline_macros() {

//tag::include[]
        File issueinlinemacro_adoc = //...
//end::include[]
                classpathResources.getResource("issue-inline-macro.adoc");
//tag::include[]
        asciidoctor.javaExtensionRegistry().inlineMacro(IssueInlineMacroProcessor.class);       // <1>

        String result = asciidoctor.convertFile(issueinlinemacro_adoc, OptionsBuilder.options().toFile(false));

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


}
