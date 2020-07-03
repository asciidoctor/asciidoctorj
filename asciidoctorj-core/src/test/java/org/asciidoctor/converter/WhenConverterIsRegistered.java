package org.asciidoctor.converter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.MemoryLogHandler;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class WhenConverterIsRegistered {

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpath;

    @ArquillianResource
    private TemporaryFolder tmp;

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

        String result = asciidoctor.convert("== Hello\n\nWorld!\n\n- a\n- b", OptionsBuilder.options().backend("test"));

        assertThat(result, is("== Hello ==\n\nWorld!\n\n-> a\n-> b\n"));
    }

    @Test
    public void shouldRegisterWithBackendNameFromAnnotation() {
        // Register as default converter
        asciidoctor.javaConverterRegistry().register(TextConverter.class);
        asciidoctor.javaConverterRegistry().register(DummyConverter.class);

        String result = asciidoctor.convert("== Hello\n\nWorld!\n\n- a\n- b", OptionsBuilder.options().backend(TextConverter.DEFAULT_FORMAT));

        assertThat(result, is("== Hello ==\n\nWorld!\n\n-> a\n-> b\n"));
    }

    @Test
    public void shouldUseDefaultBackend() {
        // Register as default converter
        asciidoctor.javaConverterRegistry().register(DummyConverter.class);

        String result = asciidoctor.convert("== Hello\n\nWorld!\n\n- a\n- b", OptionsBuilder.options().backend("Undefined"));

        assertThat(result, is("Dummy"));
    }

    private MemoryLogHandler registerMemoryLogHandler() {
        final Logger logger = Logger.getLogger("asciidoctor");
        final MemoryLogHandler handler = new MemoryLogHandler();
        logger.addHandler(handler);
        return handler;
    }

    @Test
    public void shouldBeAbleToLog() {
        MemoryLogHandler handler = registerMemoryLogHandler();
        try {
            asciidoctor.javaConverterRegistry().register(TextConverter.class);

            String result = asciidoctor.convert("== Hello\n\nWorld!\n\n- a\n- b", OptionsBuilder.options().backend(TextConverter.DEFAULT_FORMAT));

            assertThat(handler.getLogRecords(), hasSize(1));
            assertThat(handler.getLogRecords().get(0).getMessage(), is("Now we're logging"));
        } finally {
            final Logger logger = Logger.getLogger("asciidoctor");
            logger.removeHandler(handler);
        }
    }


    @Test
    public void shouldReturnConverterRegisteredWithAnnotation() {
        asciidoctor.javaConverterRegistry().register(TextConverter.class);
        assertEquals(TextConverter.class, asciidoctor.javaConverterRegistry().converters().get(TextConverter.DEFAULT_FORMAT));
    }


    @Test
    public void shouldReturnRegisteredConverter() {
        asciidoctor.javaConverterRegistry().register(TextConverter.class, "test2");
        assertEquals(TextConverter.class, asciidoctor.javaConverterRegistry().converters().get("test2"));
    }

    @Test
    public void shouldReceiveChangedLevels() {
        asciidoctor.javaConverterRegistry().register(LevelConverter.class, "test3");
        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
            @Override
            public Document process(Document document) {
                processNode(document);
                return document;
            }

            void processNode(StructuralNode block) {
                if (block instanceof Section && block.getTitle().equals("Test2")) {
                    block.setLevel(42);
                }
                for (StructuralNode node: block.getBlocks()) {
                    processNode(node);
                }
            }
        });
        String result = asciidoctor.convert("= Test\n" +
                "\n" +
                "== Test" +
                "\n" +
                "== Test2\n", OptionsBuilder.options().backend("test3").headerFooter(false));

        assertEquals("== 1 Test ==\n" +
                "== 42 Test2 ==", result);
    }

    @Test
    public void shouldRegisterConverterViaConverterRegistryExecutor() throws Exception {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[]{classpath.getResource("serviceloadertest/3").toURI().toURL()}));
            asciidoctor = JRubyAsciidoctor.create();
            String result = asciidoctor.convert("== Hello\n\nWorld!\n\n- a\n- b", OptionsBuilder.options().backend("extensiontext"));

            assertThat(result, is("== Hello ==\n\nWorld!\n\n-> a\n-> b\n"));
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    @Test
    public void shouldWriteFileWithSuffixFromConverterWithAnnotation() throws Exception {

        asciidoctor.javaConverterRegistry().register(TextConverter.class);

        File todir = tmp.newFolder();
        asciidoctor.convertFile(classpath.getResource("simple.adoc"), OptionsBuilder.options().backend(TextConverter.DEFAULT_FORMAT).toDir(todir).safe(SafeMode.UNSAFE));

        assertThat(new File(todir, "simple.html").exists(), is(false));
        assertThat(new File(todir, "simple.txt").exists(), is(true));

    }

    @Test
    public void shouldWriteFileWithSuffixFromConverterThatInvokesSetOutfileSuffix() throws Exception {

        asciidoctor.javaConverterRegistry().register(TextConverterWithSuffix.class);

        File todir = tmp.newFolder();
        asciidoctor.convertFile(classpath.getResource("simple.adoc"), OptionsBuilder.options().backend(TextConverterWithSuffix.DEFAULT_FORMAT).toDir(todir).safe(SafeMode.UNSAFE));

        assertThat(new File(todir, "simple.html").exists(), is(false));
        assertThat(new File(todir, "simple.text").exists(), is(true));

    }

}
