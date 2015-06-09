package org.asciidoctor.ast;

public interface Column extends AbstractNode {

    /**
     * Returns the style of this column.
     * The default is {@code null}.
     * Possible values are:
     * <ul>
     *     <li>{@code null}</li>
     *     <li>{@code "strong"}</li>
     *     <li>{@code "emphasis"}</li>
     *     <li>{@code "monospaced"}</li>
     *     <li>{@code "header"}</li>
     *     <li>{@code "literal"}</li>
     *     <li>{@code "verse"}</li>
     *     <li>{@code "asciidoc"}</li>
     * </ul>
     * @return The style of this cell.
     */
    String getStyle();

    /**
     * Sets the style of this column.
     * @see #getStyle()
     * @param style Values like {@code asciidoc}, {@code verse}, {@code literal}or {@code header}.
     */
    void setStyle(String style);

    Table getTable();

    int getColumnNumber();

    int getWidth();

    void setWidth(int width);

    /**
     * Returns the horizonzal alignment of all cells in this column.
     * @return a constant representing the horizontal alignment.
     */
    Table.HAlign getHAlign();

    /**
     * Sets the horizontal alignment of all cells of this column.
     * @param halign Either {@link Table.HAlign#LEFT}, {@link Table.HAlign#CENTER} or {@link Table.HAlign#RIGHT}
     */
    void setHAlign(Table.HAlign halign);

    /**
     * Returns the vertical alignment of all cells in this column.
     * @return a constant representing the vertical alignment.
     */
    Table.VAlign getVAlign();

    /**
     * Sets the vertical alignment of all cells of this column.
     * @param valign Either {@link Table.VAlign#TOP}, {@link Table.VAlign#MIDDLE} or {@link Table.VAlign#BOTTOM}
     */
    void setVAlign(Table.VAlign valign);

}
