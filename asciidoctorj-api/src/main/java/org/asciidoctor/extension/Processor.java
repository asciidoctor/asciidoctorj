package org.asciidoctor.extension;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;
import org.asciidoctor.extension.spi.AsciidoctorFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        this(new HashMap<>());
    }

    public Processor(Map<String, Object> config) {
        this.config = new HashMap<>(config);
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

    public final void updateConfig(Map<String, Object> config) {
        this.config.putAll(config);
    }

    /**
     * Lock the config of this processor so that it is no longer allowed to invoke {@link #setConfig(Map)}.
     */
    public final void setConfigFinalized() {
        this.configFinalized = true;
    }


    public Table createTable(StructuralNode parent) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createTable(parent);
    }

    public Table createTable(StructuralNode parent, Map<String, Object> attributes) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createTable(parent, attributes);
    }

    public Row createTableRow(Table parent) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createTableRow(parent);
    }

    public Column createTableColumn(Table parent, int index) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createTableColumn(parent, index);
    }

    public Column createTableColumn(Table parent, int index, Map<String, Object> attributes) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createTableColumn(parent, index, attributes);
    }

    public Cell createTableCell(Column column, String text) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createTableCell(column, text);
    }

    public Cell createTableCell(Column column, Document innerDocument) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createTableCell(column, innerDocument);
    }

    public Cell createTableCell(Column column, Document innerDocument, Map<String, Object> attributes) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createTableCell(column, innerDocument, attributes);
    }

    public Cell createTableCell(Column column, String text, Map<String, Object> attributes) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createTableCell(column, text, attributes);
    }

    public Block createBlock(StructuralNode parent, String context, String content) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createBlock(parent, context, content);
    }

    public Block createBlock(StructuralNode parent, String context, String content, Map<String, Object> attributes) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createBlock(parent, context, content, attributes);
    }

    public Block createBlock(StructuralNode parent, String context, String content, Map<String, Object> attributes,
                             Map<Object, Object> options) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createBlock(parent, context, content, attributes, options);
    }

    public Block createBlock(StructuralNode parent, String context, List<String> content) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createBlock(parent, context, content);
    }

    public Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createBlock(parent, context, content, attributes);
    }

    public Block createBlock(StructuralNode parent, String context, List<String> content, Map<String, Object> attributes,
                             Map<Object, Object> options) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createBlock(parent, context, content, attributes, options);
    }

    public Section createSection(StructuralNode parent) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createSection(parent);
    }

    public Section createSection(StructuralNode parent, Map<Object, Object> options) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createSection(parent, options);
    }

    public Section createSection(StructuralNode parent, boolean numbered, Map<Object, Object> options) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createSection(parent, numbered, options);
    }

    public Section createSection(StructuralNode parent, int level, boolean numbered, Map<Object, Object> options) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createSection(parent, Integer.valueOf(level), numbered, options);
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createPhraseNode(parent, context, text);
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text, Map<String, Object> attributes) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createPhraseNode(parent, context, text, attributes);
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, List<String> text, Map<String, Object> attributes, Map<Object, Object> options) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createPhraseNode(parent, context, text, attributes, options);
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, String text) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createPhraseNode(parent, context, text);
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createPhraseNode(parent, context, text, attributes);
    }

    public PhraseNode createPhraseNode(ContentNode parent, String context, String text, Map<String, Object> attributes, Map<String, Object> options) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createPhraseNode(parent, context, text, attributes, options);
    }

    /**
     * Creates an inner document for the given parent document.
     * Inner documents are used for tables cells with style {@code asciidoc}.
     * @param parentDocument The parent document of the new document.
     * @return A new inner document.
     */
    public Document createDocument(Document parentDocument) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createDocument(parentDocument);
    }

    public ListItem createListItem(final org.asciidoctor.ast.List parent, final String text) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createListItem(parent, text);
    }

    public ListItem createListItem(final org.asciidoctor.ast.DescriptionList parent, final String text) {
        return AsciidoctorFactory.getFactory().getProcessorNodeFactory().createListItem(parent, text);
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
        AsciidoctorFactory.getFactory().getProcessorNodeFactory().parseContent(parent, lines);
    }
}
