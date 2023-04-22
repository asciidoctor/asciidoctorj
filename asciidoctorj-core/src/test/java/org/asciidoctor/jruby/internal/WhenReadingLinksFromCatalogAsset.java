package org.asciidoctor.jruby.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Link;
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
public class WhenReadingLinksFromCatalogAsset {

    @ArquillianResource
    private ClasspathResources classpath;

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private TemporaryFolder testFolder;

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
        final File file = getAsciiDocWithLinksFile();

        Document document = asciidoctor.convertFile(file, options, Document.class);

        List<Link> links = document.getCatalog().getLinks();
        assertThat(links)
                .isEmpty();
    }

    @Test
    public void shouldReturnLinksWhenConvertingFile() {
        final Options options = catalogAssetsEnabled();
        final File file = getAsciiDocWithLinksFile();

        Document document = asciidoctor.convertFile(file, options, Document.class);

        List<Link> links = document.getCatalog().getLinks();
        assertThat(links)
                .map(link -> link.getText())
                .containsExactlyInAnyOrder(ALL_LINKS);
    }

    @Test
    public void shouldReturnLinksWhenConvertingString() throws IOException {
        final Options options = Options.builder()
                .catalogAssets(true)
                .safe(SafeMode.UNSAFE)
                .toFile(testFolder.newFile())
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
            return Files.readString(getAsciiDocWithLinksFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getAsciiDocWithLinksFile() {
        return classpath.getResource("sample-with-links.adoc");
    }

    private static Options catalogAssetsEnabled() {
        return Options.builder()
                .catalogAssets(true)
                .build();
    }
}
