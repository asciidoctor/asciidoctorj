package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class ContextMenuInlineMacroProcessorTest {

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    public void should_create_context_menu() {

        asciidoctor.javaExtensionRegistry().inlineMacro(ContextMenuInlineMacroProcessor.class);

        String result = asciidoctor.convert("rightclick:New|Class[]", Options.builder().toFile(false).safe(SafeMode.UNSAFE).build());

        assertThat(
                result,
                containsString(
                        "<span class=\"menuseq\"><b class=\"menu\">Right click</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"submenu\">New</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Class</b></span>"));

    }
}
