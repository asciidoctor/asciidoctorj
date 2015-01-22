package org.asciidoctor;

import java.io.File;

import org.asciidoctor.util.ClasspathResources;
import org.junit.Rule;
import org.junit.Test;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WhenDocumentContainsDitaaDiagram {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Rule
    public ClasspathResources classpath = new ClasspathResources();

    @Test
    public void png_should_be_rendered_for_diagram() {
        File inputFile = classpath.getResource("sample.adoc");
        File outputFile1 = new File(inputFile.getParentFile(), "asciidoctor-diagram-process.png");
        File outputFile2 = new File(inputFile.getParentFile(), "asciidoctor-diagram-process.png.cache");
        asciidoctor.requireLibrary("asciidoctor-diagram");
        asciidoctor.convertFile(inputFile, options().backend("html5").get());
        assertThat(outputFile1.exists(), is(true));
        assertThat(outputFile2.exists(), is(true));
        outputFile1.delete();
        outputFile2.delete();
    }
}
