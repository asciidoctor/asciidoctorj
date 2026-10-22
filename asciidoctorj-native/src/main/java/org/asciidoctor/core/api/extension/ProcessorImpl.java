package org.asciidoctor.core.api.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
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
import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.AbstractNode;
import org.asciidoctor.core.Inline;
import org.asciidoctor.core.LoggerManager;
import org.asciidoctor.core.Parser;
import org.asciidoctor.core.api.ast.CursorImpl;
import org.asciidoctor.core.api.ast.NodeConverter;
import org.asciidoctor.core.api.ast.RowImpl;
import org.asciidoctor.core.api.log.LogRecords;
import org.asciidoctor.extension.Processor;
import org.asciidoctor.extension.Reader;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;

/**
 * The implementation of the {@link Processor} of the AsciidoctorJ API, to which the processors of the API (i.e.,
 * subclasses of {@link org.asciidoctor.extension.BaseProcessor}) delegate (like the JRubyProcessor of AsciidoctorJ).
 */
public class ProcessorImpl implements Processor {
    private Map<String, Object> config;
    private boolean configFinalized;
    private Asciidoctor asciidoctor;

    public ProcessorImpl() {
        this(new HashMap<>());
    }

    public ProcessorImpl(Map<String, Object> config) {
        this.config = new HashMap<>(config);
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    @Override
    public final void setConfig(Map<String, Object> config) {
        if (configFinalized) {
            throw new IllegalStateException("It is only allowed to set the config in the constructor!");
        }
        this.config = config;
    }

    /**
     * Sets the configuration once the processor is registered (e.g., to add the configuration declared by
     * annotations), then prevents the configuration from being replaced.
     */
    final void finalizeConfig(Map<String, Object> config) {
        this.config = config;
        this.configFinalized = true;
    }

    @Override
    public final void updateConfig(Map<String, Object> config) {
        this.config.putAll(config);
    }

    /**
     * Associates the processor with the Asciidoctor instance it's registered with.
     */
    final void setAsciidoctor(Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    /**
     * @return this processor, or the Asciidoctor instance the processor is registered with
     */
    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(getClass())) {
            return clazz.cast(this);
        } else if (asciidoctor != null && clazz.isInstance(asciidoctor)) {
            return clazz.cast(asciidoctor);
        }
        throw new IllegalArgumentException("Cannot unwrap to " + clazz);
    }

    /**
     * Logs the record with the log handlers of the Asciidoctor instance the processor is registered with (or with the
     * logger of the implementation if the processor is not registered).
     */
    @Override
    public void log(LogRecord logRecord) {
        if (asciidoctor instanceof LogHandler logHandler) {
            logHandler.log(logRecord);
        } else {
            LoggerManager.getLogger().add(LogRecords.toSeverity(logRecord.getSeverity()),
                    LogRecords.toMessage(logRecord));
        }
    }

    // --- tables ---

    @Override
    public Table createTable(StructuralNode parent) {
        return createTable(parent, new HashMap<>());
    }

    @Override
    public Table createTable(StructuralNode parent, Map<String, Object> attributes) {
        org.asciidoctor.core.Table table = new org.asciidoctor.core.Table(NodeConverter.unwrap(parent),
                attributes(attributes));
        table.setAttribute("rowcount", 0, false);
        return NodeConverter.wrap(table);
    }

    @Override
    public Row createTableRow(Table parent) {
        return new RowImpl(new org.asciidoctor.core.Table.Row());
    }

    @Override
    public Column createTableColumn(Table parent, int index) {
        return createTableColumn(parent, index, new HashMap<>());
    }

    @Override
    public Column createTableColumn(Table parent, int index, Map<String, Object> attributes) {
        return NodeConverter.wrap(new org.asciidoctor.core.Table.Column(NodeConverter.unwrap(parent), index,
                attributes(attributes)));
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
    public Cell createTableCell(Column column, Document innerDocument, Map<String, Object> attributes) {
        Cell cell = createTableCell(column, (String) null, attributes);
        cell.setStyle("asciidoc");
        cell.setInnerDocument(innerDocument);
        return cell;
    }

    @Override
    public Cell createTableCell(Column column, String text, Map<String, Object> attributes) {
        return NodeConverter.wrap(new org.asciidoctor.core.Table.Cell(NodeConverter.unwrap(column), text,
                attributes(attributes)));
    }

    // --- blocks ---

    @Override
    public org.asciidoctor.ast.Block createBlock(StructuralNode parent, String context, String content) {
        return createBlock(parent, context, content, new HashMap<>(), new HashMap<>());
    }

    @Override
    public org.asciidoctor.ast.Block createBlock(StructuralNode parent, String context, String content,
            Map<String, Object> attributes) {
        return createBlock(parent, context, content, attributes, new HashMap<>());
    }

    @Override
    public org.asciidoctor.ast.Block createBlock(StructuralNode parent, String context, String content,
            Map<String, Object> attributes, Map<Object, Object> options) {
        Map<Object, Object> optionsCopy = new HashMap<>(options);
        optionsCopy.put("source", content);
        optionsCopy.put("attributes", attributes);
        return createBlock(parent, context, optionsCopy);
    }

    @Override
    public org.asciidoctor.ast.Block createBlock(StructuralNode parent, String context, List<String> content) {
        return createBlock(parent, context, content, new HashMap<>(), new HashMap<>());
    }

    @Override
    public org.asciidoctor.ast.Block createBlock(StructuralNode parent, String context, List<String> content,
            Map<String, Object> attributes) {
        return createBlock(parent, context, content, attributes, new HashMap<>());
    }

    @Override
    public org.asciidoctor.ast.Block createBlock(StructuralNode parent, String context, List<String> content,
            Map<String, Object> attributes, Map<Object, Object> options) {
        Map<Object, Object> optionsCopy = new HashMap<>(options);
        optionsCopy.put("source", content);
        optionsCopy.put("attributes", new HashMap<>(attributes));
        return createBlock(parent, context, optionsCopy);
    }

    @Override
    public org.asciidoctor.ast.Block createBlock(StructuralNode parent, String context, Map<Object, Object> options) {
        return NodeConverter.wrap(new org.asciidoctor.core.Block(NodeConverter.unwrap(parent), unsymbolize(context),
                blockOptions(options)));
    }

    // --- sections ---

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
    public Section createSection(StructuralNode parent, Integer level, boolean numbered, Map<Object, Object> options) {
        Map<String, Object> opts = blockOptions(options);
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) opts.get("attributes");
        org.asciidoctor.core.Section section = new org.asciidoctor.core.Section(NodeConverter.unwrap(parent), level,
                numbered, attributes(attributes));
        return NodeConverter.wrap(section);
    }

    @Override
    public Document createDocument(Document parentDocument) {
        Map<String, Object> options = new HashMap<>();
        options.put("parent", NodeConverter.unwrap(parentDocument));
        return NodeConverter.wrap(new org.asciidoctor.core.Document(null, options));
    }

    // --- lists ---

    @Override
    public org.asciidoctor.ast.List createList(StructuralNode parent, String context) {
        return createList(parent, context, new HashMap<>(), new HashMap<>());
    }

    @Override
    public org.asciidoctor.ast.List createList(StructuralNode parent, String context, Map<String, Object> attributes,
            Map<Object, Object> options) {
        Map<Object, Object> optionsCopy = new HashMap<>(options);
        optionsCopy.put("attributes", new HashMap<>(attributes));
        return createList(parent, context, optionsCopy);
    }

    @Override
    public org.asciidoctor.ast.List createList(StructuralNode parent, String context, Map<Object, Object> options) {
        Map<String, Object> opts = blockOptions(options);
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) opts.get("attributes");
        return NodeConverter.wrap(new org.asciidoctor.core.List(NodeConverter.unwrap(parent), unsymbolize(context),
                attributes(attributes)));
    }

    @Override
    public ListItem createListItem(org.asciidoctor.ast.List parent, String text) {
        return NodeConverter.wrap(new org.asciidoctor.core.ListItem(NodeConverter.unwrap(parent), text));
    }

    @Override
    public ListItem createListItem(DescriptionList parent, String text) {
        return NodeConverter.wrap(new org.asciidoctor.core.ListItem(NodeConverter.unwrap(parent), text));
    }

    // --- inline nodes ---

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text) {
        return createPhraseNode(parent, context, text, new HashMap<>(), new HashMap<>());
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text,
            Map<String, Object> attributes) {
        return createPhraseNode(parent, context, text, attributes, new HashMap<>());
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text,
            Map<String, Object> attributes, Map<Object, Object> options) {
        Map<String, Object> opts = blockOptions(options);
        opts.put("attributes", attributes(attributes));
        return createInline(parent, context, text == null ? null : String.join("\n", text), opts);
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, String text) {
        return createPhraseNode(parent, context, text, new HashMap<>(), new HashMap<>());
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, String text,
            Map<String, Object> attributes) {
        return createPhraseNode(parent, context, text, attributes, new HashMap<>());
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes,
            Map<String, Object> options) {
        Map<String, Object> opts = blockOptions(options);
        opts.put("attributes", attributes(attributes));
        return createInline(parent, context, text, opts);
    }

    private static PhraseNode createInline(ContentNode parent, String context, String text, Map<String, Object> opts) {
        AbstractNode parentNode = NodeConverter.unwrap(parent);
        return NodeConverter.wrap(new Inline(parentNode, unsymbolize(context), text, opts));
    }

    // --- parsing ---

    @Override
    public void parseContent(StructuralNode parent, List<String> lines) {
        AbstractBlock parentBlock = NodeConverter.unwrap(parent);
        org.asciidoctor.core.Reader reader = new org.asciidoctor.core.Reader(new ArrayList<>(lines));
        AbstractBlock block;
        while (reader.hasMoreLines() && (block = Parser.nextBlock(reader, parentBlock)) != null) {
            parentBlock.append(block);
        }
    }

    @Override
    public Cursor newCursor(String file) {
        return newCursor(file, null);
    }

    @Override
    public Cursor newCursor(String file, String dir) {
        return newCursor(file, dir, 1);
    }

    @Override
    public Cursor newCursor(String file, String dir, int lineno) {
        return CursorImpl.wrap(new org.asciidoctor.core.Reader.Cursor(file, dir, null, lineno));
    }

    @Override
    public Reader newReader(List<String> source, Cursor cursor, Map<Object, Object> options) {
        Map<String, Object> opts = blockOptions(options);
        return new ReaderImpl(new org.asciidoctor.core.Reader(new ArrayList<>(source), CursorImpl.unwrap(cursor),
                opts.get("normalize")));
    }

    @Override
    public Reader newReader(List<String> source, Cursor cursor) {
        return newReader(source, cursor, null);
    }

    @Override
    public Reader newReader(List<String> source, Map<Object, Object> options) {
        return newReader(source, null, options);
    }

    @Override
    public Reader newReader(List<String> source) {
        return newReader(source, null, null);
    }

    // --- helpers ---

    /**
     * Converts the options of the API (whose keys and values may be Ruby symbols, e.g., :subs or :compound) to the
     * options of the implementation.
     */
    static Map<String, Object> blockOptions(Map<?, ?> options) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (options == null) {
            return result;
        }
        options.forEach((key, optionValue) -> {
            String name = unsymbolize(String.valueOf(key));
            Object value = optionValue;
            if ("attributes".equals(name) && value instanceof Map<?, ?> map) {
                value = attributes(map);
            } else if (!"source".equals(name)) {
                // NOTE unlike AsciidoctorJ, the lines of the source are not converted to symbols
                value = symbolValue(value);
            }
            result.put(name, value);
        });
        return result;
    }

    /**
     * @return a mutable copy of the attributes, whose values are converted like AsciidoctorJ converts them to Ruby
     * objects (e.g., ":normal" is a symbol)
     */
    static Map<String, Object> attributes(Map<?, ?> attributes) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (attributes != null) {
            attributes.forEach((name, value) -> result.put(String.valueOf(name), symbolValue(value)));
        }
        return result;
    }

    /**
     * Converts a value of the API to a value of the implementation like AsciidoctorJ converts a value to a Ruby
     * object: a String that starts with a colon is a Ruby symbol (e.g., :compound), including in a List.
     */
    static Object symbolValue(Object value) {
        if (value instanceof String str) {
            return unsymbolize(str);
        } else if (value instanceof List<?> list) {
            return list.stream().map(ProcessorImpl::symbolValue).toList();
        }
        return value;
    }

    /**
     * @return the value without the leading colon of a Ruby symbol (e.g., :paragraph)
     */
    static String unsymbolize(String value) {
        return value != null && value.startsWith(":") ? value.substring(1) : value;
    }
}
