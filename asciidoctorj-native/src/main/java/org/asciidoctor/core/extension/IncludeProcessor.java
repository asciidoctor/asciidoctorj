package org.asciidoctor.core.extension;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.asciidoctor.core.Document;
import org.asciidoctor.core.PreprocessorReader;

/**
 * IncludeProcessors are used to process include::&lt;target&gt;[] directives in the source document.
 *
 * <p>When Asciidoctor comes across a include::&lt;target&gt;[] directive in the source document, it iterates through
 * the IncludeProcessors and delegates the work of reading the content to the first processor that identifies itself as
 * capable of handling that target.
 *
 * <p>IncludeProcessor implementations must extend IncludeProcessor and override the process method, or be defined
 * using the DSL (i.e., by passing a function to {@link #process(ProcessFunction)}).
 */
// TODO add file extension or regexp as shortcut for handles? method
public class IncludeProcessor extends Processor {

    /** The function that implements the process method when using the DSL. */
    @FunctionalInterface
    public interface ProcessFunction {
        void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes);
    }

    private ProcessFunction processFunction;
    private BiPredicate<Document, String> handlesFunction;

    public IncludeProcessor(Map<String, Object> config) {
        super(config);
    }

    public IncludeProcessor() {
        this(null);
    }

    public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
        if (processFunction == null) {
            throw new UnsupportedOperationException("IncludeProcessor subclass " + getClass().getName()
                    + " must implement the process method");
        }
        processFunction.process(document, reader, target, attributes);
    }

    /**
     * Defines the process method of this processor (DSL).
     */
    public IncludeProcessor process(ProcessFunction function) {
        this.processFunction = function;
        return this;
    }

    public boolean isProcessBlockGiven() {
        return processFunction != null;
    }

    /**
     * @return whether this processor can handle the include directive for the specified target (default: true)
     */
    public boolean handles(Document doc, String target) {
        return handlesFunction == null ? handles(target) : handlesFunction.test(doc, target);
    }

    /**
     * Legacy signature of {@link #handles(Document, String)}, called by its default implementation.
     *
     * @return whether this processor can handle the include directive for the specified target (default: true)
     */
    public boolean handles(String target) {
        return true;
    }

    /**
     * Defines the handles method of this processor (DSL).
     */
    public IncludeProcessor handles(BiPredicate<Document, String> function) {
        this.handlesFunction = function;
        return this;
    }

    /**
     * Defines the handles method of this processor using the legacy signature, which only accepts the target (DSL).
     */
    public IncludeProcessor handles(Predicate<String> function) {
        this.handlesFunction = (doc, target) -> function.test(target);
        return this;
    }

    /**
     * Inserts this processor as the first processor of its kind (DSL).
     */
    public IncludeProcessor prefer() {
        option("position", ">>");
        return this;
    }
}
