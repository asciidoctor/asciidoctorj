package org.asciidoctor.converter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.MemoryLogHandler;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenConverterIsRegistered {

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    @ClasspathResource("simple.adoc")
    private File simpleDocument;


//    @After
//    public void cleanUp() {
//        asciidoctor.javaConverterRegistry().unregisterAll();
//    }

    @Test
    public void shouldCleanUpRegistry() {
        asciidoctor.javaConverterRegistry().unregisterAll();

        assertThat(asciidoctor.javaConverterRegistry().converters().keySet(), empty());
    }

    @Test
    public void shouldRegisterAndExecuteGivenConverter() {
        asciidoctor.javaConverterRegistry().register(TextConverter.class, "test");

        String result = asciidoctor.convert("== Hello\n\nWorld!\n\n- a\n- b", Options.builder().backend("test").build());

        assertThat(result, is("== Hello ==\n\nWorld!\n\n-> a\n-> b\n"));
    }

    @Test
    public void shouldRegisterWithBackendNameFromAnnotation() {
        // Register as default converter
        asciidoctor.javaConverterRegistry().register(TextConverter.class);
        asciidoctor.javaConverterRegistry().register(DummyConverter.class);

        String result = asciidoctor.convert("== Hello\n\nWorld!\n\n- a\n- b", Options.builder().backend(TextConverter.DEFAULT_FORMAT).build());

        assertThat(result, is("== Hello ==\n\nWorld!\n\n-> a\n-> b\n"));
    }

    @Test
    public void shouldUseDefaultBackend() {
        // Register as default converter
        asciidoctor.javaConverterRegistry().register(DummyConverter.class);

        String result = asciidoctor.convert("== Hello\n\nWorld!\n\n- a\n- b", Options.builder().backend("Undefined").build());

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

            asciidoctor.convert("== Hello\n\nWorld!\n\n- a\n- b", Options.builder().backend(TextConverter.DEFAULT_FORMAT).build());

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
                for (StructuralNode node : block.getBlocks()) {
                    processNode(node);
                }
            }
        });
        String result = asciidoctor.convert("= Test\n" +
                "\n" +
                "== Test" +
                "\n" +
                "== Test2\n", Options.builder().backend("test3").standalone(false).build());

        assertEquals("== 1 Test ==\n" +
                "== 42 Test2 ==", result);
    }

    @Test
    public void shouldRegisterConverterViaConverterRegistryExecutor(
            @ClasspathResource("serviceloadertest/3") File serviceLoader) throws MalformedURLException {

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[]{serviceLoader.toURI().toURL()}));
            asciidoctor = JRubyAsciidoctor.create();
            String result = asciidoctor.convert("== Hello\n\nWorld!\n\n- a\n- b", Options.builder().backend("extensiontext").build());

            assertThat(result, is("== Hello ==\n\nWorld!\n\n-> a\n-> b\n"));
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    @Test
    public void shouldWriteFileWithSuffixFromConverterWithAnnotation(@TempDir File tempDir) {
        asciidoctor.javaConverterRegistry().register(TextConverter.class);

        asciidoctor.convertFile(simpleDocument, Options.builder().backend(TextConverter.DEFAULT_FORMAT).toDir(tempDir).safe(SafeMode.UNSAFE).build());

        assertThat(new File(tempDir, "simple.html").exists(), is(false));
        assertThat(new File(tempDir, "simple.txt").exists(), is(true));
    }

    @Test
    public void shouldWriteFileWithSuffixFromConverterThatInvokesSetOutfileSuffix(@TempDir File tempDir) {
        asciidoctor.javaConverterRegistry().register(TextConverterWithSuffix.class);

        asciidoctor.convertFile(simpleDocument, Options.builder().backend(TextConverterWithSuffix.DEFAULT_FORMAT).toDir(tempDir).safe(SafeMode.UNSAFE).build());

        assertThat(new File(tempDir, "simple.html").exists(), is(false));
        assertThat(new File(tempDir, "simple.text").exists(), is(true));
    }
}
