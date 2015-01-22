package org.asciidoctor.converter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WhenConverterIsRegistered {

    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

    @After
    public void cleanUp() {
        asciidoctor.javaConverterRegistry().unregisterAll();
    }

    @Test
    public void shouldCleanUpRegistry() {
        asciidoctor.javaConverterRegistry().unregisterAll();

        assertThat(asciidoctor.javaConverterRegistry().converters().keySet(), empty());
    }

    @Test
    public void shouldRegisterAndExecuteGivenConverter() {
        asciidoctor.javaConverterRegistry().register(TextConverter.class, "test");

        String result = asciidoctor.render("== Hello\n\nWorld!\n\n- a\n- b", OptionsBuilder.options().backend("test"));

        assertThat(result, is("== Hello ==\n\nWorld!\n\n-> a\n-> b\n"));
    }

    @Test
    public void shouldReturnRegisteredConverter() {
        asciidoctor.javaConverterRegistry().register(TextConverter.class, "test2");
        assertEquals(TextConverter.class, asciidoctor.javaConverterRegistry().converters().get("test2"));
    }

}
