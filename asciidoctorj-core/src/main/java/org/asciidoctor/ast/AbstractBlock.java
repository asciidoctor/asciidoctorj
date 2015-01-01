package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface AbstractBlock extends AbstractNode {

    String title();
    String style();
    List<AbstractBlock> blocks();
    Object content();
    String convert();
    DocumentRuby document();
    AbstractBlock delegate();
    List<AbstractBlock> findBy(Map<Object, Object> selector);
    int level();
}
