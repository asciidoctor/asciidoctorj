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
    public String getRawText();

    public boolean hasText();
}
