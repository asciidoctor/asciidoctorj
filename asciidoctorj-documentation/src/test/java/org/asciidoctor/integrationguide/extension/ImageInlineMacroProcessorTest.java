package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;


public class ImageInlineMacroProcessorTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

    @Test
    public void should_create_context_menu() {

        asciidoctor.javaExtensionRegistry().inlineMacro(ImageInlineMacroProcessor.class);

        String result = asciidoctor.convert("foo:1234[]", Options.builder().toFile(false).safe(SafeMode.UNSAFE).build());

        assertThat(
                result,
                containsString(
                        "<img src=\"http://foo.bar/1234\" alt=\"Image not available\" width=\"64\" height=\"64\">"));
    }
}
