package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface AbstractBlock extends AbstractNode {

    /**
     * @deprecated Please use {@linkplain #getTitle()} instead
     */
    String title();
    String getTitle();
    /**
     * @deprecated Please use {@linkplain #getStyle()} instead
     */
    String style();
    String getStyle();
    /**
     * @deprecated Please use {@linkplain #getBlocks()} instead
     */
    List<AbstractBlock> blocks();
    List<AbstractBlock> getBlocks();
    /**
     * @deprecated Please use {@linkplain #getContent()} instead
     */
    Object content();
    Object getContent();
    String convert();
    AbstractBlock delegate();
    List<AbstractBlock> findBy(Map<Object, Object> selector);
    int getLevel();
}
