package org.asciidoctor.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the on which attributes the first, second, etc attribute of a macro is mapped.
 * <p>Example: For this inline macro that defines {@code section} as the first positional parameter:
 * <pre>
 * <code>&#64;PositionalAttribute("section")
 * &#64;('man')
 * public class ManPageMacroProcessor extends InlineMacroProcessor {
 *     public ManPageMacroProcessor(String macroName) {
 *         super(macroName)
 *     }
 *
 *     public Object process(StructuralNode parent, String target, Map&lt;String, Object&gt; attributes) {
 *         assertEquals(attributes.get("section"), "7")
 *     }
 * }
 * </code>
 * </pre>
 *
 * this macro invocation will pass {@code "7"} as value for the attribute {@code section}:
 *
 * {@code man:gittutorial[7]}
 * <p>Applicable for:
 * <table>
 * <tr><td>BlockMacroProcessor</td><td>&#10003;</td></tr>
 * <tr><td>BlockProcessor</td><td>&#10003;</td></tr>
 * <tr><td>BlockProcessor</td><td>&#10003;</td></tr>
 * <tr><td>DocInfoProcessor</td><td></td></tr>
 * <tr><td>IncludeProcessor</td><td>&#10003;</td></tr>
 * <tr><td>InlineMacroProcessor</td><td>&#10003;</td></tr>
 * <tr><td>Postprocessor</td><td></td></tr>
 * <tr><td>Preprocessor</td><td></td></tr>
 * <tr><td>Treeprocessor</td><td></td></tr>
 * </table>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PositionalAttributes {

    String[] value();

}
