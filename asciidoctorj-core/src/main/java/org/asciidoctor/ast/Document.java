package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface Document extends AbstractBlock {

    /**
     * @return The Title structure for this document.
     * @see Title
     */
    Title getStructuredDoctitle();

    /**
     * @return The title as a String.
     * @see Title
     */
    String getDoctitle();

    /**
     * @deprecated Please use {@link #getDoctitle()}
     * @return The title as a String.
     * @see Title
     */
    String doctitle();

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
