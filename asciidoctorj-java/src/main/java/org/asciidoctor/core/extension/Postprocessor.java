package org.asciidoctor.core.extension;

import java.util.Map;

import org.asciidoctor.core.Document;

/**
 * Postprocessors are run after the document is converted, but before it is written to the output stream.
 *
 * <p>Asciidoctor passes a reference to the converted String to the {@link #process(Document, String)} method of each
 * registered Postprocessor. The Postprocessor modifies the String as necessary and returns the String replacement.
 *
 * <p>The markup format in the String is determined by the backend used to convert the Document. The backend and be
 * looked up using the backend method on the Document object, as well as various backend-related document attributes.
 *
 * <p>TIP: Postprocessors can also be used to relocate assets needed by the published document.
 *
 * <p>Postprocessor implementations must extend Postprocessor and override the process method, or be defined using the
 * DSL (i.e., by passing a function to {@link #process(ProcessFunction)}).
 */
public class Postprocessor extends Processor {

    /** The function that implements the process method when using the DSL. */
    @FunctionalInterface
    public interface ProcessFunction {
        String process(Document document, String output);
    }

    private ProcessFunction processFunction;

    public Postprocessor(Map<String, Object> config) {
        super(config);
    }

    public Postprocessor() {
        this(null);
    }

    public String process(Document document, String output) {
        if (processFunction == null) {
            throw new UnsupportedOperationException("Postprocessor subclass " + getClass().getName()
                    + " must implement the process method");
        }
        return processFunction.process(document, output);
    }

    /**
     * Defines the process method of this processor (DSL).
     */
    public Postprocessor process(ProcessFunction function) {
        this.processFunction = function;
        return this;
    }

    public boolean isProcessBlockGiven() {
        return processFunction != null;
    }

    /**
     * Inserts this processor as the first processor of its kind (DSL).
     */
    public Postprocessor prefer() {
        option("position", ">>");
        return this;
    }
}
