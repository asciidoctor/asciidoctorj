package org.asciidoctor.core.extension;

import java.util.Map;

import org.asciidoctor.core.Document;
import org.asciidoctor.core.PreprocessorReader;
import org.asciidoctor.core.Reader;

/**
 * Preprocessors are run after the source text is split into lines and normalized, but before parsing begins.
 *
 * <p>Prior to invoking the preprocessor, Asciidoctor splits the source text into lines and normalizes them. The
 * normalize process strips trailing whitespace and the end of line character sequence from each line.
 *
 * <p>Asciidoctor passes the document and the document's Reader to the {@link #process(Document, PreprocessorReader)}
 * method of the Preprocessor instance. The Preprocessor can modify the Reader as necessary and either return the same
 * Reader (or null, which is equivalent) or a reference to a substitute Reader.
 *
 * <p>Preprocessor implementations must extend the Preprocessor class and override the process method, or be defined
 * using the DSL (i.e., by passing a function to {@link #process(ProcessFunction)}).
 */
public class Preprocessor extends Processor {

    /** The function that implements the process method when using the DSL. */
    @FunctionalInterface
    public interface ProcessFunction {
        Reader process(Document document, PreprocessorReader reader);
    }

    private ProcessFunction processFunction;

    public Preprocessor(Map<String, Object> config) {
        super(config);
    }

    public Preprocessor() {
        this(null);
    }

    public Reader process(Document document, PreprocessorReader reader) {
        if (processFunction == null) {
            throw new UnsupportedOperationException("Preprocessor subclass " + getClass().getName()
                    + " must implement the process method");
        }
        return processFunction.process(document, reader);
    }

    /**
     * Defines the process method of this processor (DSL).
     */
    public Preprocessor process(ProcessFunction function) {
        this.processFunction = function;
        return this;
    }

    public boolean isProcessBlockGiven() {
        return processFunction != null;
    }

    /**
     * Inserts this processor as the first processor of its kind (DSL).
     */
    public Preprocessor prefer() {
        option("position", ">>");
        return this;
    }
}
