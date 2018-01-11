package org.asciidoctor;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.UUID;

import static org.asciidoctor.AttributesBuilder.attributes;
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
                .attributes(attributes()
                    .attribute("imagesoutdir", "build")));

        // then:
        assertThat(result, containsString("src=\"" + imageFileName + ".png\""));
        assertThat("PNG file not created!", new File("build/" + imageFileName + ".png").exists(), is(true));

        File cacheFile = new File(".asciidoctor/diagram/" + imageFileName + ".png.cache");
        assertThat("PNG cache file " + cacheFile + " not created!", cacheFile.exists(), is(true));
    }

    @Test
    public void should_render_ditaa_diagram_to_PDF() throws Exception {
        // given:
        final String imageFileName = UUID.randomUUID().toString();

        final File destinationDir = new File( "build");
        final File destinationFile = new File( "ditaa.pdf");

        final String document = getTestDocument(imageFileName);

        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.requireLibrary("asciidoctor-diagram");

        // when:
        asciidoctor.convert(
            document,
            OptionsBuilder.options()
                .backend("pdf")
                .toDir(destinationDir)
                .toFile(destinationFile)
                .attributes(
                    attributes()
                        .attribute("imagesdir", destinationDir.getPath())
                        .attribute("imagesoutdir", destinationDir.getPath()))
        );

        // then:
        final File expectedPdfFile = new File(destinationDir, "ditaa.pdf");
        assertThat(expectedPdfFile + " does not exist", expectedPdfFile.exists(), is(true));
        assertThat(expectedPdfFile.length(), greaterThan(0L));
        final File imageFile = new File("build/" + imageFileName + ".png");
        final File cacheFile = new File(destinationDir, ".asciidoctor/diagram/" + imageFileName + ".png.cache");
        assertThat("PNG file " + imageFile + " not created!", imageFile.exists(), is(true));
        assertThat("PNG cache file " + cacheFile + " not created!", cacheFile.exists(), is(true));
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
