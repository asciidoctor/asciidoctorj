package org.asciidoctor.extension;

import org.asciidoctor.Options;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;
import org.asciidoctor.log.LogRecord;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class BaseProcessor implements Processor {

    private static final ProcessorFactory processorFactory;

    static {
        Iterator<ProcessorFactory> iterator = ServiceLoader.load(ProcessorFactory.class).iterator();
        if (!iterator.hasNext()) {
            iterator = ServiceLoader.load(ProcessorFactory.class, BaseProcessor.class.getClassLoader()).iterator();
        }
        processorFactory = iterator.next();
    }

    private final Processor delegate;

    public BaseProcessor() {
        this(new HashMap<>());
    }

    public BaseProcessor(Map<String, Object> config) {
        this.delegate = processorFactory.createProcessorDelegate();
        this.delegate.setConfig(config);
    }

    @Override
    public Map<String, Object> getConfig() {
        return delegate.getConfig();
    }

    @Override
    public final void setConfig(Map<String, Object> config) {
        delegate.setConfig(config);
    }

    @Override
    public final void updateConfig(Map<String, Object> config) {
        delegate.updateConfig(config);
    }

    @Override
    public final <T> T unwrap(Class<T> clazz) {
        return delegate.unwrap(clazz);
    }

    @Override
    public void log(LogRecord logRecord) {
        delegate.log(logRecord);
    }

    @Override
    public Table createTable(StructuralNode parent) {
        return delegate.createTable(parent);
    }

    @Override
    public Table createTable(StructuralNode parent, Map<String, Object> attributes) {
        return delegate.createTable(parent, attributes);
    }

    @Override
    public Row createTableRow(Table parent) {
        return delegate.createTableRow(parent);
    }

    @Override
    public Column createTableColumn(Table parent, int index, Map<String, Object> attributes) {
        return delegate.createTableColumn(parent, index, attributes);
    }

    @Override
    public Cell createTableCell(Column column, Document innerDocument, Map<String, Object> attributes) {
        return delegate.createTableCell(column, innerDocument, attributes);
    }

    @Override
    public Cell createTableCell(Column column, String text, Map<String, Object> attributes) {
        return delegate.createTableCell(column, text, attributes);
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text, Map<String, Object> attributes, Map<Object, Object> options) {
        return delegate.createPhraseNode(parent, context, text, attributes, options);
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes, Map<String, Object> options) {
        return delegate.createPhraseNode(parent, context, text, attributes, options);
    }

    @Override
    public Block createBlock(StructuralNode parent, String context, Map<Object, Object> options) {
        return delegate.createBlock(parent, context, options);
    }

    @Override
    public Section createSection(StructuralNode parent, Integer level, boolean numbered, Map<Object, Object> options) {
        return delegate.createSection(parent, level, numbered, options);
    }

    @Override
    public Document createDocument(Document parentDocument) {
        return delegate.createDocument(parentDocument);
    }

    @Override
    public ListItem createListItem(org.asciidoctor.ast.List parent, String text) {
        return delegate.createListItem(parent, text);
    }

    @Override
    public org.asciidoctor.ast.List createList(StructuralNode parent, String context) {
        return delegate.createList(parent, context);
    }

    @Override
    public org.asciidoctor.ast.List createList(StructuralNode parent, String context, Map<String, Object> attributes, Map<Object, Object> options) {
        return delegate.createList(parent, context, attributes, options);
    }

    @Override
    public org.asciidoctor.ast.List createList(StructuralNode parent, String context, Map<Object, Object> options) {
        return delegate.createList(parent, context);
    }

    @Override
    public ListItem createListItem(DescriptionList parent, String text) {
        return delegate.createListItem(parent, text);
    }

    @Override
    public void parseContent(StructuralNode parent, List<String> lines) {
        delegate.parseContent(parent, lines);
    }

    @Override
    public Cursor newCursor(String file) {
        return delegate.newCursor(file);
    }

    @Override
    public Cursor newCursor(String file, String dir) {
        return delegate.newCursor(file, dir);
    }

    @Override
    public Cursor newCursor(String file, String dir, int lineno) {
        return delegate.newCursor(file, dir, lineno);
    }

    @Override
    public Reader newReader(List<String> source, Cursor cursor, Map<Object, Object> options) {
        return delegate.newReader(source, cursor, options);
    }

    @Override
    public Reader newReader(List<String> source, Cursor cursor) {
        return delegate.newReader(source, cursor);
    }

    @Override
    public Reader newReader(List<String> source, Map<Object, Object> options) {
        return delegate.newReader(source, options);
    }

    @Override
    public Reader newReader(List<String> source) {
        return delegate.newReader(source);
    }

    // REMIND: Could be default method but Groovy (with compileStatic does not resolve default methods)

    @Override
    public Column createTableColumn(Table parent, int index) {
        return createTableColumn(parent, index, new HashMap<>());
    }

    @Override
    public Cell createTableCell(Column column, String text) {
        return createTableCell(column, text, new HashMap<>());
    }

    @Override
    public Cell createTableCell(Column column, Document innerDocument) {
        return createTableCell(column, innerDocument, new HashMap<>());
    }

    @Override
    public Block createBlock(StructuralNode parent, String context, String content) {
        return createBlock(parent, context, content, new HashMap<>(), new HashMap<>());
    }

    @Override
    public Block createBlock(StructuralNode parent, String context, String content, Map<String, Object> attributes) {
        return createBlock(parent, context, content, attributes, new HashMap<>());
    }

    @Override
    public Block createBlock(StructuralNode parent, String context, String content, Map<String, Object> attributes, Map<Object, Object> options) {
        options.put(Options.SOURCE, content);
        options.put(Options.ATTRIBUTES, attributes);
        return createBlock(parent, context, options);
    }

    @Override
    public Block createBlock(StructuralNode parent, String context, List<String> content) {
        return createBlock(parent, context, content, new HashMap<>(), new HashMap<>());
    }

    @Override
    public Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes) {
        return createBlock(parent, context, content, attributes, new HashMap<>());
    }

    @Override
    public Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes,
                             Map<Object, Object> options) {

        options.put(Options.SOURCE, content);
        options.put(Options.ATTRIBUTES, new HashMap<>(attributes));

        return createBlock(parent, context, options);
    }

    @Override
    public Section createSection(StructuralNode parent) {
        return createSection(parent, null, true, new HashMap<>());
    }

    @Override
    public Section createSection(StructuralNode parent, Map<Object, Object> options) {
        return createSection(parent, null, true, options);
    }

    @Override
    public Section createSection(StructuralNode parent, boolean numbered, Map<Object, Object> options) {
        return createSection(parent, null, numbered, options);
    }

    @Override
    public Section createSection(StructuralNode parent, int level, boolean numbered, Map<Object, Object> options) {
        return createSection(parent, Integer.valueOf(level), numbered, options);
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text) {
        return createPhraseNode(parent, context, text, new HashMap<>());
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text, Map<String, Object> attributes) {
        return createPhraseNode(parent, context, text, attributes, new HashMap<>());
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, String text) {
        return createPhraseNode(parent, context, text, new HashMap<>());
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes) {
        return createPhraseNode(parent, context, text, attributes, new HashMap<>());
    }
}
