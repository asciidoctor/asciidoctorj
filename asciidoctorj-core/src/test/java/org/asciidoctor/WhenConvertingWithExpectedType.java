package org.asciidoctor;

import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.ast.Document;
import org.asciidoctor.converter.ObjectConverter;
import org.asciidoctor.converter.ObjectConverter.ObjectConverterResult;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TLDR;
 * - when using `toFile`, `toStream` returns Document, otherwise String.
 * - Java converter returns custom type unless `toFile` ot `toStream` is set.
 */
@RunWith(Arquillian.class)
public class WhenConvertingWithExpectedType {

    private static final String SAMPLE_CONTENT = "= Document Title\n\nThis is the preamble\n";

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private TemporaryFolder testFolder;

    @Test
    public void shouldReturnNullWhenExpectedTypeIsStringButSetDocument() {
        Options options = Options.builder()
                .build();

        Document inRealityIsString = asciidoctor.convert(SAMPLE_CONTENT, options, Document.class);

        assertThat(inRealityIsString).isNull();
    }

    @Test
    public void shouldReturnNullWhenExpectedTypeIsDocumentButSetString() throws IOException {
        Options options = Options.builder()
                .safe(SafeMode.UNSAFE)
                .toFile(testFolder.newFile())
                .build();

        String inRealityIsDocument = asciidoctor.convert(SAMPLE_CONTENT, options, String.class);

        assertThat(inRealityIsDocument).isNull();
    }

    @Test
    public void shouldReturnNullWhenExpectedTypeIsDocumentButSetStringWithToStream() {
        final OutputStream os = new ByteArrayOutputStream();
        Options options = Options.builder()
                .toStream(os)
                .build();

        String document = asciidoctor.convert(SAMPLE_CONTENT, options, String.class);

        assertThat(document)
                .isNull();
    }

    @Test
    public void shouldReturnStringWhenUsingToFile() {
        Options options = Options.builder()
                .build();

        String content = asciidoctor.convert(SAMPLE_CONTENT, options, String.class);

        assertThat(content)
                .isNotEmpty()
                .isInstanceOf(String.class);
    }

    @Test
    public void shouldReturnDocumentWhenUsingToFile() throws IOException {
        Options options = Options.builder()
                .safe(SafeMode.UNSAFE)
                .toFile(testFolder.newFile())
                .build();

        Document document = asciidoctor.convert(SAMPLE_CONTENT, options, Document.class);

        assertThat(document)
                .isNotNull()
                .isInstanceOf(Document.class);
    }

    @Test
    public void shouldReturnDocumentWhenUsingToStream() {
        Options options = Options.builder()
                .safe(SafeMode.UNSAFE)
                .toStream(new ByteArrayOutputStream())
                .build();

        Document document = asciidoctor.convert(SAMPLE_CONTENT, options, Document.class);

        assertThat(document)
                .isNotNull()
                .isInstanceOf(Document.class);
    }

    @Test
    public void shouldReturnCustomTypeWhenUsingJavaConverter() {
        Options options = Options.builder()
                .backend(ObjectConverter.BACKEND)
                .build();

        asciidoctor.javaConverterRegistry().register(ObjectConverter.class);
        ObjectConverterResult document = asciidoctor.convert(SAMPLE_CONTENT, options, ObjectConverterResult.class);

        assertThat(document)
                .isNotNull()
                .isInstanceOf(ObjectConverterResult.class)
                .hasFieldOrPropertyWithValue("value", ObjectConverter.FIXED_RESULT);
    }


    @Test
    public void shouldReturnNullWhenExpectingCustomTypeAndUsingToFile() throws IOException {
        Options options = Options.builder()
                .backend(ObjectConverter.BACKEND)
                .safe(SafeMode.UNSAFE)
                .toFile(testFolder.newFile())
                .build();

        asciidoctor.javaConverterRegistry().register(ObjectConverter.class);
        ObjectConverterResult document = asciidoctor.convert(SAMPLE_CONTENT, options, ObjectConverterResult.class);

        assertThat(document).isNull();
    }

    @Test
    public void shouldReturnNullWhenExpectingCustomTypeAndUsingToStream() {
        Options options = Options.builder()
                .backend(ObjectConverter.BACKEND)
                .toStream(new ByteArrayOutputStream())
                .build();

        asciidoctor.javaConverterRegistry().register(ObjectConverter.class);
        ObjectConverterResult document = asciidoctor.convert(SAMPLE_CONTENT, options, ObjectConverterResult.class);

        assertThat(document).isNull();
    }
}
