package org.asciidoctor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class WhenDitaaDiagramIsRendered {

    private static final String ASCIIDOCTOR_DIAGRAM = "asciidoctor-diagram";

    private Asciidoctor asciidoctor;

    @BeforeEach
    public void initAsciidoctor() {
        this.asciidoctor = Asciidoctor.Factory.create();
    }

    @Test
    void should_render_ditaa_diagram_to_HTML(@TempDir File testFolder) {
        // given
        final String imageFileName = String.format("%s", UUID.randomUUID());
        final File imagesOutDir = new File(testFolder, "images-dir");
        final File createdCacheImage = new File(testFolder, String.format(".asciidoctor/diagram/%s.png.cache", imageFileName));
        final String document = sourceWithDiagram(imageFileName);

        // when
        asciidoctor.requireLibrary(ASCIIDOCTOR_DIAGRAM);
        String result = asciidoctor.convert(document, Options.builder()
                .toFile(false)
                .toDir(testFolder)
                .safe(SafeMode.UNSAFE)
                .attributes(Attributes.builder()
                        .attribute("imagesdir", imagesOutDir.getName())
                        .attribute("outdir", testFolder.getAbsolutePath())
                        .build())
                .build());

        // then
        assertThat(result).contains(String.format("src=\"%s/%s.png", imagesOutDir.getName(), imageFileName));
        assertThat(new File(imagesOutDir, String.format("%s.png", imageFileName))).exists();
        assertThat(createdCacheImage).exists();
    }

    @Test
    void should_render_ditaa_diagram_to_PDF(@TempDir File testFolder) {
        // given
        final File destinationFile = new File("test.pdf");
        final String imageFileName = UUID.randomUUID().toString();
        final File createdPdf = new File(testFolder, destinationFile.getName());
        final File createdImage = new File(testFolder, String.format("%s.png", imageFileName));
        final File createdCacheImage = new File(testFolder, String.format(".asciidoctor/diagram/%s.png.cache", imageFileName));
        final String document = sourceWithDiagram(imageFileName);

        // when
        asciidoctor.requireLibrary(ASCIIDOCTOR_DIAGRAM);
        asciidoctor.convert(document, Options.builder()
                .toDir(testFolder)
                .toFile(destinationFile)
                .safe(SafeMode.UNSAFE)
                .backend("pdf")
                .build());

        //then
        assertThat(createdPdf).exists();
        assertThat(createdImage).exists();
        assertThat(createdCacheImage).exists();
    }

    private static String sourceWithDiagram(String imageFileName) {
        return new StringBuffer()
                .append("= Document Title").append("\n")
                .append("\n")
                .append("Hello World").append("\n")
                .append("\n")
                .append(String.format("[ditaa,%s]", imageFileName)).append("\n")
                .append("....").append("\n")
                .append("\n")
                .append("+---+").append("\n")
                .append("| A |").append("\n")
                .append("+---+").append("\n")
                .append("....").append("\n")
                .toString();
    }
}
