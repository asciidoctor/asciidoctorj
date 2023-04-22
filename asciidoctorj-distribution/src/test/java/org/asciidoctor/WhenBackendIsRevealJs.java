package org.asciidoctor;

import org.asciidoctor.cli.jruby.AsciidoctorInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class WhenBackendIsRevealJs {

    private Asciidoctor asciidoctor;

    @BeforeEach
    public void initAsciidoctor() {
        this.asciidoctor = Asciidoctor.Factory.create();
    }

    @Test
    public void should_create_simple_slides() throws IOException {
        String filename = "sample";
        File inputFile = new File("build/resources/test/" + filename + ".adoc");
        File outputFile1 = new File(inputFile.getParentFile(), filename + ".html");
        removeFileIfItExists(outputFile1);

        AsciidoctorInvoker.main(new String[]{
                "-b", "revealjs",
                "-r", "asciidoctor-diagram",
                "-a", "revealjsdir=https://cdn.jsdelivr.net/npm/reveal.js@3.9.2",
                inputFile.getAbsolutePath()
        });

        Document doc = Jsoup.parse(outputFile1, "UTF-8");

        assertThat(outputFile1).exists();

        List<String> stylesheets = doc.head().getElementsByTag("link").stream()
                .filter(element -> "stylesheet".equals(element.attr("rel")))
                .map(element -> element.attr("href"))
                .collect(toList());
        assertThat(stylesheets)
                .contains("https://cdn.jsdelivr.net/npm/reveal.js@3.9.2/css/reveal.css",
                        "https://cdn.jsdelivr.net/npm/reveal.js@3.9.2/css/theme/black.css");

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
