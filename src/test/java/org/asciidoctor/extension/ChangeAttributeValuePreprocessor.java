package org.asciidoctor.extension;

import java.util.Map;

import org.asciidoctor.ast.Document;

public class ChangeAttributeValuePreprocessor extends Preprocessor {

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