package org.asciidoctor.extension;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.asciidoctor.ast.Document;

public class HasMoreLinesPreprocessor extends Preprocessor {

	public HasMoreLinesPreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public PreprocessorReader process(Document document,
			PreprocessorReader reader) {

		assertThat(reader.hasMoreLines(), is(true));

		return reader;
	}

}
