package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NextLineEmptyPreprocessor extends Preprocessor {

	public NextLineEmptyPreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public Reader process(Document document,
						  PreprocessorReader reader) {

		assertFalse(reader.isNextLineEmpty());

		reader.advance();
		
		assertTrue(reader.isNextLineEmpty());

		return reader;
	}

}
