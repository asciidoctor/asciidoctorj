package org.asciidoctor.extension.spi;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import java.util.List;
import java.util.Map;

public interface ProcessorNodeFactory {

    Table createTable(StructuralNode parent);

    Table createTable(StructuralNode parent, Map<String, Object> attributes);

    Row createTableRow(Table parent);

    Column createTableColumn(Table parent, int index);

    Column createTableColumn(Table parent, int index, Map<String, Object> attributes);

    Cell createTableCell(Column column, Document innerDocument, Map<String, Object> attributes);

    Cell createTableCell(Column column, String text, Map<String, Object> attributes);

    Cell createTableCell(Column column, String text);

    Cell createTableCell(Column column, Document innerDocument);

    Block createBlock(StructuralNode parent, String context, String content);

    Block createBlock(StructuralNode parent, String context, List<String> content);

    Block createBlock(StructuralNode parent, String context, String content, Map<String, Object> attributes);

    Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes);

    Block createBlock(StructuralNode parent, String context, String content, Map<String, Object> attributes, Map<Object, Object> options);

    Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes, Map<Object, Object> options);

    PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text);

    PhraseNode createPhraseNode(ContentNode parent, String context, String text);

    PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes);

    PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text, Map<String, Object> attributes);

    PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text, Map<String, Object> attributes, Map<Object, Object> options);

    PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes, Map<String, Object> options);

    Document createDocument(Document parentDocument);

    ListItem createListItem(final org.asciidoctor.ast.List parent, final String text);

    ListItem createListItem(final org.asciidoctor.ast.DescriptionList parent, final String text);

    void parseContent(StructuralNode parent, List<String> lines);

    Section createSection(StructuralNode parent);

    Section createSection(StructuralNode parent, Map<Object, Object> options);

    Section createSection(StructuralNode parent, boolean numbered, Map<Object, Object> options);

    Section createSection(StructuralNode parent, int level, boolean numbered, Map<Object, Object> options);


}
