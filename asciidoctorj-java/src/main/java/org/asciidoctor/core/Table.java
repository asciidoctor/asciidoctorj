package org.asciidoctor.core;

import static org.asciidoctor.core.Asciidoctor.LF;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.core.converter.Converter;

import org.asciidoctor.core.Reader.Cursor;

/**
 * Methods and constants for managing AsciiDoc table content in a document. It supports all three of AsciiDoc's table
 * formats: psv, dsv and csv.
 */
public class Table extends AbstractBlock {
    /** Precision of column widths. */
    public static final int DEFAULT_PRECISION = 4;

    /**
     * A row of a table, which is the List of its cells (an Array in Ruby).
     */
    public static final class Row extends AbstractList<Cell> implements RandomAccess {
        private final List<Cell> cells;

        public Row(List<Cell> cells) {
            this.cells = new ArrayList<>(cells);
        }

        public Row() {
            this(List.of());
        }

        /**
         * @return the cells of this row (a live view)
         */
        public List<Cell> getCells() {
            return this;
        }

        @Override
        public Cell get(int index) {
            return cells.get(index);
        }

        @Override
        public int size() {
            return cells.size();
        }

        @Override
        public Cell set(int index, Cell cell) {
            return cells.set(index, cell);
        }

        @Override
        public void add(int index, Cell cell) {
            cells.add(index, cell);
        }

        @Override
        public Cell remove(int index) {
            return cells.remove(index);
        }
    }

    /**
     * A data object that encapsulates the collection of rows (head, foot, body) for a table.
     */
    public static class Rows {
        private List<Row> head;
        private List<Row> foot;
        private List<Row> body;

        public Rows(List<Row> head, List<Row> foot, List<Row> body) {
            this.head = head;
            this.foot = foot;
            this.body = body;
        }

        public Rows() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        public List<Row> getHead() {
            return head;
        }

        public void setHead(List<Row> head) {
            this.head = head;
        }

        public List<Row> getFoot() {
            return foot;
        }

        public void setFoot(List<Row> foot) {
            this.foot = foot;
        }

        public List<Row> getBody() {
            return body;
        }

        public void setBody(List<Row> body) {
            this.body = body;
        }

        /**
         * Retrieve the rows grouped by section.
         *
         * <p>Creates a Map where the keys are the section names (head, body, foot) and the values are the List of rows
         * in that section. The entries are in document order (head, body, foot).
         *
         * @return a Map of rows grouped by section.
         */
        public Map<String, List<Row>> bySection() {
            Map<String, List<Row>> result = new LinkedHashMap<>();
            result.put("head", head);
            result.put("body", body);
            result.put("foot", foot);
            return result;
        }

        /**
         * @return all the rows in this table (head, body and foot)
         */
        public List<Row> all() {
            List<Row> result = new ArrayList<>(head);
            result.addAll(body);
            result.addAll(foot);
            return result;
        }
    }

    private List<Column> columns = new ArrayList<>();
    private Rows rows = new Rows();

    // the rows by section (named like in AsciidoctorJ)

    public List<Row> getHeader() {
        return rows.getHead();
    }

    public List<Row> getBody() {
        return rows.getBody();
    }

    public List<Row> getFooter() {
        return rows.getFoot();
    }
    private Object hasHeaderOption = false;

    public Table(AbstractBlock parent, Map<String, Object> attributes) {
        super(parent, "table");

        // smells like we need a utility method here
        // to resolve an integer width from potential bogus input
        int pcwidthIntval;
        Object pcwidth = attributes.get("width");
        if (pcwidth != null) {
            pcwidthIntval = Strings.toInt(pcwidth);
            if (pcwidthIntval > 100 || pcwidthIntval < 1) {
                if (!(pcwidthIntval == 0 && ("0".equals(pcwidth) || "0%".equals(pcwidth)))) {
                    pcwidthIntval = 100;
                }
            }
        } else {
            pcwidthIntval = 100;
        }
        this.attributes.put("tablepcwidth", pcwidthIntval);

        Object pagewidth = document.getAttributes().get("pagewidth");
        if (pagewidth != null) {
            double abswidthVal = truncate((pcwidthIntval / 100.0) * Strings.toDouble(pagewidth), DEFAULT_PRECISION);
            this.attributes.put("tableabswidth", toNumber(abswidthVal));
        }

        if (attributes.get("rotate-option") != null) {
            this.attributes.put("orientation", "landscape");
        }
    }

    /**
     * Truncates the value to the specified precision (mirrors Float#truncate).
     *
     * <p>Like Ruby, the result is corrected when the scaled value is off by one unit in the last place due to the
     * binary representation of the value (e.g., 28.333299999999998 is truncated to 28.3333).
     */
    static double truncate(double value, int precision) {
        if (value < 0) {
            return -truncate(-value, precision);
        }
        double scale = Math.pow(10, precision);
        double floor = Math.floor(value * scale);
        return (floor + 1) / scale <= value ? (floor + 1) / scale : floor / scale;
    }

    /**
     * Rounds the value to the specified precision using half up rounding (mirrors Float#round).
     */
    static double round(double value, int precision) {
        double scale = Math.pow(10, precision);
        double scaled = value * scale;
        // round half away from zero (like C's round function)
        double rounded = Math.signum(scaled) * Math.floor(Math.abs(scaled) + 0.5);
        if (value > 0 && (rounded + 0.5) / scale <= value) {
            rounded += 1;
        } else if (value < 0 && (rounded - 0.5) / scale >= value) {
            rounded -= 1;
        }
        return rounded / scale;
    }

    /**
     * Converts the value to an Integer if it has no fractional part, otherwise keeps it as a Double.
     */
    static Number toNumber(double value) {
        return value == (long) value ? (Number) (int) value : (Number) value;
    }

    /**
     * @return the columns for this table
     */
    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    /**
     * @return the Rows struct for this table (encapsulates head, foot and body rows)
     */
    public Rows getRows() {
        return rows;
    }

    public void setRows(Rows rows) {
        this.rows = rows;
    }

    /**
     * @return whether this table has a header row (true, false, or "implicit" while parsing)
     */
    public boolean hasHeaderOption() {
        return hasHeaderOption != null && !Boolean.FALSE.equals(hasHeaderOption);
    }

    public Object getHasHeaderOption() {
        return hasHeaderOption;
    }

    public void setHasHeaderOption(Object hasHeaderOption) {
        this.hasHeaderOption = hasHeaderOption;
    }

    /**
     * @return the caption for this table
     */
    @Override
    public String getCaption() {
        return caption;
    }

    /**
     * Internal: Returns the current state of the header option (true or "implicit") if the row being processed is (or
     * is assumed to be) the header row, otherwise null.
     */
    public Object getHeaderRow() {
        return hasHeaderOption() && rows.getBody().isEmpty() ? hasHeaderOption : null;
    }

    /**
     * Internal: Creates the Column objects from the column spec.
     */
    public void createColumns(List<Map<String, Object>> colspecs) {
        List<Column> cols = new ArrayList<>();
        List<Column> autowidthCols = null;
        int widthBase = 0;
        for (Map<String, Object> colspec : colspecs) {
            int colwidth = Strings.toInt(colspec.get("width"));
            cols.add(new Column(this, cols.size(), colspec));
            if (colwidth < 0) {
                if (autowidthCols == null) {
                    autowidthCols = new ArrayList<>();
                }
                autowidthCols.add(cols.get(cols.size() - 1));
            } else {
                widthBase += colwidth;
            }
        }
        int numCols = (columns = cols).size();
        if (numCols > 0) {
            attributes.put("colcount", numCols);
            assignColumnWidths(widthBase > 0 || autowidthCols != null ? widthBase : null, autowidthCols);
        }
    }

    /**
     * Internal: Assign column widths to columns.
     *
     * <p>This method rounds the percentage width values to 4 decimal places and donates the balance to the final
     * column.
     *
     * <p>This method assumes there's at least one column in the columns list.
     *
     * @param widthBase the total of the relative column values used for calculating percentage widths (default: null)
     * @param autowidthCols the columns that have an automatic width (default: null)
     */
    public void assignColumnWidths(Integer widthBase, List<Column> autowidthCols) {
        int precision = DEFAULT_PRECISION;
        double totalWidth = 0;
        double colPcwidth = 0;

        if (widthBase != null) {
            if (autowidthCols != null) {
                Number autowidth;
                if (widthBase > 100) {
                    autowidth = 0;
                    getLogger().warn("total column width must not exceed 100% when using autowidth columns; got "
                            + widthBase + "%");
                } else {
                    autowidth = toNumber(truncate((100.0 - widthBase) / autowidthCols.size(), precision));
                    widthBase = 100;
                }
                for (Column col : autowidthCols) {
                    col.getAttributes().put("width", autowidth);
                    col.getAttributes().put("autowidth-option", "");
                }
            }
            for (Column col : columns) {
                totalWidth += (colPcwidth = col.assignWidth(null, widthBase, precision).doubleValue());
            }
        } else {
            Number pcwidth = toNumber(truncate(100.0 / columns.size(), precision));
            colPcwidth = pcwidth.doubleValue();
            for (Column col : columns) {
                totalWidth += col.assignWidth(pcwidth, null, precision).doubleValue();
            }
        }

        // donate balance, if any, to final column (using half up rounding)
        if (totalWidth != 100) {
            columns.get(columns.size() - 1).assignWidth(toNumber(round(100 - totalWidth + colPcwidth, precision)), null, precision);
        }
    }

    public void assignColumnWidths() {
        assignColumnWidths(null, null);
    }

    /**
     * Internal: Partition the rows into header, footer and body as determined by the options on the table.
     */
    public void partitionHeaderFooter(Map<String, Object> attrs) {
        // set rowcount before splitting up body rows
        List<Row> body = rows.getBody();
        int numBodyRows = body.size();
        attributes.put("rowcount", numBodyRows);

        if (numBodyRows > 0) {
            if (hasHeaderOption()) {
                Row headRow = new Row();
                for (Cell cell : body.remove(0)) {
                    headRow.add(cell.reinitialize(true));
                }
                List<Row> head = new ArrayList<>();
                head.add(headRow);
                rows.setHead(head);
                numBodyRows--;
            } else if (hasHeaderOption == null) {
                hasHeaderOption = false;
                Row firstRow = new Row();
                for (Cell cell : body.remove(0)) {
                    firstRow.add(cell.reinitialize(false));
                }
                body.add(0, firstRow);
            }
        }

        if (numBodyRows > 0 && attrs.get("footer-option") != null) {
            List<Row> foot = new ArrayList<>();
            foot.add(body.remove(body.size() - 1));
            rows.setFoot(foot);
        }
    }

    /**
     * Methods to manage the columns of an AsciiDoc table. In particular, it keeps track of the column specs.
     */
    public static class Column extends AbstractNode {
        private String style;

        public Column(Table table, int index, Map<String, Object> attributes) {
            super(table, "table_column");
            if (attributes == null) {
                attributes = new LinkedHashMap<>();
            }
            style = Strings.valueOf(attributes.get("style"));
            attributes.put("colnumber", index + 1);
            if (attributes.get("width") == null) {
                attributes.put("width", 1);
            }
            if (attributes.get("halign") == null) {
                attributes.put("halign", "left");
            }
            if (attributes.get("valign") == null) {
                attributes.put("valign", "top");
            }
            updateAttributes(attributes);
        }

        public Column(Table table, int index) {
            this(table, index, null);
        }

        /**
         * @return the style for this column
         */
        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        /**
         * An alias to the parent block (which is always a Table).
         */
        public Table getTable() {
            return (Table) parent;
        }

        /**
         * Internal: Calculate and assign the widths (percentage and absolute) for this column.
         *
         * <p>This method assigns the colpcwidth and colabswidth attributes.
         *
         * @return the resolved colpcwidth value
         */
        public Number assignWidth(Number colPcwidth, Integer widthBase, int precision) {
            if (widthBase != null) {
                colPcwidth = toNumber(truncate(Strings.toDouble(attributes.get("width")) * 100.0 / widthBase,
                        precision));
            }
            Object tableabswidth = parent.getAttributes().get("tableabswidth");
            if (tableabswidth != null) {
                attributes.put("colabswidth", toNumber(truncate((colPcwidth.doubleValue() / 100.0)
                        * Strings.toDouble(tableabswidth), precision)));
            }
            attributes.put("colpcwidth", colPcwidth);
            return colPcwidth;
        }

        @Override
        public boolean isBlock() {
            return false;
        }

        @Override
        public boolean isInline() {
            return false;
        }

        @Override
        public String convert() {
            return Converter.asString(getConverter().convert(this));
        }
    }

    /**
     * Methods for managing the a cell in an AsciiDoc table.
     */
    public static class Cell extends AbstractBlock {
        private static final String DOUBLE_LF = LF + LF;

        private Integer colspan;
        private Integer rowspan;
        private Document innerDocument;
        private String text;
        private Cursor cursor;
        private Object[] reinitializeArgs;

        public Cell(Column column, String cellText, Map<String, Object> attributes, Map<String, Object> opts) {
            super(column, "table_cell");
            if (opts == null) {
                opts = Map.of();
            }
            Cursor optsCursor = (Cursor) opts.get("cursor");
            if (document.isSourcemap() && optsCursor != null) {
                sourceLocation = optsCursor.copy();
            }
            Object inHeaderRow = null;
            String cellStyle = null;
            // NOTE column is always set when parsing; may not be set when building table from the API
            if (column != null) {
                if ((inHeaderRow = column.getTable().getHeaderRow()) != null) {
                    if ("implicit".equals(inHeaderRow)) {
                        cellStyle = column.getStyle();
                        if (cellStyle == null && attributes != null) {
                            cellStyle = Strings.valueOf(attributes.get("style"));
                        }
                        if (cellStyle != null) {
                            if (cellStyle.equals("asciidoc") || cellStyle.equals("literal")) {
                                reinitializeArgs = new Object[] { column, cellText,
                                        attributes == null ? null : new LinkedHashMap<>(attributes), opts };
                            }
                            cellStyle = null;
                        }
                    }
                } else {
                    cellStyle = column.getStyle();
                }
                // REVIEW feels hacky to inherit all attributes from column
                updateAttributes(column.getAttributes());
            }
            boolean asciidoc = false;
            boolean literal = false;
            boolean normalPsv = false;
            Cursor innerDocumentCursor = null;
            // NOTE if attributes is defined, we know this is a psv cell; implies text needs to be stripped
            if (attributes != null) {
                if (attributes.isEmpty()) {
                    colspan = rowspan = null;
                } else {
                    colspan = (Integer) attributes.remove("colspan");
                    rowspan = (Integer) attributes.remove("rowspan");
                    // TODO delete style attribute from @attributes if set
                    if (inHeaderRow == null) {
                        Object attrStyle = attributes.get("style");
                        if (attrStyle != null) {
                            cellStyle = attrStyle.toString();
                        }
                    }
                    updateAttributes(attributes);
                }
                if ("asciidoc".equals(cellStyle)) {
                    asciidoc = true;
                    innerDocumentCursor = optsCursor;
                    if ((cellText = Strings.rstrip(cellText)).startsWith(LF)) {
                        int linesAdvanced = 1;
                        while ((cellText = cellText.substring(1)).startsWith(LF)) {
                            linesAdvanced++;
                        }
                        // NOTE this only works if we remain in the same file
                        if (innerDocumentCursor != null) {
                            innerDocumentCursor.advance(linesAdvanced);
                        }
                    } else {
                        cellText = Strings.lstrip(cellText);
                    }
                } else if ("literal".equals(cellStyle)) {
                    literal = true;
                    cellText = Strings.rstrip(cellText);
                    // QUESTION should we use same logic as :asciidoc cell? strip leading space if text doesn't start
                    // with newline?
                    while (cellText.startsWith(LF)) {
                        cellText = cellText.substring(1);
                    }
                } else {
                    normalPsv = true;
                    // NOTE AsciidoctorJ uses null cell_text to create an empty cell
                    cellText = cellText != null ? Strings.strip(cellText) : "";
                }
            } else {
                colspan = rowspan = null;
                if ("asciidoc".equals(cellStyle)) {
                    asciidoc = true;
                    innerDocumentCursor = optsCursor;
                }
            }
            // NOTE only true for non-header rows
            if (asciidoc) {
                // FIXME hide doctitle from nested document; temporary workaround to fix
                // nested document seeing doctitle and assuming it has its own document title
                Object parentDoctitle = document.getAttributes().remove("doctitle");
                // NOTE we need to process the first line of content as it may not have been processed
                // the included content cannot expect to match conditional terminators in the remaining
                // lines of table cell content, it must be self-contained logic
                // QUESTION should we reset cell_text to nil?
                // QUESTION is is faster to check for :: before splitting?
                List<String> innerDocumentLines = Strings.split(cellText, LF, -1);
                if (!innerDocumentLines.isEmpty()) {
                    String unprocessedLine1 = innerDocumentLines.get(0);
                    if (unprocessedLine1.contains("::")) {
                        List<String> preprocessedLines = new PreprocessorReader(document, List.of(unprocessedLine1), innerDocumentCursor)
                                .readLines();
                        if (!(unprocessedLine1.equals(preprocessedLines.isEmpty() ? null : preprocessedLines.get(0))
                                && preprocessedLines.size() < 2)) {
                            innerDocumentLines.remove(0);
                            innerDocumentLines.addAll(0, preprocessedLines);
                        }
                    }
                }
                Map<String, Object> innerOpts = new LinkedHashMap<>();
                innerOpts.put("standalone", false);
                innerOpts.put("parent", document);
                innerOpts.put("cursor", innerDocumentCursor);
                innerDocument = new Document(innerDocumentLines, innerOpts);
                if (parentDoctitle != null) {
                    document.getAttributes().put("doctitle", parentDoctitle);
                }
                subs = null;
            } else if (literal) {
                contentModel = ContentModel.VERBATIM;
                subs = new ArrayList<>(BASIC_SUBS);
            } else {
                if (normalPsv) {
                    if (inHeaderRow != null) {
                        cursor = optsCursor; // used in deferred catalog_inline_anchor call
                    } else {
                        catalogInlineAnchor(cellText, optsCursor);
                    }
                }
                contentModel = ContentModel.SIMPLE;
                subs = new ArrayList<>(NORMAL_SUBS);
            }
            text = cellText;
            style = cellStyle;
        }

        public Cell(Column column, String cellText, Map<String, Object> attributes) {
            this(column, cellText, attributes, null);
        }

        public Cell(Column column, String cellText) {
            // NOTE the attributes default to an empty Map (not null), so the text is normalized like a psv cell
            this(column, cellText, new LinkedHashMap<>(), null);
        }

        @SuppressWarnings("unchecked")
        public Cell reinitialize(boolean hasHeader) {
            if (hasHeader) {
                reinitializeArgs = null;
            } else if (reinitializeArgs != null) {
                return new Cell((Column) reinitializeArgs[0], (String) reinitializeArgs[1],
                        (Map<String, Object>) reinitializeArgs[2], (Map<String, Object>) reinitializeArgs[3]);
            } else {
                style = Strings.valueOf(attributes.get("style"));
            }
            if (cursor != null) {
                catalogInlineAnchor();
            }
            return this;
        }

        public void catalogInlineAnchor(String cellText, Cursor cursor) {
            if (cursor == null) {
                cursor = this.cursor;
                this.cursor = null;
            }
            Matcher m;
            if (cellText.startsWith("[[") && (m = Rx.match(Rx.LEADING_INLINE_ANCHOR_RX, cellText)) != null) {
                Parser.catalogInlineAnchor(m.group(1), m.group(2), this, cursor, document);
            }
        }

        public void catalogInlineAnchor() {
            catalogInlineAnchor(text, null);
        }

        /**
         * @return the number of columns this cell will span (default: null)
         */
        public Integer getColspan() {
            return colspan;
        }

        public void setColspan(Integer colspan) {
            this.colspan = colspan;
        }

        /**
         * @return the number of rows this cell will span (default: null)
         */
        public Integer getRowspan() {
            return rowspan;
        }

        public void setRowspan(Integer rowspan) {
            this.rowspan = rowspan;
        }

        /**
         * An alias to the parent block (which is always a Column).
         */
        public Column getColumn() {
            return (Column) parent;
        }

        /**
         * @return the nested Document in an AsciiDoc table cell (only set when style is asciidoc)
         */
        public Document getInnerDocument() {
            return innerDocument;
        }

        public void setInnerDocument(Document innerDocument) {
            this.innerDocument = innerDocument;
        }

        /**
         * Get the String text of this cell with substitutions applied.
         *
         * <p>Used for cells in the head row as well as text-only (non-AsciiDoc) cells in the foot row and body.
         *
         * <p>This method shouldn't be used for cells that have the AsciiDoc style.
         *
         * @return the converted String text for this Cell
         */
        public String getText() {
            return applySubs(text, subs);
        }

        /**
         * Set the String text for this cell.
         *
         * <p>This method shouldn't be used for cells that have the AsciiDoc style.
         */
        public void setText(String text) {
            this.text = text;
        }

        /**
         * Handles the body data (tbody, tfoot), applying styles and partitioning into paragraphs.
         *
         * <p>This method should not be used for cells in the head row or that have the literal style.
         *
         * @return the converted String for this Cell (if the style is asciidoc), otherwise the paragraphs joined by a
         *         newline. Use {@link #getParagraphs()} to retrieve the individual paragraphs.
         */
        @Override
        public String getContent() {
            if ("asciidoc".equals(style)) {
                return innerDocument.convert();
            }
            return String.join(LF, getParagraphs());
        }

        /**
         * Handles the body data (tbody, tfoot), applying styles and partitioning into paragraphs.
         *
         * <p>This method should not be used for cells in the head row, that have the literal style or that have the
         * asciidoc style.
         *
         * @return the converted String List of paragraphs for this Cell
         */
        public List<String> getParagraphs() {
            String cellStyle = style;
            List<String> result = new ArrayList<>();
            if (text.contains(DOUBLE_LF)) {
                for (String para : Rx.BLANK_LINE_RX.split(getText())) {
                    result.add(cellStyle != null && !cellStyle.equals("header")
                            ? new Inline(parent, "quoted", para, cellStyle, null, null, null).convert() : para);
                }
                return result;
            }
            String subbedText = getText();
            if (subbedText.isEmpty()) {
                return result;
            } else if (cellStyle != null && !cellStyle.equals("header")) {
                result.add(new Inline(parent, "quoted", subbedText, cellStyle, null, null, null).convert());
            } else {
                result.add(subbedText);
            }
            return result;
        }

        public List<String> getLines() {
            return Strings.split(text, LF);
        }

        public String getSource() {
            return text;
        }

        @Override
        public String toString() {
            return super.toString() + " - [text: " + text + ", colspan: " + (colspan == null ? 1 : colspan)
                    + ", rowspan: " + (rowspan == null ? 1 : rowspan) + ", attributes: " + attributes + "]";
        }
    }

    /**
     * Methods for managing the parsing of an AsciiDoc table. Instances of this class are primarily responsible for
     * tracking the buffer of a cell as the parser moves through the lines of the table using tail recursion. When a
     * cell boundary is located, the previous cell is closed, an instance of Table.Cell is instantiated, the row is
     * closed if the cell satisfies the column count and, finally, a new buffer is allocated to track the next cell.
     */
    public static class ParserContext implements Logging {
        /** The table formats in AsciiDoc. */
        // QUESTION should we recognize !sv as a valid format value?
        public static final Set<String> FORMATS = Helpers.setOf("psv", "csv", "dsv", "tsv");

        private record Delimiter(String value, Pattern pattern) {
        }

        /** A Map mapping the AsciiDoc table formats to default delimiters. */
        private static final Map<String, Delimiter> DELIMITERS = Map.of(
                "psv", new Delimiter("|", Rx.compile("\\|")),
                "csv", new Delimiter(",", Rx.compile(",")),
                "dsv", new Delimiter(":", Rx.compile(":")),
                "tsv", new Delimiter("\t", Rx.compile("\\t")),
                "!sv", new Delimiter("!", Rx.compile("!")));

        private final Reader reader;
        private final Cursor startCursorData;
        private Table table;
        private String format;
        private int colcount;
        private String buffer;
        private final String delimiter;
        private final Pattern delimiterRx;
        private final List<Map<String, Object>> cellspecs = new ArrayList<>();
        private boolean cellOpen;
        private final List<Integer> activeRowspans = new ArrayList<>();
        private int columnVisits;
        private Row currentRow = new Row();
        private int linenum;

        public ParserContext(Reader reader, Table table, Map<String, Object> attributes) {
            this.reader = reader;
            this.startCursorData = reader.mark();
            this.table = table;

            String xsv;
            if (attributes.containsKey("format")) {
                xsv = Strings.valueOf(attributes.get("format"));
                if (FORMATS.contains(xsv)) {
                    if (xsv.equals("tsv")) {
                        // NOTE tsv is just an alias for csv with a tab separator
                        format = "csv";
                    } else if ((format = xsv).equals("psv") && table.getDocument().isNested()) {
                        xsv = "!sv";
                    }
                } else {
                    getLogger().error(Logging.messageWithContext("illegal table format: " + xsv,
                            reader.cursorAtPrevLine()));
                    format = "psv";
                    xsv = table.getDocument().isNested() ? "!sv" : "psv";
                }
            } else {
                format = "psv";
                xsv = table.getDocument().isNested() ? "!sv" : "psv";
            }

            if (attributes.containsKey("separator")) {
                String sep = Strings.valueOf(attributes.get("separator"));
                Delimiter resolved;
                if (Strings.isNullOrEmpty(sep)) {
                    resolved = DELIMITERS.get(xsv);
                // QUESTION should we support any other escape codes or multiple tabs?
                } else if (sep.equals("\\t")) {
                    resolved = DELIMITERS.get("tsv");
                } else {
                    resolved = new Delimiter(sep, Pattern.compile(Pattern.quote(sep)));
                }
                delimiter = resolved.value();
                delimiterRx = resolved.pattern();
            } else {
                Delimiter resolved = DELIMITERS.get(xsv);
                delimiter = resolved.value();
                delimiterRx = resolved.pattern();
            }

            colcount = table.getColumns().isEmpty() ? -1 : table.getColumns().size();
            buffer = "";
            cellOpen = false;
            activeRowspans.add(0);
            columnVisits = 0;
            linenum = -1;
        }

        public Table getTable() {
            return table;
        }

        public void setTable(Table table) {
            this.table = table;
        }

        /**
         * @return the AsciiDoc table format (psv, dsv, or csv)
         */
        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        /**
         * @return the expected column count for a row. A value of -1 means we use the number of columns found in the
         *         first line as the colcount
         */
        public int getColcount() {
            return colcount;
        }

        /**
         * @return the String buffer of the currently open cell
         */
        public String getBuffer() {
            return buffer;
        }

        public void setBuffer(String buffer) {
            this.buffer = buffer;
        }

        /**
         * @return the cell delimiter for this table.
         */
        public String getDelimiter() {
            return delimiter;
        }

        /**
         * @return the cell delimiter compiled Pattern for this table.
         */
        public Pattern getDelimiterRx() {
            return delimiterRx;
        }

        /**
         * Checks whether the line provided starts with the cell delimiter used by this table.
         *
         * @return true if the line starts with the delimiter, false otherwise
         */
        public boolean startsWithDelimiter(String line) {
            return line.startsWith(delimiter);
        }

        /**
         * Checks whether the line provided contains the cell delimiter used by this table.
         *
         * @return the Matcher positioned at the delimiter if the line contains the delimiter, null otherwise
         */
        public Matcher matchDelimiter(String line) {
            Matcher matcher = delimiterRx.matcher(line);
            return matcher.find() ? matcher : null;
        }

        /**
         * Skip past the matched delimiter because it's inside quoted text.
         */
        public void skipPastDelimiter(String pre) {
            buffer = buffer + pre + delimiter;
        }

        /**
         * Skip past the matched delimiter because it's escaped.
         */
        public void skipPastEscapedDelimiter(String pre) {
            buffer = buffer + Strings.chop(pre) + delimiter;
        }

        /**
         * Determines whether the buffer has unclosed quotes. Used for CSV data.
         *
         * @return true if the buffer has unclosed quotes, false if it doesn't or it isn't quoted data
         */
        public boolean isBufferHasUnclosedQuotes(String append, String q) {
            String record = append != null ? Strings.strip(buffer + append) : Strings.strip(buffer);
            if (record.equals(q)) {
                return true;
            } else if (record.startsWith(q)) {
                String qq = q + q;
                boolean trailingQuote = record.endsWith(q);
                if ((trailingQuote && record.endsWith(qq)) || record.startsWith(qq)) {
                    record = record.replace(qq, "");
                    return record.startsWith(q) && !record.endsWith(q);
                }
                return !trailingQuote;
            }
            return false;
        }

        public boolean isBufferHasUnclosedQuotes(String append) {
            return isBufferHasUnclosedQuotes(append, "\"");
        }

        public boolean isBufferHasUnclosedQuotes() {
            return isBufferHasUnclosedQuotes(null, "\"");
        }

        /**
         * Takes a cell spec from the stack. Cell specs precede the delimiter, so a stack is used to carry over the spec
         * from the previous cell to the current cell when the cell is being closed.
         *
         * @return The cell spec Map captured from parsing the previous cell
         */
        public Map<String, Object> takeCellspec() {
            return cellspecs.isEmpty() ? null : cellspecs.remove(0);
        }

        /**
         * Puts a cell spec onto the stack. Cell specs precede the delimiter, so a stack is used to carry over the spec
         * to the next cell.
         */
        public void pushCellspec(Map<String, Object> cellspec) {
            // this shouldn't be null, but we check anyway
            cellspecs.add(cellspec == null ? new LinkedHashMap<>() : cellspec);
        }

        /**
         * Marks that the cell should be kept open. Used when the end of the line is reached and the cell may contain
         * additional text.
         */
        public void keepCellOpen() {
            cellOpen = true;
        }

        /**
         * Marks the cell as closed so that the parser knows to instantiate a new cell instance and add it to the
         * current row.
         */
        public void markCellClosed() {
            cellOpen = false;
        }

        /**
         * Checks whether the current cell is still open.
         *
         * @return true if the cell is marked as open, false otherwise
         */
        public boolean isCellOpen() {
            return cellOpen;
        }

        /**
         * Checks whether the current cell has been marked as closed.
         *
         * @return true if the cell is marked as closed, false otherwise
         */
        public boolean isCellClosed() {
            return !cellOpen;
        }

        /**
         * If the current cell is open, close it. In additional, push the cell spec captured from the end of this cell
         * onto the stack for use by the next cell.
         */
        public void closeOpenCell(Map<String, Object> nextCellspec) {
            pushCellspec(nextCellspec);
            if (isCellOpen()) {
                closeCell(true);
            }
            advance();
        }

        public void closeOpenCell() {
            closeOpenCell(new LinkedHashMap<>());
        }

        /**
         * Close the current cell, instantiate a new Table.Cell, add it to the current row and, if the number of
         * expected columns for the current row has been met, close the row and begin a new one.
         */
        public void closeCell(boolean eol) {
            String cellText;
            Map<String, Object> cellspec;
            int repeat;
            if (format.equals("psv")) {
                cellText = buffer;
                buffer = "";
                if ((cellspec = takeCellspec()) != null) {
                    Object repeatcol = cellspec.remove("repeatcol");
                    repeat = repeatcol == null ? 1 : (Integer) repeatcol;
                } else {
                    getLogger().error(Logging.messageWithContext(
                            "table missing leading separator; recovering automatically", startCursorData.copy()));
                    cellspec = new LinkedHashMap<>();
                    repeat = 1;
                }
            } else {
                cellText = Strings.strip(buffer);
                buffer = "";
                cellspec = null;
                repeat = 1;
                String q = "\"";
                if (format.equals("csv") && !cellText.isEmpty() && cellText.contains(q)) {
                    // this may not be perfect logic, but it hits the 99%
                    if (cellText.startsWith(q) && cellText.endsWith(q)) {
                        // unquote
                        if (cellText.length() >= 2) {
                            cellText = cellText.substring(1, cellText.length() - 1);
                            // trim whitespace and collapse escaped quotes
                            cellText = Strings.squeeze(Strings.strip(cellText), '"');
                        } else {
                            getLogger().error(Logging.messageWithContext(
                                    "unclosed quote in CSV data; setting cell to empty", reader.cursorAtPrevLine()));
                            cellText = "";
                        }
                    } else {
                        // collapse escaped quotes
                        cellText = Strings.squeeze(cellText, '"');
                    }
                }
            }

            for (int i = 1; i <= repeat; i++) {
                Column column;
                // TODO make column resolving an operation
                if (colcount == -1) {
                    column = new Column(table, table.getColumns().size() + i - 1);
                    table.getColumns().add(column);
                    int extraCols;
                    if (cellspec != null && cellspec.containsKey("colspan")
                            && (extraCols = Strings.toInt(cellspec.get("colspan")) - 1) > 0) {
                        int offset = table.getColumns().size();
                        for (int j = 0; j < extraCols; j++) {
                            table.getColumns().add(new Column(table, offset + j));
                        }
                    }
                } else {
                    column = currentRow.size() < table.getColumns().size() ? table.getColumns().get(currentRow.size())
                            : null;
                }

                Map<String, Object> cellOpts = new LinkedHashMap<>();
                Cursor cursorBeforeMark = reader.cursorBeforeMark();
                cellOpts.put("cursor", cursorBeforeMark);
                Cell cell = new Cell(column, cellText, cellspec == null ? null : new LinkedHashMap<>(cellspec),
                        cellOpts);
                reader.mark();
                if (cell.getRowspan() != null && cell.getRowspan() != 1) {
                    activateRowspan(cell.getRowspan(), cell.getColspan() == null ? 1 : cell.getColspan());
                }
                columnVisits += cell.getColspan() == null ? 1 : cell.getColspan();
                currentRow.add(cell);
                int rowStatus = endOfRowStatus();
                if (rowStatus > -1 && (colcount != -1 || linenum > 0 || (eol && i == repeat))) {
                    if (rowStatus > 0) {
                        getLogger().error(Logging.messageWithContext(
                                "dropping cell because it exceeds specified number of columns", cursorBeforeMark));
                        closeRow(true);
                    } else {
                        closeRow();
                    }
                }
            }
            cellOpen = false;
        }

        public void closeCell() {
            closeCell(false);
        }

        public void closeTable() {
            if (columnVisits == 0) {
                return;
            }
            getLogger().error(Logging.messageWithContext("dropping cells from incomplete row detected end of table",
                    reader.cursorBeforeMark()));
        }

        /**
         * Internal: Close the row by adding it to the Table and resetting the row List and counter variables.
         */
        private void closeRow() {
            closeRow(false);
        }

        private void closeRow(boolean drop) {
            if (!drop) {
                table.getRows().getBody().add(currentRow);
            }
            // don't have to account for active rowspans here
            // since we know this is first row
            if (colcount == -1) {
                colcount = columnVisits;
            }
            columnVisits = 0;
            currentRow = new Row();
            activeRowspans.remove(0);
            if (activeRowspans.isEmpty()) {
                activeRowspans.add(0);
            }
        }

        /**
         * Internal: Activate a rowspan. The rowspan List is consulted when determining the effective number of cells in
         * the current row.
         */
        private void activateRowspan(int rowspan, int colspan) {
            for (int i = 1; i <= rowspan - 1; i++) {
                while (activeRowspans.size() <= i) {
                    activeRowspans.add(0);
                }
                activeRowspans.set(i, activeRowspans.get(i) + colspan);
            }
        }

        /**
         * Internal: Check whether we've met the number of effective columns for the current row.
         *
         * @return -1 if not at end of row, 0 if exactly at end of row, and 1 if overruns end of row
         */
        private int endOfRowStatus() {
            return colcount == -1 ? 0 : Integer.compare(effectiveColumnVisits(), colcount);
        }

        /**
         * Internal: Calculate the effective column visits, which consists of the number of cells plus any active
         * rowspans.
         */
        private int effectiveColumnVisits() {
            return columnVisits + activeRowspans.get(0);
        }

        /**
         * Internal: Advance to the next line (which may come after the parser begins processing the next line if the
         * last cell had wrapped content).
         */
        private void advance() {
            linenum++;
        }
    }
}
