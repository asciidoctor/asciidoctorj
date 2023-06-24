package org.asciidoctor;

import org.junit.Test;

import java.io.File;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WhenEpub3BackendIsUsed {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    public void epub3_should_be_rendered_for_epub3_backend() {
        File inputFile = new File("src/test/resources/epub/epub-index.adoc");
        File outputDir = new File("build/test-output");
        outputDir.mkdirs();
        File outputFile = new File(outputDir, "epub-index.epub");
        asciidoctor.convertFile(inputFile, options().safe(SafeMode.SAFE)
                .backend("epub3")
                .toDir(outputDir)
                .get());
        assertThat(outputFile.exists(), is(true));
    }
}
