package org.asciidoctor;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.Test;

public class WhenUserRequiresTheAsciidoctorRuntimeVersion {

    @Test
    public void current_version_should_be_retrieved() {
        
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        String asciidoctorVersion = asciidoctor.asciidoctorVersion();
        
        assertThat(asciidoctorVersion, is(notNullValue()));
        
    }
    
}
