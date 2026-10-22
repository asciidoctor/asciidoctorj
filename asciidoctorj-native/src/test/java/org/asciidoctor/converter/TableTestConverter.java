package org.asciidoctor.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.Table;

public class TableTestConverter extends StringConverter {

    public static final int COLWIDTH = 15;
    public static final String COL_SEPARATOR = "|";
    public static final String NEWLINE = "\n";
    public static final String NEWLINE_2 = NEWLINE.repeat(2);

    public TableTestConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
    }

    @Override
    public String convert(ContentNode node, String transform, Map<Object, Object> opts) {
        if (transform == null) {
            transform = node.getNodeName();
        }

        switch (transform) {
            case "embedded":
                return convertEmbedded((Document) node);
            case "section":
                return convertSection((Section) node);
            case "table":
                return convertTable((Table) node);
            default:
                throw new IllegalArgumentException("No method convert" + transform);
        }
    }

    String convertEmbedded(Document node) {
        String result = node.getDoctitle().toUpperCase();
        String content = (String) node.getContent();
        if (content != null && content.trim().length() > 0) {
            result += NEWLINE_2 + content;
        }
        return result;
    }

    String convertSection(Section section) {
        String result = "-- " + section.getTitle() + " --";
        String content = (String) section.getContent();
        if (content != null && content.trim().length() > 0) {
            result += NEWLINE_2 + content;
        }
        return result;
    }

    String convertTable(Table table) {

        Function<Cell, String> cellFormatter = cell -> {
            if ("asciidoc".equals(cell.getStyle())) {
                return padRight(cell.getInnerDocument().convert(), COLWIDTH);
            } else {
                switch (cell.getColumn().getHorizontalAlignment()) {
                    case LEFT:
                        return padRight(cell.getText(), COLWIDTH);
                    case CENTER:
                        return center(cell.getText(), COLWIDTH);
                    case RIGHT:
                        return padLeft(cell.getText(), COLWIDTH);
                    default:
                        return null;
                }
            }
        };

        for (int it = 1; it <= table.getColumns().size(); it++) {
            assert table.getColumns().get(it - 1).getColumnNumber() == it;
        }

        List<String> tableRows = new ArrayList<>();

        if (table.getHeader().size() > 0) {
            tableRows.add(table.getHeader().get(0).getCells().stream().map(cellFormatter).collect(Collectors.joining(COL_SEPARATOR)));
            tableRows.add(table.getHeader().get(0).getCells().stream().map(cell -> "-".repeat(COLWIDTH)).collect(Collectors.joining("+")));
        }

        table.getBody().forEach(row -> tableRows.add(row.getCells().stream().map(cellFormatter).collect(Collectors.joining(COL_SEPARATOR))));

        if (table.getFooter().size() > 0) {
            tableRows.add(table.getFooter().get(0).getCells().stream().map(cell -> "=".repeat(COLWIDTH)).collect(Collectors.joining("#")));
            tableRows.add(table.getFooter().get(0).getCells().stream().map(cellFormatter).collect(Collectors.joining(COL_SEPARATOR)));
        }

        return String.join(NEWLINE, tableRows);
    }

    // the String methods of Groovy (padRight, padLeft and center)

    static String padRight(String text, int width) {
        return text.length() >= width ? text : text + " ".repeat(width - text.length());
    }

    static String padLeft(String text, int width) {
        return text.length() >= width ? text : " ".repeat(width - text.length()) + text;
    }

    static String center(String text, int width) {
        int charsToAdd = width - text.length();
        if (charsToAdd <= 0) {
            return text;
        }
        int left = charsToAdd / 2;
        return " ".repeat(left) + text + " ".repeat(charsToAdd - left);
    }
}
