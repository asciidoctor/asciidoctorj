package org.asciidoctor.core.converter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the backend handled by a converter class, and optionally its filetype (format) and outfilesuffix.
 *
 * <p>The backend is used when the converter is registered without specifying a backend (e.g.,
 * {@code Converter.register(MyConverter.class)}). The format and suffix are used as the backend traits of an
 * {@link AbstractConverter}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConverterFor {
    String UNDEFINED = "";

    /** The backend (an alias of {@link #backend()}). */
    String value() default UNDEFINED;

    /** The backend. */
    String backend() default UNDEFINED;

    /** The filetype of the output (e.g., html). */
    String format() default UNDEFINED;

    /** The suffix of the output file (e.g., .html). */
    String suffix() default UNDEFINED;
}
