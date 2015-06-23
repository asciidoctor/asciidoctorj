package org.asciidoctor.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines what type of blocks a {@link BlockProcessor} processes.
 * Example for a BlockProcessor that transforms all open blocks with the name {@code yell} to upper case:
 * <pre><code>
 * &#64;Name("yell")
 * &#64;Contexts(Contexts.CONTEXT_OPEN)
 * &#64;ContentModel(ContentModel.SIMPLE)
 * class YellBlockProcessor extends BlockProcessor {
 *     public YellBlockProcessor(String blockName) {
 *         super(blockName);
 *     }
 *
 *     public Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
 *         List<String> lines = reader.readLines();
 *         List<String> newLines = new ArrayList<>();
 *         for (String line: lines) {
 *             newLines.add(line.toUpperCase());
 *         }
 *         return createBlock(parent, 'paragraph', newLines)
 *     }
 * }
 * </code>
 * </pre>
 *
 * Applicable for:
 * <table>
 * <tr><td>BlockMacroProcessor</td><td></td></tr>
 * <tr><td>BlockProcessor</td><td>&#10003;</td></tr>
 * <tr><td>BlockProcessor</td><td></td></tr>
 * <tr><td>DocInfoProcessor</td><td></td></tr>
 * <tr><td>IncludeProcessor</td><td></td></tr>
 * <tr><td>InlineMacroProcessor</td><td></td></tr>
 * <tr><td>Postprocessor</td><td></td></tr>
 * <tr><td>Preprocessor</td><td></td></tr>
 * <tr><td>Treeprocessor</td><td></td></tr>
 * </table>
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
