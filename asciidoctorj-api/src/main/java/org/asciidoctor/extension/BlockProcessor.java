package org.asciidoctor.extension;

import org.asciidoctor.ast.StructuralNode;

import java.util.HashMap;
import java.util.Map;

public abstract class BlockProcessor extends BaseProcessor {

    /**
     * This value is used as the config option key when defining the block type
     * a Processor should process.
     * Its value must be a list of String constants:
     *
     * <p>Example to make a BlockProcessor work on listings and examples named foo:
     * <pre>
     * <verbatim>
     * Map&lt;String, Object&gt; config = new HashMap&lt;&gt;();
     * config.put(CONTEXTS, Arrays.asList(EXAMPLE, LISTING));
     * BlockProcessor blockProcessor = new BlockProcessor("foo", config);
     * asciidoctor.javaExtensionRegistry().block(blockProcessor);
     * </verbatim>
     * </pre>
     * </p>
     *
     * @deprecated Please use {@link Contexts#KEY}
     */
    @Deprecated
    public static final String CONTEXTS = "contexts";

    /**
     * Predefined constant for making a BlockProcessor work on open blocks.
     * When passed with the {@link #CONTEXTS} config option this BlockProcessor works on open blocks:
     * <pre>
     * [foo]
     * --
     * An open block can be an anonymous container,
     * or it can masquerade as any other block.
     * --
     * </pre>
     *
     * @deprecated Please use {@link Contexts#OPEN}
     */
    @Deprecated
    public static final String CONTEXT_OPEN = ":open";

    /**
     * Predefined constant for making a BlockProcessor work on example blocks.
     * When passed with the {@link #CONTEXTS} config option this BlockProcessor works on example blocks:
     * <pre>
     * [foo]
     * ====
     * This is just a neat example.
     * ====
     * </pre>
     *
     * @deprecated Please use {@link Contexts#EXAMPLE}
     */
    @Deprecated
    public static final String CONTEXT_EXAMPLE = ":example";

    /**
     * Predefined constant for making a BlockProcessor work on sidebar blocks.
     * When passed with the {@link #CONTEXTS} config option this BlockProcessor works on sidebar blocks:
     * <pre>
     * [foo]
     * ****
     * This is just a sidebar.
     * ****
     * </pre>
     *
     * @deprecated Please use {@link Contexts#SIDEBAR}
     */
    @Deprecated
    public static final String CONTEXT_SIDEBAR = ":sidebar";

    /**
     * Predefined constant for making a BlockProcessor work on literal blocks.
     * When passed with the {@link #CONTEXTS} config option this BlockProcessor works on literal blocks:
     * <pre>
     * [foo]
     * ....
     * This is just a literal block.
     * ....
     * </pre>
     *
     * @deprecated Please use {@link Contexts#LITERAL}
     */
    @Deprecated
    public static final String CONTEXT_LITERAL = ":literal";

    /**
     * Predefined constant for making a BlockProcessor work on source blocks.
     * When passed with the {@link #CONTEXTS} config option this BlockProcessor works on source blocks:
     * <pre>
     * [foo]
     * ....
     * This is just a literal block.
     * ....
     * </pre>
     *
     * @deprecated Please use {@link Contexts#LISTING}
     */
    @Deprecated
    public static final String CONTEXT_LISTING = ":listing";

    /**
     * Predefined constant for making a BlockProcessor work on quote blocks.
     * When passed with the {@link #CONTEXTS} config option this BlockProcessor works on quote blocks:
     * <pre>
     * [foo]
     * ____
     * To be or not to be...
     * ____
     * </pre>
     *
     * @deprecated Please use {@link Contexts#QUOTE}
     */
    @Deprecated
    public static final String CONTEXT_QUOTE = ":quote";

    /**
     * Predefined constant for making a BlockProcessor work on passthrough blocks.
     * When passed with the {@link #CONTEXTS} config option this BlockProcessor works on passthrough blocks:
     *
     * <pre>
     * [foo]
     * ++++
     * &lt;h1&gt;Big text&lt;/h1&gt;
     * ++++
     * </pre>
     *
     * @deprecated Please use {@link Contexts#PASS}
     */
    @Deprecated
    public static final String CONTEXT_PASS = ":pass";

    /**
     * Predefined constant for making a BlockProcessor work on paragraph blocks.
     * This is also the default for the {@link #CONTEXTS} config option if no other context is given.
     * When passed with the {@link #CONTEXTS} config option this BlockProcessor works on paragraph blocks:
     *
     * <pre>
     * [foo]
     * Please process this paragraph.
     *
     * And don't process this.
     * </pre>
     *
     * @deprecated Please use {@link Contexts#PARAGRAPH}
     */
    @Deprecated
    public static final String CONTEXT_PARAGRAPH = ":paragraph";

    protected String name;

    public BlockProcessor() {
        this(null);
    }

    public BlockProcessor(String name) {
        this(name, new HashMap<>());
    }

    public BlockProcessor(String name, Map<String, Object> config) {
        super(config);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes);
}
