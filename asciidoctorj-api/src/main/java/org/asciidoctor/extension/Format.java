package org.asciidoctor.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines how an InlineMacroProcessor is applied.
 * Possible values are:
 * <dl>
 *  <dt>{@link FormatType#LONG}</dt>
 *  <dd>to match inline macros of the form: {@code <macro name> ':' <target> '[' <attributes> ']'}</dd>
 *  <dt>{@link FormatType#SHORT}</dt>
 *  <dd>to match inline macros of the form: {@code <macro name> ':' '[' <attributes> ']'}</dd>
 *  <dt>{@link FormatType#CUSTOM}</dt>
 *  <dd>the regular expression defined by the {@linkplain #regexp()} has to match the macro. The first capture will
 *  be mapped to the target, the second to the attributes.</dd>
 * </dl>
 * <p>Applicable for:
 * <table>
 * <tr><td>BlockMacroProcessor</td><td></td></tr>
 * <tr><td>BlockProcessor</td><td></td></tr>
 * <tr><td>BlockProcessor</td><td></td></tr>
 * <tr><td>DocInfoProcessor</td><td></td></tr>
 * <tr><td>IncludeProcessor</td><td></td></tr>
 * <tr><td>InlineMacroProcessor</td><td>&#10003;</td></tr>
 * <tr><td>Postprocessor</td><td></td></tr>
 * <tr><td>Preprocessor</td><td></td></tr>
 * <tr><td>Treeprocessor</td><td></td></tr>
 * </table>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Format {

    FormatType value() default FormatType.CUSTOM;

    String regexp() default "";

}
