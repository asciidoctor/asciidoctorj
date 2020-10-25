package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;


public interface Catalog {

    /**
     * Note that footnotes are only available after `Document.getContent()` has been called.
     *
     * A converter uses cataloged footnotes to render them, presumably, at the bottom of a document.
     *
     * @return footnotes occurring in document.
     */
    List<Footnote> getFootnotes();

    /**
     * Refs is a map of asciidoctor ids to asciidoctor document elements.
     *
     * For example, by default, each section is automatically assigned an id.
     * In this case the id would map to a {@link Section} element.
     *
     * Ids can also be explicitly assigned by document authors to any document element.
     * See https://asciidoctor.org/docs/user-manual/#id
     *
     * A converter might use cataloged refs to lookup ids to support rendering inline anchors.
     *
     * @return a map of ids to elements that asciidoctor has collected from the document.
     */
    Map<String, Object> getRefs();
}
