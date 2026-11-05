package org.asciidoctor.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.converter.ObjectConverter.ObjectConverterResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class WhenWritingConverterIsRegisteredTest {

    public static final String BACKEND_NAME_42 = "42";

    private Asciidoctor asciidoctor;

    @TempDir
    private File tmp;

    @BeforeEach
    public void setup() {
        asciidoctor = Asciidoctor.Factory.create();
    }

    @AfterEach
    public void cleanup() {
        WritingTextConverter.targetFile = null;
    }

    @Test
    @DisplayName("should_write_in_writable_converter_write_method")
    public void should_write_in_writable_converter_write_method() throws Exception {
        // given:
        WritingTextConverter.called = false;
        // Register as default converter
        asciidoctor.javaConverterRegistry().register(WritingTextConverter.class);

        File file = new File(tmp, "target.txt");

        String document = """
                == Hello

                World!

                - a
                - b
                """;

        try {
            // when:
            asciidoctor.convert(document, Options.builder().backend(WritingTextConverter.DEFAULT_FORMAT).toFile(file).safe(SafeMode.UNSAFE).build());

            // then:
            assertThat(Files.readString(file.toPath())).isEqualTo("""
                    == Hello ==

                    World!

                    -> a
                    -> b
                    """);

            assertThat(WritingTextConverter.called).isTrue();
        } finally {
            // cleanup:
            WritingTextConverter.called = false;
        }
    }

    private static final String DONT_CARE_DOCUMENT = "= Don't care";

    @Test
    @DisplayName("should convert to object format")
    public void shouldConvertToObjectFormat() {
        // given:
        asciidoctor.javaConverterRegistry().register(ObjectConverter.class);

        // when:
        ObjectConverterResult result = asciidoctor.convert(DONT_CARE_DOCUMENT, Options.builder().backend(BACKEND_NAME_42).build(), ObjectConverterResult.class);

        // then:
        assertThat(result.getValue()).isEqualTo(ObjectConverter.FIXED_RESULT);
        assertThat(result).isInstanceOf(ObjectConverterResult.class);
    }

    @Test
    @DisplayName("should write to stream")
    public void shouldWriteToStream() {
        // given:
        asciidoctor.javaConverterRegistry().register(ObjectConverter.class);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        // when:
        Options options = Options.builder().backend(BACKEND_NAME_42).toStream(bout).build();
        ObjectConverterResult result = asciidoctor.convert(DONT_CARE_DOCUMENT, options, ObjectConverterResult.class);

        // then:
        assertThat(new String(bout.toByteArray())).isEqualTo(new ObjectConverterResult(ObjectConverter.FIXED_RESULT).toString());
        assertThat(result).isNull();
    }

}
