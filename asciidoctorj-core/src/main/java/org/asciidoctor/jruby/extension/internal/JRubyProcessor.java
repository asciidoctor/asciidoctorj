package org.asciidoctor.jruby.extension.internal;

import org.asciidoctor.Options;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;
import org.asciidoctor.extension.Processor;
import org.asciidoctor.extension.Reader;
import org.asciidoctor.jruby.ast.impl.ColumnImpl;
import org.asciidoctor.jruby.ast.impl.ContentNodeImpl;
import org.asciidoctor.jruby.ast.impl.CursorImpl;
import org.asciidoctor.jruby.ast.impl.DescriptionListImpl;
import org.asciidoctor.jruby.ast.impl.DocumentImpl;
import org.asciidoctor.jruby.ast.impl.ListImpl;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.asciidoctor.jruby.ast.impl.RowImpl;
import org.asciidoctor.jruby.ast.impl.StructuralNodeImpl;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.asciidoctor.jruby.internal.JRubyRuntimeContext;
import org.asciidoctor.jruby.internal.RubyHashUtil;
import org.asciidoctor.jruby.internal.RubyObjectWrapper;
import org.asciidoctor.jruby.internal.RubyUtils;
import org.asciidoctor.log.LogRecord;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyFixnum;
import org.jruby.RubyHash;
import org.jruby.RubySymbol;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JRubyProcessor implements Processor {

    private JRubyAsciidoctor asciidoctor;

    public static final String CONTENT_MODEL_EMPTY =":empty";

    protected Map<String, Object> config;

    /**
     * The config map must not be reset once configFinalized is true.
     * With the Asciidoctor Ruby implementation this flag will be set to
     * true after the Ruby part of the extension is initialized.
     */
    private boolean configFinalized = false;

    public JRubyProcessor() {
        this(new HashMap<>());
    }

    public JRubyProcessor(Map<String, Object> config) {
        this.config = new HashMap<>(config);
    }

    @Override
    public Map<String, Object> getConfig() {
        return this.config;
    }

    @Override
    public final void setConfig(Map<String, Object> config) {
        if (configFinalized) {
            throw new IllegalStateException("It is only allowed to set the config in the constructor!");
        }
        this.config = config;
    }

    @Override
    public final void updateConfig(Map<String, Object> config) {
        this.config.putAll(config);
    }

    public final void setConfigFinalized() {
        this.configFinalized = true;
    }

    @Override
    public Table createTable(StructuralNode parent) {
        return createTable(parent, new HashMap<>());
    }

    @Override
    public Table createTable(StructuralNode parent, Map<String, Object> attributes) {
        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        RubyHash rubyAttributes = RubyHash.newHash(rubyRuntime);
        rubyAttributes.putAll(attributes);

        IRubyObject[] parameters = {
                ((StructuralNodeImpl) parent).getRubyObject(),
                rubyAttributes};
        Table ret = (Table) NodeConverter.createASTNode(rubyRuntime, NodeConverter.NodeType.TABLE_CLASS, parameters);
        ret.setAttribute("rowcount", 0, false);
        return ret;
    }

    @Override
    public Row createTableRow(Table parent) {
        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        RubyArray rubyRow = rubyRuntime.newArray();
        return new RowImpl(rubyRow);
    }

    @Override
    public Column createTableColumn(Table parent, int index, Map<String, Object> attributes) {
        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        RubyHash rubyAttributes = RubyHash.newHash(rubyRuntime);
        rubyAttributes.putAll(attributes);

        IRubyObject[] parameters = {
                ((StructuralNodeImpl) parent).getRubyObject(),
                RubyFixnum.newFixnum(rubyRuntime, index),
                rubyAttributes}; // No cursor parameter yet

        return (Column) NodeConverter.createASTNode(rubyRuntime, NodeConverter.NodeType.TABLE_COLUMN_CLASS, parameters);
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
        Ruby rubyRuntime = JRubyRuntimeContext.get(column);

        RubyHash rubyAttributes = RubyHash.newHash(rubyRuntime);
        rubyAttributes.putAll(attributes);

        IRubyObject[] parameters = {
                ((ColumnImpl) column).getRubyObject(),
                text != null ? rubyRuntime.newString(text) : rubyRuntime.getNil(),
                rubyAttributes}; // No cursor parameter yet

        return (Cell) NodeConverter.createASTNode(rubyRuntime, NodeConverter.NodeType.TABLE_CELL_CLASS, parameters);
    }

    @Override
    public Block createBlock(StructuralNode parent, String context, String content, Map<String, Object> attributes,
                             Map<Object, Object> options) {

        Map<Object, Object> optionsCopy = new HashMap<>(options);
        optionsCopy.put(Options.SOURCE, content);
        optionsCopy.put(Options.ATTRIBUTES, attributes);

        return createBlock(parent, context, optionsCopy);
    }

    @Override
    public Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes,
                             Map<Object, Object> options) {

        Map<Object, Object> optionsCopy = new HashMap<>(options);
        optionsCopy.put(Options.SOURCE, content);
        optionsCopy.put(Options.ATTRIBUTES, new HashMap<>(attributes));
        return createBlock(parent, context, optionsCopy);
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text, Map<String, Object> attributes, Map<Object, Object> options) {

        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        Map<Object, Object> optionsCopy = new HashMap<>(options);
        optionsCopy.put(Options.ATTRIBUTES, attributes);

        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(rubyRuntime,
                optionsCopy);

        RubyArray rubyText = rubyRuntime.newArray();
        rubyText.addAll(text);

        IRubyObject[] parameters = {
                ((ContentNodeImpl) parent).getRubyObject(),
                RubyUtils.toSymbol(rubyRuntime, context),
                rubyText,
                convertMapToRubyHashWithSymbols};
        return (PhraseNode) NodeConverter.createASTNode(rubyRuntime, NodeConverter.NodeType.INLINE_CLASS, parameters);
    }

    @Override
    public PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes, Map<String, Object> options) {

        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        Map<String, Object> optionsCopy = new HashMap<>(options);
        optionsCopy.put(Options.ATTRIBUTES, RubyHashUtil.convertMapToRubyHashWithStrings(rubyRuntime, attributes));

        RubyHash convertedOptions = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, optionsCopy);

        IRubyObject[] parameters = {
                ((ContentNodeImpl) parent).getRubyObject(),
                RubyUtils.toSymbol(rubyRuntime, context),
                text == null ? rubyRuntime.getNil() : rubyRuntime.newString(text),
                convertedOptions};
        return (PhraseNode) NodeConverter.createASTNode(rubyRuntime, NodeConverter.NodeType.INLINE_CLASS, parameters);
    }

    @Override
    public Block createBlock(StructuralNode parent, String context,
                             Map<Object, Object> options) {

        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(rubyRuntime,
                filterBlockOptions(parent, options, "subs", ContentModel.KEY));

        IRubyObject[] parameters = {
                ((StructuralNodeImpl) parent).getRubyObject(),
                RubyUtils.toSymbol(rubyRuntime, context),
                convertMapToRubyHashWithSymbols};
        return (Block) NodeConverter.createASTNode(rubyRuntime, NodeConverter.NodeType.BLOCK_CLASS, parameters);
    }

    private Map<Object, Object> filterBlockOptions(
            StructuralNode parent,
            Map<Object, Object> options,
            String... optionNames) {
        final Map<Object, Object> copy = new HashMap<>(options);
        final Ruby ruby = JRubyRuntimeContext.get(parent);
        for (String optionName : optionNames) {
            final Object optionValue = copy.get(optionName);
            if (optionValue != null) {
                if (optionValue instanceof String) {
                    copy.put(optionName, getRubySymbol(ruby, (String) optionValue));
                } else if (optionValue instanceof List) {
                    List valueList = (List) optionValue;
                    List newValueList = new ArrayList(valueList.size());
                    for (Object v : valueList) {
                        if (v instanceof String) {
                            newValueList.add(getRubySymbol(ruby, (String) v));
                        } else {
                            newValueList.add(v);
                        }
                    }
                    copy.put(optionName, newValueList);
                }
            }
        }
        return copy;
    }

    private RubySymbol getRubySymbol(Ruby ruby, String s) {
        return ruby.newSymbol(s.startsWith(":") ? s.substring(1) : s);
    }

    @Override
    public Section createSection(StructuralNode parent, Integer level, boolean numbered, Map<Object, Object> options) {

        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(rubyRuntime,
                options);

        IRubyObject[] parameters = {
                ((StructuralNodeImpl) parent).getRubyObject(),
                level == null ? rubyRuntime.getNil() : rubyRuntime.newFixnum(level),
                rubyRuntime.newBoolean(numbered),
                convertMapToRubyHashWithSymbols};
        return (Section) NodeConverter.createASTNode(rubyRuntime, NodeConverter.NodeType.SECTION_CLASS, parameters);
    }

    /**
     * Creates an inner document for the given parent document.
     * Inner documents are used for tables cells with style {@code asciidoc}.
     *
     * @param parentDocument The parent document of the new document.
     * @return A new inner document.
     */
    @Override
    public Document createDocument(Document parentDocument) {
        Ruby runtime = JRubyRuntimeContext.get(parentDocument);
        RubyHash options = RubyHash.newHash(runtime);
        options.put(
                runtime.newSymbol("parent"),
                ((DocumentImpl) parentDocument).getRubyObject());

        return (Document) NodeConverter.createASTNode(runtime, NodeConverter.NodeType.DOCUMENT_CLASS, runtime.getNil(), options);
    }


    @Override
    public org.asciidoctor.ast.List createList(StructuralNode parent, String context) {
        return createList(parent, context, new HashMap<>(), new HashMap<>());

    }

    @Override
    public org.asciidoctor.ast.List createList(StructuralNode parent, String context,
                                               Map<String, Object> attributes,
                                               Map<Object, Object> options) {

        HashMap<Object, Object> optionsCopy = new HashMap<>(options);
        optionsCopy.put(Options.ATTRIBUTES, new HashMap<>(attributes));
        return createList(parent, context, optionsCopy);
    }

    @Override
    public org.asciidoctor.ast.List createList(StructuralNode parent, String context,
                                               Map<Object, Object> options) {

        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(rubyRuntime,
            filterBlockOptions(parent, options, "subs", ContentModel.KEY));

        IRubyObject[] parameters = {
            ((StructuralNodeImpl) parent).getRubyObject(),
            RubyUtils.toSymbol(rubyRuntime, context),
            convertMapToRubyHashWithSymbols};
        return (org.asciidoctor.ast.List) NodeConverter.createASTNode(rubyRuntime, NodeConverter.NodeType.LIST_CLASS, parameters);
    }

    @Override
    public ListItem createListItem(final org.asciidoctor.ast.List parent, final String text) {
        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        return (ListItem) NodeConverter.createASTNode(rubyRuntime, NodeConverter.NodeType.LIST_ITEM_CLASS, ((ListImpl) parent).getRubyObject(), rubyRuntime.newString(text));
    }

    @Override
    public ListItem createListItem(final org.asciidoctor.ast.DescriptionList parent, final String text) {
        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        return (ListItem) NodeConverter.createASTNode(rubyRuntime, NodeConverter.NodeType.LIST_ITEM_CLASS, ((DescriptionListImpl) parent).getRubyObject(), rubyRuntime.newString(text));
    }

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
    @Override
    public void parseContent(StructuralNode parent, List<String> lines) {
        Ruby runtime = JRubyRuntimeContext.get(parent);
        Parser parser = new Parser(runtime, parent, ReaderImpl.createReader(runtime, lines));

        StructuralNode nextBlock = parser.nextBlock();
        while (nextBlock != null) {
            parent.append(nextBlock);
            nextBlock = parser.nextBlock();
        }
    }

    public Cursor newCursor(String file) {
        return newCursor(file, null);
    }

    public Cursor newCursor(String file, String dir) {
        return newCursor(file, dir, 1);
    }

    public Cursor newCursor(String file, String dir, int lineNo) {
        Ruby runtime = JRubyRuntimeContext.get(asciidoctor);
        RubyClass klazz = runtime.getModule("Asciidoctor").getClass("Reader").getClass("Cursor");
        IRubyObject rubyCursor = klazz.newInstance(runtime.getCurrentContext(), new IRubyObject[]{
                runtime.newString(file),
                dir == null ? runtime.getNil() : runtime.newString(dir),
                runtime.newFixnum(lineNo),
        }, org.jruby.runtime.Block.NULL_BLOCK);
        return new CursorImpl(rubyCursor);
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

    @Override
    public Reader newReader(List<String> source, Cursor cursor, Map<Object, Object> options) {
        Ruby runtime = JRubyRuntimeContext.get(asciidoctor);
        RubyClass klazz = runtime.getModule("Asciidoctor").getClass("Reader");

        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime, options);

        RubyArray data = runtime.newArray();
        for (String line : source) {
            data.add(runtime.newString(line));
        }

        IRubyObject rubyReader = klazz.newInstance(runtime.getCurrentContext(), new IRubyObject[]{
                data,
                cursor == null ? runtime.getNil() : ((RubyObjectWrapper) cursor).getRubyObject(),
                convertMapToRubyHashWithSymbols
        }, org.jruby.runtime.Block.NULL_BLOCK);
        return new ReaderImpl(rubyReader);
    }

    private class Parser extends RubyObjectWrapper {

        private final Reader reader;
        private final StructuralNode parent;

        public Parser(Ruby runtime, StructuralNode parent, Reader reader) {
            super(runtime.getModule("Asciidoctor").getClass("Parser"));

            this.reader = reader;
            this.parent = parent;
        }

        public StructuralNode nextBlock() {
            if (!reader.hasMoreLines()) {
                return null;
            }
            IRubyObject nextBlock = getRubyProperty("next_block", reader, ((StructuralNodeImpl) parent).getRubyObject());
            if (nextBlock.isNil()) {
                return null;
            } else {
                return (StructuralNode) NodeConverter.createASTNode(nextBlock);
            }
        }
    }


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
    public Block createBlock(StructuralNode parent, String context, List<String> content) {
        return createBlock(parent, context, content, new HashMap<>(), new HashMap<>());
    }

    @Override
    public Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes) {
        return createBlock(parent, context, content, attributes, new HashMap<>());
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

    public JRubyAsciidoctor getAsciidoctor() {
        return asciidoctor;
    }

    public void setAsciidoctor(JRubyAsciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(JRubyProcessor.class)) {
            return clazz.cast(this);
        }
        if (clazz.isAssignableFrom(JRubyAsciidoctor.class)) {
            return clazz.cast(this.asciidoctor);
        }
        throw new IllegalArgumentException("Cannot unwrap to " + clazz);
    }

    @Override
    public void log(LogRecord logRecord) {
        unwrap(JRubyAsciidoctor.class).log(logRecord);
    }
}
