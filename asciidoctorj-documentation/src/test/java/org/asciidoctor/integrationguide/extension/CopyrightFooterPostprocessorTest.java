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
public class CopyrightFooterPostprocessorTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("comment.adoc")
    private File commentDocument;

    @Test
    public void should_render_comments_as_notes() {

//tag::include[]
        File doc = //...
//end::include[]
            commentDocument;

//tag::include[]
        asciidoctor.javaExtensionRegistry().postprocessor(CopyrightFooterPostprocessor.class); // <1>

        String result =
                asciidoctor.convertFile(doc,
                        Options.builder()
                                .standalone(true)                                            // <2>
                                .toFile(false)
                                .build());

        assertThat(result, containsString(CopyrightFooterPostprocessor.COPYRIGHT_NOTICE));
//end::include[]
    }

}
