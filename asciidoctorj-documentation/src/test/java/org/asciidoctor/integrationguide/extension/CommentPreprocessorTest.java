package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class CommentPreprocessorTest {

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
        File comment_adoc = //...
//end::include[]
            classpathResources.getResource("comment.adoc");

//tag::include[]
        File comment_with_note_adoc = //...
//end::include[]
            classpathResources.getResource("comment-with-note.adoc");
//tag::include[]
        asciidoctor.javaExtensionRegistry().preprocessor(CommentPreprocessor.class);      // <1>

        String result1 = asciidoctor.convertFile(comment_adoc, Options.builder().toFile(false).build());
        String result2 = asciidoctor.convertFile(comment_with_note_adoc, Options.builder().toFile(false).build());

        assertThat(result1, is(result2)); // <2>
//end::include[]
    }

}
