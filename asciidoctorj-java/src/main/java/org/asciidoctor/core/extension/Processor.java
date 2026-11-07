package org.asciidoctor.core.extension;

import static org.asciidoctor.core.Asciidoctor.ATTR_REF_HEAD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.AbstractNode;
import org.asciidoctor.core.Asciidoctor;
import org.asciidoctor.core.AttributeList;
import org.asciidoctor.core.Block;
import org.asciidoctor.core.Document;
import org.asciidoctor.core.Helpers;
import org.asciidoctor.core.Inline;
import org.asciidoctor.core.ListItem;
import org.asciidoctor.core.LoggerManager;
import org.asciidoctor.core.Logging;
import org.asciidoctor.core.Parser;
import org.asciidoctor.core.Reader;
import org.asciidoctor.core.Section;
import org.asciidoctor.core.Strings;
import org.asciidoctor.core.log.LogRecord;

/**
 * An abstract base class for document and syntax processors.
 *
 * <p>This class provides access to a configuration Map. Default configuration options can be defined using the
 * {@link #option(String, Object)} method (typically in the constructor of a subclass). An overriding configuration Map
 * can be passed to the constructor.
 *
 * <p>Instances of the Processor class provide convenience methods for creating AST nodes, such as Block and Inline,
 * and for parsing child content.
 */
public abstract class Processor {
    protected final Map<String, Object> config;

    protected Processor(Map<String, Object> config) {
        this.config = new LinkedHashMap<>();
        if (config != null) {
            this.config.putAll(config);
        }
        applyAnnotations();
    }

    /**
     * Configures this processor from the annotations of its class (e.g., {@link Name} or {@link Contexts}), for
     * compatibility with AsciidoctorJ. The configuration passed to the constructor takes precedence.
     */
    private void applyAnnotations() {
        Class<?> type = getClass();
        Name name = type.getAnnotation(Name.class);
        if (name != null) {
            config.putIfAbsent("name", name.value());
        }
        Contexts contexts = type.getAnnotation(Contexts.class);
        if (contexts != null) {
            config.putIfAbsent("contexts", new LinkedHashSet<>(Arrays.stream(contexts.value()).map(Processor::unsymbolize)
                    .toList()));
        }
        ContentModel contentModel = type.getAnnotation(ContentModel.class);
        if (contentModel != null) {
            config.putIfAbsent("content_model", org.asciidoctor.core.ContentModel.of(contentModel.value()));
        }
        PositionalAttributes positionalAttributes = type.getAnnotation(PositionalAttributes.class);
        if (positionalAttributes != null) {
            config.putIfAbsent("positional_attrs", new ArrayList<>(Arrays.asList(positionalAttributes.value())));
        }
        DefaultAttribute[] defaultAttributes = type.getAnnotationsByType(DefaultAttribute.class);
        if (defaultAttributes.length > 0) {
            Map<String, Object> defaults = new LinkedHashMap<>();
            for (DefaultAttribute defaultAttribute : defaultAttributes) {
                defaults.put(defaultAttribute.key(), defaultAttribute.value());
            }
            config.putIfAbsent("default_attrs", defaults);
        }
        Format format = type.getAnnotation(Format.class);
        if (format != null) {
            if (format.value() == FormatType.CUSTOM) {
                config.putIfAbsent("regexp", format.regexp());
            } else {
                config.putIfAbsent("format", unsymbolize(format.value().optionValue()));
            }
        }
        Location location = type.getAnnotation(Location.class);
        if (location != null) {
            config.putIfAbsent("location", unsymbolize(location.value().optionValue()));
        }
    }

    /**
     * @return the value without the leading colon of a Ruby symbol (e.g., :paragraph)
     */
    private static String unsymbolize(String value) {
        return value.startsWith(":") ? value.substring(1) : value;
    }

    protected Processor() {
        this(null);
    }

    /**
     * @return the configuration Map for this processor instance.
     */
    public Map<String, Object> getConfig() {
        return config;
    }

    public void updateConfig(Map<String, Object> newConfig) {
        if (newConfig != null) {
            config.putAll(newConfig);
        }
    }

    /**
     * Assigns a value for the specified configuration option.
     */
    public void option(String key, Object value) {
        config.put(key, value);
    }

    // QUESTION should attributes be an option instead of a parameter?

    /**
     * Creates a new Section node.
     *
     * <p>Creates a Section node in the same manner as the parser.
     *
     * @param parent The parent Section (or Document) of this new Section.
     * @param title The String title of the new Section.
     * @param attrs A Map of attributes to control how the section is built. Use the style attribute to set the name of
     *            a special section (ex. appendix). Use the id attribute to assign an explicit ID or set the value to
     *            false to disable automatic ID generation (when sectids document attribute is set).
     * @param opts An optional Map of options (default: {}):
     *            <ul>
     *            <li>level - [Integer] The level to assign to this section; defaults to one greater than the parent
     *            level (optional).</li>
     *            <li>numbered - [Boolean] A flag to force numbering, which falls back to the state of the sectnums
     *            document attribute (optional).</li>
     *            </ul>
     * @return a Section node with all properties properly initialized.
     */
    public Section createSection(AbstractBlock parent, String title, Map<String, Object> attrs,
            Map<String, Object> opts) {
        if (opts == null) {
            opts = Map.of();
        }
        attrs = attrs == null ? new LinkedHashMap<>() : attrs;
        Document doc = parent.getDocument();
        String doctype = doc.getDoctype();
        boolean book = "book".equals(doctype);
        int level = opts.get("level") != null ? Strings.toInt(opts.get("level")) : parent.getLevel() + 1;
        String sectname;
        boolean special = false;
        Object styleVal = attrs.remove("style");
        String style = Strings.valueOf(styleVal);
        if (style != null) {
            if (book && style.equals("abstract")) {
                sectname = "chapter";
                level = 1;
            } else {
                sectname = style;
                special = true;
                if (level == 0) {
                    level = 1;
                }
            }
        } else if (book) {
            sectname = level == 0 ? "part" : (level > 1 ? "section" : "chapter");
        } else if ("manpage".equals(doctype) && title.equalsIgnoreCase("synopsis")) {
            sectname = "synopsis";
            special = true;
        } else {
            sectname = "section";
        }
        Section sect = new Section(parent, level);
        sect.setTitle(title);
        sect.setSectionName(sectname);
        if (special) {
            sect.setSpecial(true);
            if (opts.containsKey("numbered") ? Boolean.TRUE.equals(opts.get("numbered")) : "appendix".equals(style)) {
                sect.setNumbered(true);
            } else if (!opts.containsKey("numbered") && doc.isAttribute("sectnums", "all")) {
                sect.setNumbered(book && level == 1 ? "chapter" : true);
            }
        } else if (level > 0) {
            if (opts.containsKey("numbered") ? Boolean.TRUE.equals(opts.get("numbered"))
                    : doc.hasAttribute("sectnums")) {
                sect.setNumbered(sect.isSpecial() ? parent instanceof Section ps && ps.isNumbered() : true);
            }
        } else if (opts.containsKey("numbered") ? Boolean.TRUE.equals(opts.get("numbered"))
                : book && doc.hasAttribute("partnums")) {
            sect.setNumbered(true);
        }
        Object id = attrs.get("id");
        if (Boolean.FALSE.equals(id)) {
            attrs.remove("id");
        } else {
            String resolvedId = id != null ? id.toString()
                    : (doc.hasAttribute("sectids") ? Section.generateId(sect.getTitle(), doc) : null);
            sect.setId(resolvedId);
            attrs.put("id", resolvedId);
        }
        sect.updateAttributes(attrs);
        return sect;
    }

    public Section createSection(AbstractBlock parent, String title, Map<String, Object> attrs) {
        return createSection(parent, title, attrs, Map.of());
    }

    /**
     * Creates a new Block node and links it to the specified parent.
     *
     * @param parent The parent block of this new block.
     * @param context The context of the block (e.g., paragraph).
     * @param source The source of the block as a String or List of lines (may be null).
     * @param attrs A Map of attributes to set on the block.
     * @param opts An optional Map of options (e.g., content_model, subs).
     * @return a Block node
     */
    public Block createBlock(AbstractBlock parent, String context, Object source, Map<String, Object> attrs,
            Map<String, Object> opts) {
        Map<String, Object> blockOpts = new LinkedHashMap<>();
        blockOpts.put("source", source);
        blockOpts.put("attributes", attrs);
        if (opts != null) {
            blockOpts.putAll(opts);
        }
        return new Block(parent, context, blockOpts);
    }

    public Block createBlock(AbstractBlock parent, String context, Object source, Map<String, Object> attrs) {
        return createBlock(parent, context, source, attrs, null);
    }

    /**
     * Creates a list node and links it to the specified parent.
     *
     * @param parent The parent Block (Block, Section, or Document) of this new list block.
     * @param context The list context (e.g., ulist, olist, colist, dlist)
     * @param attrs A Map of attributes to set on this list block
     * @return a List node with all properties properly initialized.
     */
    public org.asciidoctor.core.List createList(AbstractBlock parent, String context, Map<String, Object> attrs) {
        org.asciidoctor.core.List list = new org.asciidoctor.core.List(parent, context);
        if (attrs != null) {
            list.updateAttributes(attrs);
        }
        return list;
    }

    public org.asciidoctor.core.List createList(AbstractBlock parent, String context) {
        return createList(parent, context, null);
    }

    /**
     * Creates a list item node and links it to the specified parent.
     *
     * @param parent The parent List of this new list item block.
     * @param text The text of the list item.
     * @return a ListItem node with all properties properly initialized.
     */
    public ListItem createListItem(AbstractBlock parent, String text) {
        return new ListItem(parent, text);
    }

    public ListItem createListItem(AbstractBlock parent) {
        return new ListItem(parent, null);
    }

    /**
     * Creates an image block node and links it to the specified parent.
     *
     * @param parent The parent Block (Block, Section, or Document) of this new image block.
     * @param attrs A Map of attributes to control how the image block is built. Use the target attribute to set the
     *            source of the image. Use the alt attribute to specify an alternative text for the image.
     * @param opts An optional Map of options (default: {})
     * @return a Block node with all properties properly initialized.
     */
    public Block createImageBlock(AbstractBlock parent, Map<String, Object> attrs, Map<String, Object> opts) {
        Object target = attrs.get("target");
        if (target == null) {
            throw new IllegalArgumentException("Unable to create an image block, target attribute is required");
        }
        if (attrs.get("alt") == null) {
            String defaultAlt = Strings.tr(Helpers.basename(target.toString(), true), "_-", "  ");
            attrs.put("default-alt", defaultAlt);
            attrs.put("alt", defaultAlt);
        }
        Object title = attrs.containsKey("title") ? attrs.remove("title") : null;
        Block block = createBlock(parent, "image", null, attrs, opts);
        if (title != null) {
            block.setTitle(title.toString());
            block.assignCaption(Strings.valueOf(attrs.remove("caption")), "figure");
        }
        return block;
    }

    public Block createImageBlock(AbstractBlock parent, Map<String, Object> attrs) {
        return createImageBlock(parent, attrs, null);
    }

    /**
     * Creates an inline node and binds it to the specified parent.
     *
     * @param parent The parent Block of this new inline node.
     * @param context The context of the inline node to create (e.g., quoted, anchor, etc).
     * @param text The text of the inline node.
     * @param opts An optional Map of options (default: {}):
     *            <ul>
     *            <li>type - The subtype of the inline node context. For a quoted node, this can be strong, emphasis,
     *            monospaced, etc. For an anchor node, this can be xref, link, ref, etc.</li>
     *            <li>attributes - The attributes to set on the inline node. If the "subs" attribute is specified, the
     *            convert will apply the specified substitutions to the text of the inline node.</li>
     *            </ul>
     * @return an Inline node with all properties properly initialized.
     */
    public Inline createInline(AbstractNode parent, String context, String text, Map<String, Object> opts) {
        Map<String, Object> inlineOpts = new LinkedHashMap<>();
        if ("quoted".equals(context)) {
            inlineOpts.put("type", "unquoted");
        }
        if (opts != null) {
            inlineOpts.putAll(opts);
        }
        return new Inline(parent, context, text, inlineOpts);
    }

    public Inline createInline(AbstractNode parent, String context, String text) {
        return createInline(parent, context, text, null);
    }

    /**
     * Parses blocks in the content and attaches the block to the parent.
     *
     * @param parent the parent block into which the blocks are parsed
     * @param content the content to parse (a Reader, String or List of lines)
     * @param attributes the attributes to pass to each block (optional)
     * @return The parent node into which the blocks are parsed.
     */
    // QUESTION is parse_content the right method name? should we wrap in open block automatically?
    public AbstractBlock parseContent(AbstractBlock parent, Object content, Map<String, Object> attributes) {
        Reader reader = content instanceof Reader r ? r : new Reader(content);
        Parser.parseBlocks(reader, parent, attributes);
        return parent;
    }

    public AbstractBlock parseContent(AbstractBlock parent, Object content) {
        return parseContent(parent, content, null);
    }

    /**
     * Parses the attrlist String into a Map of attributes.
     *
     * @param block the current AbstractBlock or the parent AbstractBlock if there is no current block (used for
     *            applying subs)
     * @param attrlist the list of attributes as a String
     * @param opts an optional Map of options to control processing:
     *            <ul>
     *            <li>positional_attributes - a List of attribute names to map positional arguments to (optional,
     *            default: none)</li>
     *            <li>sub_attributes - enables attribute substitution on the attrlist argument (optional, default:
     *            false)</li>
     *            </ul>
     * @return a Map of parsed attributes
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseAttributes(AbstractNode block, String attrlist, Map<String, Object> opts) {
        if (attrlist == null || attrlist.isEmpty()) {
            return new LinkedHashMap<>();
        }
        if (opts == null) {
            opts = Map.of();
        }
        if (Boolean.TRUE.equals(opts.get("sub_attributes")) && attrlist.contains(ATTR_REF_HEAD)) {
            attrlist = block.subAttributes(attrlist);
        }
        Object positional = opts.get("positional_attributes");
        return new AttributeList(attrlist).parse(positional == null ? List.of() : (List<String>) positional);
    }

    public Map<String, Object> parseAttributes(AbstractNode block, String attrlist) {
        return parseAttributes(block, attrlist, null);
    }

    // TODO fill out remaining methods

    public Block createParagraph(AbstractBlock parent, Object source, Map<String, Object> attrs,
            Map<String, Object> opts) {
        return createBlock(parent, "paragraph", source, attrs, opts);
    }

    public Block createParagraph(AbstractBlock parent, Object source, Map<String, Object> attrs) {
        return createParagraph(parent, source, attrs, null);
    }

    public Block createOpenBlock(AbstractBlock parent, Object source, Map<String, Object> attrs,
            Map<String, Object> opts) {
        return createBlock(parent, "open", source, attrs, opts);
    }

    public Block createOpenBlock(AbstractBlock parent, Object source, Map<String, Object> attrs) {
        return createOpenBlock(parent, source, attrs, null);
    }

    public Block createExampleBlock(AbstractBlock parent, Object source, Map<String, Object> attrs,
            Map<String, Object> opts) {
        return createBlock(parent, "example", source, attrs, opts);
    }

    public Block createExampleBlock(AbstractBlock parent, Object source, Map<String, Object> attrs) {
        return createExampleBlock(parent, source, attrs, null);
    }

    public Block createPassBlock(AbstractBlock parent, Object source, Map<String, Object> attrs,
            Map<String, Object> opts) {
        return createBlock(parent, "pass", source, attrs, opts);
    }

    public Block createPassBlock(AbstractBlock parent, Object source, Map<String, Object> attrs) {
        return createPassBlock(parent, source, attrs, null);
    }

    public Block createListingBlock(AbstractBlock parent, Object source, Map<String, Object> attrs,
            Map<String, Object> opts) {
        return createBlock(parent, "listing", source, attrs, opts);
    }

    public Block createListingBlock(AbstractBlock parent, Object source, Map<String, Object> attrs) {
        return createListingBlock(parent, source, attrs, null);
    }

    public Block createLiteralBlock(AbstractBlock parent, Object source, Map<String, Object> attrs,
            Map<String, Object> opts) {
        return createBlock(parent, "literal", source, attrs, opts);
    }

    public Block createLiteralBlock(AbstractBlock parent, Object source, Map<String, Object> attrs) {
        return createLiteralBlock(parent, source, attrs, null);
    }

    /**
     * Creates an inline node (the name used by AsciidoctorJ for {@link #createInline(AbstractNode, String, String,
     * Map)}).
     *
     * @param attributes the attributes of the inline node
     * @param options the options (e.g., type, target, id)
     */
    public Inline createPhraseNode(AbstractNode parent, String context, String text, Map<String, Object> attributes,
            Map<String, Object> options) {
        Map<String, Object> opts = new LinkedHashMap<>(options);
        opts.put("attributes", new LinkedHashMap<>(attributes));
        return createInline(parent, context, text, opts);
    }

    public Inline createPhraseNode(AbstractNode parent, String context, String text, Map<String, Object> attributes) {
        return createPhraseNode(parent, context, text, attributes, Map.of());
    }

    public Inline createPhraseNode(AbstractNode parent, String context, String text) {
        return createPhraseNode(parent, context, text, Map.of(), Map.of());
    }

    public Inline createPhraseNode(AbstractNode parent, String context, List<String> text,
            Map<String, Object> attributes, Map<String, Object> options) {
        return createPhraseNode(parent, context, String.join(Asciidoctor.LF, text), attributes, options);
    }

    /**
     * Logs the record using the logger of the processor (for compatibility with AsciidoctorJ).
     */
    public void log(LogRecord logRecord) {
        Object message = logRecord.cursor() == null ? logRecord.message()
                : Logging.messageWithContext(logRecord.message(), logRecord.cursor());
        LoggerManager.getLogger().add(logRecord.severity(), message);
    }

    public Inline createAnchor(AbstractNode parent, String text, Map<String, Object> opts) {
        return createInline(parent, "anchor", text, opts);
    }

    public Inline createInlinePass(AbstractNode parent, String text, Map<String, Object> opts) {
        return createInline(parent, "quoted", text, opts);
    }

    public Inline createAnchor(AbstractNode parent, String text) {
        return createAnchor(parent, text, new LinkedHashMap<>());
    }

    public Inline createInlinePass(AbstractNode parent, String text) {
        return createInlinePass(parent, text, new LinkedHashMap<>());
    }
}
