package org.asciidoctor;

import static org.junit.Assert.assertThat;
import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;

import java.io.File;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Test;

public class WhenANoneStandardBackendIsSet {

    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();
    
    @Test
    public void epub3_should_be_rendered_for_epub3_backend() {
        
        asciidoctor.renderFile(new File("target/test-classes/epub-index.adoc"),
                options().safe(SafeMode.SAFE).backend("epub3").get());
        
        assertThat(new File("target/test-classes/epub-index.epub").exists(), is(true));
        
    }
    
}
