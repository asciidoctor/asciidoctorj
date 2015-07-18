package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class ImageInlineMacroProcessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @Test
    public void should_create_context_menu() {

        asciidoctor.javaExtensionRegistry().inlineMacro(ImageInlineMacroProcessor.class);

        String result = asciidoctor.convert("foo:1234[]", OptionsBuilder.options().toFile(false).safe(SafeMode.UNSAFE));

        assertThat(
                result,
                containsString(
                        "<img src=\"http://foo.bar/1234\" alt=\"Image not available\" width=\"64\" height=\"64\">"));
    }
}
