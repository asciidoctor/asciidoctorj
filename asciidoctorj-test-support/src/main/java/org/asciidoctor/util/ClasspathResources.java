package org.asciidoctor.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit TestRule to handle classpath files.
 * <p>
 * Delegates to {@link ClasspathHelper}.
 */
public class ClasspathResources extends ClasspathHelper implements TestRule {

    public ClasspathResources() {
    }

    public ClasspathResources(Class<?> clazz) {
        setClassloader(clazz);
    }

    protected void before(Class<?> clazz) {
        super.setClassloader(clazz);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before(description.getTestClass());
                base.evaluate();
            }
        };
    }
}
