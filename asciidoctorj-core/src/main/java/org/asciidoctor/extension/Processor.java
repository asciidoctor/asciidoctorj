package org.asciidoctor.extension;

import org.asciidoctor.Options;
import org.asciidoctor.ast.*;
import org.asciidoctor.ast.impl.ColumnImpl;
import org.asciidoctor.ast.impl.ContentNodeImpl;
import org.asciidoctor.ast.impl.DocumentImpl;
import org.asciidoctor.ast.impl.RowImpl;
import org.asciidoctor.ast.impl.StructuralNodeImpl;
import org.asciidoctor.internal.JRubyRuntimeContext;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyObjectWrapper;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyFixnum;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.asciidoctor.ast.NodeConverter.NodeType.*;

public class Processor {

    /**
     * This value is used as the config option key to configure how Asciidoctor should treat blocks created by
     * this Processor.
     * Its value must be a String constant.
     *
     * <p>Example to Asciidoctor know that a BlockProcessor creates zero or more child blocks:
     * <pre>
     * <verbatim>
     * Map&lt;String, Object&gt; config = new HashMap&lt;&gt;();
     * config.put(CONTENT_MODEL, ContentModel.COMPOUND);
     * BlockProcessor blockProcessor = new BlockProcessor("foo", config);
     * asciidoctor.javaExtensionRegistry().block(blockProcessor);
     * </verbatim>
     * </pre>
     * </p>
     *
     * @deprecated Use the constant {@link ContentModel#KEY} instead or the annotation {@link ContentModel} to describe this Processor
     */
    @Deprecated
    public static final String CONTENT_MODEL = "content_model";

    /**
     * Predefined constant to let Asciidoctor know that this BlockProcessor creates zero or more child blocks.
     * @deprecated Use {@link ContentModel#COMPOUND} instead.
     */
    @Deprecated
    public static final String CONTENT_MODEL_COMPOUND = ":compound";

    /**
     * Predefined constant to let Asciidoctor know that this BlockProcessor creates simple paragraph content.
     * @deprecated Use {@link ContentModel#SIMPLE} instead.
     */
    @Deprecated
    public static final String CONTENT_MODEL_SIMPLE =":simple";

    /**
     * Predefined constant to let Asciidoctor know that this BlockProcessor creates literal content.
     * @deprecated Use {@link ContentModel#VERBATIM} instead.
     */
    @Deprecated
    public static final String CONTENT_MODEL_VERBATIM =":verbatim";

    /**
     * Predefined constant to make Asciidoctor pass through the content unprocessed.
     * @deprecated Use {@link ContentModel#RAW} instead.
     */
    @Deprecated
    public static final String CONTENT_MODEL_RAW =":raw";

    /**
     * Predefined constant to make Asciidoctor drop the content.
     * @deprecated Use {@link ContentModel#SKIP} instead.
     */
    @Deprecated
    public static final String CONTENT_MODEL_SKIP =":skip";

    /**
     * Predefined constant to make Asciidoctor not expect any content.
     * @deprecated Use {@link ContentModel#EMPTY} instead.
     */
    @Deprecated
    public static final String CONTENT_MODEL_EMPTY =":empty";

    /**
     * Predefined constant to make Asciidoctor parse content as attributes.
     * @deprecated Use {@link ContentModel#ATTRIBUTES} instead.
     */
    @Deprecated
    public static final String CONTENT_MODEL_ATTRIBUTES =":attributes";


    protected Map<String, Object> config;

    /**
     * The config map must not be reset once configFinalized is true.
     * With the Asciidoctor Ruby implementation this flag will be set to
     * true after the Ruby part of the extension is initialized.
     */
    private boolean configFinalized = false;

    public Processor() {
        this(new HashMap<String, Object>());
    }

    public Processor(Map<String, Object> config) {
        this.config = new HashMap<String, Object>(config);
    }

    public Map<String, Object> getConfig() {
    	return this.config;
    }

    public final void setConfig(Map<String, Object> config) {
        if (configFinalized) {
            throw new IllegalStateException("It is only allowed to set the config in the constructor!");
        }
        this.config = config;
    }

    /**
     * Lock the config of this processor so that it is no longer allowed to invoke {@link #setConfig(Map)}.
     */
    public final void setConfigFinalized() {
        this.configFinalized = true;
    }


    public Table createTable(StructuralNode parent) {
        return createTable(parent, new HashMap<String, Object>());
    }

    public Table createTable(StructuralNode parent, Map<String, Object> attributes) {
        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        RubyHash rubyAttributes = RubyHash.newHash(rubyRuntime);
        rubyAttributes.putAll(attributes);

        IRubyObject[] parameters = {
                ((StructuralNodeImpl) parent).getRubyObject(),
                rubyAttributes};
        Table ret = (Table) NodeConverter.createASTNode(rubyRuntime, TABLE_CLASS, parameters);
        ret.setAttr("rowcount", 0, false);
        return ret;
    }

    public Row createTableRow(Table parent) {
        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        RubyArray rubyRow = rubyRuntime.newArray();
        return new RowImpl(rubyRow);
    }

    public Column createTableColumn(Table parent, int index) {
        return createTableColumn(parent, index, new HashMap<String, Object>());
    }

    public Column createTableColumn(Table parent, int index, Map<String, Object> attributes) {
        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        RubyHash rubyAttributes = RubyHash.newHash(rubyRuntime);
        rubyAttributes.putAll(attributes);

        IRubyObject[] parameters = {
                ((StructuralNodeImpl) parent).getRubyObject(),
                RubyFixnum.newFixnum(rubyRuntime, index),
                rubyAttributes}; // No cursor parameter yet

        return (Column) NodeConverter.createASTNode(rubyRuntime, TABLE_COLUMN_CLASS, parameters);
    }

    public Cell createTableCell(Column column, String text) {
        return createTableCell(column, text, new HashMap<String, Object>());
    }

    public Cell createTableCell(Column column, Document innerDocument) {
        return createTableCell(column, innerDocument, new HashMap<String, Object>());
    }

    public Cell createTableCell(Column column, Document innerDocument, Map<String, Object> attributes) {
        Cell cell = createTableCell(column, (String) null, attributes);
        cell.setStyle("asciidoc");
        cell.setInnerDocument(innerDocument);
        return cell;
    }

    public Cell createTableCell(Column column, String text, Map<String, Object> attributes) {
        Ruby rubyRuntime = JRubyRuntimeContext.get(column);

        RubyHash rubyAttributes = RubyHash.newHash(rubyRuntime);
        rubyAttributes.putAll(attributes);

        IRubyObject[] parameters = {
                ((ColumnImpl) column).getRubyObject(),
                text != null ? rubyRuntime.newString(text) : rubyRuntime.getNil(),
                rubyAttributes}; // No cursor parameter yet

        return (Cell) NodeConverter.createASTNode(rubyRuntime, TABLE_CELL_CLASS, parameters);
    }

    public Block createBlock(StructuralNode parent, String context, String content) {
        return createBlock(parent, context, content, new HashMap<String, Object>(), new HashMap<Object, Object>());
    }

    public Block createBlock(StructuralNode parent, String context, String content, Map<String, Object> attributes) {
        return createBlock(parent, context, content, attributes, new HashMap<Object, Object>());
    }

    public Block createBlock(StructuralNode parent, String context, String content, Map<String, Object> attributes,
            Map<Object, Object> options) {

        options.put(Options.SOURCE, content);
        options.put(Options.ATTRIBUTES, attributes);        
        
        return createBlock(parent, context, options);
    }

    public Block createBlock(StructuralNode parent, String context, List<String> content) {
        return createBlock(parent, context, content, new HashMap<String, Object>(), new HashMap<Object, Object>());
    }

    public Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes) {
        return createBlock(parent, context, content, attributes, new HashMap<Object, Object>());
    }

    public Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes,
            Map<Object, Object> options) {

        options.put(Options.SOURCE, content);
        options.put(Options.ATTRIBUTES, new HashMap<String, Object>(attributes));
        
        return createBlock(parent, context, options);
    }

    public Section createSection(StructuralNode parent) {
        return createSection(parent, null, true, new HashMap<Object, Object>());
    }

    public Section createSection(StructuralNode parent, Map<Object, Object> options) {
        return createSection(parent, null, true, options);
    }

    public Section createSection(StructuralNode parent, boolean numbered, Map<Object, Object> options) {
        return createSection(parent, null, numbered, options);
    }

    public Section createSection(StructuralNode parent, int level, boolean numbered, Map<Object, Object> options) {
        return createSection(parent, Integer.valueOf(level), numbered, options);
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text) {
        return createPhraseNode(parent, context, text, new HashMap<String, Object>());
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text, Map<String, Object> attributes) {
        return createPhraseNode(parent, context, text, attributes, new HashMap<Object, Object>());
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text, Map<String, Object> attributes, Map<Object, Object> options) {

        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        options.put(Options.ATTRIBUTES, attributes);
        
        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(rubyRuntime,
                options);

        RubyArray rubyText = rubyRuntime.newArray();
        rubyText.addAll(text);

        IRubyObject[] parameters = {
                ((ContentNodeImpl) parent).getRubyObject(),
                RubyUtils.toSymbol(rubyRuntime, context),
                rubyText,
                convertMapToRubyHashWithSymbols };
        return (PhraseNode) NodeConverter.createASTNode(rubyRuntime, INLINE_CLASS, parameters);
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, String text) {
        return createPhraseNode(parent, context, text, new HashMap<String, Object>());
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes) {
        return createPhraseNode(parent, context, text, attributes, new HashMap<String, Object>());
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes, Map<String, Object> options) {
        
        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        options.put(Options.ATTRIBUTES, RubyHashUtil.convertMapToRubyHashWithStrings(rubyRuntime, attributes));

        RubyHash convertedOptions = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);

        IRubyObject[] parameters = {
                ((ContentNodeImpl) parent).getRubyObject(),
                RubyUtils.toSymbol(rubyRuntime, context),
                text == null ? rubyRuntime.getNil() : rubyRuntime.newString(text),
                convertedOptions };
        return (PhraseNode) NodeConverter.createASTNode(rubyRuntime, INLINE_CLASS, parameters);
    }
    
    private Block createBlock(StructuralNode parent, String context,
            Map<Object, Object> options) {

        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(rubyRuntime,
                options);

        IRubyObject[] parameters = {
                ((StructuralNodeImpl) parent).getRubyObject(),
                RubyUtils.toSymbol(rubyRuntime, context),
                convertMapToRubyHashWithSymbols };
        return (Block) NodeConverter.createASTNode(rubyRuntime, BLOCK_CLASS, parameters);
    }

    private Section createSection(StructuralNode parent, Integer level, boolean numbered, Map<Object, Object> options) {

        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(rubyRuntime,
                options);

        IRubyObject[] parameters = {
                ((StructuralNodeImpl) parent).getRubyObject(),
                level == null ? rubyRuntime.getNil() : rubyRuntime.newFixnum(level),
                rubyRuntime.newBoolean(numbered),
                convertMapToRubyHashWithSymbols };
        return (Section) NodeConverter.createASTNode(rubyRuntime, SECTION_CLASS, parameters);
    }

    /**
     * Creates an inner document for the given parent document.
     * Inner documents are used for tables cells with style {@code asciidoc}.
     * @param parentDocument The parent document of the new document.
     * @return A new inner document.
     */
    public Document createDocument(Document parentDocument) {
        Ruby runtime = JRubyRuntimeContext.get(parentDocument);
        RubyHash options = RubyHash.newHash(runtime);
        options.put(
                runtime.newSymbol("parent"),
                ((DocumentImpl) parentDocument).getRubyObject());

        return (Document) NodeConverter.createASTNode(runtime, DOCUMENT_CLASS, runtime.getNil(), options);
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
     * @param lines Raw asciidoctor content
     */
    public void parseContent(StructuralNode parent, List<String> lines) {
        Ruby runtime = JRubyRuntimeContext.get(parent);
        Parser parser = new Parser(runtime, parent, ReaderImpl.createReader(runtime, lines));

        StructuralNode nextBlock = parser.nextBlock();
        while (nextBlock != null) {
            parent.append(nextBlock);
            nextBlock = parser.nextBlock();
        }
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
            IRubyObject nextBlock = getRubyProperty("next_block", reader, ((StructuralNodeImpl)parent).getRubyObject());
            if (nextBlock.isNil()) {
                return null;
            } else {
                return (StructuralNode) NodeConverter.createASTNode(nextBlock);
            }
        }
    }
}
