package org.asciidoctor.core.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the block contexts a block processor applies to (the equivalent of the on_contexts DSL method).
 *
 * <p>The contexts may be specified with a leading colon (e.g., :paragraph), for compatibility with AsciidoctorJ.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Contexts {
    String KEY = "contexts";
    String OPEN = ":open";
    String EXAMPLE = ":example";
    String SIDEBAR = ":sidebar";
    String LITERAL = ":literal";
    String LISTING = ":listing";
    String QUOTE = ":quote";
    String PASS = ":pass";
    String PARAGRAPH = ":paragraph";
    String OLIST = ":olist";
    String ULIST = ":ulist";
    String COLIST = ":colist";
    String DLIST = ":dlist";

    String[] value();
}
