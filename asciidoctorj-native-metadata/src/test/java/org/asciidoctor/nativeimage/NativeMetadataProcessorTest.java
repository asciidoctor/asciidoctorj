package org.asciidoctor.nativeimage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NativeMetadataProcessorTest {

    @TempDir
    Path folder;

    @Test
    void shouldCreateTheFileWithTheEntriesOfTheAnnotations() throws IOException {
        Path metadata = folder.resolve("native-image/reachability-metadata.json");

        compile(metadata, """
                package sample;
                import org.asciidoctor.nativeimage.*;
                @RegisterClass({Sample.Inner.class, Sample.class})
                @RegisterDirectory("data")
                public class Sample {
                    public static class Inner {}
                }
                """);

        assertThat(Files.readString(metadata)).isEqualTo("""
                {
                  "reflection": [
                    {
                      "type": "sample.Sample$Inner",
                      "allDeclaredConstructors": true,
                      "allPublicConstructors": true
                    },
                    {
                      "type": "sample.Sample",
                      "allDeclaredConstructors": true,
                      "allPublicConstructors": true
                    }
                  ],
                  "resources": [
                    {
                      "glob": "data"
                    },
                    {
                      "glob": "data/**"
                    }
                  ]
                }
                """);
    }

    @Test
    void shouldRegisterOnlyTheConstructorsByDefault() throws IOException {
        Path metadata = folder.resolve("reachability-metadata.json");

        compile(metadata, """
                package sample;
                import org.asciidoctor.nativeimage.*;
                @RegisterClass(Sample.class)
                public class Sample {}
                """);

        assertThat(Files.readString(metadata)).isEqualTo("""
                {
                  "reflection": [
                    {
                      "type": "sample.Sample",
                      "allDeclaredConstructors": true,
                      "allPublicConstructors": true
                    }
                  ]
                }
                """);
    }

    @Test
    void shouldRegisterTheMethodsAndTheFieldsWhenTheyAreSelected() throws IOException {
        Path metadata = folder.resolve("reachability-metadata.json");

        compile(metadata, """
                package sample;
                import org.asciidoctor.nativeimage.*;
                @RegisterClass(value = {Sample.class, Sample.Data.class}, constructors = false, methods = true)
                @RegisterClass(value = Sample.Data.class, constructors = false, fields = true)
                public class Sample {
                    public record Data(String name) {}
                }
                """);

        assertThat(Files.readString(metadata)).isEqualTo("""
                {
                  "reflection": [
                    {
                      "type": "sample.Sample",
                      "allPublicMethods": true
                    },
                    {
                      "type": "sample.Sample$Data",
                      "allPublicMethods": true,
                      "allDeclaredFields": true
                    }
                  ]
                }
                """);
    }

    @Test
    void shouldMergeTheRepeatedAnnotationsOfTheSameClass() throws IOException {
        Path metadata = folder.resolve("reachability-metadata.json");

        compile(metadata, """
                package sample;
                import org.asciidoctor.nativeimage.*;
                @RegisterClass(Sample.class)
                @RegisterClass(value = Sample.class, constructors = false, methods = true)
                public class Sample {}
                """);

        assertThat(Files.readString(metadata)).isEqualTo("""
                {
                  "reflection": [
                    {
                      "type": "sample.Sample",
                      "allDeclaredConstructors": true,
                      "allPublicConstructors": true,
                      "allPublicMethods": true
                    }
                  ]
                }
                """);
    }

    @Test
    void shouldKeepTheExistingEntriesAndNotRepeatThem() throws IOException {
        Path metadata = folder.resolve("reachability-metadata.json");
        Files.writeString(metadata, """
                {
                  "reflection": [
                    {
                      "type": "sample.Sample",
                      "allDeclaredMethods": true
                    },
                    {
                      "type": "other.Type"
                    }
                  ],
                  "bundles": [
                    {
                      "name": "a.Bundle"
                    }
                  ],
                  "resources": [
                    {
                      "glob": "data"
                    }
                  ]
                }
                """);

        compile(metadata, """
                package sample;
                import org.asciidoctor.nativeimage.*;
                @RegisterClass(Sample.class)
                @RegisterFile({"data", "other/**"})
                public class Sample {}
                """);

        assertThat(Files.readString(metadata)).isEqualTo("""
                {
                  "reflection": [
                    {
                      "type": "sample.Sample",
                      "allDeclaredMethods": true,
                      "allDeclaredConstructors": true,
                      "allPublicConstructors": true
                    },
                    {
                      "type": "other.Type"
                    }
                  ],
                  "bundles": [
                    {
                      "name": "a.Bundle"
                    }
                  ],
                  "resources": [
                    {
                      "glob": "data"
                    },
                    {
                      "glob": "other/**"
                    }
                  ]
                }
                """);
    }

    @Test
    void shouldNotChangeTheFileWhenThereAreNoAnnotations() throws IOException {
        Path metadata = folder.resolve("reachability-metadata.json");
        String original = "{\"resources\":[{\"glob\":\"a\"}]}";
        Files.writeString(metadata, original);

        compile(metadata, """
                package sample;
                public class Sample {}
                """);

        assertThat(Files.readString(metadata)).isEqualTo(original);
    }

    @Test
    void shouldNotWriteAnyFileWithoutTheOption() throws IOException {
        compile(null, """
                package sample;
                import org.asciidoctor.nativeimage.*;
                @RegisterFile("data")
                public class Sample {}
                """);

        try (var files = Files.walk(folder)) {
            assertThat(files.filter(path -> path.toString().endsWith(".json"))).isEmpty();
        }
    }

    @Test
    void shouldAddTwoEntriesForADirectoryAndOneEntryForAFile() throws IOException {
        Path metadata = folder.resolve("reachability-metadata.json");

        compile(metadata, """
                package sample;
                import org.asciidoctor.nativeimage.*;
                @RegisterDirectory({"data/inner", "other/"})
                @RegisterFile({"file.txt", "glob/**"})
                public class Sample {}
                """);

        assertThat(Files.readString(metadata)).isEqualTo("""
                {
                  "resources": [
                    {
                      "glob": "data/inner"
                    },
                    {
                      "glob": "data/inner/**"
                    },
                    {
                      "glob": "other"
                    },
                    {
                      "glob": "other/**"
                    },
                    {
                      "glob": "file.txt"
                    },
                    {
                      "glob": "glob/**"
                    }
                  ]
                }
                """);
    }

    private void compile(Path metadata, String source) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///sample/Sample.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        Path classes = Files.createDirectories(folder.resolve("classes-output"));
        List<String> options = new java.util.ArrayList<>(List.of(
                "-d", classes.toString(),
                "-classpath", System.getProperty("java.class.path"),
                "-processor", NativeMetadataProcessor.class.getName()));
        if (metadata != null) {
            options.add("-A" + NativeMetadataProcessor.OPTION + "=" + metadata);
        }
        Boolean success = compiler.getTask(null, null, null, options, null, List.of(file)).call();
        assertThat(success).isTrue();
    }
}
