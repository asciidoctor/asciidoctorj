package org.asciidoctor.arquillian;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.arquillian.api.Shared;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import java.lang.annotation.Annotation;

public class AsciidoctorResourceProvider implements ResourceProvider {

    @Inject @ApplicationScoped
    private Instance<ScopedAsciidoctor> scopedAsciidoctorInstance;

    @Override
    public boolean canProvide(Class<?> type) {
        return Asciidoctor.class == type || AsciidoctorJRuby.class == type;
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        if (resource.value() == ArquillianResource.class // Default is ArquillianResource and should be unshared
                || resource.value() == Unshared.class) {
            return scopedAsciidoctorInstance.get().getUnsharedAsciidoctor();
        } else if (resource.value() == Shared.class) {
            return scopedAsciidoctorInstance.get().getSharedAsciidoctor();
        }
        return null;
    }
}
