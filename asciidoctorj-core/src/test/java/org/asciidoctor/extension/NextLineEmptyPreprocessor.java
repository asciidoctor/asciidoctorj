package org.asciidoctor.extension;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.asciidoctor.ast.Document;

public class NextLineEmptyPreprocessor extends Preprocessor {

	public NextLineEmptyPreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public PreprocessorReader process(Document document,
			PreprocessorReader reader) {

		assertThat(reader.isNextLineEmpty(), is(false));

		reader.advance();
		
		assertThat(reader.isNextLineEmpty(), is(true));
		
		return reader;
	}

}
