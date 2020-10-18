package org.asciidoctor.ast;

public interface Footnote {

   /**
    * The index is the number asciidoctor has assigned to the footnote.
    * Footnotes start at 1 and are numbered consecutively throughout the document.
    *
    * @return footnote number
    */
   Long getIndex();

   /**
    * Each footnote can optionally have an id.
    * Ids are used when a document author wants to reference a single footnote more than once.
    *
    * @return footnote id or null
    */
   String getId();

   /**
    * @return the text the document author has specified for the footnote
    */
   String getText();
}
