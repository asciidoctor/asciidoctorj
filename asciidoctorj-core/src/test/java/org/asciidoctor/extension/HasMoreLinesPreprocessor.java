package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HasMoreLinesPreprocessor extends Preprocessor {

	public HasMoreLinesPreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public void process(Document document,
			PreprocessorReader reader) {

		assertThat(reader.hasMoreLines(), is(true));
	}

}
