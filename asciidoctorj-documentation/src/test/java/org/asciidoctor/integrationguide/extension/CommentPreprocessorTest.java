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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class CommentPreprocessorTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("comment.adoc")
    private File commentDocument;
    @ClasspathResource("comment-with-note.adoc")
    private File commentWithNoteDocument;

    @Test
    public void should_render_comments_as_notes() {

//tag::include[]
        File comment_adoc = //...
//end::include[]
                commentDocument;

//tag::include[]
        File comment_with_note_adoc = //...
//end::include[]
                commentWithNoteDocument;
//tag::include[]
        asciidoctor.javaExtensionRegistry().preprocessor(CommentPreprocessor.class);      // <1>

        String result1 = asciidoctor.convertFile(comment_adoc, Options.builder().toFile(false).build());
        String result2 = asciidoctor.convertFile(comment_with_note_adoc, Options.builder().toFile(false).build());

        assertThat(result1, is(result2)); // <2>
//end::include[]
    }

}
