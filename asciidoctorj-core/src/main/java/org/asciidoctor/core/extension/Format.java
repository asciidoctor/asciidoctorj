package org.asciidoctor.core.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the format of an inline macro (short, long or a custom regular expression).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Format {
    FormatType value() default FormatType.CUSTOM;

    String regexp() default "";
}
