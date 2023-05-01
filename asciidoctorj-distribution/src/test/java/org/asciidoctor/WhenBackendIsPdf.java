package org.asciidoctor;

import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.asciidoctor.util.RougeColors;
import org.asciidoctor.util.pdf.ColorsProcessor;
import org.asciidoctor.util.pdf.ImageProcessor;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.asciidoctor.OptionsBuilder.options;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenBackendIsPdf {

    private static final String DOCUMENT = "= A document\n\n Test";

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;


    @Test
    void pdf_should_be_rendered_to_object_for_pdf_backend() {
        // The asciidoctor-pdf backend returns the converter itself on convert.
        // If the result should be written to a file the write method will convert to a PDF stream
        // Therefore, if the result should not be written to a file the PDF converter should be returned.
        IRubyObject o = asciidoctor.convert(DOCUMENT, options().backend("pdf").get(), IRubyObject.class);
        assertThat(o.getMetaClass().getRealClass().getName()).isEqualTo("Asciidoctor::PDF::Converter");
    }

    @Test
    void pdf_should_include_images(@ClasspathResource("image-sample.adoc") File inputFile) throws IOException {

        asciidoctor.convertFile(inputFile, options().backend("pdf").safe(SafeMode.UNSAFE).get());

        File outputFile = new File(inputFile.getParentFile(), "image-sample.pdf");
        assertThat(outputFile).exists();
        ImageProcessor imageProcessor = new ImageProcessor();
        imageProcessor.parse(outputFile.getAbsolutePath());
        List images = imageProcessor.getImages();
        assertThat(images).hasSize(2);

        outputFile.delete();
    }

    @Test
    void pdf_source_code_should_be_highlighted(@ClasspathResource("code-sample.adoc") File inputFile) throws IOException {

        asciidoctor.convertFile(inputFile, options().backend("pdf").safe(SafeMode.UNSAFE).get());

        File outputFile = new File(inputFile.getParentFile(), "code-sample.pdf");
        assertThat(outputFile).exists();

        ColorsProcessor colorsProcessor = new ColorsProcessor("program", "System.out.println", "printHello", "HelloWorld", "<body>", "else", "Math.sqrt");
        colorsProcessor.parse(outputFile.getAbsolutePath());
        Map<String, List<Color>> colors = colorsProcessor.getColors();
        assertThat(colors.get("program").get(0)).isEqualTo(RougeColors.GREY);
        assertThat(colors.get("System.out.println").get(0)).isEqualTo(RougeColors.LIGHT_BLUE);

        assertThat(colors.get("printHello").get(0)).isEqualTo(RougeColors.DARK_BLUE);
        assertThat(colors.get("HelloWorld").get(0)).isEqualTo(RougeColors.PINK);
        assertThat(colors.get("<body>").get(0)).isEqualTo(RougeColors.PINK);
        assertThat(colors.get("else").get(0)).isEqualTo(RougeColors.GREEN);
        assertThat(colors.get("Math.sqrt").get(0)).isEqualTo(RougeColors.LIGHT_BLUE);

        outputFile.delete();
    }

    @Test
    void pdf_should_not_fail_with_empty_tables(@ClasspathResource("empty-table.adoc") File inputFile) {
        asciidoctor.convertFile(inputFile, options().backend("pdf").safe(SafeMode.SAFE).get());

        File outputFile = new File(inputFile.getParentFile(), "empty-table.pdf");
        assertThat(outputFile).exists();
    }
}
