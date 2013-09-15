package org.asciidoctor.extension;

import java.io.File;
import java.util.HashMap;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Test;

public class ExtensionTest {

    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();
    
    @Test
    public void render() {
        String render_file = asciidoctor.renderFileExtension(new File("target/test-classes/render-with-front-matter.adoc"), "FrontMatterPreprocessor",
                new Options());
        
        System.out.println(render_file);
    }
    
}
