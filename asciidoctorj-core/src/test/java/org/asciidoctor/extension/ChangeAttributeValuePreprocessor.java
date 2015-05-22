package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import java.util.Map;

public class ChangeAttributeValuePreprocessor extends Preprocessor {

	public ChangeAttributeValuePreprocessor() {}

	public ChangeAttributeValuePreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public PreprocessorReader process(Document document,
			PreprocessorReader reader) {

		document.getAttributes().put("content", "Alex");
		
		return reader;
	}

}