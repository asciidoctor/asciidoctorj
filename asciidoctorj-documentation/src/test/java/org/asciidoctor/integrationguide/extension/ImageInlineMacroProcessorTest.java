package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(AsciidoctorExtension.class)
public class ImageInlineMacroProcessorTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;


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
