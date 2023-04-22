package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;


public class LsIncludeProcessorTest {

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
        File lsinclude_adoc = //...
//end::include[]
                classpathResources.getResource("ls-include.adoc");
//tag::include[]

        String firstFileName = new File(".").listFiles()[0].getName();

        asciidoctor.javaExtensionRegistry().includeProcessor(LsIncludeProcessor.class);       // <1>

        String result = asciidoctor.convertFile(lsinclude_adoc, Options.builder().toFile(false).build());

        assertThat(
                result,
                containsString(firstFileName));
//end::include[]
    }

}
