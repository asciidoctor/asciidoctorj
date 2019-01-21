package org.asciidoctor.ast;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines how to handle content created by a {@code BlockProcessor}.
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
public @interface ContentModel {

    /**
     * This value is used as the config option key to configure how Asciidoctor should treat blocks created by
     * this Processor.
     * Its value must be a String constant.
     *
     * <p>Example to Asciidoctor know that a BlockProcessor creates zero or more child blocks:
     * <pre>
     * <verbatim>
     * Map&lt;String, Object&gt; config = new HashMap&lt;&gt;();
     * config.put(ContentModel.KEY, ContentModel.COMPOUND);
     * BlockProcessor blockProcessor = new BlockProcessor("foo", config);
     * asciidoctor.javaExtensionRegistry().block(blockProcessor);
     * </verbatim>
     * </pre>
     * </p>
     */
    String KEY = "content_model";

    /**
     * Predefined constant to let Asciidoctor know that this BlockProcessor creates zero or more child blocks.
     */
    String COMPOUND = ":compound";

    /**
     * Predefined constant to let Asciidoctor know that this BlockProcessor creates simple paragraph content.
     */
    String SIMPLE = ":simple";

    /**
     * Predefined constant to let Asciidoctor know that this BlockProcessor creates literal content.
     */
    String VERBATIM = ":verbatim";

    /**
     * Predefined constant to make Asciidoctor pass through the content unprocessed.
     */
    String RAW = ":raw";

    /**
     * Predefined constant to make Asciidoctor drop the content.
     */
    String SKIP = ":skip";

    /**
     * Predefined constant to make Asciidoctor not expect any content.
     */
    String EMPTY = ":empty";

    /**
     * Predefined constant to make Asciidoctor parse content as attributes.
     */
    String ATTRIBUTES = ":attributes";

    /**
     * See the constants defined in this enumeration for possible values.
     * @return The defined content model
     */
    String value();

}
