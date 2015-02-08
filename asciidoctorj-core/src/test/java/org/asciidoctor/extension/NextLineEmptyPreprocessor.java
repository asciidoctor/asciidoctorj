package org.asciidoctor.extension;

import org.asciidoctor.ast.DocumentRuby;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NextLineEmptyPreprocessor extends Preprocessor {

	public NextLineEmptyPreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public PreprocessorReader process(DocumentRuby document,
			PreprocessorReader reader) {

		assertThat(reader.isNextLineEmpty(), is(false));

		reader.advance();
		
		assertThat(reader.isNextLineEmpty(), is(true));
		
		return reader;
	}

}
