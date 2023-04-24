package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;


public class CopyrightFooterPostprocessorTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

    @Test
    public void should_render_comments_as_notes() {

//tag::include[]
        File doc = //...
//end::include[]
            classpathResources.getResource("comment.adoc");

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
