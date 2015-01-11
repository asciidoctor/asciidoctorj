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
        asciidoctor.converterRegistry().unregisterAll();
    }

    @Test
    public void shouldCleanUpRegistry() {
        asciidoctor.converterRegistry().unregisterAll();

        assertThat(asciidoctor.converterRegistry().converters().keySet(), empty());
    }

    @Test
    public void shouldRegisterAndExecuteGivenConverter() {
        asciidoctor.converterRegistry().register(TextConverter.class, "test");

        String result = asciidoctor.render("== Hello\n\nWorld!\n\n- a\n- b", OptionsBuilder.options().backend("test"));

        assertThat(result, is("== Hello ==\n\nWorld!\n\n-> a\n-> b\n"));
    }

    @Test
    public void shouldReturnRegisteredConverter() {
        asciidoctor.converterRegistry().register(TextConverter.class, "test2");

        System.out.println(asciidoctor.converterRegistry().converters());
        assertEquals(TextConverter.class, asciidoctor.converterRegistry().converters().get("test2"));
    }

}
