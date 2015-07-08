package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NumberLinesPreprocessor extends Preprocessor {

	public NumberLinesPreprocessor() {}

	@Override
	public PreprocessorReader process(Document document,
			PreprocessorReader reader) {

		assertThat(reader.getLineno(), is(1));

		return reader;
	}

}
