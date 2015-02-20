package org.asciidoctor.extension;

import org.asciidoctor.ast.DocumentRuby;

import java.util.Map;

public class ChangeAttributeValuePreprocessor extends Preprocessor {

	public ChangeAttributeValuePreprocessor(Map<String, Object> config) {
		super(config);
	}

	@Override
	public PreprocessorReader process(DocumentRuby document,
			PreprocessorReader reader) {

		document.getAttributes().put("content", "Alex");
		
		return reader;
	}

}