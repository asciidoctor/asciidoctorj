package org.asciidoctor.extension;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.util.Map;

import org.asciidoctor.ast.Document;

public class NumberLinesPreprocessor extends Preprocessor {

	public NumberLinesPreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public PreprocessorReader process(Document document,
			PreprocessorReader reader) {

		assertThat(reader.getLineno(), is(1));

		return reader;
	}

}
