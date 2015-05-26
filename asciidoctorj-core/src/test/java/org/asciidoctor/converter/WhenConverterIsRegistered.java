package org.asciidoctor.converter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.util.ClasspathResources;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WhenConverterIsRegistered {

    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

    @Rule
    public ClasspathResources classpath = new ClasspathResources();

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

    @Test
    public void shouldRegisterConverterViaConverterRegistryExecutor() throws Exception {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[]{classpath.getResource("serviceloadertest/3").toURI().toURL()}));
            asciidoctor = JRubyAsciidoctor.create();
            String result = asciidoctor.render("== Hello\n\nWorld!\n\n- a\n- b", OptionsBuilder.options().backend("extensiontext"));

            assertThat(result, is("== Hello ==\n\nWorld!\n\n-> a\n-> b\n"));
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }

}
