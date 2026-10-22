package org.asciidoctor.core.extension;

import java.util.Map;

import org.asciidoctor.core.Document;

/**
 * TreeProcessors are run on the Document after the source has been parsed into an abstract syntax tree (AST), as
 * represented by the Document object and its child Node objects (e.g., Section, Block, List, ListItem).
 *
 * <p>Asciidoctor invokes the {@link #process(Document)} method on an instance of each registered TreeProcessor.
 *
 * <p>TreeProcessor implementations must extend TreeProcessor and override the process method, or be defined using the
 * DSL (i.e., by passing a function to {@link #process(ProcessFunction)}).
 */
// QUESTION should the tree processor get invoked after parse header too?
public class TreeProcessor extends Processor {

    /** The function that implements the process method when using the DSL. */
    @FunctionalInterface
    public interface ProcessFunction {
        Document process(Document document);
    }

    private ProcessFunction processFunction;

    public TreeProcessor(Map<String, Object> config) {
        super(config);
    }

    public TreeProcessor() {
        this(null);
    }

    public Document process(Document document) {
        if (processFunction == null) {
            throw new UnsupportedOperationException("TreeProcessor subclass " + getClass().getName()
                    + " must implement the process method");
        }
        return processFunction.process(document);
    }

    /**
     * Defines the process method of this processor (DSL).
     */
    public TreeProcessor process(ProcessFunction function) {
        this.processFunction = function;
        return this;
    }

    public boolean isProcessBlockGiven() {
        return processFunction != null;
    }

    /**
     * Inserts this processor as the first processor of its kind (DSL).
     */
    public TreeProcessor prefer() {
        option("position", ">>");
        return this;
    }
}
