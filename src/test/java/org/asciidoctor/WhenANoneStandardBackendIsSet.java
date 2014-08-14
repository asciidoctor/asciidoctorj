package org.asciidoctor;

import static org.asciidoctor.OptionsBuilder.options;

import java.io.File;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Test;

public class WhenANoneStandardBackendIsSet {

    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();
    
    @Test
    public void epub3_should_be_rendered_for_epub3_backend() {
        
        System.setProperty("file.encoding", "UTF-8");
        
        asciidoctor.renderFile(new File("target/test-classes/epub-index.adoc"),
                options().backend("epub3").get());
    }
    
}
