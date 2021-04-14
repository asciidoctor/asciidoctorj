package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

/**
 * Document header information holder.
 *
 */
@Deprecated
public interface DocumentHeader {

    /**
     * @deprecated Use {@link Document#getAuthors()} instead.
     */
    @Deprecated
    List<Author> getAuthors();

    /**
     * @deprecated Use {@link Document#getStructuredDoctitle()} instead.
     */
    @Deprecated
    Title getDocumentTitle();

    /**
     * @deprecated Use {@link Document#getDoctitle()} instead.
     */
    @Deprecated
    String getPageTitle();

    /**
     * @deprecated Use {@link Document#getAuthors()} instead.
     */
    @Deprecated
    Author getAuthor();

    /**
     * @deprecated Use {@link Document#getRevisionInfo()} instead.
     */
    @Deprecated
    RevisionInfo getRevisionInfo();

    /**
     * @deprecated Use {@link Document#getAttributes()} instead.
     */
    @Deprecated
    Map<String, Object> getAttributes();
}
