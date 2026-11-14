package org.asciidoctor.integrationguide.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.Test;


public class ContextMenuInlineMacroProcessorTest {

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    public void should_create_context_menu() {

        asciidoctor.javaExtensionRegistry().inlineMacro(ContextMenuInlineMacroProcessor.class);

        String result = asciidoctor.convert("rightclick:New|Class[]", Options.builder().toFile(false).safe(SafeMode.UNSAFE).build());

        assertThat(result).contains("<span class=\"menuseq\"><b class=\"menu\">Right click</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"submenu\">New</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Class</b></span>");

    }
}
