package org.asciidoctor.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to define the block name handled by a BlockProcessor, or the macro name of a
 * BlockMacroProcessor or InlineMacroProcessor.
 * <p>Applicable for:
 * <table>
 * <tr><td>BlockMacroProcessor</td><td>&#10003;</td></tr>
 * <tr><td>BlockProcessor</td><td>&#10003;</td></tr>
 * <tr><td>BlockProcessor</td><td>&#10003;</td></tr>
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
public @interface Name {

    String value();

}
