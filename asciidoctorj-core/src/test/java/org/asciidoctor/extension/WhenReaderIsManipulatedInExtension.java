package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.Map;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenReaderIsManipulatedInExtension {

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    @ClasspathResource("rendersample.asciidoc")
    private File renderSample;


    @Test
    public void currentLineNumberShouldBeReturned() {
        var javaExtensionRegistry = asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(NumberLinesPreprocessor.class);

        asciidoctor.convertFile(renderSample, Map.of());

        File outpuFile = new File(renderSample.getParent(), "rendersample.asciidoc");
        assertThat(outpuFile).exists();
    }

    @Test
    public void hasMoreLinesShouldBeReturned() {
        var javaExtensionRegistry = asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(HasMoreLinesPreprocessor.class);

        asciidoctor.convertFile(renderSample, Map.of());

        File outpuFile = new File(renderSample.getParent(), "rendersample.asciidoc");
        assertThat(outpuFile).exists();
    }

    @Test
    public void isNextLineEmptyShouldBeReturned() {
        var javaExtensionRegistry = asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(NextLineEmptyPreprocessor.class);

        asciidoctor.convertFile(renderSample, Map.of());

        File outpuFile = new File(renderSample.getParent(), "rendersample.asciidoc");
        assertThat(outpuFile).exists();
    }

}
