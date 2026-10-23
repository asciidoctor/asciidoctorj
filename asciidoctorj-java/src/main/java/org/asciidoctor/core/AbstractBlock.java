package org.asciidoctor.core;

import static org.asciidoctor.core.Asciidoctor.CAPTION_ATTRIBUTE_NAMES;
import static org.asciidoctor.core.Asciidoctor.LF;
import static org.asciidoctor.core.Asciidoctor.ORDERED_LIST_KEYWORDS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.asciidoctor.core.converter.Converter;

import org.asciidoctor.core.Reader.Cursor;

/**
 * The base class of all block-level nodes (i.e., nodes that can be appended to the document tree).
 */
public abstract class AbstractBlock extends AbstractNode {

    /**
     * The verdict returned by a filter passed to {@link #findBy(Selector, Function)}.
     */
    public enum FilterVerdict {
        /** The node is accepted and node traversal continues. */
        ACCEPT,
        /** The node is skipped, but its children are still visited. */
        SKIP,
        /** The node and all its descendants are rejected. */
        REJECT,
        /** The node is accepted, but its descendants are rejected. */
        PRUNE,
        /** The traversal stops (the node is not accepted). */
        STOP
    }

    /**
     * A selector used to find block-level nodes in the document tree.
     *
     * @param context the context of the nodes to find (e.g., section) or null to match any context
     * @param style the style of the nodes to find or null to match any style
     * @param role a role of the nodes to find or null to match any role
     * @param id the id of the node to find or null to match any id
     * @param traverseDocuments whether to traverse the nested documents of AsciiDoc table cells
     */
    public record Selector(String context, String style, String role, String id, boolean traverseDocuments) {
        public static Selector any() {
            return new Selector(null, null, null, null, false);
        }

        public static Selector context(String context) {
            return new Selector(context, null, null, null, false);
        }

        public Selector withContext(String value) {
            return new Selector(value, style, role, id, traverseDocuments);
        }

        public Selector withStyle(String value) {
            return new Selector(context, value, role, id, traverseDocuments);
        }

        public Selector withRole(String value) {
            return new Selector(context, style, value, id, traverseDocuments);
        }

        public Selector withId(String value) {
            return new Selector(context, style, role, value, traverseDocuments);
        }

        public Selector withTraverseDocuments(boolean value) {
            return new Selector(context, style, role, id, value);
        }
    }

    private static final class StopIteration extends RuntimeException {
        StopIteration() {
            super(null, null, false, false);
        }
    }

    protected List<AbstractBlock> blocks = new ArrayList<>();
    protected String caption;
    protected ContentModel contentModel = ContentModel.COMPOUND;
    protected Integer level;
    protected String numeral;
    protected Cursor sourceLocation;
    protected String style;
    protected List<Substitution> subs = new ArrayList<>();
    protected List<Substitution> defaultSubs;
    protected String title;
    protected String convertedTitle;
    protected int nextSectionIndex;
    protected int nextSectionOrdinal;

    protected AbstractBlock(AbstractNode parent, String context, Map<String, Object> attributes) {
        super(parent, context, attributes);
        if ("document".equals(context) || "section".equals(context)) {
            level = 0;
            nextSectionIndex = 0;
            nextSectionOrdinal = 1;
        } else if (parent instanceof AbstractBlock parentBlock) {
            level = parentBlock.getLevel();
        } else {
            level = null;
        }
    }

    protected AbstractBlock(AbstractNode parent, String context) {
        this(parent, context, null);
    }

    @Override
    public boolean isBlock() {
        return true;
    }

    @Override
    public boolean isInline() {
        return false;
    }

    /**
     * @return the List of child blocks for this block. Only applies if content model is compound.
     */
    public List<AbstractBlock> getBlocks() {
        return blocks;
    }

    /**
     * @return the type of content this block accepts and how it should be converted
     */
    public ContentModel getContentModel() {
        return contentModel;
    }

    public void setContentModel(ContentModel contentModel) {
        this.contentModel = contentModel;
    }

    /**
     * @return the level of this Section or the level of the Section to which this block belongs.
     */
    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    /**
     * @return the String numeral of this block (if section, relative to parent, otherwise absolute). Only assigned to
     *         section if automatic section numbering is enabled. Only assigned to formal block (block with title) if
     *         corresponding caption attribute is present.
     */
    public String getNumeral() {
        return numeral;
    }

    public void setNumeral(String numeral) {
        this.numeral = numeral;
    }

    /**
     * @return the location in the AsciiDoc source where this block begins. Tracking source location is not enabled by
     *         default, and is controlled by the sourcemap option.
     */
    public Cursor getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(Cursor sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    /**
     * @return the String style (block type qualifier) for this block.
     */
    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * @return the substitutions to be applied to content in this block.
     */
    public List<Substitution> getSubs() {
        return subs;
    }

    /**
     * @return the source file where this block started
     */
    public String getFile() {
        return sourceLocation == null ? null : sourceLocation.getFile();
    }

    /**
     * @return the source line number where this block started
     */
    public Integer getLineNumber() {
        return sourceLocation == null ? null : sourceLocation.getLineNumber();
    }

    /**
     * Get the converted String content for this Block. If the block has child blocks, the content method should cause
     * them to be converted and returned as content that can be included in the parent block's template.
     */
    @Override
    public String convert() {
        document.playbackAttributes(attributes);
        return Converter.asString(getConverter().convert(this));
    }

    /**
     * Get the converted result of the child blocks by converting the children appropriate to content model that this
     * block supports.
     */
    public String getContent() {
        List<String> result = new ArrayList<>();
        for (AbstractBlock block : blocks) {
            result.add(block.convert());
        }
        return String.join(LF, result);
    }

    /**
     * Update the context of this block.
     *
     * <p>This method changes the context of this block. It also updates the node name accordingly.
     *
     * @param context the context String context to assign to this block
     */
    public void setContext(String context) {
        this.context = context;
        this.nodeName = context;
    }

    /**
     * Append a content block to this block's list of blocks.
     *
     * <pre>
     *   block = new Block(parent, "preamble", Map.of("content_model", ContentModel.COMPOUND));
     *
     *   block.append(new Block(block, "paragraph", Map.of("source", "p1")));
     *   block.append(new Block(block, "paragraph", Map.of("source", "p2")));
     *   block.hasBlocks();
     *   // => true
     *   block.getBlocks().size();
     *   // => 2
     * </pre>
     *
     * @param block The new child block.
     * @return The parent Block
     */
    public AbstractBlock append(AbstractBlock block) {
        if (block.getParent() != this) {
            block.setParent(this);
        }
        blocks.add(block);
        return this;
    }

    /**
     * Determine whether this Block contains block content.
     *
     * @return A Boolean indicating whether this Block has block content
     */
    public boolean hasBlocks() {
        return !blocks.isEmpty();
    }

    /**
     * Check whether this block has any child Section objects.
     *
     * <p>Acts an an abstract method that always returns false unless this block is an instance of Document or Section.
     * Both Document and Section provide overrides for this method.
     *
     * @return false
     */
    public boolean hasSections() {
        return false;
    }

    /**
     * Walk the document tree and find all block-level nodes that match the specified selector (context, style, id,
     * role, and/or custom filter).
     *
     * <p>If a filter is given, it's applied as a supplemental filter. If the filter returns ACCEPT, the node is accepted
     * and node traversal continues. If the filter returns SKIP (or null), the node is skipped, but its children are
     * still visited. If the filter returns REJECT, the node and all its descendants are rejected. If the filter returns
     * PRUNE, the node is accepted, but its descendants are rejected. If the filter returns STOP, the node is accepted
     * and the traversal stops. If no selector or filter is supplied, all block-level nodes in the tree are returned.
     *
     * <pre>
     *   doc.findBy(Selector.context("section"))
     *   // => [Section { level: 0, title: "Hello, AsciiDoc!", blocks: 0 }, Section { level: 1, ... }]
     *
     *   doc.findBy(Selector.context("section"), section -> section.getLevel() == 1 ? ACCEPT : SKIP)
     *   // => [Section { level: 1, title: "First Section", blocks: 1 }]
     * </pre>
     *
     * @return A List of block-level nodes that match the filter or an empty List if no matches are found
     */
    public List<AbstractBlock> findBy(Selector selector, Function<AbstractBlock, FilterVerdict> filter) {
        List<AbstractBlock> result = new ArrayList<>();
        try {
            findByInternal(selector == null ? Selector.any() : selector, result, filter);
        } catch (StopIteration e) {
            // stop
        }
        return result;
    }

    public List<AbstractBlock> findBy(Selector selector) {
        return findBy(selector, null);
    }

    public List<AbstractBlock> findBy() {
        return findBy(Selector.any(), null);
    }

    /**
     * Walk the document tree and find all block-level nodes that match the specified selector, specified as a Map (for
     * compatibility with AsciidoctorJ). The supported keys are context, style, role, id and traverse_documents (a
     * leading colon in a key or value is permitted, e.g., :context or :paragraph).
     */
    public List<AbstractBlock> findBy(Map<?, ?> selector) {
        Selector result = Selector.any();
        for (Map.Entry<?, ?> entry : selector.entrySet()) {
            String key = String.valueOf(entry.getKey()).replaceFirst("^:", "");
            Object value = entry.getValue();
            String str = value == null ? null : String.valueOf(value).replaceFirst("^:", "");
            result = switch (key) {
                case "context" -> result.withContext(str);
                case "style" -> result.withStyle(str);
                case "role" -> result.withRole(str);
                case "id" -> result.withId(str);
                case "traverse_documents" -> result.withTraverseDocuments(Boolean.parseBoolean(str));
                default -> throw new IllegalArgumentException("Unknown selector: " + key);
            };
        }
        return findBy(result);
    }

    public List<AbstractBlock> findBy(Function<AbstractBlock, FilterVerdict> filter) {
        return findBy(Selector.any(), filter);
    }

    /**
     * Move to the next adjacent block in document order. If the current block is the last item in a list, this method
     * will return the following sibling of the list block.
     */
    public AbstractBlock getNextAdjacentBlock() {
        if ("document".equals(context)) {
            return null;
        }
        AbstractBlock p = (AbstractBlock) parent;
        if ("dlist".equals(p.getContext()) && "list_item".equals(context)) {
            List<AbstractBlock> items = p.getBlocks();
            int idx = -1;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) instanceof DescriptionListEntry entry
                        && (entry.getTerms().contains(this) || entry.getDescription() == this)) {
                    idx = i;
                    break;
                }
            }
            AbstractBlock sib = idx + 1 < items.size() ? items.get(idx + 1) : null;
            return sib != null ? sib : p.getNextAdjacentBlock();
        }
        int idx = p.getBlocks().indexOf(this);
        AbstractBlock sib = idx >= 0 && idx + 1 < p.getBlocks().size() ? p.getBlocks().get(idx + 1) : null;
        return sib != null ? sib : p.getNextAdjacentBlock();
    }

    /**
     * Get the List of child Section objects.
     *
     * <p>Only applies to Document and Section instances.
     *
     * @return a List of Section objects
     */
    public List<Section> getSections() {
        List<Section> result = new ArrayList<>();
        for (AbstractBlock block : blocks) {
            if ("section".equals(block.getContext()) && block instanceof Section section) {
                result.add(section);
            }
        }
        return result;
    }

    /**
     * Returns the converted alt text for this block image.
     *
     * @return the String value of the alt attribute with XML special character and replacement substitutions applied.
     */
    public String getAlt() {
        Object textVal = attributes.get("alt");
        if (textVal != null) {
            String text = textVal.toString();
            if (text.equals(attributes.get("default-alt"))) {
                return subSpecialchars(text);
            }
            text = subSpecialchars(text);
            return Rx.REPLACEABLE_TEXT_RX.matcher(text).find() ? subReplacements(text) : text;
        }
        return "";
    }

    /**
     * Gets the caption for this block.
     *
     * <p>This method routes the deprecated use of the caption method on an admonition block to the textlabel
     * attribute.
     *
     * @return the String caption for this block (or the value of the textlabel attribute if this is an admonition
     *         block).
     */
    public String getCaption() {
        return "admonition".equals(context) ? Strings.valueOf(attributes.get("textlabel")) : caption;
    }

    /**
     * Set the caption for this block.
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * Convenience method that returns the interpreted title of the Block with the caption prepended.
     *
     * <p>Concatenates the value of this Block's caption instance variable and the return value of this Block's title
     * method. No space is added between the two values. If the Block does not have a caption, the interpreted title is
     * returned.
     *
     * @return the converted String title prefixed with the caption, or just the converted String title if no caption
     *         is set
     */
    public String getCaptionedTitle() {
        return (caption == null ? "" : caption) + java.util.Objects.toString(getTitle(), "");
    }

    /**
     * Retrieve the list marker keyword for the specified list type.
     *
     * <p>For use in the HTML type attribute.
     *
     * @param listType the type of list; default to the style if not specified
     * @return the single-character String keyword that represents the marker for the specified list type
     */
    public String getListMarkerKeyword(String listType) {
        String key = listType != null ? listType : style;
        return key == null ? null : ORDERED_LIST_KEYWORDS.get(key);
    }

    public String getListMarkerKeyword() {
        return getListMarkerKeyword(null);
    }

    /**
     * Get the String title of this Block with title substitutions applied.
     *
     * <p>The following substitutions are applied to block and section titles:
     *
     * <p>specialcharacters, quotes, replacements, macros, attributes and post_replacements
     *
     * <pre>
     *   block.setTitle("Foo 3^ # {two-colons} Bar(1)");
     *   block.getTitle();
     *   // => "Foo 3^ # :: Bar(1)"
     * </pre>
     *
     * @return the converted String title for this Block, or null if the source title is not set
     */
    public String getTitle() {
        // prevent substitutions from being applied to title multiple times
        if (convertedTitle == null && title != null) {
            convertedTitle = applyTitleSubs(title);
        }
        return convertedTitle;
    }

    /**
     * A convenience method that checks whether the title of this block is defined.
     *
     * @return a Boolean indicating whether this block has a title.
     */
    public boolean hasTitle() {
        return title != null;
    }

    /**
     * Set the String block title.
     */
    public void setTitle(String title) {
        this.convertedTitle = null;
        this.title = title;
    }

    /**
     * @return the source (unconverted) String title of this block
     */
    public String getRawTitle() {
        return title;
    }

    /**
     * A convenience method that checks whether the specified substitution is enabled for this block.
     *
     * @param name The substitution
     * @return A Boolean indicating whether the specified substitution is enabled for this block
     */
    public boolean hasSub(Substitution name) {
        return subs != null && subs.contains(name);
    }

    /**
     * Remove a substitution from this block.
     *
     * @param sub The substitution
     */
    public void removeSub(Substitution sub) {
        if (subs != null) {
            subs.remove(sub);
        }
    }

    // the substitutions identified by name (for compatibility with AsciidoctorJ)

    /**
     * @return the names of the substitutions to be applied to content in this block (e.g., specialcharacters)
     */
    public List<String> getSubstitutions() {
        return subs == null ? List.of() : subs.stream().map(Substitution::toString).toList();
    }

    public boolean isSubstitutionEnabled(String substitution) {
        return hasSub(Substitution.forKey(substitution));
    }

    public void removeSubstitution(String substitution) {
        removeSub(Substitution.forKey(substitution));
    }

    public void addSubstitution(String substitution) {
        subs.add(requireSubstitution(substitution));
    }

    public void prependSubstitution(String substitution) {
        subs.add(0, requireSubstitution(substitution));
    }

    public void setSubstitutions(String... substitutions) {
        subs.clear();
        for (String substitution : substitutions) {
            subs.add(requireSubstitution(substitution));
        }
    }

    private static Substitution requireSubstitution(String name) {
        Substitution substitution = Substitution.forKey(name);
        if (substitution == null) {
            throw new IllegalArgumentException("Unknown substitution: " + name);
        }
        return substitution;
    }

    /**
     * Generate cross reference text (xreftext) that can be used to refer to this block.
     *
     * <p>Use the explicit reftext for this block, if specified, retrieved from the {@link #getReftext()} method.
     * Otherwise, if this is a section or captioned block (a block with both a title and caption), generate the xreftext
     * according to the value of the xrefstyle argument (e.g., full, short). This logic may leverage the
     * {@link #subQuotes(String)} method to apply formatting to the text. If this is not a captioned block, return the
     * title, if present, or null otherwise.
     *
     * @param xrefstyle An optional String that specifies the style to use to format the xreftext ('full', 'short', or
     *            'basic') (default: null).
     * @return the generated String xreftext used to refer to this block or null if there isn't sufficient information
     *         to generate one.
     */
    @Override
    public String getXreftext(String xrefstyle) {
        String val = getReftext();
        if (val != null && !val.isEmpty()) {
            return val;
        // NOTE xrefstyle only applies to blocks with a title and a caption or number
        } else if (xrefstyle != null && title != null && !Strings.isNullOrEmpty(caption)) {
            String captionAttrName;
            Object prefix;
            switch (xrefstyle) {
                case "full" -> {
                    String quotedTitle = subPlaceholder(subQuotes(document.isCompatMode() ? "``%s''" : "\"`%s`\""),
                            getTitle());
                    if (numeral != null && (captionAttrName = CAPTION_ATTRIBUTE_NAMES.get(context)) != null
                            && (prefix = document.getAttributes().get(captionAttrName)) != null) {
                        return prefix + " " + numeral + ", " + quotedTitle;
                    }
                    return Strings.chomp(caption, ". ") + ", " + quotedTitle;
                }
                case "short" -> {
                    if (numeral != null && (captionAttrName = CAPTION_ATTRIBUTE_NAMES.get(context)) != null
                            && (prefix = document.getAttributes().get(captionAttrName)) != null) {
                        return prefix + " " + numeral;
                    }
                    return Strings.chomp(caption, ". ");
                }
                default -> { // 'basic'
                    return getTitle();
                }
            }
        }
        return getTitle();
    }


    /**
     * Generate and assign caption to block if not already assigned.
     *
     * <p>If the block has a title and a caption prefix is available for this block, then build a caption from this
     * information, assign it a number and store it to the caption attribute on the block.
     *
     * <p>If a caption has already been assigned to this block, do nothing.
     *
     * <p>The parts of a complete caption are: &lt;prefix&gt; &lt;number&gt;. &lt;title&gt; This partial caption
     * represents the part the precedes the title.
     *
     * @param value The String caption to assign to this block or null to use document attribute.
     * @param captionContext The String context to use when resolving caption-related attributes. If not provided, the
     *            name of the context for this block is used. Only certain contexts allow the caption to be looked up.
     *            (default: context)
     */
    public void assignCaption(String value, String captionContext) {
        if (caption != null || title == null) {
            return;
        }
        if (value != null) {
            caption = value;
            return;
        }
        Object docCaption = document.getAttributes().get("caption");
        if (docCaption != null) {
            caption = docCaption.toString();
            return;
        }
        String attrName = CAPTION_ATTRIBUTE_NAMES.get(captionContext == null ? context : captionContext);
        Object prefix;
        if (attrName != null && (prefix = document.getAttributes().get(attrName)) != null) {
            numeral = String.valueOf(document.incrementAndStoreCounter(
                    (captionContext == null ? context : captionContext) + "-number", this));
            caption = prefix + " " + numeral + ". ";
        }
    }

    public void assignCaption(String value) {
        assignCaption(value, context);
    }

    /**
     * Internal: Assign the next index (0-based) and numeral (1-based) to the section. If the section is an appendix,
     * the numeral is a letter (starting with A). This method also assigns the appendix caption.
     *
     * @param section The section to which to assign the next index and numeral.
     */
    public void assignNumeral(Section section) {
        section.setIndex(nextSectionIndex);
        nextSectionIndex++;
        Object like = section.getNumbered();
        if (like != null && !Boolean.FALSE.equals(like)) {
            String sectname = section.getSectionName();
            if ("appendix".equals(sectname)) {
                section.setNumeral(String.valueOf(document.counter("appendix-number", "A")));
                Object appendixCaption = document.getAttributes().get("appendix-caption");
                section.setCaption(appendixCaption != null ? appendixCaption + " " + section.getNumeral() + ": "
                        : section.getNumeral() + ". ");
            // NOTE currently chapters in a book doctype are sequential even for multi-part books (see #979)
            } else if ("chapter".equals(sectname) || "chapter".equals(like)) {
                section.setNumeral(String.valueOf(document.counter("chapter-number", 1)));
            } else {
                section.setNumeral("part".equals(sectname) ? Helpers.intToRoman(nextSectionOrdinal)
                        : String.valueOf(nextSectionOrdinal));
                nextSectionOrdinal++;
            }
        }
    }

    /**
     * Internal: Reassign the section indexes.
     *
     * <p>Walk the descendents of the current Document or Section and reassign the section 0-based index value to each
     * Section as it appears in document order.
     *
     * <p>IMPORTANT You must invoke this method on a node after removing child sections or else the internal counters
     * will be off.
     */
    public void reindexSections() {
        nextSectionIndex = 0;
        nextSectionOrdinal = 1;
        for (AbstractBlock block : blocks) {
            if ("section".equals(block.getContext()) && block instanceof Section section) {
                assignNumeral(section);
                section.reindexSections();
            }
        }
    }

    /**
     * Internal: Commit the requested substitutions to this block.
     *
     * <p>Looks for an attribute named "subs". If present, resolves substitutions from the value of that attribute and
     * assigns them to the subs property on this block. Otherwise, uses the substitutions assigned to the defaultSubs
     * property, if specified, or selects a default set of substitutions based on the content model of the block.
     */
    public void commitSubs() {
        List<Substitution> defaultSubs = this.defaultSubs;
        if (defaultSubs == null) {
            switch (contentModel) {
                case SIMPLE -> defaultSubs = NORMAL_SUBS;
                case VERBATIM -> defaultSubs = "verse".equals(context) ? NORMAL_SUBS : VERBATIM_SUBS;
                // TODO make pass subs a compliance setting; AsciiDoc.py performs :attributes and :macros on a pass
                // block
                case RAW -> defaultSubs = "stem".equals(context) ? BASIC_SUBS : NO_SUBS;
                default -> {
                    return;
                }
            }
        }

        Object customSubs = attributes.get("subs");
        if (customSubs != null) {
            List<Substitution> resolved = resolveBlockSubs(customSubs.toString(), defaultSubs, context);
            subs = resolved != null ? new ArrayList<>(resolved) : new ArrayList<>();
        } else {
            subs = new ArrayList<>(defaultSubs);
        }

        // QUESTION delegate this logic to a method?
        int idx;
        if ("listing".equals(context) && "source".equals(style) && document.getSyntaxHighlighter() != null
                && document.getSyntaxHighlighter().isHighlight()
                && (idx = subs.indexOf(Substitution.SPECIAL_CHARACTERS)) >= 0) {
            subs.set(idx, Substitution.HIGHLIGHT);
        }
    }

    /**
     * Internal: Performs the work for findBy, but does not handle the StopIteration exception.
     */
    protected List<AbstractBlock> findByInternal(Selector selector, List<AbstractBlock> result,
            Function<AbstractBlock, FilterVerdict> filter) {
        String contextSelector = selector.context();
        boolean anyContext = contextSelector == null;
        String styleSelector = selector.style();
        String roleSelector = selector.role();
        String idSelector = selector.id();
        if ((anyContext || contextSelector.equals(context)) && (styleSelector == null || styleSelector.equals(style))
                && (roleSelector == null || hasRole(roleSelector)) && (idSelector == null || idSelector.equals(id))) {
            if (filter != null) {
                FilterVerdict verdict = filter.apply(this);
                if (verdict != null && verdict != FilterVerdict.SKIP) {
                    switch (verdict) {
                        case PRUNE -> {
                            result.add(this);
                            if (idSelector != null) {
                                throw new StopIteration();
                            }
                            return result;
                        }
                        case REJECT -> {
                            if (idSelector != null) {
                                throw new StopIteration();
                            }
                            return result;
                        }
                        case STOP -> throw new StopIteration();
                        default -> {
                            result.add(this);
                            if (idSelector != null) {
                                throw new StopIteration();
                            }
                        }
                    }
                } else if (idSelector != null) {
                    throw new StopIteration();
                }
            } else {
                result.add(this);
                if (idSelector != null) {
                    throw new StopIteration();
                }
            }
        }
        switch (context) {
            case "document" -> {
                if (!"document".equals(contextSelector)) {
                    Document doc = (Document) this;
                    // process document header as a section, if present
                    if (doc.hasHeader() && (anyContext || "section".equals(contextSelector))) {
                        doc.getHeader().findByInternal(selector, result, filter);
                    }
                    for (AbstractBlock b : blocks) {
                        if ("section".equals(contextSelector) && !"section".equals(b.getContext())) {
                            continue; // optimization
                        }
                        b.findByInternal(selector, result, filter);
                    }
                }
            }
            case "dlist" -> {
                // dlist has different structure than other blocks
                if (anyContext || !"section".equals(contextSelector)) { // optimization
                    // NOTE the description of a dlist item can be null, so we have to check
                    for (AbstractBlock b : blocks) {
                        if (b instanceof DescriptionListEntry entry) {
                            for (ListItem term : entry.getTerms()) {
                                term.findByInternal(selector, result, filter);
                            }
                            if (entry.getDescription() != null) {
                                entry.getDescription().findByInternal(selector, result, filter);
                            }
                        } else {
                            b.findByInternal(selector, result, filter);
                        }
                    }
                }
            }
            case "table" -> {
                Table table = (Table) this;
                if (selector.traverseDocuments()) {
                    for (Table.Row row : table.getRows().getHead()) {
                        for (Table.Cell cell : row) {
                            cell.findByInternal(selector, result, filter);
                        }
                    }
                    Selector innerSelector = "inner_document".equals(contextSelector) ? selector.withContext("document")
                            : selector;
                    List<Table.Row> rows = new ArrayList<>(table.getRows().getBody());
                    rows.addAll(table.getRows().getFoot());
                    for (Table.Row row : rows) {
                        for (Table.Cell cell : row) {
                            cell.findByInternal(innerSelector, result, filter);
                            if ("asciidoc".equals(cell.getStyle())) {
                                cell.getInnerDocument().findByInternal(innerSelector, result, filter);
                            }
                        }
                    }
                } else {
                    for (Table.Row row : table.getRows().all()) {
                        for (Table.Cell cell : row) {
                            cell.findByInternal(selector, result, filter);
                        }
                    }
                }
            }
            default -> {
                for (AbstractBlock b : blocks) {
                    if ("section".equals(contextSelector) && !"section".equals(b.getContext())) {
                        continue; // optimization
                    }
                    b.findByInternal(selector, result, filter);
                }
            }
        }
        return result;
    }
}
