package org.asciidoctor.arquillian;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class AsciidoctorExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(ResourceProvider.class, AsciidoctorResourceProvider.class);
        builder.service(ResourceProvider.class, ClasspathResourcesResourceProvider.class);
        builder.service(ResourceProvider.class, TemporaryFolderResourceProvider.class);
        builder.observer(AsciidoctorTestObserver.class);
    }

}
