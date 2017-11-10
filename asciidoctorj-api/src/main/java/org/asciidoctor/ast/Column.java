package org.asciidoctor.ast;

public interface Column extends ContentNode {

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
    Table.HorizontalAlignment getHorizontalAlignment();

    /**
     * Sets the horizontal alignment of all cells of this column.
     * @param halign Either {@link Table.HorizontalAlignment#LEFT}, {@link Table.HorizontalAlignment#CENTER} or {@link Table.HorizontalAlignment#RIGHT}
     */
    void setHorizontalAlignment(Table.HorizontalAlignment halign);

    /**
     * Returns the vertical alignment of all cells in this column.
     * @return a constant representing the vertical alignment.
     */
    Table.VerticalAlignment getVerticalAlignment();

    /**
     * Sets the vertical alignment of all cells of this column.
     * @param valign Either {@link Table.VerticalAlignment#TOP}, {@link Table.VerticalAlignment#MIDDLE} or {@link Table.VerticalAlignment#BOTTOM}
     */
    void setVerticalAlignment(Table.VerticalAlignment valign);

}
