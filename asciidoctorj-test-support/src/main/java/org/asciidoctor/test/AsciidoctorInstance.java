package org.asciidoctor.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_CLASS;

/**
 * Marks and {@link org.asciidoctor.Asciidoctor} property to be automatically injected.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AsciidoctorInstance {

    /**
     * When the {@link org.asciidoctor.Asciidoctor} instance will be initialized.
     * <p>
     * Based on {@link org.junit.jupiter.api.TestInstance.Lifecycle}.
     */
    enum InstanceScope {

        /**
         * Once per class, before running any tests. The same instance will
         * be shared across all tests.
         */
        PER_CLASS,
        /**
         * Once per test, before running every test method.
         */
        PER_METHOD
    }


    /**
     * Scope for the Asciidoctor instance being created.
     */
    InstanceScope scope() default PER_CLASS;

}
