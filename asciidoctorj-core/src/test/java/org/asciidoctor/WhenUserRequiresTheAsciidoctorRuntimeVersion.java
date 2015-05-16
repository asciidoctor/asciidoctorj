package org.asciidoctor;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.asciidoctor.arquillian.api.Unshared;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WhenUserRequiresTheAsciidoctorRuntimeVersion {

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @Test
    public void current_version_should_be_retrieved() {
        
        String asciidoctorVersion = asciidoctor.asciidoctorVersion();
        
        assertThat(asciidoctorVersion, is(notNullValue()));
        
    }
    
}
