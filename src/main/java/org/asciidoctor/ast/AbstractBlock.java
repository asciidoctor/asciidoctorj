package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface AbstractBlock {

	String id();
	String title();
    String role();
    String style();
    List<AbstractBlock> blocks();
    Map<String, Object> attributes();
    Object content();
    String render();
    
    DocumentRuby document();
    String context();
    
    AbstractBlock delegate();
    List<AbstractBlock> findBy(Map<Object, Object> selector);
}
