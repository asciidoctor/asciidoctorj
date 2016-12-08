package org.asciidoctor.extension;

import java.util.Iterator;
import java.util.Map;

import org.asciidoctor.ast.Document;

public class PositionalAttrsIncludeProcessor extends IncludeProcessor {

    public PositionalAttrsIncludeProcessor() {}

    public PositionalAttrsIncludeProcessor(Map<String, Object> config) {
        super(config);
    }

    @Override
    public boolean handles(String target) {
		return true;
    }

    @Override
	public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {

		String str = "";
		Iterator<Object> it = attributes.values().iterator();
		if (it.hasNext())
			str += it.next();
		while (it.hasNext()) {
			str += "," + it.next();
		}

		reader.push_include(str, target, target, 1, attributes);

    }


}
