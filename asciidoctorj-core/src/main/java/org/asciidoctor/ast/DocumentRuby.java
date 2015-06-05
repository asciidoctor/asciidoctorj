package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface DocumentRuby extends AbstractBlock {

    /**
     * Get doc title
     * 
     * @param opts
     *            to get the doc title. Key should be Ruby symbols.
     * @return String if partition flag is not set to false or not present, Title if partition is set to true.
     * @see Title
     */
    Object doctitle(Map<Object, Object> opts);

    /**
     * Get doc title
     *
     * @return String if partition flag is not set to false or not present, Title if partition is set to true.
     * @see Title
     */
    Object doctitle();

    /**
     * 
     * @return page title
     */
    String title();

    /**
     * 
     * @return attributes defined in document
     */
    Map<String, Object> getAttributes();

    /**
     * @return basebackend attribute value
     */
    boolean isBasebackend(String backend);

    /**
     * @deprecated Please use {@link #isBasebackend(String)}
     * @return basebackend attribute value
     */
    boolean basebackend(String backend);

    /**
     *
     * @return blocks contained within current Document.
     */
    List<AbstractBlock> blocks();

    /**
     *
     * @return options defined in document.
     */
    Map<Object, Object> getOptions();
}
