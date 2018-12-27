package org.asciidoctor.extension;

import org.asciidoctor.api.ast.Document;
import org.asciidoctor.api.extension.Preprocessor;
import org.asciidoctor.api.extension.PreprocessorReader;

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
