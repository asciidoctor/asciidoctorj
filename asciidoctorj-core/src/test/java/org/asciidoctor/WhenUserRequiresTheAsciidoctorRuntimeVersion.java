package org.asciidoctor;

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
    public void current_version_should_be_retrieved() {
        final String asciidoctorVersion = asciidoctor.asciidoctorVersion();

        assertThat(asciidoctorVersion).isNotNull();
    }
}
