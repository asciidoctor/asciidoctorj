package org.asciidoctor.categories;

import org.junitpioneer.jupiter.SetEnvironmentVariable;

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
@SetEnvironmentVariable(key = "GEM_PATH", value = Polluted.GEM_PATH)
@SetEnvironmentVariable(key = "GEM_HOME", value = Polluted.GEM_HOME)
public @interface Polluted {

    String GEM_PATH = "/some/path";
    String GEM_HOME = "/some/other/path";
}
