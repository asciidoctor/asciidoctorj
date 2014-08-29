package org.asciidoctor.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


public class ClasspathResources extends ClassPathHelper implements TestRule {

    protected void before(Class<?> clazz) throws Throwable {
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
