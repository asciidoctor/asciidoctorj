package org.asciidoctor.arquillian;

import org.asciidoctor.junit.ClasspathResources;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import java.lang.annotation.Annotation;

public class ClasspathResourcesResourceProvider implements ResourceProvider {

    @Inject
    @ApplicationScoped
    private Instance<ClasspathResources> classpathResourcesInstance;

    @Override
    public boolean canProvide(Class<?> type) {
        return type == ClasspathResources.class;
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return classpathResourcesInstance.get();
    }
}
