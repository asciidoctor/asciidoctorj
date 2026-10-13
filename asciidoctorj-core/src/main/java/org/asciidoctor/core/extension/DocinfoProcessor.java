package org.asciidoctor.core.extension;

import java.util.Map;

import org.asciidoctor.core.Document;

/**
 * DocinfoProcessors are used to add additional content to the header and/or footer of the generated document.
 *
 * <p>The placement of docinfo content is controlled by the converter.
 *
 * <p>DocinfoProcessors implementations must extend DocinfoProcessor. If a location is not specified, the
 * DocinfoProcessor is assumed to add content to the header.
 */
public class DocinfoProcessor extends Processor {

    /** The function that implements the process method when using the DSL. */
    @FunctionalInterface
    public interface ProcessFunction {
        String process(Document document);
    }

    private ProcessFunction processFunction;

    public DocinfoProcessor(Map<String, Object> config) {
        super(config);
        if (this.config.get("location") == null) {
            this.config.put("location", "head");
        }
    }

    public DocinfoProcessor() {
        this(null);
    }

    public String process(Document document) {
        if (processFunction == null) {
            throw new UnsupportedOperationException("DocinfoProcessor subclass " + getClass().getName()
                    + " must implement the process method");
        }
        return processFunction.process(document);
    }

    /**
     * Defines the process method of this processor (DSL).
     */
    public DocinfoProcessor process(ProcessFunction function) {
        this.processFunction = function;
        return this;
    }

    public boolean isProcessBlockGiven() {
        return processFunction != null;
    }

    /**
     * Sets the location (head or footer) where the content should be inserted (DSL).
     */
    public DocinfoProcessor atLocation(String location) {
        option("location", location);
        return this;
    }

    /**
     * Inserts this processor as the first processor of its kind (DSL).
     */
    public DocinfoProcessor prefer() {
        option("position", ">>");
        return this;
    }
}
