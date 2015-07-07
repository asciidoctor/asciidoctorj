package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface AbstractBlock extends AbstractNode {

    /**
     * @deprecated Please use {@linkplain #getTitle()} instead
     */
    String title();
    String getTitle();
    void setTitle(String title);
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
    List<AbstractBlock> findBy(Map<Object, Object> selector);
    int getLevel();

    /**
     * Returns the source location of this block.
     * This information is only available if the {@code sourcemap} option is enabled when loading or rendering the document.
     * @return the source location of this block or {@code null} if the {@code sourcemap} option is not enabled when loading the document.
     */
    Cursor getSourceLocation();
}
