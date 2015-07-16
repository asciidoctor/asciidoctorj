package org.asciidoctor;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WhenBackendIsPdf {

    public static final String DOCUMENT = "= A document\n\n Test";

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpath;

    @Test
    public void pdf_should_be_rendered_for_pdf_backend() {
        File inputFile = classpath.getResource("sample.adoc");
        File outputFile1 = new File(inputFile.getParentFile(), "sample.pdf");
        File outputFile2 = new File(inputFile.getParentFile(), "sample.pdfmarks");
        asciidoctor.convertFile(inputFile, options().backend("pdf").get());
        assertThat(outputFile1.exists(), is(true));
        assertThat(outputFile2.exists(), is(true));
        outputFile1.delete();
        outputFile2.delete();
    }

    @Test
    public void pdf_should_be_rendered_to_object_for_pdf_backend() {
        // The asciidoctor-pdf backend returns the converter itself on convert.
        // If the result should be written to a file the write method will convert to a PDF stream
        // Therefore, if the result should not be written to a file the PDF converter should be returned.
        IRubyObject o = asciidoctor.convert(DOCUMENT, options().backend("pdf").get(), IRubyObject.class);
        assertThat(o.getMetaClass().getRealClass().getName(), is("Asciidoctor::Pdf::Converter"));
    }

}
