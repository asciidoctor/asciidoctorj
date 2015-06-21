package org.asciidoctor.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines how an {@link InlineMacroProcessor} is applied.
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
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Format {

    public FormatType value() default FormatType.CUSTOM;

    public String regexp() default "";

}
