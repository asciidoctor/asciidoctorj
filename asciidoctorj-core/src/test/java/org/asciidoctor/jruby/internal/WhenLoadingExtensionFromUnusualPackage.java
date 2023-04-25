package org.asciidoctor.jruby.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import unusual.extension.BoldifyPostProcessor;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;

@ExtendWith(AsciidoctorExtension.class)
public class WhenLoadingExtensionFromUnusualPackage {

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    @Test
    public void shouldAllowLoadingUsingInstance() {
        final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
        registry.postprocessor(new unusual.extension.BoldifyPostProcessor());
    }

    @Test
    public void shouldAllowLoadingByClassName() {
        final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
        registry.postprocessor(BoldifyPostProcessor.class.getCanonicalName());
    }

    @Test
    public void shouldAllowLoadingByClass() {
        final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
        registry.postprocessor(BoldifyPostProcessor.class);
    }
}
