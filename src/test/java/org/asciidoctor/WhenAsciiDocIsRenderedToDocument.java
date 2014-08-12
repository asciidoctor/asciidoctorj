package org.asciidoctor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Test;

public class WhenAsciiDocIsRenderedToDocument {

    private static final String DOCUMENT = "= Document Title\n" + 
            "\n" + 
            "preamble\n" + 
            "\n" + 
            "== Section A\n" + 
            "\n" + 
            "paragraph\n" + 
            "\n" + 
            "--\n" + 
            "Exhibit A::\n" + 
            "+\n" + 
            "[#tiger.animal]\n" + 
            "image::tiger.png[Tiger]\n" + 
            "--\n" + 
            "\n" + 
            "image::cat.png[Cat]\n" + 
            "\n" + 
            "== Section B\n" + 
            "\n" + 
            "paragraph";
    
    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

    @Test
    public void should_return_blocks_from_a_document() {
        
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        for (Block block : document.blocks()) {
            System.out.println(block.role());
        }
        
    }
    
    @Test
    public void should_return_a_document_object_from_string() {
        
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.doctitle(), is("Document Title"));
    }
    
    @Test
    public void should_find_elements_from_document() {
        
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        Map<Object, Object> selector = new HashMap<Object, Object>();
        selector.put("context", ":image");
        List<AbstractBlock> findBy = document.findBy(selector);
        assertThat(findBy, hasSize(2));
        
        assertThat((String)findBy.get(0).attributes().get("target"), is("tiger.png"));
        
    }
    
}
