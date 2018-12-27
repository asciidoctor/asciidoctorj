package org.asciidoctor.extension;

import org.asciidoctor.api.ast.Document;
import org.asciidoctor.api.extension.Preprocessor;
import org.asciidoctor.api.extension.PreprocessorReader;

import java.util.Map;

public class ChangeAttributeValuePreprocessor extends Preprocessor {

	public ChangeAttributeValuePreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public void process(Document document,
			PreprocessorReader reader) {

		document.getAttributes().put("content", "Alex");
	}

}
