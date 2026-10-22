package org.asciidoctor.jruby.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ImageReference;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.converter.StringConverter;
import org.asciidoctor.jruby.ast.impl.TestImageReference;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenReadingImagesFromCatalogAssetFromConverter {

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    @ClasspathResource("sample-with-images.adoc")
    private File sampleWithImages;

    static final String CONVERTER_BACKEND = "custom-backend";

    static final TestImageReference[] BLOCK_IMAGES = new TestImageReference[]{
            new TestImageReference("images/block-image.jpg")
    };

    static final TestImageReference[] ALL_IMAGES = new TestImageReference[]{
            new TestImageReference("images/block-image.jpg"),
            new TestImageReference("images/inline-image.png")
    };

    private static List<ImageReference> imagesBeforeConvert;
    private static List<ImageReference> imagesAfterConvert;

    @BeforeEach
    public void beforeEach() {
        final var javaConverterRegistry = asciidoctor.javaConverterRegistry();
        javaConverterRegistry.converters().clear();
        javaConverterRegistry.register(TestConverter.class, CONVERTER_BACKEND);

        imagesBeforeConvert = null;
        imagesAfterConvert = null;
    }

    @Test
    public void shouldReturnEmptyWhenThereAreNoImages() {
        final String content = "";

        convert(content);

        assertThat(imagesBeforeConvert).isEmpty();
        assertThat(imagesAfterConvert).isEmpty();
    }

    @Test
    public void shouldNotCatalogAnyImageWhenUsingLoad() {
        final String content = getAsciiDodWithImagesDocument();

        load(content);

        assertThat(imagesBeforeConvert).isNull();
        assertThat(imagesAfterConvert).isNull();
    }

    @Test
    public void shouldReturnAllImages() {
        final String content = getAsciiDodWithImagesDocument();

        convert(content);

        assertThat(imagesBeforeConvert)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(BLOCK_IMAGES);
        assertThat(imagesAfterConvert)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(ALL_IMAGES);
    }

    private String convert(String document) {
        var options = optionsWithConverter();
        return asciidoctor.convert(document, options);
    }

    private void load(String document) {
        var options = optionsWithConverter();
        asciidoctor.load(document, options);
    }

    private static Options optionsWithConverter() {
        return Options.builder()
                .catalogAssets(true)
                .backend(CONVERTER_BACKEND)
                .build();
    }

    private String getAsciiDodWithImagesDocument() {
        try {
            return Files.readString(sampleWithImages.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class TestConverter extends StringConverter {

        public TestConverter(String backend, Map<String, Object> opts) {
            super(backend, opts);
        }

        /*
         * For this conversion test we do not care about the conversion result,
         * we simply want to force the conversion to verify that images references
         * are populated.
         */

        @Override
        public String convert(ContentNode node, String transform, Map<Object, Object> opts) {
            String content = "";
            if (node instanceof Document) {
                var doc = (Document) node;
                imagesBeforeConvert = doc.getCatalog().getImages();
                // force content to process inline images
                content = (String) doc.getContent();
                imagesAfterConvert = doc.getCatalog().getImages();
            } else if (node instanceof StructuralNode) {
                content = (String) ((StructuralNode) node).getContent();
            }
            return content;
        }
    }
}
