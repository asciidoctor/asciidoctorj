package org.asciidoctor.ast;

import java.util.List;

/**
 * The asciidoctor catalog contains data collected by asciidoctor that is useful to a converter.
 * The catalog is considered an internal structure by asciidoctor, and subject to change.
 */
public interface Catalog {

    /**
     * Note that asciidoctor does not populate footnotes until after the
     * Document's content has been converted.
     *
     * @return footnotes occurring in document.
     */
    List<Footnote> getFootnotes();
}