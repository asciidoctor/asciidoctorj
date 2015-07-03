package org.asciidoctor.extension;

import org.asciidoctor.ast.DocumentRuby;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NumberLinesPreprocessor extends Preprocessor {

	public NumberLinesPreprocessor() {}

	@Override
	public PreprocessorReader process(DocumentRuby document,
			PreprocessorReader reader) {

		assertThat(reader.getLineno(), is(1));

		return reader;
	}

}
