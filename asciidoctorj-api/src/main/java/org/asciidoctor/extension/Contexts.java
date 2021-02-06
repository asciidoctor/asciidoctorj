package org.asciidoctor.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines what type of blocks a BlockProcessor processes.
 * Example for a BlockProcessor that transforms all open blocks with the name {@code yell} to upper case:
 * <pre><code>
 * &#64;Name("yell")
 * &#64;Contexts(Contexts.OPEN)
 * &#64;ContentModel(ContentModel.SIMPLE)
 * class YellBlockProcessor extends BlockProcessor {
 *     public YellBlockProcessor(String blockName) {
 *         super(blockName);
 *     }
 *
 *     public Object process(StructuralNode parent, Reader reader, Map&lt;String, Object&gt; attributes) {
 *         List&lt;String&gt; lines = reader.readLines();
 *         List&lt;String&gt; newLines = new ArrayList&lt;&gt;();
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
     * This value is used as the config option key when defining the block type
     * a Processor should process.
     * Its value must be a list of String constants:
     *
     * <p>Example to make a BlockProcessor work on listings and examples named foo:
     * <pre>
     * <verbatim>
     * Map&lt;String, Object&gt; config = new HashMap&lt;&gt;();
     * config.put(Contexts.KEY, Arrays.asList(Contexts.EXAMPLE, Contexts.LISTING));
     * BlockProcessor blockProcessor = new BlockProcessor("foo", config);
     * asciidoctor.javaExtensionRegistry().block(blockProcessor);
     * </verbatim>
     * </pre>
     * </p>
     */
    String KEY = "contexts";
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
    String OPEN = ":open";

    /**
     * Predefined constant for making a Processor work on example blocks.
     * <pre>
     * [foo]
     * ====
     * This is just a neat example.
     * ====
     * </pre>
     */
    String EXAMPLE = ":example";

    /**
     * Predefined constant for making a Processor work on sidebar blocks.
     * <pre>
     * [foo]
     * ****
     * This is just a sidebar.
     * ****
     * </pre>
     */
    String SIDEBAR = ":sidebar";

    /**
     * Predefined constant for making a Processor work on literal blocks.
     * <pre>
     * [foo]
     * ....
     * This is just a literal block.
     * ....
     * </pre>
     */
    String LITERAL = ":literal";

    /**
     * Predefined constant for making a Processor work on source blocks.
     * <pre>
     * [foo]
     * ....
     * This is just a literal block.
     * ....
     * </pre>
     */
    String LISTING = ":listing";

    /**
     * Predefined constant for making a Processor work on quote blocks.
     * <pre>
     * [foo]
     * ____
     * To be or not to be...
     * ____
     * </pre>
     */
    String QUOTE = ":quote";

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
    String PASS = ":pass";

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
    String PARAGRAPH = ":paragraph";

    /**
     * Predefined constant for making a Processor work on ordered lists.
     *
     * <pre>
     * 1. First item
     * 2. Second item
     * </pre>
     */
    String OLIST = ":olist";

    /**
     * Predefined constant for making a Processor work on unordered lists.
     *
     * <pre>
     * . First item
     * . Second item
     * </pre>
     */
    String ULIST = ":ulist";

    /**
     * Predefined constant for making a Processor work on unordered lists.
     *
     * <pre>
     * . First item
     * . Second item
     * </pre>
     */
    String COLIST = ":colist";

    /**
     * Predefined constant for making a Processor work on unordered lists.
     *
     * <pre>
     * First:: The first item
     * Second:: The second item
     * </pre>
     */
    String DLIST = ":dlist";

    String[] value();

}
