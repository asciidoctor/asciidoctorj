package org.asciidoctor.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to define the block name handled by a {@link BlockProcessor}, or the macro name of a
 * {@link BlockMacroProcessor} or {@link InlineMacroProcessor}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Name {

    String value();

}
