package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;


public class KeyboardInlineMacroProcessorTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

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
