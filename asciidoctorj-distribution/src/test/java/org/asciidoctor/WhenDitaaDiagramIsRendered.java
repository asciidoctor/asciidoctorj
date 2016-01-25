package org.asciidoctor;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class WhenDitaaDiagramIsRendered {

    @ClassRule
    public static TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void should_render_ditaa_diagram() throws Exception {
        // given:
        final String imageFileName = UUID.randomUUID().toString();
        final String document = getTestDocument(imageFileName);

        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.requireLibrary("asciidoctor-diagram");

        // when:
        final String result = asciidoctor.convert(
            document,
            OptionsBuilder.options()
                .toFile(false)
                .attributes(AttributesBuilder.attributes()
                    .attribute("imagesoutdir", "build")));

        // then:
        assertThat(result, containsString("src=\"" + imageFileName + ".png\""));
        assertThat("PNG file not created!", new File("build/" + imageFileName + ".png").exists(), is(true));
        assertThat("PNG cache file not created!", new File("build/" + imageFileName + ".png.cache").exists(), is(true));
    }

    @Test
    public void should_render_ditaa_diagram_to_PDF() throws Exception {
        // given:
        final String imageFileName = UUID.randomUUID().toString();

        final File destinationFile = new File("build/ditaa.pdf");

        final String document = getTestDocument(imageFileName);

        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.requireLibrary("asciidoctor-diagram");

        // when:
        asciidoctor.convert(
            document,
            OptionsBuilder.options()
                .backend("pdf")
                .toFile(destinationFile)
                .attributes(AttributesBuilder.attributes()
                    .attribute("imagesoutdir", "build")));

        // then:
        assertThat(destinationFile.exists(), is(true));
        assertThat(destinationFile.length(), greaterThan(0L));
        assertThat("PNG file not created!", new File("build/" + imageFileName + ".png").exists(), is(true));
        assertThat("PNG cache file not created!", new File("build/" + imageFileName + ".png.cache").exists(), is(true));
    }

    private String getTestDocument(String imageFileName) {
        return "= Document Title\n" +
            "\n" +
            "Hello World\n" +
            "\n" +
            "[ditaa," + imageFileName + "]\n" +
            "....\n" +
            "\n" +
            "+---+\n" +
            "| A |\n" +
            "+---+\n" +
            "....\n" +
            "\n";
    }
}
