package org.asciidoctor.junit;

import org.asciidoctor.test.extension.ClasspathHelper;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit TestRule to handle classpath files.
 * <p>
 * Delegates to {@link ClasspathHelper}.
 */
public class ClasspathResources implements TestRule {

    private ClasspathHelper classpathHelper;

    public ClasspathResources() {
    }

    public ClasspathResources(Class<?> clazz) {
        classpathHelper = new ClasspathHelper(clazz);
    }

    protected void before(Class<?> clazz) {
        classpathHelper = new ClasspathHelper(clazz);
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
