package org.asciidoctor.api.extension;

import org.asciidoctor.api.Options;
import org.asciidoctor.api.ast.Block;
import org.asciidoctor.api.ast.Cell;
import org.asciidoctor.api.ast.Column;
import org.asciidoctor.api.ast.ContentNode;
import org.asciidoctor.api.ast.DescriptionList;
import org.asciidoctor.api.ast.Document;
import org.asciidoctor.api.ast.ListItem;
import org.asciidoctor.api.ast.PhraseNode;
import org.asciidoctor.api.ast.Row;
import org.asciidoctor.api.ast.Section;
import org.asciidoctor.api.ast.StructuralNode;
import org.asciidoctor.api.ast.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class BaseProcessor implements Processor {

    private Processor delegate;

    public BaseProcessor() {
        this(new HashMap<>());
    }

    public BaseProcessor(Processor delegate) {
        this(delegate, new HashMap<>());
    }

    public BaseProcessor(Map<String, Object> config) {
        // FIXME: Use a strategy to resolve the processor
        this(ServiceLoader.load(Processor.class).iterator().next(), config);
    }

    public BaseProcessor(Processor delegate, Map<String, Object> config) {
        this.delegate = delegate;
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
    public final void setConfigFinalized() {
        delegate.setConfigFinalized();
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
    public ListItem createListItem(org.asciidoctor.api.ast.List parent, String text) {
        return delegate.createListItem(parent, text);
    }

    @Override
    public ListItem createListItem(DescriptionList parent, String text) {
        return delegate.createListItem(parent, text);
    }

    @Override
    public void parseContent(StructuralNode parent, List<String> lines) {
        delegate.parseContent(parent, lines);
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
