package org.asciidoctor.core.extension;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.ContentModel;
import org.asciidoctor.core.Reader;

/**
 * BlockProcessors are used to handle delimited blocks and paragraphs that have a custom name.
 *
 * <p>When Asciidoctor encounters a delimited block or paragraph with an unrecognized name while parsing the document,
 * it looks for a BlockProcessor registered to handle this name and, if found, invokes its process method to build a
 * corresponding node in the document tree.
 *
 * <p>If the process method returns an instance of Block, the content model of that Block is compound, and the Block
 * contains at least one line, the parser will parse those lines into blocks and append them to the returned block.
 *
 * <p>If your custom block can be applied to a paragraph or delimited block, and you want to preserve the content model
 * of the input, check whether the value of the cloaked-context attribute is paragraph. If it is, set the content model
 * of the returned block to simple. Otherwise, set the content model to compound.
 *
 * <p>Recognized options:
 *
 * <ul>
 * <li>name - The name of the block (required: true)</li>
 * <li>contexts - The blocks contexts on which this style can be used (default: [paragraph, open]</li>
 * <li>content_model - The structure of the content supported in this block (default: compound)</li>
 * <li>positional_attrs - A list of attribute names used to map positional attributes (default: null)</li>
 * <li>default_attrs - A map of attribute names and values used to seed the attributes map (default: null)</li>
 * </ul>
 */
public class BlockProcessor extends SyntaxProcessor {

    /** The function that implements the process method when using the DSL. */
    @FunctionalInterface
    public interface ProcessFunction {
        AbstractBlock process(AbstractBlock parent, Reader reader, Map<String, Object> attributes);
    }

    private ProcessFunction processFunction;

    public BlockProcessor(String name, Map<String, Object> config) {
        super(name, config);
        // assign fallbacks
        Object contexts = this.config.get("contexts");
        if (contexts == null) {
            this.config.put("contexts", new LinkedHashSet<>(Set.of("open", "paragraph")));
        } else if (contexts instanceof String context) {
            this.config.put("contexts", new LinkedHashSet<>(Set.of(context)));
        } else if (contexts instanceof Collection<?> collection) {
            Set<String> resolved = new LinkedHashSet<>();
            for (Object context : collection) {
                resolved.add(String.valueOf(context));
            }
            this.config.put("contexts", resolved);
        }
        // QUESTION should the default content model be raw??
        if (this.config.get("content_model") == null) {
            this.config.put("content_model", ContentModel.COMPOUND);
        }
    }

    public BlockProcessor(String name) {
        this(name, null);
    }

    public BlockProcessor() {
        this(null, null);
    }

    public AbstractBlock process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
        if (processFunction == null) {
            throw new UnsupportedOperationException("BlockProcessor subclass " + getClass().getName()
                    + " must implement the process method");
        }
        return processFunction.process(parent, reader, attributes);
    }

    /**
     * Defines the process method of this processor (DSL).
     */
    public BlockProcessor process(ProcessFunction function) {
        this.processFunction = function;
        return this;
    }

    public boolean isProcessBlockGiven() {
        return processFunction != null;
    }

    /**
     * Sets the contexts (e.g., paragraph, open, example) on which this block can be used (DSL).
     */
    public BlockProcessor contexts(String... value) {
        option("contexts", new LinkedHashSet<>(Arrays.asList(value)));
        return this;
    }

    public BlockProcessor onContexts(String... value) {
        return contexts(value);
    }

    public BlockProcessor onContext(String... value) {
        return contexts(value);
    }

    public BlockProcessor bindTo(String... value) {
        return contexts(value);
    }

    /**
     * @return the contexts on which this block can be used
     */
    @SuppressWarnings("unchecked")
    public Set<String> getContexts() {
        return (Set<String>) config.get("contexts");
    }
}
