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
public class LsIncludeProcessorTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("ls-include.adoc")
    private File lsincludeDocument;


    @Test
    public void should_create_anchor_elements_for_inline_macros() {

//tag::include[]
        File lsinclude_adoc = //...
//end::include[]
                lsincludeDocument;

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
