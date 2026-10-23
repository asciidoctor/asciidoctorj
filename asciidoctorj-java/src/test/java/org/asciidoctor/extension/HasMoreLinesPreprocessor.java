package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HasMoreLinesPreprocessor extends Preprocessor {

	public HasMoreLinesPreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public Reader process(Document document,
			PreprocessorReader reader) {

		assertThat(reader.hasMoreLines()).isTrue();
		return reader;
	}

}
