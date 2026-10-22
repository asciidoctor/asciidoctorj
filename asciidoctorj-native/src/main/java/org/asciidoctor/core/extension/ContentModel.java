package org.asciidoctor.core.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the content model of a block or macro processor (the equivalent of the parse_content_as DSL method).
 *
 * <p>The content model may be specified with a leading colon (e.g., :simple), for compatibility with AsciidoctorJ.
 *
 * @see org.asciidoctor.core.ContentModel
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContentModel {
    String KEY = "content_model";
    String COMPOUND = ":compound";
    String SIMPLE = ":simple";
    String VERBATIM = ":verbatim";
    String RAW = ":raw";
    String SKIP = ":skip";
    String EMPTY = ":empty";
    String ATTRIBUTES = ":attributes";

    String value();
}
