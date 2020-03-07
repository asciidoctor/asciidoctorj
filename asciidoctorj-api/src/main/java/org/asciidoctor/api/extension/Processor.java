package org.asciidoctor.api.extension;

import org.asciidoctor.ast.*;
import org.asciidoctor.log.LogRecord;

import java.util.List;
import java.util.Map;

public interface Processor {

    Table createTable(StructuralNode parent);

    Table createTable(StructuralNode parent, Map<String, Object> attributes);

    Row createTableRow(Table parent);

    Column createTableColumn(Table parent, int index);

    Column createTableColumn(Table parent, int index, Map<String, Object> attributes);

    Cell createTableCell(Column column, String text);

    Cell createTableCell(Column column, Document innerDocument);

    Cell createTableCell(Column column, Document innerDocument, Map<String, Object> attributes);

    Cell createTableCell(Column column, String text, Map<String, Object> attributes);

    Block createBlock(StructuralNode parent, String context, String content);

    Block createBlock(StructuralNode parent, String context, String content, Map<String, Object> attributes);

    Block createBlock(StructuralNode parent, String context, String content, Map<String, Object> attributes, Map<Object, Object> options);

    Block createBlock(StructuralNode parent, String context, List<String> content);

    Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes);

    Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes, Map<Object, Object> options);

    Section createSection(StructuralNode parent);

    Section createSection(StructuralNode parent, Map<Object, Object> options);

    Section createSection(StructuralNode parent, boolean numbered, Map<Object, Object> options);

    Section createSection(StructuralNode parent, int level, boolean numbered, Map<Object, Object> options);

    PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text);

    PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text, Map<String, Object> attributes);

    PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text, Map<String, Object> attributes, Map<Object, Object> options);

    PhraseNode createPhraseNode(ContentNode parent, String context, String text);

    PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes);

    PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes, Map<String, Object> options);

    Block createBlock(StructuralNode parent, String context, Map<Object, Object> options);

    Section createSection(StructuralNode parent, Integer level, boolean numbered, Map<Object, Object> options);

    /**
     * Creates an inner document for the given parent document.
     * Inner documents are used for tables cells with style {@code asciidoc}.
     *
     * @param parentDocument The parent document of the new document.
     * @return A new inner document.
     */
    Document createDocument(Document parentDocument);

    ListItem createListItem(final org.asciidoctor.ast.List parent, final String text);

    ListItem createListItem(final DescriptionList parent, final String text);

    /**
     * Parses the given raw asciidoctor content, parses it and appends it as children to the given parent block.
     * <p>The following example will add two paragraphs with the role {@code newcontent} to all top
     * level sections of a document:
     * <pre>
     *     <verbatim>
     * Asciidoctor asciidoctor = ...
     * asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
     *     DocumentRuby process(DocumentRuby document) {
     *         for (AbstractBlock block: document.getBlocks()) {
     *             if (block instanceof Section) {
     *                 parseContent(block, Arrays.asList(new String[]{
     *                                             "[newcontent]",
     *                                             "This is new content"
     *                                             "",
     *                                             "[newcontent]",
     *                                             "This is also new content"}));
     *             }
     *         }
     *     }
     * });
     *     </verbatim>
     * </pre>
     *
     * @param parent The block to which the parsed content should be added as children.
     * @param lines  Raw asciidoctor content
     */
    void parseContent(StructuralNode parent, List<String> lines);

    Map<String, Object> getConfig();

    void log(LogRecord logRecord);

    void setConfig(Map<String, Object> config);

    void updateConfig(Map<String, Object> config);

    <T> T unwrap(Class<T> clazz);
}
