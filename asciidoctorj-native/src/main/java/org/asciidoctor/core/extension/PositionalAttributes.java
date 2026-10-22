package org.asciidoctor.core.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the names of the positional attributes of a block or macro processor (the equivalent of the
 * positional_attributes DSL method).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PositionalAttributes {
    String[] value();
}
