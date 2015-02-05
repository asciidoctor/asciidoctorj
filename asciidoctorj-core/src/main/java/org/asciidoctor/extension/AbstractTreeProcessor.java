package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTreeProcessor extends Processor {

    public AbstractTreeProcessor() {
        this(new HashMap<String, Object>());
    }

    public AbstractTreeProcessor(Map<String, Object> config) {
        super(config);
    }

    public abstract DocumentRuby process(DocumentRuby document);

}
