package org.asciidoctor.categories;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines tests that are executed within a "polluted" environment, that is
 * environment variables GEM_PATH and GEM_HOME will be set.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Tag("polluted")
public @interface Polluted {
}
