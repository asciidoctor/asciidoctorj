package org.asciidoctor.extension;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Test;

public class WhenReaderIsManipulatedInExtension {

	private Asciidoctor asciidoctor = JRubyAsciidoctor.create();


	@Test
	public void currentLineNumberShouldBeReturned() {

		JavaExtensionRegistry javaExtensionRegistry = asciidoctor
				.javaExtensionRegistry();

		javaExtensionRegistry.preprocessor(NumberLinesPreprocessor.class);

		asciidoctor.renderFile(new File(
				"target/test-classes/rendersample.asciidoc"),
				new HashMap<String, Object>());

	}

	@Test
	public void hasMoreLinesShouldBeReturned() {

		JavaExtensionRegistry javaExtensionRegistry = asciidoctor
				.javaExtensionRegistry();

		javaExtensionRegistry.preprocessor(HasMoreLinesPreprocessor.class);

		asciidoctor.renderFile(new File(
				"target/test-classes/rendersample.asciidoc"),
				new HashMap<String, Object>());

	}
	
	@Test
	public void isNextLineEmptyShouldBeReturned() {

		JavaExtensionRegistry javaExtensionRegistry = asciidoctor
				.javaExtensionRegistry();

		javaExtensionRegistry.preprocessor(NextLineEmptyPreprocessor.class);

		asciidoctor.renderFile(new File(
				"target/test-classes/rendersample.asciidoc"),
				new HashMap<String, Object>());

	}
	
}
