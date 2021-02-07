package org.asciidoctor.arquillian;

import org.asciidoctor.arquillian.api.Shared;
import org.asciidoctor.arquillian.api.Unshared;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.junit.rules.TemporaryFolder;

import java.lang.annotation.Annotation;

public class TemporaryFolderResourceProvider implements ResourceProvider {

    @Inject @ApplicationScoped
    private Instance<ScopedTemporaryFolder> scopedTemporaryFolderInstance;

    @Override
    public boolean canProvide(Class<?> type) {
        return TemporaryFolder.class == type;
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        if (resource.value() == ArquillianResource.class // Default is ArquillianResource and should be unshared
                || resource.value() == Unshared.class) {
            return scopedTemporaryFolderInstance.get().getUnsharedTemporaryFolder();
        } else if (resource.value() == Shared.class) {
            return scopedTemporaryFolderInstance.get().getSharedTemporaryFolder();
        }
        return null;
    }
}
