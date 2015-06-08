package org.asciidoctor.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assigns a default backend name to a converter so that it is not required to define
 * the backend name at registration time.
 *
 * <p>Example:
 * <pre>
 * <code>&#64;Backend("my")
 * public class MyConverter extends AbstractConverter {
 *     ...
 * }
 *
 * asciidoctor.javaConverterRegistry().register(MyConverter.class);
 * </code>
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Backend {

    String value();

}
