package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.util.ClasspathResources;
import org.hamcrest.Matcher;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class CommentPreprocessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

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

        String result1 = asciidoctor.convertFile(comment_adoc, OptionsBuilder.options().toFile(false));
        String result2 = asciidoctor.convertFile(comment_with_note_adoc, OptionsBuilder.options().toFile(false));

        assertThat(result1, is(result2)); // <2>
//end::include[]
    }

}
