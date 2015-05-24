package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface BlockNode extends Node {

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
    List<BlockNode> blocks();
    List<BlockNode> getBlocks();
    /**
     * @deprecated Please use {@linkplain #getContent()} instead
     */
    Object content();
    Object getContent();
    String convert();
    BlockNode delegate();
    List<BlockNode> findBy(Map<Object, Object> selector);
    int getLevel();
}
