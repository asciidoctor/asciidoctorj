package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.asciidoctor.util.OptionsTestHelper.emptyOptions;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenReaderIsManipulatedInExtension {

    private static final String RENDERSAMPLE = "rendersample.asciidoc";

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    @ClasspathResource(RENDERSAMPLE)
    private File renderSample;


    @Test
    public void currentLineNumberShouldBeReturned() {
        var javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
        javaExtensionRegistry.preprocessor(NumberLinesPreprocessor.class);

        asciidoctor.convertFile(renderSample, emptyOptions());

        File outpuFile = new File(renderSample.getParent(), RENDERSAMPLE);
        assertThat(outpuFile).exists();
    }

    @Test
    public void hasMoreLinesShouldBeReturned() {
        var javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
        javaExtensionRegistry.preprocessor(HasMoreLinesPreprocessor.class);

        asciidoctor.convertFile(renderSample, emptyOptions());

        File outpuFile = new File(renderSample.getParent(), RENDERSAMPLE);
        assertThat(outpuFile).exists();
    }

    @Test
    public void isNextLineEmptyShouldBeReturned() {
        var javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
        javaExtensionRegistry.preprocessor(NextLineEmptyPreprocessor.class);

        asciidoctor.convertFile(renderSample, emptyOptions());

        File outpuFile = new File(renderSample.getParent(), RENDERSAMPLE);
        assertThat(outpuFile).exists();
    }
}
