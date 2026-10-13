package org.asciidoctor.core.extension;

import java.util.Map;

import org.asciidoctor.core.ContentModel;

/**
 * The base class for block and inline macro processors.
 */
public abstract class MacroProcessor extends SyntaxProcessor {

    protected MacroProcessor(String name, Map<String, Object> config) {
        super(name, config);
        if (this.config.get("content_model") == null) {
            this.config.put("content_model", ContentModel.ATTRIBUTES);
        }
    }

    /**
     * Resolves all attributes, or none if value is false (in which case the content model is set to text) (DSL).
     */
    @Override
    public SyntaxProcessor resolveAttributes(boolean value) {
        if (!value) {
            option("content_model", ContentModel.TEXT);
            return this;
        }
        super.resolveAttributes(true);
        option("content_model", ContentModel.ATTRIBUTES);
        return this;
    }

    @Override
    public SyntaxProcessor resolveAttributes(String... specs) {
        super.resolveAttributes(specs);
        option("content_model", ContentModel.ATTRIBUTES);
        return this;
    }

    @Override
    public SyntaxProcessor resolveAttributes(Map<String, Object> spec) {
        super.resolveAttributes(spec);
        option("content_model", ContentModel.ATTRIBUTES);
        return this;
    }
}
