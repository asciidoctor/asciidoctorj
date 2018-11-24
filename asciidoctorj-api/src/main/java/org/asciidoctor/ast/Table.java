package org.asciidoctor.ast;

import java.util.List;

public interface Table extends StructuralNode {

    enum HorizontalAlignment {
        LEFT, CENTER, RIGHT
    }

    enum VerticalAlignment {
        TOP, BOTTOM, MIDDLE
    }

    boolean hasHeaderOption();

    List<Column> getColumns();

    List<Row> getHeader();

    List<Row> getFooter();

    List<Row> getBody();

    /**
     * Returns the frame attribute of the table that defines what frame to render around the table.
     * By default, the frame attribute is assigned the {@code all} value, which draws a border on each side of the table.
     * If you set the frame attribute, you can override the default value with {@code topbot}, {@code sides} or {@code none}.
     * @return the frame attribute
     */
    String getFrame();

    /**
     * Sets the frame attribute.
     * @see #getFrame()
     * @param frame {@code all}, {@code topbot}, {@code sides} or {@code none}
     */
    void setFrame(String frame);

    /**
     * Returns the grid attribute that defines what boundary lines to draw between rows and columns.
     * By default the grid attribute is assigned the {@code all} value, which draws lines around each cell.
     * Alternative values are {@code cols} to draw lines between columns, {@code rows} to draw boundary lines
     * between rows and {@code none} to draw no boundary lines
     * @return the value of the {@code grid} attribute, usually either {@code all}, {@code cols}, {@code rows} or {@code none}
     */
    String getGrid();

    /**
     * Sets the value of the {@grid} attribute.
     * @see #getGrid()
     * @param grid usually either {@code all}, {@code cols}, {@code rows} or {@code none}
     */
    void setGrid(String grid);

}
