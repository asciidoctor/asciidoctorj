package org.asciidoctor.jruby.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Link;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenReadingLinksFromCatalogAsset {

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    @ClasspathResource("sample-with-links.adoc")
    private File sampleWithLinks;

    static final String[] ALL_LINKS = new String[]{
            "https://docs.asciidoctor.org",
            "https://github.com",
            "https://some.rangomsite.org",
            "downloads/report.pdf"
    };

    @Test
    public void shouldReturnEmptyWhenThereAreNoLinks() {
        final Options options = catalogAssetsEnabled();

        Document document = asciidoctor.load("= Hello", options);
        List<Link> links = document.getCatalog().getLinks();

        assertThat(links)
                .isEmpty();
    }

    @Test
    public void shouldNotReturnLinksWhenNotConverting() {
        final Options options = catalogAssetsEnabled();
        final String content = getAsciiDocWithLinksContent();

        Document document = asciidoctor.load(content, options);
        List<Link> links = document.getCatalog().getLinks();

        assertThat(links)
                .isEmpty();
    }

    @Test
    public void shouldNotReturnLinksWhenCatalogAssetsIsFalse() {
        final Options options = Options.builder()
                .catalogAssets(false)
                .build();
        final File file = sampleWithLinks;

        Document document = asciidoctor.convertFile(file, options, Document.class);

        List<Link> links = document.getCatalog().getLinks();
        assertThat(links)
                .isEmpty();
    }

    @Test
    public void shouldReturnLinksWhenConvertingFile() {
        final Options options = catalogAssetsEnabled();
        final File file = sampleWithLinks;

        Document document = asciidoctor.convertFile(file, options, Document.class);

        List<Link> links = document.getCatalog().getLinks();
        assertThat(links)
                .map(link -> link.getText())
                .containsExactlyInAnyOrder(ALL_LINKS);
    }

    @Test
    public void shouldReturnLinksWhenConvertingString(@TempDir Path tempFolder) {
        final Options options = Options.builder()
                .catalogAssets(true)
                .safe(SafeMode.UNSAFE)
                .toFile(outputFile(tempFolder))
                .build();
        final String content = getAsciiDocWithLinksContent();

        Document document = asciidoctor.convert(content, options, Document.class);

        List<Link> links = document.getCatalog().getLinks();
        assertThat(links)
                .map(link -> link.getText())
                .containsExactlyInAnyOrder(ALL_LINKS);
    }

    private String getAsciiDocWithLinksContent() {
        try {
            return Files.readString(sampleWithLinks.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Options catalogAssetsEnabled() {
        return Options.builder()
                .catalogAssets(true)
                .build();
    }

    private File outputFile(Path tempFolder) {
        return tempFolder.resolve("output").toFile();
    }
}
