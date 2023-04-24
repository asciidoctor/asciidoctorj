package org.asciidoctor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class WhenAPdfDocumentIsRenderedToStream {

    private static final String BACKEND_PDF = "pdf";
    private static final String ASCIIDOCTOR_DIAGRAM = "asciidoctor-diagram";

    private Asciidoctor asciidoctor;

    @BeforeEach
    public void initAsciidoctor() {
        this.asciidoctor = Asciidoctor.Factory.create();
    }

    @Test
    void should_render_PDF_to_ByteArrayOutputStream(@TempDir File testFolder) throws IOException {
        // given
        final String imageFileName = String.format("%s", random());
        final File referenceFile = new File(testFolder, String.format("stream-test-file-%s.pdf", random()));
        final File imagesOutDir = new File(testFolder, "images-dir");
        final File createdCacheImage = new File(testFolder, String.format(".asciidoctor/diagram/%s.png.cache", imageFileName));

        String testDoc = new StringBuilder()
                .append("= Test").append("\n")
                .append("\n")
                .append("A test document").append("\n")
                .append("\n")
                .append("== A Section").append("\n")
                .append("\n")
                .append("Hello World").append("\n")
                .append("\n")
                .append(String.format("[ditaa,%s]", imageFileName)).append("\n")
                .append("....")
                .append("\n")
                .append("+---+")
                .append("| A |")
                .append("+---+")
                .append("....")
                .append("\n")
                .append("== Another Section")
                .append("\n")
                .append("Some other test")
                .append("\n")
                .append("a")
                .append("\n")
                .append("<<<")
                .append("\n")
                .append("b")
                .append("\n")
                .append("c")
                .append("\n")
                .toString();

        var out = new ByteArrayOutputStream();
        var dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        var now = new Date();

        Attributes attrs = Attributes.builder()
                .attribute("outdir", testFolder.getAbsolutePath())
                .attribute("imagesdir", imagesOutDir.getName())
                .attribute("docdatetime", dateTimeFormatter.format(now))
                .attribute("localdatetime", dateTimeFormatter.format(now))
                .attribute("reproducible", "true")
                .build();

        // when
        asciidoctor.requireLibrary(ASCIIDOCTOR_DIAGRAM);
        asciidoctor.convert(testDoc,
                Options.builder()
                        .backend(BACKEND_PDF)
                        .standalone(true)
                        .attributes(attrs)
                        .safe(SafeMode.UNSAFE)
                        .toStream(out)
                        .build());
        asciidoctor.convert(testDoc,
                Options.builder()
                        .backend(BACKEND_PDF)
                        .standalone(true)
                        .attributes(attrs)
                        .safe(SafeMode.UNSAFE)
                        .toFile(referenceFile)
                        .build());

        // then
        assertThat(createdCacheImage).exists();
        final byte[] toStreamBytes = out.toByteArray();
        final byte[] toFileBytes = Files.readAllBytes(referenceFile.toPath());
        assertThat(toStreamBytes).isEqualTo(toFileBytes);
        assertThat(toStreamBytes).hasSameSizeAs(toFileBytes);
    }

    private static UUID random() {
        return UUID.randomUUID();
    }
}
