package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class WhenReaderIsManipulatedInExtension {

    @ArquillianResource
    private ClasspathResources classpath;

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @Test
    public void currentLineNumberShouldBeReturned() {

        JavaExtensionRegistry javaExtensionRegistry = asciidoctor
                .javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(NumberLinesPreprocessor.class);

        File inputFile = classpath.getResource("rendersample.asciidoc");
        asciidoctor.convertFile(inputFile, Map.of());

        File outpuFile = new File(inputFile.getParent(), "rendersample.asciidoc");
        assertThat(outpuFile.exists(), is(true));
    }

    @Test
    public void hasMoreLinesShouldBeReturned() {

        JavaExtensionRegistry javaExtensionRegistry = asciidoctor
                .javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(HasMoreLinesPreprocessor.class);

        asciidoctor.convertFile(
                classpath.getResource("rendersample.asciidoc"),
                Map.of());

        File inputFile = classpath.getResource("rendersample.asciidoc");
        asciidoctor.convertFile(inputFile, Map.of());

        File outpuFile = new File(inputFile.getParent(), "rendersample.asciidoc");
        assertThat(outpuFile.exists(), is(true));
    }

    @Test
    public void isNextLineEmptyShouldBeReturned() {

        JavaExtensionRegistry javaExtensionRegistry = asciidoctor
                .javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(NextLineEmptyPreprocessor.class);

        asciidoctor.convertFile(
                classpath.getResource("rendersample.asciidoc"),
                Map.of());

        File inputFile = classpath.getResource("rendersample.asciidoc");
        asciidoctor.convertFile(inputFile, Map.of());

        File outpuFile = new File(inputFile.getParent(), "rendersample.asciidoc");
        assertThat(outpuFile.exists(), is(true));
    }

}
