package org.asciidoctor.core.extension;

import java.util.Map;

import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.Rx;

/**
 * BlockMacroProcessors are used to handle block macros that have a custom name.
 *
 * <p>If the process method returns an instance of Block, the content model of that Block is compound, and the Block
 * contains at least one line, the parser will parse those lines into blocks and assign them to the returned block.
 *
 * <p>BlockMacroProcessor implementations must extend BlockMacroProcessor.
 */
public class BlockMacroProcessor extends MacroProcessor {

    /** The function that implements the process method when using the DSL. */
    @FunctionalInterface
    public interface ProcessFunction {
        AbstractBlock process(AbstractBlock parent, String target, Map<String, Object> attributes);
    }

    private ProcessFunction processFunction;

    public BlockMacroProcessor(String name, Map<String, Object> config) {
        super(name, config);
    }

    public BlockMacroProcessor(String name) {
        this(name, null);
    }

    public BlockMacroProcessor() {
        this(null, null);
    }

    @Override
    public String getName() {
        if (!Rx.matches(Rx.MACRO_NAME_RX, name == null ? "" : name)) {
            throw new IllegalArgumentException("invalid name for block macro: " + name);
        }
        return name;
    }

    public AbstractBlock process(AbstractBlock parent, String target, Map<String, Object> attributes) {
        if (processFunction == null) {
            throw new UnsupportedOperationException("BlockMacroProcessor subclass " + getClass().getName()
                    + " must implement the process method");
        }
        return processFunction.process(parent, target, attributes);
    }

    /**
     * Defines the process method of this processor (DSL).
     */
    public BlockMacroProcessor process(ProcessFunction function) {
        this.processFunction = function;
        return this;
    }

    public boolean isProcessBlockGiven() {
        return processFunction != null;
    }
}
