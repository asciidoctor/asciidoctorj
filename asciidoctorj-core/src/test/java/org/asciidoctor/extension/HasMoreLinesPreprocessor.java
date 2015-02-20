package org.asciidoctor.extension;

import org.asciidoctor.ast.DocumentRuby;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HasMoreLinesPreprocessor extends Preprocessor {

	public HasMoreLinesPreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public PreprocessorReader process(DocumentRuby document,
			PreprocessorReader reader) {

		assertThat(reader.hasMoreLines(), is(true));

		return reader;
	}

}
