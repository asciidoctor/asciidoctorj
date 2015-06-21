package org.asciidoctor.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines what type of blocks a {@link BlockProcessor} processes.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Contexts {

    /**
     * Predefined constant for making a Processor work on open blocks.
     * <pre>
     * [foo]
     * --
     * An open block can be an anonymous container,
     * or it can masquerade as any other block.
     * --
     * </pre>
     */
    public static final String CONTEXT_OPEN = ":open";

    /**
     * Predefined constant for making a Processor work on example blocks.
     * <pre>
     * [foo]
     * ====
     * This is just a neat example.
     * ====
     * </pre>
     */
    public static final String CONTEXT_EXAMPLE = ":example";

    /**
     * Predefined constant for making a Processor work on sidebar blocks.
     * <pre>
     * [foo]
     * ****
     * This is just a sidebar.
     * ****
     * </pre>
     */
    public static final String CONTEXT_SIDEBAR = ":sidebar";

    /**
     * Predefined constant for making a Processor work on literal blocks.
     * <pre>
     * [foo]
     * ....
     * This is just a literal block.
     * ....
     * </pre>
     */
    public static final String CONTEXT_LITERAL = ":literal";

    /**
     * Predefined constant for making a Processor work on source blocks.
     * <pre>
     * [foo]
     * ....
     * This is just a literal block.
     * ....
     * </pre>
     */
    public static final String CONTEXT_LISTING = ":listing";

    /**
     * Predefined constant for making a Processor work on quote blocks.
     * <pre>
     * [foo]
     * ____
     * To be or not to be...
     * ____
     * </pre>
     */
    public static final String CONTEXT_QUOTE = ":quote";

    /**
     * Predefined constant for making a Processor work on passthrough blocks.
     *
     * <pre>
     * [foo]
     * ++++
     * &lt;h1&gt;Big text&lt;/h1&gt;
     * ++++
     * </pre>
     */
    public static final String CONTEXT_PASS = ":pass";

    /**
     * Predefined constant for making a Processor work on paragraph blocks.
     *
     * <pre>
     * [foo]
     * Please process this paragraph.
     *
     * And don't process this.
     * </pre>
     */
    public static final String CONTEXT_PARAGRAPH = ":paragraph";


    String[] value();

}
