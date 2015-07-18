package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class KeyboardInlineMacroProcessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @Test
    public void should_create_keyboard_elements() {

        asciidoctor.javaExtensionRegistry().inlineMacro(KeyboardInlineMacroProcessor.class);       // <1>

        String result = asciidoctor.convert("ctrl:S[]", OptionsBuilder.options().toFile(false).safe(SafeMode.UNSAFE));

        assertThat(
                result,
                containsString(
                        "<kbd>Ctrl</kbd>+<kbd>S</kbd>"));

    }
}
