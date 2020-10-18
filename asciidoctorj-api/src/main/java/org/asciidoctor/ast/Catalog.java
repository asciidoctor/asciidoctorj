package org.asciidoctor.ast;

import java.util.List;


public interface Catalog {

    /**
     * Note that footnotes are only available after `Document.getContent()` has been called.
     *
     * @return footnotes occurring in document.
     */
    List<Footnote> getFootnotes();
}