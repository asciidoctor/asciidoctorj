package org.asciidoctor;

import org.asciidoctor.cli.jruby.AsciidoctorInvoker;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({ClasspathExtension.class})
public class WhenBackendIsRevealJs {

    @Test
    void should_create_simple_slides(@ClasspathResource("sample.adoc") File inputFile) throws IOException {
        File outputFile = new File(inputFile.getParentFile(), "sample.html");
        removeFileIfItExists(outputFile);

        AsciidoctorInvoker.main(new String[]{
                "-b", "revealjs",
                "-r", "asciidoctor-diagram",
                "-a", "revealjsdir=https://cdn.jsdelivr.net/npm/reveal.js@4.1.2",
                inputFile.getAbsolutePath()
        });

        Document doc = Jsoup.parse(outputFile, "UTF-8");

        assertThat(outputFile).exists();

        List<String> stylesheets = doc.head().getElementsByTag("link").stream()
                .filter(element -> "stylesheet".equals(element.attr("rel")))
                .map(element -> element.attr("href"))
                .collect(toList());
        assertThat(stylesheets)
                .contains("https://cdn.jsdelivr.net/npm/reveal.js@4.1.2/dist/reveal.css",
                        "https://cdn.jsdelivr.net/npm/reveal.js@4.1.2/dist/theme/black.css");

        Element diagramSlide = doc.selectFirst("#diagram");
        assertThat(diagramSlide).isNotNull();

        Element diagram = diagramSlide.selectFirst("div.imageblock img");
        assertThat(diagram).isNotNull();
        assertThat(diagram.attr("src")).startsWith("data:image/svg+xml;base64,");
    }

    private void removeFileIfItExists(File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("can't delete file");
            }
        }
    }
}
