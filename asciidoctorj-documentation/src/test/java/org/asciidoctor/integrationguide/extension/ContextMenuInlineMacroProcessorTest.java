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
public class ContextMenuInlineMacroProcessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @Test
    public void should_create_context_menu() {

        asciidoctor.javaExtensionRegistry().inlineMacro(ContextMenuInlineMacroProcessor.class);

        String result = asciidoctor.convert("rightclick:New|Class[]", OptionsBuilder.options().toFile(false).safe(SafeMode.UNSAFE));


        assertThat(
                result,
                containsString(
                        "<span class=\"menuseq\"><b class=\"menu\">Right click</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"submenu\">New</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Class</b></span>"));

    }
}
