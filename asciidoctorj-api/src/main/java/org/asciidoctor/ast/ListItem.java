package org.asciidoctor.ast;

public interface ListItem extends StructuralNode {

    public String getMarker();

    /**
     * @return The text of the cell including substitutions being applied.
     */
    public String getText();

    /**
     * @return The text of the cell without substitutions being applied.
     */
    public String getSource();

    /**
     * Sets the source of the ListItem.
     * @param source The source of this ListItem, substitutions will still be applied.
     */
    public void setSource(String source);

    public boolean hasText();
}
