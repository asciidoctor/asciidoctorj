package org.asciidoctor.extension;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.HashMap;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.util.ClasspathResources;
import org.junit.Rule;
import org.junit.Test;

public class WhenReaderIsManipulatedInExtension {

    @Rule
    public ClasspathResources classpath = new ClasspathResources();

	private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

	@Test
	public void currentLineNumberShouldBeReturned() {

		JavaExtensionRegistry javaExtensionRegistry = asciidoctor
				.javaExtensionRegistry();

		javaExtensionRegistry.preprocessor(NumberLinesPreprocessor.class);

		File inputFile = classpath.getResource("rendersample.asciidoc");
		asciidoctor.renderFile(inputFile, new HashMap<String, Object>());
		
		File outpuFile = new File(inputFile.getParent(), "rendersample.asciidoc");
		assertThat(outpuFile.exists(), is(true));
	}

	@Test
	public void hasMoreLinesShouldBeReturned() {

		JavaExtensionRegistry javaExtensionRegistry = asciidoctor
				.javaExtensionRegistry();

		javaExtensionRegistry.preprocessor(HasMoreLinesPreprocessor.class);

        asciidoctor.renderFile(
                classpath.getResource("rendersample.asciidoc"),
                new HashMap<String, Object>());

        File inputFile = classpath.getResource("rendersample.asciidoc");
        asciidoctor.renderFile(inputFile, new HashMap<String, Object>());

        File outpuFile = new File(inputFile.getParent(), "rendersample.asciidoc");
        assertThat(outpuFile.exists(), is(true));
	}
	
	@Test
	public void isNextLineEmptyShouldBeReturned() {

		JavaExtensionRegistry javaExtensionRegistry = asciidoctor
				.javaExtensionRegistry();

		javaExtensionRegistry.preprocessor(NextLineEmptyPreprocessor.class);

        asciidoctor.renderFile(
                classpath.getResource("rendersample.asciidoc"),
                new HashMap<String, Object>());
        
        File inputFile = classpath.getResource("rendersample.asciidoc");
        asciidoctor.renderFile(inputFile, new HashMap<String, Object>());

        File outpuFile = new File(inputFile.getParent(), "rendersample.asciidoc");
        assertThat(outpuFile.exists(), is(true));
	}
	
}
