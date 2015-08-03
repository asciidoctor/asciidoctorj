package org.asciidoctor;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.util.ClasspathResources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Ignore;

public class WhenBackendIsPdf {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Rule
    public ClasspathResources classpath = new ClasspathResources();

    @Test
    public void pdf_should_be_rendered_for_pdf_backend() {
        File inputFile = classpath.getResource("sample.adoc");
        File outputFile1 = new File(inputFile.getParentFile(), "sample.pdf");
        File outputFile2 = new File(inputFile.getParentFile(), "sample.pdfmarks");
        asciidoctor.convertFile(inputFile, options().backend("pdf").safe(SafeMode.UNSAFE).get());
        assertThat(outputFile1.exists(), is(true));
        assertThat(outputFile2.exists(), is(true));
        outputFile1.delete();
        outputFile2.delete();
    }
}
