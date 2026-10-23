package org.asciidoctor.extension;

import java.util.List;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

public class TableUpdaterTreeProcessor extends Treeprocessor {

    @Override
    public Document process(Document document) {
        processNode(document);
        return document;
    }

    static void processNode(StructuralNode node) {
        if (node instanceof Table) {
            processTable((Table) node);
        } else {
            node.getBlocks().forEach(TableUpdaterTreeProcessor::processNode);
        }
    }

    static void processTable(Table table) {
        List<Cell> cells = table.getBody().stream().flatMap(it -> it.getCells().stream()).collect(Collectors.toList());
        cells.forEach(cell -> cell.setSource("*Replaced*"));
    }
}
