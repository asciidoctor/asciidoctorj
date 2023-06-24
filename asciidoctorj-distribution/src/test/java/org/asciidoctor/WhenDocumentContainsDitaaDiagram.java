package org.asciidoctor;


import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class WhenDocumentContainsDitaaDiagram {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    public void png_should_be_rendered_for_diagram() {
        File sourceDir = new File("build/resources/test");
        File inputFile = new File(sourceDir, "sample-with-diagram.adoc");

        File outputDiagram = new File(sourceDir, "asciidoctor-diagram-process.png");
        File outputDiagramCache = new File(sourceDir, ".asciidoctor/diagram/asciidoctor-diagram-process.png.cache");

        asciidoctor.requireLibrary("asciidoctor-diagram");
        asciidoctor.convertFile(inputFile,
                Options.builder()
                        .backend("html5")
                        .toFile(new File(sourceDir, "sample.html"))
                        .build());

        assertThat(outputDiagram.exists(), is(true));
        assertThat(outputDiagramCache.exists(), is(true));
    }
}
