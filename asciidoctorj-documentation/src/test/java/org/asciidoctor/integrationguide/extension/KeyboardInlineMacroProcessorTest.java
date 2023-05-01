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
public class KeyboardInlineMacroProcessorTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;


    @Test
    public void should_create_keyboard_elements() {

        asciidoctor.javaExtensionRegistry().inlineMacro(KeyboardInlineMacroProcessor.class);       // <1>

        String result = asciidoctor.convert("ctrl:S[]", Options.builder().toFile(false).safe(SafeMode.UNSAFE).build());

        assertThat(
                result,
                containsString(
                        "<kbd>Ctrl</kbd>+<kbd>S</kbd>"));

    }
}
