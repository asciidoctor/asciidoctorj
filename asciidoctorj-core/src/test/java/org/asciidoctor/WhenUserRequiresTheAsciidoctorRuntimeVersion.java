package org.asciidoctor;

import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(AsciidoctorExtension.class)
public class WhenUserRequiresTheAsciidoctorRuntimeVersion {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @Test
    public void asciidoctorj_version_should_be_retrieved() {
        final String version = asciidoctor.getVersion();

        assertThat(version).isNotBlank();
    }

    @Test
    public void asciidoctor_ruby_version_should_be_retrieved() {
        final String asciidoctorVersion = asciidoctor.unwrap(AsciidoctorJRuby.class).asciidoctorVersion();

        assertThat(asciidoctorVersion).isNotBlank();
    }
}
