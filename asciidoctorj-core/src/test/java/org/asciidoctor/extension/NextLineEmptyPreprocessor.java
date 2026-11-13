package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.ast.Document;

import java.util.Map;


public class NextLineEmptyPreprocessor extends Preprocessor {

	public NextLineEmptyPreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public Reader process(Document document,
						  PreprocessorReader reader) {

		assertThat(reader.isNextLineEmpty()).isFalse();

		reader.advance();
		
		assertThat(reader.isNextLineEmpty()).isTrue();

		return reader;
	}

}
