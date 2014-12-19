package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface AbstractBlock extends AbstractNode {

    String id();
    String title();
    String role();
    String style();
    List<AbstractBlock> blocks();
    Object content();
    String convert();
    DocumentRuby document();
    String context();
    AbstractBlock delegate();
    List<AbstractBlock> findBy(Map<Object, Object> selector);
}
