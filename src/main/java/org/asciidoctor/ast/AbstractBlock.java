package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface AbstractBlock {

	String id();
	String title();
    String role();
    String style();
    List<Block> blocks();
    Map<String, Object> attributes();
    
    Object content();
    String render();
    
    String context();
    
    AbstractBlock delegate();
}
