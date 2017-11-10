package org.asciidoctor.ast;

public interface ListItem extends StructuralNode {

    String getMarker();

    /**
     * @return The text of the cell including substitutions being applied.
     */
    String getText();

    /**
     * @return The text of the cell without substitutions being applied.
     */
    String getSource();

    /**
     * Sets the source of the ListItem.
     * @param source The source of this ListItem, substitutions will still be applied.
     */
    void setSource(String source);

    boolean hasText();
}
