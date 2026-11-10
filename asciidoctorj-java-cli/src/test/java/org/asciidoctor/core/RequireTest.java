package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the loading of extensions, converters and services from the load paths (-I) and the required classes and JAR
 * files (-r), which are compiled at runtime so that they aren't on the class path of the tests.
 */
@DisplayName("Require")
public class RequireTest extends CliTestHelper {

    private static final Map<String, String> SOURCES = Map.ofEntries(
            Map.entry("com/example/ShoutInlineMacroProcessor.java", """
                    package com.example;

                    import java.util.List;
                    import java.util.Map;

                    import org.asciidoctor.ast.PhraseNode;
                    import org.asciidoctor.ast.StructuralNode;
                    import org.asciidoctor.extension.InlineMacroProcessor;
                    import org.asciidoctor.extension.Name;

                    @Name("shout")
                    public class ShoutInlineMacroProcessor extends InlineMacroProcessor {
                        @Override
                        public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
                            return createPhraseNode(parent, "quoted", List.of(target.toUpperCase()));
                        }
                    }
                    """),
            Map.entry("com/example/UnsupportedProcessor.java", """
                    package com.example;

                    import org.asciidoctor.extension.BaseProcessor;

                    public class UnsupportedProcessor extends BaseProcessor {
                    }
                    """),
            Map.entry("com/example/FooterPostprocessor.java", """
                    package com.example;

                    import org.asciidoctor.ast.Document;
                    import org.asciidoctor.extension.Postprocessor;

                    public class FooterPostprocessor extends Postprocessor {
                        @Override
                        public String process(Document document, String output) {
                            return output + "<!-- postprocessed -->";
                        }
                    }
                    """),
            Map.entry("com/example/FooterExtensionRegistry.java", """
                    package com.example;

                    import org.asciidoctor.Asciidoctor;
                    import org.asciidoctor.extension.spi.ExtensionRegistry;

                    public class FooterExtensionRegistry implements ExtensionRegistry {
                        @Override
                        public void register(Asciidoctor asciidoctor) {
                            asciidoctor.javaExtensionRegistry().postprocessor(FooterPostprocessor.class);
                        }
                    }
                    """),
            Map.entry("com/example/TextConverter.java", """
                    package com.example;

                    import java.util.Map;

                    import org.asciidoctor.ast.ContentNode;
                    import org.asciidoctor.ast.Document;
                    import org.asciidoctor.converter.ConverterFor;
                    import org.asciidoctor.converter.StringConverter;

                    @ConverterFor("text")
                    public class TextConverter extends StringConverter {
                        public TextConverter(String backend, Map<String, Object> opts) {
                            super(backend, opts);
                        }

                        @Override
                        public String convert(ContentNode node, String transform, Map<Object, Object> opts) {
                            return node instanceof Document document ? getBackend() + ": " + document.getDoctitle() : "";
                        }
                    }
                    """),
            Map.entry("com/example/TextConverterRegistry.java", """
                    package com.example;

                    import org.asciidoctor.Asciidoctor;
                    import org.asciidoctor.converter.spi.ConverterRegistry;

                    public class TextConverterRegistry implements ConverterRegistry {
                        @Override
                        public void register(Asciidoctor asciidoctor) {
                            asciidoctor.javaConverterRegistry().register(TextConverter.class, "plain");
                        }
                    }
                    """));

    @TempDir
    static Path tmpDir;

    static Path classesDir;

    static Path jarFile;

    @BeforeAll
    static void compileExtensions() throws IOException {
        Path sourcesDir = tmpDir.resolve("src");
        classesDir = tmpDir.resolve("classes");
        Files.createDirectories(classesDir);
        List<String> args = new java.util.ArrayList<>(List.of("-d", classesDir.toString(),
                "-cp", System.getProperty("java.class.path")));
        for (Map.Entry<String, String> source : SOURCES.entrySet()) {
            Path file = sourcesDir.resolve(source.getKey());
            Files.createDirectories(file.getParent());
            Files.writeString(file, source.getValue());
            args.add(file.toString());
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertEquals(0, compiler.run(null, null, null, args.toArray(String[]::new)));
        Path servicesDir = classesDir.resolve("META-INF/services");
        Files.createDirectories(servicesDir);
        Files.writeString(servicesDir.resolve("org.asciidoctor.extension.spi.ExtensionRegistry"),
                "com.example.FooterExtensionRegistry\n");
        copyClasses(tmpDir.resolve("classes-without-services"));
        jarFile = tmpDir.resolve("extensions.jar");
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarFile));
                Stream<Path> files = Files.walk(classesDir)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                jar.putNextEntry(new JarEntry(classesDir.relativize(file).toString().replace('\\', '/')));
                Files.copy(file, (OutputStream) jar);
                jar.closeEntry();
            }
        }
    }

    @Test
    @DisplayName("should not load the services of the extensions that aren't on the class path")
    void shouldNotLoadTheServicesOfTheExtensionsThatArentOnTheClassPath() throws Exception {
        var invoker = invokeCliToBuffer(list("-s", "-o", "-"), "sample.adoc");
        assertEquals(0, invoker.getCode());
        assertFalse(invoker.readOutput().contains("<!-- postprocessed -->"));
    }

    @Test
    @DisplayName("should load the services of a JAR file added to the load path")
    void shouldLoadTheServicesOfAJarFileAddedToTheLoadPath() throws Exception {
        var invoker = invokeCliToBuffer(list("-I", jarFile.toString(), "-s", "-o", "-"), "sample.adoc");
        assertEquals(0, invoker.getCode());
        assertTrue(invoker.readOutput().stripTrailing().endsWith("<!-- postprocessed -->"));
    }

    @Test
    @DisplayName("should load the services of a directory added to the load path")
    void shouldLoadTheServicesOfADirectoryAddedToTheLoadPath() throws Exception {
        var invoker = invokeCliToBuffer(list("-I", classesDir.toString(), "-s", "-o", "-"), "sample.adoc");
        assertEquals(0, invoker.getCode());
        assertTrue(invoker.readOutput().stripTrailing().endsWith("<!-- postprocessed -->"));
    }

    @Test
    @DisplayName("should load the services of a required JAR file")
    void shouldLoadTheServicesOfARequiredJarFile() throws Exception {
        var invoker = invokeCliToBuffer(list("-r", jarFile.toString(), "-s", "-o", "-"), "sample.adoc");
        assertEquals(0, invoker.getCode());
        assertTrue(invoker.readOutput().stripTrailing().endsWith("<!-- postprocessed -->"));
    }

    @Test
    @DisplayName("should register the extensions of a required extension registry only once")
    void shouldRegisterTheExtensionsOfARequiredExtensionRegistryOnlyOnce() throws Exception {
        // NOTE the registry isn't a service of the directory (which has no META-INF/services)
        Path dir = tmpDir.resolve("classes-without-services");
        var invoker = invokeCliToBuffer(list("-I", dir.toString(), "-r", "com.example.FooterExtensionRegistry",
                "-s", "-o", "-"), "sample.adoc");
        assertEquals(0, invoker.getCode());
        String output = invoker.readOutput();
        assertTrue(output.stripTrailing().endsWith("<!-- postprocessed -->"));
        assertEquals(output.indexOf("<!-- postprocessed -->"), output.lastIndexOf("<!-- postprocessed -->"));
    }

    @Test
    @DisplayName("should register the converters of a required converter registry")
    void shouldRegisterTheConvertersOfARequiredConverterRegistry() throws Exception {
        var invoker = invokeCliToBuffer(list("-I", jarFile.toString(), "-r", "com.example.TextConverterRegistry",
                "-b", "plain", "-o", "-"), "sample.adoc");
        assertEquals(0, invoker.getCode());
        assertTrue(invoker.readOutput().startsWith("plain: Document Title"));
    }

    @Test
    @DisplayName("should register a required converter")
    void shouldRegisterARequiredConverter() throws Exception {
        var invoker = invokeCliToBuffer(list("-I", jarFile.toString(), "-r", "com.example.TextConverter",
                "-b", "text", "-o", "-"), "sample.adoc");
        assertEquals(0, invoker.getCode());
        assertTrue(invoker.readOutput().startsWith("text: Document Title"));
    }

    @Test
    @DisplayName("should register a required postprocessor")
    void shouldRegisterARequiredPostprocessor() throws Exception {
        var invoker = invokeCliToBuffer(list("-I", jarFile.toString(), "-r", "com.example.FooterPostprocessor",
                "-s", "-o", "-"), "sample.adoc");
        assertEquals(0, invoker.getCode());
        String output = invoker.readOutput().stripTrailing();
        // NOTE the postprocessor is also registered by the service of the JAR file
        assertTrue(output.endsWith("<!-- postprocessed --><!-- postprocessed -->"));
    }

    @Test
    @DisplayName("should register a required inline macro processor")
    void shouldRegisterARequiredInlineMacroProcessor() throws Exception {
        Path input = tmpDir.resolve("shout.adoc");
        Files.writeString(input, "Say shout:hello[]!");
        Path dir = tmpDir.resolve("classes-without-services");
        var invoker = invokeCliToBuffer(list("-I", dir.toString(), "-r", "com.example.ShoutInlineMacroProcessor",
                "-s", "-o", "-"), input.toString());
        assertEquals(0, invoker.getCode());
        assertTrue(invoker.readOutput().contains("<p>Say HELLO!</p>"));
    }

    @Test
    @DisplayName("should fail if the type of the required extension isn't supported")
    void shouldFailIfTheTypeOfTheRequiredExtensionIsntSupported() throws Exception {
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            var invoker = invokeCli(list("-I", jarFile.toString(), "-r", "com.example.UnsupportedProcessor"),
                    "sample.adoc");
            assertMatch(rx("'com.example.UnsupportedProcessor' could not be loaded"), err.getString());
            assertEquals(1, invoker.getCode());
        }
    }

    @Test
    @DisplayName("should fail if the required JAR file doesn't exist")
    void shouldFailIfTheRequiredJarFileDoesntExist() throws Exception {
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            var invoker = invokeCli(list("-r", "no-such-extensions.jar"), "sample.adoc");
            assertMatch(rx("'no-such-extensions.jar' could not be loaded\\n *Use --trace to show backtrace"),
                    err.getString());
            assertEquals(1, invoker.getCode());
        }
    }

    private static void copyClasses(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(classesDir)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                Path relative = classesDir.relativize(file);
                if (!relative.startsWith("META-INF")) {
                    Path target = dir.resolve(relative.toString());
                    Files.createDirectories(target.getParent());
                    Files.copy(file, target);
                }
            }
        }
    }
}
