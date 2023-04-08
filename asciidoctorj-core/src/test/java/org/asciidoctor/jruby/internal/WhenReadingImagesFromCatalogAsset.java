package org.asciidoctor.jruby.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ImageReference;
import org.asciidoctor.jruby.ast.impl.TestImageReference;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class WhenReadingImagesFromCatalogAsset {

    @ArquillianResource
    private ClasspathResources classpath;

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private TemporaryFolder testFolder;

    static final TestImageReference[] BLOCK_IMAGES = new TestImageReference[]{
            new TestImageReference("images/block-image.jpg")
    };

    static final TestImageReference[] ALL_IMAGES = new TestImageReference[]{
            new TestImageReference("images/block-image.jpg"),
            new TestImageReference("images/inline-image.png")
    };

    @Test
    public void shouldReturnEmptyWhenThereAreNoImages() {
        final Options options = Options.builder()
                .catalogAssets(true)
                .build();

        Document document = asciidoctor.load("= Hello", options);
        List<ImageReference> images = document.getCatalog().getImages();

        assertThat(images)
                .isEmpty();
    }

    @Test
    public void shouldReturnNullImagesDirWhenNotSet() {
        final Options options = Options.builder()
                .catalogAssets(true)
                .build();
        final String content = getAsciiDocWithImagesContent();

        Document document = asciidoctor.load(content, options);
        List<ImageReference> images = document.getCatalog().getImages();

        assertThat(images)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(BLOCK_IMAGES);
    }

    @Test
    public void shouldReturnImagesDirWhenSet() {
        final Options options = Options.builder()
                .catalogAssets(true)
                .attributes(Attributes.builder()
                        .imagesDir("some-path")
                        .build())
                .build();
        final String content = getAsciiDocWithImagesContent();

        Document document = asciidoctor.load(content, options);
        List<ImageReference> images = document.getCatalog().getImages();

        assertThat(images)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new TestImageReference("images/block-image.jpg", "some-path"));
    }

    @Test
    public void shouldNotCatalogInlineImagesWhenNotConverting() {
        final Options options = Options.builder()
                .catalogAssets(true)
                .build();
        final String content = getAsciiDocWithImagesContent();

        Document document = asciidoctor.load(content, options);

        List<ImageReference> images = document.getCatalog().getImages();
        assertThat(images)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(BLOCK_IMAGES);
    }

    @Test
    public void shouldCatalogInlineImagesWhenProcessingContentAfterLoad() {
        final Options options = Options.builder()
                .catalogAssets(true)
                .build();
        final String content = getAsciiDocWithImagesContent();

        Document document = asciidoctor.load(content, options);
        document.getContent();

        List<ImageReference> images = document.getCatalog().getImages();
        assertThat(images)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(ALL_IMAGES);
    }

    @Test
    public void shouldNotCatalogImagesWhenCatalogAssetsIsFalse() {
        final Options options = Options.builder()
                .catalogAssets(false)
                .build();
        final String content = getAsciiDocWithImagesContent();

        Document document = asciidoctor.load(content, options);
        document.getContent();

        List<ImageReference> images = document.getCatalog().getImages();
        assertThat(images).isEmpty();
    }

    @Test
    public void shouldCatalogAllImagesWhenUsingConvertFile() throws IOException {
        final Options options = Options.builder()
                .catalogAssets(true)
                .safe(SafeMode.UNSAFE)
                .toFile(testFolder.newFile())
                .build();
        final File file = getAsciiDocWithImagesFile();

        var document = asciidoctor.convertFile(file, options, Document.class);

        assertThat(document)
                .isInstanceOf(Document.class);

        List<ImageReference> images = document.getCatalog().getImages();
        assertThat(images)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(ALL_IMAGES);
    }

    @Test
    public void shouldCatalogAllImagesWhenUsingConvert() throws IOException {
        final Options options = Options.builder()
                .catalogAssets(true)
                .safe(SafeMode.UNSAFE)
                .toFile(testFolder.newFile())
                .build();
        final String content = getAsciiDocWithImagesContent();

        var document = asciidoctor.convert(content, options, Document.class);

        assertThat(document)
                .isInstanceOf(Document.class);

        List<ImageReference> images = document.getCatalog().getImages();
        assertThat(images)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(ALL_IMAGES);
    }

    private String getAsciiDocWithImagesContent() {
        try {
            return Files.readString(getAsciiDocWithImagesFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getAsciiDocWithImagesFile() {
        return classpath.getResource("sample-with-images.adoc");
    }
}
