package org.asciidoctor;

import org.asciidoctor.jruby.cli.AsciidoctorInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class WhenBackendIsRevealJs {

    @Test
    public void should_create_simple_slides() throws IOException {
        String filename = "sample";
        File inputFile = new File("build/resources/test/" + filename + ".adoc");
        File outputFile1 = new File(inputFile.getParentFile(), filename + ".html");
        removeFileIfItExists(outputFile1);

        AsciidoctorInvoker.main(new String[]{
            "-b", "revealjs",
            "-r", "asciidoctor-diagram",
            "-a", "revealjsdir=https://cdn.jsdelivr.net/npm/reveal.js@4.1.2",
            inputFile.getAbsolutePath()
        });

        Document doc = Jsoup.parse(outputFile1, "UTF-8");

        assertThat(outputFile1.exists(), is(true));

        List<String> stylesheets = doc.head().getElementsByTag("link").stream()
            .filter(element -> "stylesheet".equals(element.attr("rel")))
            .map(element -> element.attr("href"))
            .collect(toList());
        assertThat(stylesheets,
                hasItems("https://cdn.jsdelivr.net/npm/reveal.js@4.1.2/dist/reveal.css",
                        "https://cdn.jsdelivr.net/npm/reveal.js@4.1.2/dist/theme/black.css"));

        Element diagramSlide = doc.selectFirst("#diagram");
        assertThat(diagramSlide, notNullValue());

        Element diagram = diagramSlide.selectFirst("div.imageblock img");
        assertThat(diagram, notNullValue());

        assertThat(diagram.attr("src"), startsWith("data:image/svg+xml;base64,"));
    }


    private void removeFileIfItExists(File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("can't delete file");
            }
        }
    }
}
