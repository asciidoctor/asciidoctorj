package org.asciidoctor;

import org.asciidoctor.util.ClasspathHelper;
import org.asciidoctor.util.RougeColors;
import org.asciidoctor.util.pdf.ColorsProcessor;
import org.asciidoctor.util.pdf.ImageProcessor;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.asciidoctor.OptionsBuilder.options;
import static org.assertj.core.api.Assertions.assertThat;


public class WhenBackendIsPdf {

    public static final String DOCUMENT = "= A document\n\n Test";

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpath;

    @BeforeEach
    public void initAsciidoctor() {
        this.asciidoctor = Asciidoctor.Factory.create();
        this.classpath = new ClasspathHelper();
        this.classpath.setClassloader(this.getClass());
    }

    @Test
    public void pdf_should_be_rendered_to_object_for_pdf_backend() {
        // The asciidoctor-pdf backend returns the converter itself on convert.
        // If the result should be written to a file the write method will convert to a PDF stream
        // Therefore, if the result should not be written to a file the PDF converter should be returned.
        IRubyObject o = asciidoctor.convert(DOCUMENT, options().backend("pdf").get(), IRubyObject.class);
        assertThat(o.getMetaClass().getRealClass().getName()).isEqualTo("Asciidoctor::PDF::Converter");
    }

    @Test
    public void pdf_should_include_images() throws IOException {
        String filename = "image-sample";
        File inputFile = classpath.getResource(filename + ".adoc");
        File outputFile1 = new File(inputFile.getParentFile(), filename + ".pdf");

        asciidoctor.convertFile(inputFile, options().backend("pdf").safe(SafeMode.UNSAFE).get());

        assertThat(outputFile1).exists();
        ImageProcessor imageProcessor = new ImageProcessor();
        imageProcessor.parse(outputFile1.getAbsolutePath());
        List images = imageProcessor.getImages();
        assertThat(images).hasSize(2);

        outputFile1.delete();
    }

    @Test
    public void pdf_source_code_should_be_highlighted() throws IOException {
        String filename = "code-sample";
        File inputFile = classpath.getResource(filename + ".adoc");
        File outputFile1 = new File(inputFile.getParentFile(), filename + ".pdf");

        asciidoctor.convertFile(inputFile, options().backend("pdf").safe(SafeMode.UNSAFE).get());

        assertThat(outputFile1).exists();

        ColorsProcessor colorsProcessor = new ColorsProcessor("program", "System.out.println", "printHello", "HelloWorld", "<body>", "else", "Math.sqrt");
        colorsProcessor.parse(outputFile1.getAbsolutePath());
        Map<String, List<Color>> colors = colorsProcessor.getColors();
        assertThat(colors.get("program").get(0)).isEqualTo(RougeColors.GREY);
        assertThat(colors.get("System.out.println").get(0)).isEqualTo(RougeColors.LIGHT_BLUE);

        assertThat(colors.get("printHello").get(0)).isEqualTo(RougeColors.DARK_BLUE);
        assertThat(colors.get("HelloWorld").get(0)).isEqualTo(RougeColors.PINK);
        assertThat(colors.get("<body>").get(0)).isEqualTo(RougeColors.PINK);
        assertThat(colors.get("else").get(0)).isEqualTo(RougeColors.GREEN);
        assertThat(colors.get("Math.sqrt").get(0)).isEqualTo(RougeColors.LIGHT_BLUE);

        outputFile1.delete();
    }

    @Test
    public void pdf_should_not_fail_with_empty_tables() {
        String filename = "empty-table";
        File inputFile = classpath.getResource(filename + ".adoc");
        File outputFile1 = new File(inputFile.getParentFile(), filename + ".pdf");

        asciidoctor.convertFile(inputFile, options().backend("pdf").safe(SafeMode.SAFE).get());

        assertThat(outputFile1).exists();
    }
}
