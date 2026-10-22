package org.asciidoctor.core;

import static org.asciidoctor.core.Asciidoctor.ADMONITION_STYLES;
import static org.asciidoctor.core.Asciidoctor.ADMONITION_STYLE_HEADS;
import static org.asciidoctor.core.Asciidoctor.ATTR_REF_HEAD;
import static org.asciidoctor.core.Asciidoctor.CAPTION_ATTRIBUTE_NAMES;
import static org.asciidoctor.core.Asciidoctor.DELIMITED_BLOCKS;
import static org.asciidoctor.core.Asciidoctor.DELIMITED_BLOCK_HEADS;
import static org.asciidoctor.core.Asciidoctor.DELIMITED_BLOCK_TAILS;
import static org.asciidoctor.core.Asciidoctor.HARD_LINE_BREAK;
import static org.asciidoctor.core.Asciidoctor.HYBRID_LAYOUT_BREAK_CHARS;
import static org.asciidoctor.core.Asciidoctor.LAYOUT_BREAK_CHARS;
import static org.asciidoctor.core.Asciidoctor.LF;
import static org.asciidoctor.core.Asciidoctor.LINE_CONTINUATION;
import static org.asciidoctor.core.Asciidoctor.LINE_CONTINUATION_LEGACY;
import static org.asciidoctor.core.Asciidoctor.LIST_CONTINUATION;
import static org.asciidoctor.core.Asciidoctor.MARKDOWN_THEMATIC_BREAK_CHARS;
import static org.asciidoctor.core.Asciidoctor.NESTABLE_LIST_CONTEXTS;
import static org.asciidoctor.core.Asciidoctor.ORDERED_LIST_STYLES;
import static org.asciidoctor.core.Asciidoctor.PARAGRAPH_STYLES;
import static org.asciidoctor.core.Asciidoctor.SETEXT_SECTION_LEVELS;
import static org.asciidoctor.core.Asciidoctor.TAB;
import static org.asciidoctor.core.Asciidoctor.VERBATIM_STYLES;
import static org.asciidoctor.core.Logging.messageWithContext;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.core.Asciidoctor.DelimitedBlockInfo;
import org.asciidoctor.core.Reader.Cursor;
import org.asciidoctor.core.Reader.ReadOptions;
import org.asciidoctor.core.Substitutors.ParseOption;
import org.asciidoctor.core.extension.BlockMacroProcessor;
import org.asciidoctor.core.extension.BlockProcessor;
import org.asciidoctor.core.extension.ProcessorExtension;
import org.asciidoctor.core.extension.Registry;

/**
 * Internal: Methods to parse lines of AsciiDoc into an object hierarchy representing the structure of the document.
 * All methods are static methods and should be invoked from the Parser class. The main entry point is
 * {@link #nextBlock}. No Parser instances shall be discovered running around. (Any attempt to instantiate a Parser
 * will be futile).
 *
 * <p>The object hierarchy created by the Parser consists of zero or more Section and Block objects. Section objects
 * may be nested and a Section object contains zero or more Block objects. Block objects may be nested, but may only
 * contain other Block objects. Block objects which represent lists may contain zero or more ListItem objects.
 *
 * <pre>
 *   // Create a Reader for the AsciiDoc lines and retrieve the next block from it.
 *   // Parser.nextBlock requires a parent, so we begin by instantiating an empty Document.
 *
 *   doc = new Document();
 *   reader = new Reader(lines);
 *   block = Parser.nextBlock(reader, doc);
 *   block.getClass();
 *   // => Block
 * </pre>
 */
public final class Parser {

    /**
     * Describes a matched delimited block.
     *
     * @param context the context of the delimited block
     * @param masq the styles the block can masquerade as
     * @param tip the delimiter tip (i.e., the first 2-4 characters)
     * @param terminator the delimiter line that terminates the block
     */
    public record BlockMatchData(String context, Set<String> masq, String tip, String terminator) {
    }

    /**
     * Options that control how the next block is parsed.
     *
     * @param textOnly indicates that the parser is only looking for text content
     * @param listType indicates this block will be attached to a list item in a list of the specified type
     * @param parseMetadata whether to parse the block metadata lines (default: true)
     */
    public record BlockOptions(boolean textOnly, String listType, boolean parseMetadata) {
        public static final BlockOptions DEFAULT = new BlockOptions(false, null, true);
    }

    /**
     * The result of parsing a section title.
     */
    public record SectionTitle(String id, String reftext, String title, int level, boolean atx) {
    }

    /**
     * The result of storing an attribute: the resolved attribute name (minus the ! indicator) and value.
     */
    public record AttributeEntryResult(String name, String value) {
    }

    /**
     * The result of parsing a section: the section (null if the parent is the document) and the orphaned attributes.
     */
    public record SectionResult(Section section, Map<String, Object> attributes) {
    }

    /**
     * The result of parsing a cell spec: the spec Map (null if not at a cell boundary) and the remaining text.
     */
    public record CellSpec(Map<String, Object> spec, String rest) {
    }

    /**
     * The result of resolving an ordered list marker: the 0-index marker and the implicit style.
     */
    public record OrderedListMarker(String marker, String style) {
    }

    // String for matching tab character
    private static final String TAB_CHAR = TAB;

    // Regexp for leading tab indentation
    private static final Pattern TAB_INDENT_RX = Rx.compile("^\\t+");

    private static final Predicate<String> START_OF_BLOCK_PROC = l ->
            (l.startsWith("[") && Rx.matches(Rx.BLOCK_ATTRIBUTE_LINE_RX, l)) || isDelimitedBlock(l);

    private static final Predicate<String> START_OF_LIST_PROC = l -> Rx.matches(Rx.ANY_LIST_RX, l);

    private static final Predicate<String> START_OF_BLOCK_OR_LIST_PROC = l -> isDelimitedBlock(l)
            || (l.startsWith("[") && Rx.matches(Rx.BLOCK_ATTRIBUTE_LINE_RX, l)) || Rx.matches(Rx.ANY_LIST_RX, l);

    private static final Set<String> AUTHOR_KEYS = new java.util.LinkedHashSet<>(
            List.of("author", "authorinitials", "firstname", "middlename", "lastname", "email"));

    // NOTE these are identity-compared marker strings
    @SuppressWarnings("StringOperationCanBeSimplified")
    private static final String LIST_CONTINUATION_PLACEHOLDER = new String("");

    @SuppressWarnings("StringOperationCanBeSimplified")
    private static final String LIST_CONTINUATION_STRING = new String(LIST_CONTINUATION);

    /** Internal: A Map mapping horizontal alignment abbreviations to alignments. */
    private static final Map<String, String> TABLE_CELL_HORZ_ALIGNMENTS = Map.of(
            "<", "left",
            ">", "right",
            "^", "center");

    /** Internal: A Map mapping vertical alignment abbreviations to alignments. */
    private static final Map<String, String> TABLE_CELL_VERT_ALIGNMENTS = Map.of(
            "<", "top",
            ">", "bottom",
            "^", "middle");

    /** Internal: A Map mapping styles abbreviations to styles that can be applied to a table cell. */
    private static final Map<String, String> TABLE_CELL_STYLES = Map.of(
            "d", "none",
            "s", "strong",
            "e", "emphasis",
            "m", "monospaced",
            "h", "header",
            "l", "literal",
            "a", "asciidoc");

    // Hide the default constructor to make sure this class doesn't get instantiated.
    private Parser() {
    }

    private static Logger logger() {
        return LoggerManager.getLogger();
    }

    private static boolean isMarker(String line) {
        return line == LIST_CONTINUATION_PLACEHOLDER || line == LIST_CONTINUATION_STRING;
    }

    /**
     * Parses AsciiDoc source read from the Reader into the Document.
     *
     * <p>This method is the main entry-point into the Parser when parsing a full document. It first looks for and, if
     * found, processes the document title. It then proceeds to iterate through the lines in the Reader, parsing the
     * document into nested Sections and Blocks.
     *
     * @param reader the Reader holding the source lines of the document
     * @param document the empty Document into which the lines will be parsed
     * @param headerOnly whether to only parse the document header
     * @return the Document object
     */
    public static Document parse(Reader reader, Document document, boolean headerOnly) {
        Map<String, Object> blockAttributes = parseDocumentHeader(reader, document, headerOnly);

        // NOTE don't use a postfix conditional here as it's known to confuse JRuby in certain circumstances
        if (!headerOnly) {
            while (reader.hasMoreLines()) {
                SectionResult result = nextSection(reader, document, blockAttributes);
                blockAttributes = result.attributes();
                if (result.section() != null) {
                    document.assignNumeral(result.section());
                    document.getBlocks().add(result.section());
                }
            }
        }

        return document;
    }

    public static Document parse(Reader reader, Document document) {
        return parse(reader, document, false);
    }

    /**
     * Parses the document header of the AsciiDoc source read from the Reader.
     *
     * <p>Reads the AsciiDoc source from the Reader until the end of the document header is reached. The Document object
     * is populated with information from the header (document title, document attributes, etc). The document
     * attributes are then saved to establish a save point to which to rollback after parsing is complete.
     *
     * <p>This method assumes that there are no blank lines at the start of the document, which are automatically
     * removed by the reader.
     *
     * @return the Map of orphan block attributes captured above the header
     */
    public static Map<String, Object> parseDocumentHeader(Reader reader, Document document, boolean headerOnly) {
        // capture lines of block-level metadata and plow away comment lines that precede first block
        Map<String, Object> blockAttrs = reader.skipBlankLines() != null
                ? parseBlockMetadataLines(reader, document, new LinkedHashMap<>(), false)
                : new LinkedHashMap<>();
        Map<String, Object> docAttrs = document.getAttributes();

        // special cases, block style or title is not allowed above document title,
        // carry attributes over to the document body
        boolean implicitDoctitle = isNextLineDoctitle(reader, blockAttrs, docAttrs.get("leveloffset"));
        if (implicitDoctitle && (blockAttrs.get("title") != null || blockAttrs.get("style") != null)) {
            docAttrs.put("authorcount", 0);
            return document.finalizeHeader(blockAttrs, false);
        }

        // yep, document title logic in AsciiDoc is just insanity
        // definitely an area for spec refinement

        String doctitleAttrVal = null;
        Object val = docAttrs.get("doctitle");
        if (!Strings.isNullOrEmpty(val)) {
            document.setTitle(doctitleAttrVal = val.toString());
        }

        // if the first line is the document title, add a header to the document and parse the header metadata
        if (implicitDoctitle) {
            Cursor sourceLocation = document.isSourcemap() ? reader.cursor() : null;
            SectionTitle sectionTitle = parseSectionTitle(reader, document, null);
            document.setId(sectionTitle.id());
            String l0SectionTitle = sectionTitle.title();
            if (doctitleAttrVal != null) {
                // NOTE doctitle attribute (set above or below implicit doctitle) overrides implicit doctitle
                l0SectionTitle = null;
            } else {
                document.setTitle(l0SectionTitle);
                doctitleAttrVal = document.subSpecialchars(l0SectionTitle);
                docAttrs.put("doctitle", doctitleAttrVal);
                if (doctitleAttrVal.contains(ATTR_REF_HEAD)) {
                    // QUESTION should we defer substituting attributes until the end of the header? or should we
                    // substitute again if necessary?
                    doctitleAttrVal = document.subAttributes(doctitleAttrVal, "skip", null);
                    docAttrs.put("doctitle", doctitleAttrVal);
                }
            }
            if (sourceLocation != null) {
                document.getHeader().setSourceLocation(sourceLocation);
            }
            // default to compat-mode if document has setext doctitle
            if (!(sectionTitle.atx() || document.isAttributeLocked("compat-mode"))) {
                docAttrs.put("compat-mode", "");
            }
            Object separator = blockAttrs.get("separator");
            if (separator != null) {
                if (!document.isAttributeLocked("title-separator")) {
                    docAttrs.put("title-separator", separator);
                }
            }
            String docId = Strings.valueOf(blockAttrs.get("id"));
            if (docId != null) {
                document.setId(docId);
            } else {
                docId = document.getId();
            }
            Object role = blockAttrs.get("role");
            if (role != null) {
                docAttrs.put("role", role);
            }
            Object reftext = blockAttrs.get("reftext");
            if (reftext != null) {
                docAttrs.put("reftext", reftext);
            }
            blockAttrs.clear();
            Set<String> modifiedAttrs = document.getAttributesModified();
            modifiedAttrs.remove("doctitle");
            parseHeaderMetadata(reader, document, false);
            if (modifiedAttrs.contains("doctitle")) {
                Object newVal = docAttrs.get("doctitle");
                if (Strings.isNullOrEmpty(newVal) || newVal.equals(doctitleAttrVal)) {
                    docAttrs.put("doctitle", doctitleAttrVal);
                } else {
                    document.setTitle(newVal.toString());
                }
            } else if (l0SectionTitle == null) {
                modifiedAttrs.add("doctitle");
            }
            if (docId != null) {
                document.registerRef(docId, document);
            }
        } else {
            Object author = docAttrs.get("author");
            if (author != null) {
                Map<String, Object> authorMetadata = processAuthors(author, true, false);
                if (docAttrs.get("authorinitials") != null) {
                    authorMetadata.remove("authorinitials");
                }
                docAttrs.putAll(authorMetadata);
            } else if ((author = docAttrs.get("authors")) != null) {
                Map<String, Object> authorMetadata = processAuthors(author, true, true);
                docAttrs.putAll(authorMetadata);
            } else {
                docAttrs.put("authorcount", 0);
            }
        }

        // parse title and consume name section of manpage document
        if ("manpage".equals(document.getDoctype())) {
            parseManpageHeader(reader, document, blockAttrs, headerOnly);
        }

        // NOTE block_attrs are the block-level attributes (not document attributes) that
        // precede the first line of content (document title, first section or first block)
        return document.finalizeHeader(blockAttrs);
    }

    /**
     * Parses the manpage header of the AsciiDoc source read from the Reader.
     */
    public static void parseManpageHeader(Reader reader, Document document, Map<String, Object> blockAttributes,
            boolean headerOnly) {
        Map<String, Object> docAttrs = document.getAttributes();
        String manvolnum;
        Matcher m = Rx.match(Rx.MANPAGE_TITLE_VOLNUM_RX, Strings.valueOf(docAttrs.get("doctitle")));
        if (m != null) {
            docAttrs.put("manvolnum", manvolnum = m.group(2));
            String mantitle = m.group(1);
            docAttrs.put("mantitle", (mantitle.contains(ATTR_REF_HEAD) ? document.subAttributes(mantitle) : mantitle)
                    .toLowerCase());
        } else {
            logger().error(messageWithContext("non-conforming manpage title", reader.cursorAtLine(1)));
            // provide sensible fallbacks
            Object fallback = docAttrs.get("doctitle");
            if (fallback == null) {
                fallback = docAttrs.get("docname");
            }
            docAttrs.put("mantitle", fallback == null ? "command" : fallback);
            docAttrs.put("manvolnum", manvolnum = "1");
        }
        Object manname = docAttrs.get("manname");
        if (manname != null && docAttrs.get("manpurpose") != null) {
            if (docAttrs.get("manname-title") == null) {
                docAttrs.put("manname-title", "Name");
            }
            docAttrs.put("mannames", new ArrayList<>(List.of(manname.toString())));
            if ("manpage".equals(document.getBackend())) {
                docAttrs.put("docname", manname);
                docAttrs.put("outfilesuffix", "." + manvolnum);
            }
        } else if (headerOnly) {
            // done
        } else {
            reader.skipBlankLines();
            reader.save();
            blockAttributes.putAll(parseBlockMetadataLines(reader, document, new LinkedHashMap<>(), false));
            String errorMsg = null;
            Integer nameSectionLevel = isNextLineSection(reader, new LinkedHashMap<>());
            if (nameSectionLevel != null) {
                if (nameSectionLevel == 1) {
                    Section nameSection = initializeSection(reader, document, new LinkedHashMap<>());
                    List<String> nameSectionLines = reader.readLinesUntil(ReadOptions.create().breakOnBlankLines()
                            .skipLineComments());
                    List<String> stripped = new ArrayList<>();
                    for (String l : nameSectionLines) {
                        stripped.add(Strings.lstrip(l));
                    }
                    String nameSectionBuffer = String.join(" ", stripped);
                    Matcher nm = Rx.match(Rx.MANPAGE_NAME_PURPOSE_RX, nameSectionBuffer);
                    if (nm != null) {
                        String mannameVal = nm.group(1);
                        if (mannameVal.contains(ATTR_REF_HEAD)) {
                            mannameVal = document.subAttributes(mannameVal);
                        }
                        List<String> mannames;
                        if (mannameVal.contains(",")) {
                            mannames = new ArrayList<>();
                            for (String n : Strings.split(mannameVal, ",")) {
                                mannames.add(Strings.lstrip(n));
                            }
                            mannameVal = mannames.get(0);
                        } else {
                            mannames = new ArrayList<>(List.of(mannameVal));
                        }
                        String manpurpose = nm.group(2);
                        if (manpurpose.contains(ATTR_REF_HEAD)) {
                            manpurpose = document.subAttributes(manpurpose);
                        }
                        if (docAttrs.get("manname-title") == null) {
                            docAttrs.put("manname-title", nameSection.getTitle());
                        }
                        if (nameSection.getId() != null) {
                            docAttrs.put("manname-id", nameSection.getId());
                        }
                        docAttrs.put("manname", mannameVal);
                        docAttrs.put("mannames", mannames);
                        docAttrs.put("manpurpose", manpurpose);
                        if ("manpage".equals(document.getBackend())) {
                            docAttrs.put("docname", mannameVal);
                            docAttrs.put("outfilesuffix", "." + manvolnum);
                        }
                    } else {
                        errorMsg = "non-conforming name section body";
                    }
                } else {
                    errorMsg = "name section must be at level 1";
                }
            } else {
                errorMsg = "name section expected";
            }
            if (errorMsg != null) {
                reader.restoreSave();
                logger().error(messageWithContext(errorMsg, reader.cursor()));
                Object docname = docAttrs.get("docname");
                String fallback = docname == null ? "command" : docname.toString();
                docAttrs.put("manname", fallback);
                docAttrs.put("mannames", new ArrayList<>(List.of(fallback)));
                if ("manpage".equals(document.getBackend())) {
                    docAttrs.put("docname", fallback);
                    docAttrs.put("outfilesuffix", "." + manvolnum);
                }
            } else {
                reader.discardSave();
            }
        }
    }

    /**
     * Return the next section from the Reader.
     *
     * <p>This method process block metadata, content and subsections for this section and returns the Section object
     * and any orphaned attributes.
     *
     * <p>If the parent is a Document and has a header (document title), then this method will put any non-section
     * blocks at the start of document into a preamble Block. If there are no such blocks, the preamble is dropped.
     *
     * <p>Since we are reading line-by-line, there's a chance that metadata that should be associated with the
     * following block gets consumed. To deal with this case, the method returns a running Map of "orphaned" attributes
     * that get passed to the next Section or Block.
     *
     * @param reader the source Reader
     * @param parent the parent Section or Document of this new section
     * @param attributes a Map of metadata that was left orphaned from the previous Section.
     * @return the Section and Map of orphaned attributes
     */
    public static SectionResult nextSection(Reader reader, AbstractBlock parent, Map<String, Object> attributes) {
        Block preamble = null;
        AbstractBlock intro = null;
        boolean part = false;
        boolean book;
        Document document;
        AbstractBlock section;
        int currentLevel;
        Integer expectedNextLevel;
        Integer expectedNextLevelAlt = null;
        String sectname = null;

        // check if we are at the start of processing the document
        // NOTE we could drop a hint in the attributes to indicate
        // that we are at a section title (so we don't have to check)
        boolean hasHeader = false;
        if ("document".equals(parent.getContext()) && parent.getBlocks().isEmpty()
                && ((hasHeader = ((Document) parent).hasHeader()) || attributes.remove("invalid-header") != null
                        || isNextLineSection(reader, attributes) == null)) {
            document = (Document) parent;
            book = "book".equals(document.getDoctype());
            if (hasHeader || (book && !"abstract".equals(attributes.get("1")))) {
                preamble = new Block(parent, "preamble", Options.of("content_model", ContentModel.COMPOUND));
                intro = preamble;
                if (book && parent.hasAttribute("preface-title")) {
                    preamble.setTitle(parent.getStringAttribute("preface-title"));
                }
                parent.getBlocks().add(preamble);
            }
            section = parent;
            currentLevel = 0;
            if (parent.getAttributes().containsKey("fragment")) {
                expectedNextLevel = -1;
            // small tweak to allow subsequent level-0 sections for book doctype
            } else if (book) {
                expectedNextLevel = 1;
                expectedNextLevelAlt = 0;
            } else {
                expectedNextLevel = 1;
            }
        } else {
            document = parent.getDocument();
            book = "book".equals(document.getDoctype());
            Section newSection = initializeSection(reader, parent, attributes);
            section = newSection;
            // clear attributes except for title attribute, which must be carried over to next content block
            Object title = attributes.get("title");
            attributes = new LinkedHashMap<>();
            if (title != null) {
                attributes.put("title", title);
            }
            expectedNextLevel = (currentLevel = newSection.getLevel()) + 1;
            if (currentLevel == 0) {
                part = book;
            } else if (currentLevel == 1 && newSection.isSpecial()) {
                // NOTE technically preface sections are only permitted in the book doctype
                sectname = newSection.getSectionName();
                if (!("appendix".equals(sectname) || "preface".equals(sectname) || "abstract".equals(sectname))) {
                    expectedNextLevel = null;
                }
            }
        }

        reader.skipBlankLines();

        // Parse lines belonging to this section and its subsections until we
        // reach the end of this section level
        //
        // 1. first look for metadata thingies (anchor, attribute list, block title line, etc)
        // 2. then look for a section, recurse if found
        // 3. then process blocks
        //
        // We have to parse all the metadata lines before continuing with the loop,
        // otherwise subsequent metadata lines get interpreted as block content
        while (reader.hasMoreLines()) {
            parseBlockMetadataLines(reader, document, attributes, false);
            Integer nextLevel = isNextLineSection(reader, attributes);
            if (nextLevel != null) {
                if (document.hasAttribute("leveloffset")) {
                    nextLevel += Strings.toInt(document.getAttribute("leveloffset"));
                    if (nextLevel < 0) {
                        nextLevel = 0;
                    }
                }
                if (nextLevel > currentLevel) {
                    if (expectedNextLevel != null) {
                        if (!(nextLevel.equals(expectedNextLevel)
                                || (expectedNextLevelAlt != null && nextLevel.equals(expectedNextLevelAlt))
                                || expectedNextLevel < 0)) {
                            String expectedCondition = expectedNextLevelAlt != null
                                    ? "expected levels " + expectedNextLevelAlt + " or " + expectedNextLevel
                                    : "expected level " + expectedNextLevel;
                            logger().warn(messageWithContext("section title out of sequence: " + expectedCondition
                                    + ", got level " + nextLevel, reader.cursor()));
                        }
                    } else {
                        logger().error(messageWithContext(sectname + " sections do not support nested sections",
                                reader.cursor()));
                    }
                    SectionResult result = nextSection(reader, section, attributes);
                    attributes = result.attributes();
                    section.assignNumeral(result.section());
                    section.getBlocks().add(result.section());
                } else if (nextLevel == 0 && section == document) {
                    if (!book) {
                        logger().error(messageWithContext("level 0 sections can only be used when doctype is book",
                                reader.cursor()));
                    }
                    SectionResult result = nextSection(reader, section, attributes);
                    attributes = result.attributes();
                    section.assignNumeral(result.section());
                    section.getBlocks().add(result.section());
                } else {
                    // close this section (and break out of the nesting) to begin a new one
                    break;
                }
            } else {
                // just take one block or else we run the risk of overrunning section boundaries
                Cursor blockCursor = reader.cursor();
                AbstractBlock newBlock = nextBlock(reader, intro != null ? intro : section, attributes,
                        new BlockOptions(false, null, false));
                if (newBlock != null) {
                    // REVIEW this may be doing too much
                    if (part) {
                        if (!section.hasBlocks()) {
                            // if this not a [partintro] open block, enclose it in a [partintro] open block
                            if (!"partintro".equals(newBlock.getStyle())) {
                                // if this is already a normal open block, simply add the partintro style
                                if ("open".equals(newBlock.getStyle()) && "open".equals(newBlock.getContext())) {
                                    newBlock.setStyle("partintro");
                                } else {
                                    intro = new Block(section, "open", Options.of("content_model",
                                            ContentModel.COMPOUND));
                                    newBlock.setParent(intro);
                                    intro.setStyle("partintro");
                                    section.getBlocks().add(intro);
                                }
                            // if this is a [partintro] paragraph, convert it to a [partintro] open block w/ single
                            // paragraph
                            } else if (newBlock.getContentModel() == ContentModel.SIMPLE) {
                                Block newBlockAsBlock = (Block) newBlock;
                                newBlock.setContentModel(ContentModel.COMPOUND);
                                newBlock.append(new Block(newBlock, "paragraph", Options.of("source",
                                        new ArrayList<>(newBlockAsBlock.getLines()), "subs",
                                        new ArrayList<>(newBlock.getSubs()))));
                                newBlockAsBlock.getLines().clear();
                                newBlock.getSubs().clear();
                            }
                        } else if (section.getBlocks().size() == 1) {
                            AbstractBlock firstBlock = section.getBlocks().get(0);
                            // open the [partintro] open block for appending
                            if (intro == null && firstBlock.getContentModel() == ContentModel.COMPOUND) {
                                logger().error(messageWithContext("illegal block content outside of partintro block",
                                        blockCursor));
                            // rebuild [partintro] paragraph as an open block
                            } else if (firstBlock.getContentModel() != ContentModel.COMPOUND) {
                                intro = new Block(section, "open", Options.of("content_model", ContentModel.COMPOUND));
                                newBlock.setParent(intro);
                                intro.setStyle("partintro");
                                if ("partintro".equals(firstBlock.getStyle())) {
                                    firstBlock.setContext("paragraph");
                                    firstBlock.setStyle(null);
                                }
                                section.getBlocks().remove(0);
                                intro.append(firstBlock);
                                section.getBlocks().add(intro);
                            }
                        }
                    }

                    (intro != null ? intro : section).getBlocks().add(newBlock);
                    attributes.clear();
                }
            }

            if (reader.skipBlankLines() == null) {
                break;
            }
        }

        if (part) {
            if (!(section.hasBlocks() && "section".equals(section.getBlocks().get(section.getBlocks().size() - 1).getContext()))) {
                logger().error(messageWithContext(
                        "invalid part, must have at least one section (e.g., chapter, appendix, etc.)",
                        reader.cursor()));
            }
        // NOTE we could try to avoid creating a preamble in the first place, though
        // that would require reworking assumptions in next_section since the preamble
        // is treated like an untitled section
        } else if (preamble != null) { // implies parent == document
            if (preamble.hasBlocks()) {
                if (book || document.getBlocks().size() > 1 || !Compliance.isUnwrapStandalonePreamble()) {
                    if (document.isSourcemap()) {
                        preamble.setSourceLocation(preamble.getBlocks().get(0).getSourceLocation());
                    }
                // unwrap standalone preamble (i.e., document has no sections) except for books, if permissible
                } else {
                    document.getBlocks().remove(0);
                    while (!preamble.getBlocks().isEmpty()) {
                        document.append(preamble.getBlocks().remove(0));
                    }
                }
            // drop the preamble if it has no content
            } else {
                document.getBlocks().remove(0);
            }
        }

        // The attributes returned here are orphaned attributes that fall at the end
        // of a section that need to get transferred to the next section
        // see "trailing block attributes transfer to the following section" in
        // test/attributes_test.rb for an example
        return new SectionResult(section == parent ? null : (Section) section, new LinkedHashMap<>(attributes));
    }

    public static SectionResult nextSection(Reader reader, AbstractBlock parent) {
        return nextSection(reader, parent, new LinkedHashMap<>());
    }

    /**
     * Parse and return the next Block at the Reader's current location.
     *
     * <p>This method begins by skipping over blank lines to find the start of the next block (paragraph, block macro,
     * or delimited block). If a block is found, that block is parsed, initialized as a Block object, and returned.
     * Otherwise, the method returns null.
     *
     * <p>Regular expressions from the Rx class are used to match block boundaries. The ensuing lines are then processed
     * according to the content model.
     *
     * @param reader The Reader from which to retrieve the next Block.
     * @param parent The Document, Section or Block to which the next Block belongs.
     * @param attributes A Map of attributes that will become the attributes associated with the parsed Block (default:
     *            {}).
     * @param options the options to control parsing
     * @return a Block object built from the parsed content of the processed lines, or null if no block is found.
     */
    @SuppressWarnings("unchecked")
    public static AbstractBlock nextBlock(Reader reader, AbstractBlock parent, Map<String, Object> attributes,
            BlockOptions options) {
        if (attributes == null) {
            attributes = new LinkedHashMap<>();
        }
        if (options == null) {
            options = BlockOptions.DEFAULT;
        }
        // skip ahead to the block content; bail if we've reached the end of the reader
        Integer skipped = reader.skipBlankLines();
        if (skipped == null) {
            return null;
        }

        // check for option to find list item text only
        // if skipped a line, assume a list continuation was
        // used and block content is acceptable
        boolean textOnly = options.textOnly();
        if (textOnly && skipped > 0) {
            textOnly = false;
        }

        Document document = parent.getDocument();

        if (options.parseMetadata()) {
            // read lines until there are no more metadata lines to read; note that :text_only option impacts parsing
            // rules
            while (parseBlockMetadataLine(reader, document, attributes, textOnly)) {
                // discard the line just processed
                reader.shift();
                // QUESTION should we clear the attributes? no known cases when it's necessary
                if (reader.skipBlankLines() == null) {
                    return null;
                }
            }
        }

        Registry extensions = document.getExtensions();
        boolean blockExtensions = false;
        boolean blockMacroExtensions = false;
        if (extensions != null) {
            blockExtensions = extensions.hasBlocks();
            blockMacroExtensions = extensions.hasBlockMacros();
        }

        // QUESTION should we introduce a parsing context object?
        reader.mark();
        String thisLine = reader.readLine();
        Map<String, Object> docAttrs = document.getAttributes();
        String style = Strings.valueOf(attributes.get("1"));
        AbstractBlock block = null;
        String blockContext = null;
        String cloakedContext = null;
        String terminator = null;
        ProcessorExtension<BlockProcessor> blockExtension = null;

        BlockMatchData delimitedBlock = isDelimitedBlock(thisLine, true);
        if (delimitedBlock != null) {
            blockContext = cloakedContext = delimitedBlock.context();
            terminator = delimitedBlock.terminator();
            if (style != null) {
                if (!style.equals(blockContext)) {
                    if (delimitedBlock.masq().contains(style)) {
                        blockContext = style;
                    } else if (delimitedBlock.masq().contains("admonition") && ADMONITION_STYLES.contains(style)) {
                        blockContext = "admonition";
                    } else if (blockExtensions && extensions.isRegisteredForBlock(style, blockContext) != null) {
                        blockContext = style;
                    } else {
                        if (logger().isDebug()) {
                            logger().debug(messageWithContext("unknown style for " + blockContext + " block: " + style,
                                    reader.cursorAtMark()));
                        }
                        style = blockContext;
                    }
                }
            } else {
                style = blockContext;
                attributes.put("style", style);
            }
        }

        // this loop is used for flow control; it only executes once, and only when delimited_block is not set
        // break once a block is found or at end of loop
        // returns null if the line should be dropped
        if (delimitedBlock == null) {
            flow: {
                // process lines verbatim
                if (style != null && Compliance.isStrictVerbatimParagraphs() && VERBATIM_STYLES.contains(style)) {
                    blockContext = style;
                    cloakedContext = "paragraph";
                    reader.unshiftLine(thisLine);
                    // advance to block parsing =>
                    break flow;
                }

                boolean indented;
                String ch0 = null;
                boolean mdSyntax = false;
                // process lines normally
                if (textOnly) {
                    indented = thisLine.startsWith(" ") || thisLine.startsWith(TAB_CHAR);
                } else {
                    // NOTE move this declaration up if we need it when text_only is false
                    mdSyntax = Compliance.isMarkdownSyntax();
                    if (thisLine.startsWith(" ")) {
                        indented = true;
                        ch0 = " ";
                        // QUESTION should we test line length?
                        String stripped = Strings.lstrip(thisLine);
                        if (mdSyntax && !stripped.isEmpty()
                                && MARKDOWN_THEMATIC_BREAK_CHARS.containsKey(stripped.charAt(0))
                                && Rx.matches(Rx.MARKDOWN_THEMATIC_BREAK_RX, thisLine)) {
                            // NOTE we're letting break lines (horizontal rule, page_break, etc) have attributes
                            block = new Block(parent, "thematic_break", Options.of("content_model",
                                    ContentModel.EMPTY));
                            break flow;
                        }
                    } else if (thisLine.startsWith(TAB_CHAR)) {
                        indented = true;
                        ch0 = TAB_CHAR;
                    } else {
                        indented = false;
                        ch0 = Strings.chr(thisLine);
                        Map<Character, String> layoutBreakChars = mdSyntax ? HYBRID_LAYOUT_BREAK_CHARS
                                : LAYOUT_BREAK_CHARS;
                        int ll = thisLine.length();
                        if (!ch0.isEmpty() && layoutBreakChars.containsKey(ch0.charAt(0))
                                && (mdSyntax ? Rx.matches(Rx.EXT_LAYOUT_BREAK_RX, thisLine)
                                        : isUniform(thisLine, ch0.charAt(0), ll) && ll > 2)) {
                            // NOTE we're letting break lines (horizontal rule, page_break, etc) have attributes
                            block = new Block(parent, layoutBreakChars.get(ch0.charAt(0)), Options.of("content_model",
                                    ContentModel.EMPTY));
                            break flow;
                        // NOTE very rare that a text-only line will end in ] (e.g., inline macro), so check that
                        // first
                        } else if (thisLine.endsWith("]") && thisLine.contains("::")) {
                            Matcher m;
                            if ((ch0.equals("i") || thisLine.startsWith("video:") || thisLine.startsWith("audio:"))
                                    && (m = Rx.match(Rx.BLOCK_MEDIA_MACRO_RX, thisLine)) != null) {
                                String blkCtx = m.group(1);
                                String target = m.group(2);
                                String blkAttrs = m.group(3);
                                block = new Block(parent, blkCtx, Options.of("content_model", ContentModel.EMPTY));
                                if (blkAttrs != null) {
                                    List<String> posattrs = switch (blkCtx) {
                                        case "video" -> List.of("poster", "width", "height");
                                        case "audio" -> List.of();
                                        default -> List.of("alt", "width", "height"); // image
                                    };
                                    block.parseAttributes(blkAttrs, posattrs, EnumSet.of(ParseOption.SUB_INPUT),
                                            attributes);
                                }
                                // style doesn't have special meaning for media macros
                                attributes.remove("style");
                                if (target.contains(ATTR_REF_HEAD)) {
                                    String expandedTarget = block.subAttributes(target);
                                    if (expandedTarget.isEmpty() && "drop-line".equals(attributeMissing(docAttrs))
                                            && block.subAttributes(target + " ", "drop-line", "ignore").isEmpty()) {
                                        attributes.clear();
                                        return null;
                                    }
                                    target = expandedTarget;
                                }
                                if (blkCtx.equals("image")) {
                                    document.register("images", target);
                                    if (attributes.get("imagesdir") == null) {
                                        attributes.put("imagesdir", docAttrs.get("imagesdir"));
                                    }
                                    // NOTE style is the value of the first positional attribute in the block attribute
                                    // line
                                    if (attributes.get("alt") == null) {
                                        if (style != null) {
                                            attributes.put("alt", style);
                                        } else {
                                            String defaultAlt = Strings.tr(Helpers.basename(target, true), "_-", "  ");
                                            attributes.put("default-alt", defaultAlt);
                                            attributes.put("alt", defaultAlt);
                                        }
                                    }
                                    Object scaledwidth = attributes.remove("scaledwidth");
                                    if (!Strings.isNullOrEmpty(scaledwidth)) {
                                        // NOTE assume % units if not specified
                                        String sw = scaledwidth.toString();
                                        attributes.put("scaledwidth", Rx.matches(Rx.TRAILING_DIGITS_RX, sw) ? sw + "%"
                                                : sw);
                                    }
                                    if (attributes.get("title") != null) {
                                        block.setTitle(Strings.valueOf(attributes.remove("title")));
                                        block.assignCaption(Strings.valueOf(attributes.remove("caption")), "figure");
                                    }
                                }
                                attributes.put("target", target);
                                break flow;
                            } else if (ch0.equals("t") && thisLine.startsWith("toc:")
                                    && (m = Rx.match(Rx.BLOCK_TOC_MACRO_RX, thisLine)) != null) {
                                block = new Block(parent, "toc", Options.of("content_model", ContentModel.EMPTY));
                                if (m.group(1) != null) {
                                    block.parseAttributes(m.group(1), List.of(), EnumSet.of(ParseOption.SUB_INPUT),
                                            attributes);
                                }
                                break flow;
                            } else {
                                Matcher cm = Rx.match(Rx.CUSTOM_BLOCK_MACRO_RX, thisLine);
                                ProcessorExtension<BlockMacroProcessor> extension = null;
                                boolean reportUnknownBlockMacro = false;
                                boolean process;
                                if (blockMacroExtensions) {
                                    process = cm != null && ((extension = extensions.isRegisteredForBlockMacro(
                                            cm.group(1))) != null || (reportUnknownBlockMacro = logger().isDebug()));
                                } else {
                                    process = logger().isDebug() && (reportUnknownBlockMacro = cm != null);
                                }
                                if (process) {
                                    if (reportUnknownBlockMacro) {
                                        logger().debug(messageWithContext("unknown name for block macro: " + cm.group(1),
                                                reader.cursorAtMark()));
                                    } else {
                                        String content = cm.group(3);
                                        String target = cm.group(2);
                                        if (target.contains(ATTR_REF_HEAD)) {
                                            String expandedTarget = parent.subAttributes(target);
                                            if (expandedTarget.isEmpty()
                                                    && "drop-line".equals(attributeMissing(docAttrs))
                                                    && parent.subAttributes(target + " ", "drop-line", "ignore")
                                                            .isEmpty()) {
                                                attributes.clear();
                                                return null;
                                            }
                                            target = expandedTarget;
                                        }
                                        Map<String, Object> extConfig = extension.getConfig();
                                        if (ContentModel.of(extConfig.get("content_model")) == ContentModel.ATTRIBUTES) {
                                            if (content != null) {
                                                document.parseAttributes(content,
                                                        Substitutors.positionalAttrs(extConfig),
                                                        EnumSet.of(ParseOption.SUB_INPUT), attributes);
                                            }
                                        } else {
                                            attributes.put("text", content == null ? "" : content);
                                        }
                                        if (extConfig.get("default_attrs") instanceof Map<?, ?> defaultAttrs) {
                                            for (Map.Entry<?, ?> entry : defaultAttrs.entrySet()) {
                                                attributes.putIfAbsent(String.valueOf(entry.getKey()),
                                                        entry.getValue());
                                            }
                                        }
                                        block = extension.getInstance().process(parent, target, attributes);
                                        if (block != null && block != parent) {
                                            attributes.clear();
                                            attributes.putAll(block.getAttributes());
                                            break flow;
                                        }
                                        attributes.clear();
                                        return null;
                                    }
                                }
                            }
                        }
                    }
                }

                // haven't found anything yet, continue
                Matcher m;
                if (ch0 == null) {
                    ch0 = Strings.chr(thisLine);
                }
                if (!indented && ch0.equals("<") && (m = Rx.match(Rx.CALLOUT_LIST_RX, thisLine)) != null) {
                    reader.unshiftLine(thisLine);
                    block = parseCalloutList(reader, m, parent, document.getCallouts());
                    attributes.put("style", "arabic");
                    break flow;
                } else if (Rx.matches(Rx.UNORDERED_LIST_RX, thisLine)) {
                    reader.unshiftLine(thisLine);
                    if (style == null && parent instanceof Section section
                            && "bibliography".equals(section.getSectionName())) {
                        attributes.put("style", style = "bibliography");
                    }
                    block = parseList(reader, "ulist", parent, style);
                    break flow;
                } else if (Rx.matches(Rx.ORDERED_LIST_RX, thisLine)) {
                    reader.unshiftLine(thisLine);
                    block = parseList(reader, "olist", parent, style, attributes.remove("start"));
                    if (block.getStyle() != null) {
                        attributes.put("style", block.getStyle());
                    }
                    break flow;
                } else if ((thisLine.contains("::") || thisLine.contains(";;"))
                        && (m = Rx.match(Rx.DESCRIPTION_LIST_RX, thisLine)) != null) {
                    reader.unshiftLine(thisLine);
                    block = parseDescriptionList(reader, m, parent);
                    break flow;
                } else if (("float".equals(style) || "discrete".equals(style))
                        && (Compliance.isUnderlineStyleSectionTitles()
                                ? isSectionTitle(thisLine, reader.peekLine()) != null
                                : !indented && isAtxSectionTitle(thisLine) != null)) {
                    reader.unshiftLine(thisLine);
                    SectionTitle floatTitle = parseSectionTitle(reader, document,
                            Strings.valueOf(attributes.get("id")));
                    if (floatTitle.reftext() != null) {
                        attributes.put("reftext", floatTitle.reftext());
                    }
                    block = new Block(parent, "floating_title", Options.of("content_model", ContentModel.EMPTY));
                    block.setTitle(floatTitle.title());
                    attributes.remove("title");
                    block.setId(floatTitle.id() != null ? floatTitle.id()
                            : (docAttrs.containsKey("sectids") ? Section.generateId(block.getTitle(), document) : null));
                    block.setLevel(floatTitle.level());
                    break flow;
                // FIXME create another set for "passthrough" styles
                // FIXME make this more DRY!
                } else if (style != null && !style.equals("normal")) {
                    if (PARAGRAPH_STYLES.contains(style)) {
                        blockContext = style;
                        cloakedContext = "paragraph";
                        reader.unshiftLine(thisLine);
                        // advance to block parsing =>
                        break flow;
                    } else if (ADMONITION_STYLES.contains(style)) {
                        blockContext = "admonition";
                        cloakedContext = "paragraph";
                        reader.unshiftLine(thisLine);
                        // advance to block parsing =>
                        break flow;
                    } else if (blockExtensions && extensions.isRegisteredForBlock(style, "paragraph") != null) {
                        blockContext = style;
                        cloakedContext = "paragraph";
                        reader.unshiftLine(thisLine);
                        // advance to block parsing =>
                        break flow;
                    } else {
                        if (logger().isDebug()) {
                            logger().debug(messageWithContext("unknown style for paragraph: " + style,
                                    reader.cursorAtMark()));
                        }
                        style = null;
                        // continue to process paragraph
                    }
                }

                reader.unshiftLine(thisLine);

                // a literal paragraph: contiguous lines starting with at least one whitespace character
                // NOTE style can only be null or "normal" at this point
                if (indented && style == null) {
                    String contentAdjacent = skipped == 0 ? options.listType() : null;
                    List<String> lines = readParagraphLines(reader, contentAdjacent != null,
                            ReadOptions.create().skipLineComments(textOnly));
                    adjustIndentation(lines);
                    if (textOnly || "dlist".equals(contentAdjacent)) {
                        // this block gets folded into the list item text
                        block = new Block(parent, "paragraph", Options.of("content_model", ContentModel.SIMPLE,
                                "source", lines, "attributes", attributes));
                    } else {
                        block = new Block(parent, "literal", Options.of("content_model", ContentModel.VERBATIM,
                                "source", lines, "attributes", attributes));
                    }
                // a normal paragraph: contiguous non-blank/non-continuation lines (left-indented or normal style)
                } else {
                    List<String> lines = readParagraphLines(reader, skipped == 0 && options.listType() != null,
                            ReadOptions.create().skipLineComments());
                    Matcher am;
                    // NOTE don't check indented here since it's extremely rare
                    //if text_only || indented
                    if (textOnly) {
                        // if [normal] is used over an indented paragraph, shift content to left margin
                        // QUESTION do we even need to shift since whitespace is normalized by XML in this case?
                        if (indented && "normal".equals(style)) {
                            adjustIndentation(lines);
                        }
                        block = new Block(parent, "paragraph", Options.of("content_model", ContentModel.SIMPLE,
                                "source", lines, "attributes", attributes));
                    } else if (ADMONITION_STYLE_HEADS.contains(ch0) && thisLine.contains(":")
                            && (am = Rx.match(Rx.ADMONITION_PARAGRAPH_RX, thisLine)) != null) {
                        lines.set(0, thisLine.substring(am.end())); // string after match
                        String admonitionName = am.group(1).toLowerCase();
                        attributes.put("style", am.group(1));
                        attributes.put("name", admonitionName);
                        Object caption = attributes.remove("caption");
                        attributes.put("textlabel", caption != null ? caption : docAttrs.get(admonitionName
                                + "-caption"));
                        block = new Block(parent, "admonition", Options.of("content_model", ContentModel.SIMPLE,
                                "source", lines, "attributes", attributes));
                    } else if (mdSyntax && ch0.equals(">") && thisLine.startsWith("> ")) {
                        lines.replaceAll(line -> line.equals(">") ? line.substring(1)
                                : (line.startsWith("> ") ? line.substring(2) : line));
                        String creditLine = null;
                        if (lines.get(lines.size() - 1).startsWith("-- ")) {
                            creditLine = lines.remove(lines.size() - 1).substring(3);
                            if (!lines.isEmpty()) {
                                while (lines.get(lines.size() - 1).isEmpty()) {
                                    lines.remove(lines.size() - 1);
                                }
                            }
                        }
                        attributes.put("style", "quote");
                        // NOTE will only detect discrete (aka free-floating) headings
                        // TODO could assume a discrete heading when inside a block context
                        // FIXME Reader needs to be created w/ line info
                        block = buildBlock("quote", ContentModel.COMPOUND, false, parent, new Reader(lines),
                                attributes, null);
                        if (creditLine != null) {
                            List<String> credit = Strings.split(block.applySubs(creditLine), ", ", 2);
                            if (!credit.isEmpty()) {
                                attributes.put("attribution", credit.get(0));
                            }
                            if (credit.size() > 1) {
                                attributes.put("citetitle", credit.get(1));
                            }
                        }
                    } else if (ch0.equals("\"") && lines.size() > 1 && lines.get(lines.size() - 1).startsWith("-- ")
                            && lines.get(lines.size() - 2).endsWith("\"")) {
                        lines.set(0, thisLine.substring(1)); // strip leading quote
                        String creditLine = lines.remove(lines.size() - 1).substring(3);
                        while (lines.get(lines.size() - 1).isEmpty()) {
                            lines.remove(lines.size() - 1);
                        }
                        lines.add(Strings.chop(lines.remove(lines.size() - 1))); // strip trailing quote
                        attributes.put("style", "quote");
                        block = new Block(parent, "quote", Options.of("content_model", ContentModel.SIMPLE, "source",
                                lines, "attributes", attributes));
                        List<String> credit = Strings.split(block.applySubs(creditLine), ", ", 2);
                        if (!credit.isEmpty()) {
                            attributes.put("attribution", credit.get(0));
                        }
                        if (credit.size() > 1) {
                            attributes.put("citetitle", credit.get(1));
                        }
                    } else {
                        // if [normal] is used over an indented paragraph, shift content to left margin
                        // QUESTION do we even need to shift since whitespace is normalized by XML in this case?
                        if (indented && "normal".equals(style)) {
                            adjustIndentation(lines);
                        }
                        block = new Block(parent, "paragraph", Options.of("content_model", ContentModel.SIMPLE,
                                "source", lines, "attributes", attributes));
                    }

                    catalogInlineAnchors(String.join(LF, lines), block, document, reader);
                }
            }
        }

        // either delimited block or styled paragraph
        if (block == null) {
            switch (blockContext) {
                case "listing", "source" -> {
                    String language = null;
                    if (blockContext.equals("source") || (attributes.get("1") == null
                            && (language = firstNonNull(attributes.get("2"), docAttrs.get("source-language")))
                                    != null)) {
                        if (language != null) { // listing
                            attributes.put("style", "source");
                            attributes.put("language", language);
                            AttributeList.rekey(attributes, nullFirst(null, null, "linenums"));
                        } else { // source
                            AttributeList.rekey(attributes, nullFirst(null, "language", "linenums"));
                            if (!attributes.containsKey("language")) {
                                if (docAttrs.containsKey("source-language")) {
                                    attributes.put("language", docAttrs.get("source-language"));
                                }
                            }
                            if (!"listing".equals(cloakedContext)) {
                                attributes.put("cloaked-context", cloakedContext);
                            }
                        }
                        if (!attributes.containsKey("linenums-option")) {
                            if (attributes.containsKey("linenums") || docAttrs.containsKey("source-linenums-option")) {
                                attributes.put("linenums-option", "");
                            }
                        }
                        if (!attributes.containsKey("indent")) {
                            if (docAttrs.containsKey("source-indent")) {
                                attributes.put("indent", docAttrs.get("source-indent"));
                            }
                        }
                    }
                    block = buildBlock("listing", ContentModel.VERBATIM, terminator, parent, reader, attributes, null);
                }
                case "fenced_code" -> {
                    attributes.put("style", "source");
                    String language = null;
                    int ll = thisLine.length();
                    if (ll > 3) {
                        language = thisLine.substring(3);
                        int commaIdx = language.indexOf(',');
                        if (commaIdx >= 0) {
                            if (commaIdx > 0) {
                                language = Strings.strip(language.substring(0, commaIdx));
                                if (commaIdx < ll - 4) {
                                    attributes.put("linenums", "");
                                }
                            } else if (ll > 4) {
                                attributes.put("linenums", "");
                            }
                        } else {
                            language = Strings.lstrip(language);
                        }
                    }
                    if (Strings.isNullOrEmpty(language)) {
                        if (docAttrs.containsKey("source-language")) {
                            attributes.put("language", docAttrs.get("source-language"));
                        }
                    } else {
                        attributes.put("language", language);
                    }
                    attributes.put("cloaked-context", cloakedContext);
                    if (!attributes.containsKey("linenums-option")) {
                        if (attributes.containsKey("linenums") || docAttrs.containsKey("source-linenums-option")) {
                            attributes.put("linenums-option", "");
                        }
                    }
                    if (!attributes.containsKey("indent")) {
                        if (docAttrs.containsKey("source-indent")) {
                            attributes.put("indent", docAttrs.get("source-indent"));
                        }
                    }
                    terminator = terminator.substring(0, 3);
                    block = buildBlock("listing", ContentModel.VERBATIM, terminator, parent, reader, attributes, null);
                }
                case "table" -> {
                    Cursor blockCursor = reader.cursor();
                    Reader blockReader = new Reader(reader.readLinesUntil(ReadOptions.create().terminator(terminator)
                            .skipLineComments().context("table").cursorAtMark()), blockCursor);
                    // NOTE it's very rare that format is set when using a format hint char, so short-circuit
                    if (!(terminator.startsWith("|") || terminator.startsWith("!"))) {
                        // NOTE infer dsv once all other format hint chars are ruled out
                        if (attributes.get("format") == null) {
                            attributes.put("format", terminator.startsWith(",") ? "csv" : "dsv");
                        }
                    }
                    block = parseTable(blockReader, parent, attributes);
                }
                case "sidebar" -> block = buildBlock(blockContext, ContentModel.COMPOUND, terminator, parent, reader,
                        attributes, null);
                case "admonition" -> {
                    String admonitionName = style.toLowerCase();
                    attributes.put("name", admonitionName);
                    Object caption = attributes.remove("caption");
                    attributes.put("textlabel", caption != null ? caption : docAttrs.get(admonitionName
                            + "-caption"));
                    block = buildBlock(blockContext, ContentModel.COMPOUND, terminator, parent, reader, attributes,
                            null);
                }
                case "open", "abstract", "partintro" -> block = buildBlock("open", ContentModel.COMPOUND, terminator,
                        parent, reader, attributes, null);
                case "literal" -> block = buildBlock(blockContext, ContentModel.VERBATIM, terminator, parent, reader,
                        attributes, null);
                case "example" -> {
                    if (attributes.get("collapsible-option") != null) {
                        attributes.put("caption", "");
                    }
                    block = buildBlock(blockContext, ContentModel.COMPOUND, terminator, parent, reader, attributes,
                            null);
                }
                case "quote", "verse" -> {
                    AttributeList.rekey(attributes, nullFirst(null, "attribution", "citetitle"));
                    block = buildBlock(blockContext, blockContext.equals("verse") ? ContentModel.VERBATIM
                            : ContentModel.COMPOUND, terminator, parent, reader, attributes, null);
                }
                case "stem", "latexmath", "asciimath" -> {
                    if (blockContext.equals("stem")) {
                        Object stemType = attributes.get("2");
                        attributes.put("style", Asciidoctor.resolveStemType(stemType != null ? stemType
                                : docAttrs.get("stem")));
                    }
                    block = buildBlock("stem", ContentModel.RAW, terminator, parent, reader, attributes, null);
                }
                case "pass" -> block = buildBlock(blockContext, ContentModel.RAW, terminator, parent, reader,
                        attributes, null);
                case "comment" -> {
                    buildBlock(blockContext, ContentModel.SKIP, terminator, parent, reader, attributes, null);
                    attributes.clear();
                    return null;
                }
                default -> {
                    if (!(blockExtensions && (blockExtension = extensions.isRegisteredForBlock(blockContext,
                            cloakedContext)) != null)) {
                        // this should only happen if there's a misconfiguration
                        throw new IllegalStateException("Unsupported block type " + blockContext + " at "
                                + reader.cursor());
                    }
                    Map<String, Object> extConfig = blockExtension.getConfig();
                    ContentModel contentModel = ContentModel.of(extConfig.get("content_model"));
                    if (contentModel != ContentModel.SKIP) {
                        List<String> positionalAttrs = Substitutors.positionalAttrs(extConfig);
                        if (!positionalAttrs.isEmpty()) {
                            List<String> keys = new ArrayList<>();
                            keys.add(null);
                            keys.addAll(positionalAttrs);
                            AttributeList.rekey(attributes, keys);
                        }
                        if (extConfig.get("default_attrs") instanceof Map<?, ?> defaultAttrs) {
                            for (Map.Entry<?, ?> entry : defaultAttrs.entrySet()) {
                                if (attributes.get(String.valueOf(entry.getKey())) == null) {
                                    attributes.put(String.valueOf(entry.getKey()), entry.getValue());
                                }
                            }
                        }
                        // QUESTION should we clone the extension for each cloaked context and set in config?
                        attributes.put("cloaked-context", cloakedContext);
                    }
                    block = buildBlock(blockContext, contentModel, terminator, parent, reader, attributes,
                            blockExtension);
                    if (block == null) {
                        attributes.clear();
                        return null;
                    }
                }
            }
        }

        // FIXME we've got to clean this up, it's horrible!
        if (document.isSourcemap()) {
            block.setSourceLocation(reader.cursorAtMark());
        }
        // FIXME title and caption should be assigned when block is constructed (though we need to handle all cases)
        String blockTitle = null;
        if (attributes.get("title") != null) {
            block.setTitle(blockTitle = Strings.valueOf(attributes.remove("title")));
            if (CAPTION_ATTRIBUTE_NAMES.containsKey(block.getContext())) {
                block.assignCaption(Strings.valueOf(attributes.remove("caption")));
            }
        }
        // TODO eventually remove the style attribute from the attributes hash
        //block.style = attributes.delete 'style'
        block.setStyle(Strings.valueOf(attributes.get("style")));
        String blockId = block.getId();
        if (blockId == null) {
            block.setId(blockId = Strings.valueOf(attributes.get("id")));
        }
        if (blockId != null) {
            // convert title to resolve attributes while in scope
            if (blockTitle != null ? blockTitle.contains(ATTR_REF_HEAD) : block.hasTitle()) {
                block.getTitle();
            }
            if (document.registerRef(blockId, block) == null) {
                logger().warn(messageWithContext("id assigned to block already in use: " + blockId,
                        reader.cursorAtMark()));
            }
        }
        // FIXME remove the need for this update!
        if (!attributes.isEmpty()) {
            block.updateAttributes(attributes);
        }
        block.commitSubs();

        //if doc_attrs.key? :pending_attribute_entries
        //  doc_attrs.delete(:pending_attribute_entries).each do |entry|
        //    entry.save_to block.attributes
        //  end
        //end

        if (block.hasSub(Substitution.CALLOUTS)) {
            // No need to sub callouts if none are found when cataloging
            if (!catalogCallouts(((Block) block).getSource(), document)) {
                block.removeSub(Substitution.CALLOUTS);
            }
        }

        return block;
    }

    public static AbstractBlock nextBlock(Reader reader, AbstractBlock parent, Map<String, Object> attributes) {
        return nextBlock(reader, parent, attributes, BlockOptions.DEFAULT);
    }

    public static AbstractBlock nextBlock(Reader reader, AbstractBlock parent) {
        return nextBlock(reader, parent, new LinkedHashMap<>(), BlockOptions.DEFAULT);
    }

    private static String attributeMissing(Map<String, Object> docAttrs) {
        Object missing = docAttrs.get("attribute-missing");
        return missing != null ? missing.toString() : Compliance.getAttributeMissing();
    }

    private static String firstNonNull(Object first, Object second) {
        return first != null ? first.toString() : (second != null ? second.toString() : null);
    }

    private static List<String> nullFirst(String... keys) {
        List<String> result = new ArrayList<>();
        for (String key : keys) {
            result.add(key);
        }
        return result;
    }

    public static List<String> readParagraphLines(Reader reader, boolean breakAtList, ReadOptions opts) {
        opts.breakOnBlankLines().breakOnListContinuation().preserveLastLine();
        Predicate<String> breakCondition = breakAtList
                ? (Compliance.isBlockTerminatesParagraph() ? START_OF_BLOCK_OR_LIST_PROC : START_OF_LIST_PROC)
                : (Compliance.isBlockTerminatesParagraph() ? START_OF_BLOCK_PROC : null);
        return reader.readLinesUntil(opts, breakCondition);
    }

    /**
     * Determines whether this line is the start of a known delimited block.
     *
     * @return the BlockMatchData if this line is the start of a delimited block, otherwise null.
     */
    public static BlockMatchData isDelimitedBlock(String line, boolean returnMatchData) {
        // highly optimized for best performance
        int lineLen = line.length();
        if (!(lineLen > 1 && DELIMITED_BLOCK_HEADS.contains(line.substring(0, 2)))) {
            return null;
        }
        String tip;
        int tipLen;
        // open block
        if (lineLen == 2) {
            tip = line;
            tipLen = 2;
        } else {
            // all other delimited blocks, including fenced code
            if (lineLen < 5) {
                tip = line;
                tipLen = lineLen;
            } else {
                tip = line.substring(0, tipLen = 4);
            }
            // special case for fenced code blocks
            if (Compliance.isMarkdownSyntax() && tip.startsWith("`")) {
                if (tipLen == 4) {
                    if (tip.equals("````") || !(tip = Strings.chop(tip)).equals("```")) {
                        return null;
                    }
                    line = tip;
                    lineLen = tipLen = 3;
                } else if (!tip.equals("```")) {
                    return null;
                }
            } else if (tipLen == 3) {
                return null;
            }
        }
        // NOTE line matches the tip when delimiter is minimum length or fenced code
        DelimitedBlockInfo info = DELIMITED_BLOCKS.get(tip);
        if (info != null && (lineLen == tipLen || isUniform(line.substring(1), DELIMITED_BLOCK_TAILS.get(tip),
                lineLen - 1))) {
            return new BlockMatchData(info.context(), info.masq(), tip, line);
        }
        return null;
    }

    public static boolean isDelimitedBlock(String line) {
        return isDelimitedBlock(line, false) != null;
    }

    /**
     * Builds a block from the lines read by the reader.
     *
     * <p>Whether a block supports compound content should be a config setting. If terminator is Boolean.FALSE, that
     * means the all the lines in the reader should be parsed.
     */
    // NOTE could invoke filter in here, before and after parsing
    public static AbstractBlock buildBlock(String blockContext, ContentModel contentModel, Object terminator,
            AbstractBlock parent, Reader reader, Map<String, Object> attributes,
            ProcessorExtension<BlockProcessor> extension) {
        boolean skipProcessing;
        ContentModel parseAsContentModel;
        switch (contentModel) {
            case SKIP -> {
                skipProcessing = true;
                parseAsContentModel = ContentModel.SIMPLE;
            }
            case RAW -> {
                skipProcessing = false;
                parseAsContentModel = ContentModel.SIMPLE;
            }
            default -> {
                skipProcessing = false;
                parseAsContentModel = contentModel;
            }
        }

        List<String> lines;
        Reader blockReader;
        if (terminator == null) {
            if (parseAsContentModel == ContentModel.VERBATIM) {
                lines = reader.readLinesUntil(ReadOptions.create().breakOnBlankLines().breakOnListContinuation());
            } else {
                if (contentModel == ContentModel.COMPOUND) {
                    contentModel = ContentModel.SIMPLE;
                }
                // TODO we could also skip processing if we're able to detect reader is a BlockReader
                lines = readParagraphLines(reader, false, ReadOptions.create().skipLineComments()
                        .skipProcessing(skipProcessing));
                // QUESTION check for empty lines after grabbing lines for simple content model?
            }
            blockReader = null;
        } else if (parseAsContentModel != ContentModel.COMPOUND) {
            lines = reader.readLinesUntil(ReadOptions.create().terminator((String) terminator)
                    .skipProcessing(skipProcessing).context(blockContext).cursorAtMark());
            blockReader = null;
        // terminator is false when reader has already been prepared
        } else if (Boolean.FALSE.equals(terminator)) {
            lines = null;
            blockReader = reader;
        } else {
            lines = null;
            Cursor blockCursor = reader.cursor();
            blockReader = new Reader(reader.readLinesUntil(ReadOptions.create().terminator((String) terminator)
                    .skipProcessing(skipProcessing).context(blockContext).cursorAtMark()), blockCursor);
        }

        switch (contentModel) {
            case VERBATIM -> {
                Object tabsizeVal = attributes.get("tabsize");
                int tabSize = Strings.toInt(tabsizeVal != null ? tabsizeVal
                        : parent.getDocument().getAttributes().get("tabsize"));
                Object indent = attributes.get("indent");
                if (indent != null) {
                    adjustIndentation(lines, Strings.toInt(indent), tabSize);
                } else if (tabSize > 0) {
                    adjustIndentation(lines, -1, tabSize);
                }
            }
            case SKIP -> {
                // QUESTION should we still invoke process method if extension is specified?
                return null;
            }
            default -> {
            }
        }

        AbstractBlock block;
        if (extension != null) {
            // QUESTION do we want to delete the style?
            attributes.remove("style");
            block = extension.getInstance().process(parent, blockReader != null ? blockReader : new Reader(lines),
                    new LinkedHashMap<>(attributes));
            if (block == null || block == parent) {
                return null;
            }
            attributes.clear();
            attributes.putAll(block.getAttributes());
            // NOTE an extension can change the content model from :simple to :compound. It's up to the extension
            // to decide which one to use. The extension can consult the cloaked-context attribute to determine
            // if the input is a paragraph or delimited block.
            if (block.getContentModel() == ContentModel.COMPOUND && block instanceof Block blk
                    && !(lines = blk.getLines()).isEmpty()) {
                contentModel = ContentModel.COMPOUND;
                blockReader = new Reader(lines);
            }
        } else {
            block = new Block(parent, blockContext, Options.of("content_model", contentModel, "source", lines,
                    "attributes", attributes));
        }

        // reader is confined within boundaries of a delimited block, so look for
        // blocks until there are no more lines
        if (contentModel == ContentModel.COMPOUND) {
            parseBlocks(blockReader, block, null);
        }

        return block;
    }

    /**
     * Parse blocks from this reader until there are no more lines.
     *
     * <p>This method calls {@link #nextBlock} until there are no more lines in the Reader. It does not consider
     * sections because it's assumed the Reader only has lines which are within a delimited block region.
     *
     * @param reader The Reader containing the lines to process
     * @param parent The parent Block to which to attach the parsed blocks
     * @param attributes the attributes to pass to each block (optional)
     */
    public static void parseBlocks(Reader reader, AbstractBlock parent, Map<String, Object> attributes) {
        while (true) {
            AbstractBlock block = attributes != null
                    ? nextBlock(reader, parent, new LinkedHashMap<>(attributes), BlockOptions.DEFAULT)
                    : nextBlock(reader, parent, new LinkedHashMap<>(), BlockOptions.DEFAULT);
            if (block != null) {
                parent.getBlocks().add(block);
            } else if (!reader.hasMoreLines()) {
                break;
            }
        }
    }

    public static void parseBlocks(Reader reader, AbstractBlock parent) {
        parseBlocks(reader, parent, null);
    }

    /**
     * Internal: Parse and construct an ordered or unordered list at the current position of the Reader.
     *
     * @param reader The Reader from which to retrieve the list
     * @param listType A String representing the list type (olist for ordered, ulist for unordered)
     * @param parent The parent Block to which this list belongs
     * @param style The block style assigned to this list (optional, default: null)
     * @param start The value of the start attribute of the list (optional, default: null)
     * @return the Block encapsulating the parsed unordered or ordered list
     */
    public static org.asciidoctor.core.List parseList(Reader reader, String listType, AbstractBlock parent, String style) {
        return parseList(reader, listType, parent, style, null);
    }

    /**
     * Parse and construct an ordered or unordered list at the current position of the Reader.
     *
     * @see #parseList(Reader, String, AbstractBlock, String)
     */
    public static org.asciidoctor.core.List parseList(Reader reader, String listType, AbstractBlock parent, String style,
            Object start) {
        int startValue;
        org.asciidoctor.core.List listBlock = start != null && (startValue = Strings.toInt(start)) != 1
                ? new org.asciidoctor.core.List(parent, listType, Map.of("start", startValue))
                : new org.asciidoctor.core.List(parent, listType);
        Pattern listRx = Rx.LIST_RX_MAP.get(listType);

        Matcher m;
        while (reader.hasMoreLines() && (m = Rx.match(listRx, reader.peekLine())) != null) {
            // NOTE parse_list_item will stop at sibling item or end of list; never sees ancestor items
            ListItem listItem = (ListItem) parseListItem(reader, listBlock, m, m.group(1), style);
            if (listItem != null) {
                listBlock.getItems().add(listItem);
            }

            if (reader.skipBlankLines() == null) {
                break;
            }
        }

        return listBlock;
    }

    /**
     * Internal: Catalog any callouts found in the text, but don't process them.
     *
     * @param text The String of text in which to look for callouts
     * @param document The current document in which the callouts are stored
     * @return A Boolean indicating whether callouts were found
     */
    public static boolean catalogCallouts(String text, Document document) {
        boolean found = false;
        int autonum = 0;
        if (text.contains("<")) {
            Matcher m = Rx.CALLOUT_SCAN_RX.matcher(text);
            while (m.find()) {
                if (!m.group().startsWith("\\")) {
                    document.getCallouts().register(".".equals(m.group(2)) ? String.valueOf(++autonum) : m.group(2));
                }
                // we have to mark as found even if it's escaped so it can be unescaped
                found = true;
            }
        }
        return found;
    }

    /**
     * Internal: Catalog a matched inline anchor.
     *
     * @param id The String id of the anchor
     * @param reftext The optional String reference text of the anchor
     * @param node The AbstractNode parent node of the anchor node
     * @param location The source location (file and line) where the anchor was found (a Cursor or a Reader)
     * @param doc The document to which the node belongs; computed from node if not specified
     */
    public static void catalogInlineAnchor(String id, String reftext, AbstractNode node, Object location,
            Document doc) {
        if (doc == null) {
            doc = node.getDocument();
        }
        if (reftext != null && reftext.contains(ATTR_REF_HEAD)) {
            reftext = doc.subAttributes(reftext);
        }
        if (doc.registerRef(id, new Inline(node, "anchor", reftext, "ref", id, null, null)) == null) {
            Cursor cursor = location instanceof Reader r ? r.cursor() : (Cursor) location;
            logger().warn(messageWithContext("id assigned to anchor already in use: " + id, cursor));
        }
    }

    /**
     * Internal: Catalog any inline anchors found in the text (but don't convert).
     *
     * @param text The String text in which to look for inline anchors
     * @param block The block in which the references should be searched
     * @param document The current Document on which the references are stored
     * @param reader the source Reader
     */
    public static void catalogInlineAnchors(String text, AbstractBlock block, Document document, Reader reader) {
        if (!(text.contains("[[") || text.contains("or:"))) {
            return;
        }
        Matcher m = Rx.INLINE_ANCHOR_SCAN_RX.matcher(text);
        while (m.find()) {
            String id = m.group(1);
            String reftext;
            if (id != null) {
                if ((reftext = m.group(2)) != null && reftext.contains(ATTR_REF_HEAD)
                        && (reftext = document.subAttributes(reftext)).isEmpty()) {
                    continue;
                }
            } else {
                id = m.group(3);
                if ((reftext = m.group(4)) != null) {
                    if (reftext.contains("]")) {
                        reftext = reftext.replace("\\]", "]");
                        if (reftext.contains(ATTR_REF_HEAD)) {
                            reftext = document.subAttributes(reftext);
                        }
                    } else if (reftext.contains(ATTR_REF_HEAD)) {
                        if ((reftext = document.subAttributes(reftext)).isEmpty()) {
                            reftext = null;
                        }
                    }
                }
            }
            if (document.registerRef(id, new Inline(block, "anchor", reftext, "ref", id, null, null)) == null) {
                Cursor location = reader.cursorAtMark();
                String preMatch = text.substring(0, m.start());
                int offset = Strings.count(preMatch, '\n') + (m.group().startsWith(LF) ? 1 : 0);
                if (offset > 0) {
                    (location = location.copy()).advance(offset);
                }
                logger().warn(messageWithContext("id assigned to anchor already in use: " + id, location));
            }
        }
    }

    /**
     * Internal: Catalog the bibliography inline anchor found in the start of the list item (but don't convert).
     *
     * @param id The String id of the anchor
     * @param reftext The optional String reference text of the anchor
     * @param node The AbstractNode parent node of the anchor node
     * @param reader The source Reader for the current Document, positioned at the current list item
     */
    public static void catalogInlineBiblioAnchor(String id, String reftext, AbstractNode node, Reader reader) {
        // QUESTION should we sub attributes in reftext (like with regular anchors)?
        if (node.getDocument().registerRef(id, new Inline(node, "anchor", reftext != null ? "[" + reftext + "]" : null,
                "bibref", id, null, null)) == null) {
            logger().warn(messageWithContext("id assigned to bibliography anchor already in use: " + id,
                    reader.cursor()));
        }
    }

    /**
     * Internal: Parse and construct a description list Block from the current position of the Reader.
     *
     * @param reader The Reader from which to retrieve the description list
     * @param match The match for the head of the list
     * @param parent The parent Block to which this description list belongs
     * @return the Block encapsulating the parsed description list
     */
    public static org.asciidoctor.core.List parseDescriptionList(Reader reader, Matcher match, AbstractBlock parent) {
        org.asciidoctor.core.List listBlock = new org.asciidoctor.core.List(parent, "dlist");
        // detects a description list item that uses the same delimiter (::, :::, :::: or ;;)
        Pattern siblingPattern = Rx.DESCRIPTION_LIST_SIBLING_RX.get(match.group(2));
        DescriptionListEntry currentPair = (DescriptionListEntry) parseListItem(reader, listBlock, match,
                siblingPattern, null);
        listBlock.getItems().add(currentPair);

        Matcher m;
        while (reader.hasMoreLines() && (m = Rx.match(siblingPattern, reader.peekLine())) != null) {
            DescriptionListEntry nextPair = (DescriptionListEntry) parseListItem(reader, listBlock, m, siblingPattern,
                    null);
            if (currentPair.getDescription() != null) {
                listBlock.getItems().add(currentPair = nextPair);
            } else {
                currentPair.getTerms().add(nextPair.getTerms().get(0));
                currentPair.setDescription(nextPair.getDescription());
            }
        }

        return listBlock;
    }

    /**
     * Internal: Parse and construct a callout list Block from the current position of the Reader and advance the
     * document callouts catalog to the next list.
     *
     * @param reader The Reader from which to retrieve the callout list.
     * @param match The match containing the head of the list.
     * @param parent The parent Block to which this callout list belongs.
     * @param callouts The document callouts catalog.
     * @return the Block that represents the parsed callout list.
     */
    public static org.asciidoctor.core.List parseCalloutList(Reader reader, Matcher match, AbstractBlock parent,
            Callouts callouts) {
        org.asciidoctor.core.List listBlock = new org.asciidoctor.core.List(parent, "colist");
        int nextIndex = 1;
        int autonum = 0;
        // NOTE skip the match on the first time through as we've already done it (emulates begin...while)
        while (match != null || ((match = Rx.match(Rx.CALLOUT_LIST_RX, reader.peekLine())) != null
                && reader.mark() != null)) {
            String num = match.group(1);
            if (num.equals(".")) {
                num = String.valueOf(++autonum);
            }
            // might want to move this check to a validate method
            if (!num.equals(String.valueOf(nextIndex))) {
                logger().warn(messageWithContext("callout list item index: expected " + nextIndex + ", got " + num,
                        reader.cursorAtMark()));
            }
            ListItem listItem = (ListItem) parseListItem(reader, listBlock, match, "<1>", null);
            if (listItem != null) {
                listBlock.getItems().add(listItem);
                String coids = callouts.calloutIds(listBlock.getItems().size());
                if (coids.isEmpty()) {
                    logger().warn(messageWithContext("no callout found for <" + listBlock.getItems().size() + ">",
                            reader.cursorAtMark()));
                } else {
                    listItem.getAttributes().put("coids", coids);
                }
            }
            nextIndex++;
            match = null;
        }

        callouts.nextList();
        return listBlock;
    }

    /**
     * Internal: Parse and construct the next ListItem (unordered, ordered, or callout list) or next term ListItem and
     * description ListItem pair (description list) for the specified list Block.
     *
     * <p>First, collect and process all the lines that constitute the next list item for the specified list (according
     * to its type). Next, create a ListItem (in the case of a description list, a description ListItem), parse the
     * lines into blocks, and associate those blocks with that ListItem. Finally, fold the first block into the item's
     * text attribute according to rules described in ListItem.
     *
     * @param reader The Reader from which to retrieve the next list item
     * @param listBlock The parent list Block for this ListItem. Also provides access to the list type.
     * @param match The match that contains the list item marker and first line text of the ListItem
     * @param siblingTrait The trait to match a sibling list item. For ordered and unordered lists, this is a String
     *            marker (e.g., '**' or 'ii)'). For description lists, this is a Pattern marker pattern.
     * @param style The block style assigned to this list (optional, default: null)
     * @return the next ListItem or DescriptionListEntry (description list) for the parent list Block.
     */
    public static AbstractBlock parseListItem(Reader reader, org.asciidoctor.core.List listBlock, Matcher match,
            Object siblingTrait, String style) {
        String listType = listBlock.getContext();
        boolean dlist = false;
        boolean hasText;
        boolean sourcemapAssignmentDeferred = false;
        ListItem listTerm = null;
        ListItem listItem;
        if (listType.equals("dlist")) {
            dlist = true;
            String termText = match.group(1);
            listTerm = new ListItem(listBlock, termText);
            Matcher am;
            if (termText.startsWith("[[") && (am = Rx.match(Rx.LEADING_INLINE_ANCHOR_RX, termText)) != null) {
                catalogInlineAnchor(am.group(1), am.group(2) != null ? am.group(2)
                        : Strings.lstrip(termText.substring(am.end())), listTerm, reader, null);
            }
            String itemText = match.group(3);
            hasText = itemText != null;
            listItem = new ListItem(listBlock, itemText);
            if (listBlock.getDocument().isSourcemap()) {
                listTerm.setSourceLocation(reader.cursor());
                if (hasText) {
                    listItem.setSourceLocation(listTerm.getSourceLocation());
                } else {
                    sourcemapAssignmentDeferred = true;
                }
            }
        } else {
            hasText = true;
            String itemText = match.group(2);
            listItem = new ListItem(listBlock, itemText);
            if (listBlock.getDocument().isSourcemap()) {
                listItem.setSourceLocation(reader.cursor());
            }
            switch (listType) {
                case "ulist" -> {
                    listItem.setMarker((String) siblingTrait);
                    if (itemText.startsWith("[")) {
                        Matcher am;
                        if ("bibliography".equals(style)) {
                            if ((am = Rx.match(Rx.INLINE_BIBLIO_ANCHOR_RX, itemText)) != null) {
                                catalogInlineBiblioAnchor(am.group(1), am.group(2), listItem, reader);
                            }
                        } else if (itemText.startsWith("[[")) {
                            if ((am = Rx.match(Rx.LEADING_INLINE_ANCHOR_RX, itemText)) != null) {
                                catalogInlineAnchor(am.group(1), am.group(2), listItem, reader, null);
                            }
                        } else if (itemText.startsWith("[ ] ") || itemText.startsWith("[x] ")
                                || itemText.startsWith("[*] ")) {
                            listBlock.getAttributes().put("checklist-option", "");
                            listItem.getAttributes().put("checkbox", "");
                            if (!itemText.startsWith("[ ")) {
                                listItem.getAttributes().put("checked", "");
                            }
                            listItem.setText(itemText.substring(4));
                        }
                    }
                }
                case "olist" -> {
                    int ordinal = listBlock.getItems().size();
                    boolean first = ordinal == 0;
                    boolean validate = true;
                    Object start = listBlock.getAttributes().get("start");
                    int startValue;
                    if (start != null) {
                        ordinal += Strings.toInt(start) - 1;
                    } else if (first && (startValue = resolveOrderedListStart((String) siblingTrait)) != 1) {
                        listBlock.getAttributes().put("start", startValue);
                        ordinal += startValue - 1;
                        validate = false;
                    }
                    OrderedListMarker resolved = resolveOrderedListMarker((String) siblingTrait, ordinal, validate,
                            reader);
                    siblingTrait = resolved.marker();
                    listItem.setMarker(resolved.marker());
                    if (first && style == null) {
                        // using list level makes more sense, but we don't track it
                        // basing style on marker level is compliant with AsciiDoc.py
                        String implicitStyle = resolved.style();
                        if (implicitStyle == null) {
                            int len = resolved.marker().length();
                            implicitStyle = len - 1 < ORDERED_LIST_STYLES.size() ? ORDERED_LIST_STYLES.get(len - 1)
                                    : "arabic";
                        }
                        listBlock.setStyle(implicitStyle);
                    }
                    Matcher am;
                    if (itemText.startsWith("[[") && (am = Rx.match(Rx.LEADING_INLINE_ANCHOR_RX, itemText)) != null) {
                        catalogInlineAnchor(am.group(1), am.group(2), listItem, reader, null);
                    }
                }
                default -> { // colist
                    listItem.setMarker((String) siblingTrait);
                    Matcher am;
                    if (itemText.startsWith("[[") && (am = Rx.match(Rx.LEADING_INLINE_ANCHOR_RX, itemText)) != null) {
                        catalogInlineAnchor(am.group(1), am.group(2), listItem, reader, null);
                    }
                }
            }
        }

        // first skip the line with the marker / term (it gets put back onto the reader by next_block)
        reader.shift();
        Cursor blockCursor = reader.cursor();
        Reader listItemReader = new Reader(readLinesForListItem(reader, listType, siblingTrait, hasText),
                blockCursor);
        if (listItemReader.hasMoreLines()) {
            if (sourcemapAssignmentDeferred) {
                listItem.setSourceLocation(blockCursor);
            }
            // NOTE peek on the other side of any comment lines
            List<String> commentLines = listItemReader.skipLineComments();
            boolean contentAdjacent = false;
            String subsequentLine = listItemReader.peekLine();
            if (subsequentLine != null) {
                if (!commentLines.isEmpty()) {
                    listItemReader.unshiftLines(commentLines);
                }
                if (!subsequentLine.isEmpty()) {
                    contentAdjacent = true;
                    // treat lines as paragraph text if continuation does not connect first block (i.e., has_text =
                    // null)
                    if (!dlist) {
                        hasText = false;
                    }
                }
            }

            // reader is confined to boundaries of list, which means only blocks will be found (no sections)
            AbstractBlock block = nextBlock(listItemReader, listItem, new LinkedHashMap<>(),
                    new BlockOptions(!hasText, listType, true));
            if (block != null) {
                listItem.getBlocks().add(block);
            }

            while (listItemReader.hasMoreLines()) {
                block = nextBlock(listItemReader, listItem, new LinkedHashMap<>(),
                        new BlockOptions(false, listType, true));
                if (block != null) {
                    listItem.getBlocks().add(block);
                }
            }

            if (contentAdjacent && !listItem.getBlocks().isEmpty()
                    && "paragraph".equals(listItem.getBlocks().get(0).getContext())) {
                listItem.foldFirst();
            }
        }

        if (dlist) {
            return new DescriptionListEntry(listBlock, List.of(listTerm),
                    listItem.hasText() || listItem.hasBlocks() ? listItem : null);
        }
        return listItem;
    }

    /**
     * Internal: Collect the lines belonging to the current list item, navigating through all the rules that determine
     * what comprises a list item.
     *
     * <p>Grab lines until a sibling list item is found, or the block is broken by a terminator (such as a line
     * comment). Description lists are more greedy if they don't have optional inline item text...they want that text
     *
     * @param reader The Reader from which to retrieve the lines.
     * @param listType The String context of the list (ulist, olist, colist or dlist)
     * @param siblingTrait A Pattern that matches a sibling of this list item or String list marker of the items in this
     *            list (default: null)
     * @param hasText Whether the list item has text defined inline (always true except for description lists)
     * @return a List of lines belonging to the current list item.
     */
    public static List<String> readLinesForListItem(Reader reader, String listType, Object siblingTrait,
            boolean hasText) {
        List<String> buffer = new ArrayList<>();

        // three states for continuation: inactive, active & frozen
        // frozen signifies we've detected sequential continuation lines &
        // continuation is not permitted until reset
        String continuation = "inactive";

        // if we are within a nested list, we don't throw away the list
        // continuation marks because they will be processed when grabbing
        // the lines for those nested lists
        boolean withinNestedList = false;

        // a detached continuation is a list continuation that follows a blank line
        // it gets associated with the outermost block
        Integer detachedContinuation = null;

        boolean dlist = listType.equals("dlist");

        String thisLine = null;
        while (reader.hasMoreLines()) {
            thisLine = reader.readLine();

            // if we've arrived at a sibling item in this list, we've captured
            // the complete list item and can begin processing it
            // the remainder of the method determines whether we've reached
            // the termination of the list
            if (isSiblingListItem(thisLine, listType, siblingTrait)) {
                break;
            }

            if (thisLine.equals(LIST_CONTINUATION)) {
                thisLine = LIST_CONTINUATION_STRING;
            }
            String prevLine = buffer.isEmpty() ? null : buffer.get(buffer.size() - 1);

            if (prevLine != null && isMarker(prevLine)) {
                if (continuation.equals("inactive")) {
                    continuation = "active";
                    hasText = true;
                    if (!withinNestedList) {
                        buffer.set(buffer.size() - 1, LIST_CONTINUATION_PLACEHOLDER);
                    }
                }

                // dealing with adjacent list continuations (which is really a syntax error)
                if (isMarker(thisLine)) {
                    if (!continuation.equals("frozen")) {
                        continuation = "frozen";
                        buffer.add(thisLine);
                    }
                    thisLine = null;
                    continue;
                }
            }

            // a delimited block immediately breaks the list unless preceded
            // by a list continuation (they are harsh like that ;0)
            BlockMatchData match = isDelimitedBlock(thisLine, true);
            if (match != null) {
                if (!continuation.equals("active")) {
                    break;
                }
                buffer.add(thisLine);
                // grab all the lines in the block, leaving the delimiters in place
                // we're being more strict here about the terminator, but I think that's a good thing
                buffer.addAll(reader.readLinesUntil(ReadOptions.create().terminator(match.terminator()).readLastLine()
                        .context(null)));
                continuation = "inactive";
            // BlockAttributeLineRx only breaks dlist if ensuing line is not a list item
            } else if (dlist && !continuation.equals("active") && thisLine.startsWith("[")
                    && Rx.matches(Rx.BLOCK_ATTRIBUTE_LINE_RX, thisLine)) {
                List<String> blockAttributeLines = new ArrayList<>();
                blockAttributeLines.add(thisLine);
                boolean interrupt = false;
                String nextLine;
                while ((nextLine = reader.peekLine()) != null) {
                    if (isDelimitedBlock(nextLine)) {
                        interrupt = true;
                    } else if (nextLine.isEmpty() || (nextLine.startsWith("[")
                            && Rx.matches(Rx.BLOCK_ATTRIBUTE_LINE_RX, nextLine))) {
                        blockAttributeLines.add(reader.readLine());
                        continue;
                    } else if (Rx.matches(Rx.ANY_LIST_RX, nextLine)
                            && !isSiblingListItem(nextLine, listType, siblingTrait)) {
                        buffer.addAll(blockAttributeLines);
                    } else {
                        interrupt = true;
                    }
                    break;
                }
                if (interrupt) {
                    reader.unshiftLines(blockAttributeLines);
                    thisLine = null;
                    break;
                }
            } else if (continuation.equals("active") && !thisLine.isEmpty()) {
                // literal paragraphs have special considerations (and this is one of
                // two entry points into one)
                // if we don't process it as a whole, then a line in it that looks like a
                // list item will throw off the exit from it
                if (Rx.matches(Rx.LITERAL_PARAGRAPH_RX, thisLine)) {
                    reader.unshiftLine(thisLine);
                    if (dlist) {
                        // we may be in an indented list disguised as a literal paragraph
                        // so we need to make sure we don't slurp up a legitimate sibling
                        Object trait = siblingTrait;
                        buffer.addAll(reader.readLinesUntil(ReadOptions.create().preserveLastLine().breakOnBlankLines()
                                .breakOnListContinuation(), line -> isSiblingListItem(line, listType, trait)));
                    } else {
                        buffer.addAll(reader.readLinesUntil(ReadOptions.create().preserveLastLine().breakOnBlankLines()
                                .breakOnListContinuation()));
                    }
                    continuation = "inactive";
                // let block metadata play out until we find the block
                } else {
                    String ch0 = Strings.chr(thisLine);
                    if ((ch0.equals(".") && Rx.matches(Rx.BLOCK_TITLE_RX, thisLine))
                            || (ch0.equals("[") && Rx.matches(Rx.BLOCK_ATTRIBUTE_LINE_RX, thisLine))
                            || (ch0.equals(":") && Rx.matches(Rx.ATTRIBUTE_ENTRY_RX, thisLine))) {
                        buffer.add(thisLine);
                    } else {
                        String nestedListType = findNestedListType(withinNestedList ? List.of("dlist")
                                : NESTABLE_LIST_CONTEXTS, thisLine);
                        if (nestedListType != null) {
                            withinNestedList = true;
                            if (nestedListType.equals("dlist") && Strings.isNullOrEmpty(dlistItemText(thisLine))) {
                                // get greedy again
                                hasText = false;
                            }
                        }
                        buffer.add(thisLine);
                        continuation = "inactive";
                    }
                }
            } else if (prevLine != null && prevLine.isEmpty()) {
                // advance to the next line of content
                if (thisLine.isEmpty()) {
                    // stop reading if we reach eof
                    if (reader.skipBlankLines() == null || (thisLine = reader.readLine()) == null) {
                        thisLine = null;
                        break;
                    }
                    // stop reading if we hit a sibling list item
                    if (isSiblingListItem(thisLine, listType, siblingTrait)) {
                        break;
                    }
                }

                if (thisLine.equals(LIST_CONTINUATION)) {
                    detachedContinuation = buffer.size();
                    buffer.add(LIST_CONTINUATION_STRING);
                } else if (hasText) { // has_text only relevant for dlist, which is more greedy until it has text for an
                                      // item; has_text is always true for all other lists
                    // in this block, we have to see whether we stay in the list
                    // TODO any way to combine this with the check after skipping blank lines?
                    if (isSiblingListItem(thisLine, listType, siblingTrait)) {
                        break;
                    }
                    String nestedListType = findNestedListType(NESTABLE_LIST_CONTEXTS, thisLine);
                    if (nestedListType != null) {
                        buffer.add(thisLine);
                        withinNestedList = true;
                        if (nestedListType.equals("dlist") && Strings.isNullOrEmpty(dlistItemText(thisLine))) {
                            // get greedy again
                            hasText = false;
                        }
                    // slurp up any literal paragraph offset by blank lines
                    // NOTE we have to check for indented list items first
                    } else if (Rx.matches(Rx.LITERAL_PARAGRAPH_RX, thisLine)) {
                        reader.unshiftLine(thisLine);
                        if (dlist) {
                            // we may be in an indented list disguised as a literal paragraph
                            // so we need to make sure we don't slurp up a legitimate sibling
                            Object trait = siblingTrait;
                            buffer.addAll(reader.readLinesUntil(ReadOptions.create().preserveLastLine()
                                    .breakOnBlankLines().breakOnListContinuation(),
                                    line -> isSiblingListItem(line, listType, trait)));
                        } else {
                            buffer.addAll(reader.readLinesUntil(ReadOptions.create().preserveLastLine()
                                    .breakOnBlankLines().breakOnListContinuation()));
                        }
                    } else {
                        break;
                    }
                } else { // only dlist in need of item text, so slurp it up!
                    // pop the blank line so it's not interpreted as a list continuation
                    if (!withinNestedList) {
                        buffer.remove(buffer.size() - 1);
                    }
                    buffer.add(thisLine);
                    hasText = true;
                }
            } else if (isMarker(thisLine)) {
                hasText = true;
                buffer.add(thisLine);
            } else {
                if (!thisLine.isEmpty()) {
                    hasText = true;
                    String nestedListType = findNestedListType(withinNestedList ? List.of("dlist")
                            : NESTABLE_LIST_CONTEXTS, thisLine);
                    if (nestedListType != null) {
                        withinNestedList = true;
                        if (nestedListType.equals("dlist") && Strings.isNullOrEmpty(dlistItemText(thisLine))) {
                            // get greedy again
                            hasText = false;
                        }
                    }
                }
                buffer.add(thisLine);
            }
            thisLine = null;
        }

        if (thisLine != null) {
            reader.unshiftLine(thisLine);
        }

        if (detachedContinuation != null) {
            buffer.set(detachedContinuation, LIST_CONTINUATION_PLACEHOLDER);
        }

        while (!buffer.isEmpty()) {
            String lastLine = buffer.get(buffer.size() - 1);
            // drop optional trailing continuation
            if (isMarker(lastLine)) {
                buffer.remove(buffer.size() - 1);
                break;
            // strip trailing blank lines to prevent empty blocks
            } else if (lastLine.isEmpty()) {
                buffer.remove(buffer.size() - 1);
            } else {
                break;
            }
        }

        return buffer;
    }

    private static String findNestedListType(List<String> contexts, String line) {
        for (String ctx : contexts) {
            if (Rx.matches(Rx.LIST_RX_MAP.get(ctx), line)) {
                return ctx;
            }
        }
        return null;
    }

    private static String dlistItemText(String line) {
        Matcher m = Rx.match(Rx.DESCRIPTION_LIST_RX, line);
        return m == null ? null : m.group(3);
    }

    /**
     * Internal: Initialize a new Section object and assign any attributes provided.
     *
     * <p>The information for this section is retrieved by parsing the lines at the current position of the reader.
     *
     * @param reader the source reader
     * @param parent the parent Section or Document of this Section
     * @param attributes a Map of attributes to assign to this section (default: {})
     * @return the section Block
     */
    public static Section initializeSection(Reader reader, AbstractBlock parent, Map<String, Object> attributes) {
        Document document = parent.getDocument();
        String doctype = document.getDoctype();
        boolean book = "book".equals(doctype);
        Cursor sourceLocation = document.isSourcemap() ? reader.cursor() : null;
        String sectStyle = Strings.valueOf(attributes.get("1"));
        SectionTitle sectionTitle = parseSectionTitle(reader, document, Strings.valueOf(attributes.get("id")));
        int sectLevel = sectionTitle.level();
        String sectTitle = sectionTitle.title();
        String sectName;
        boolean sectSpecial = false;
        boolean sectNumbered = false;

        if (sectStyle != null) {
            if (book && sectStyle.equals("abstract")) {
                sectName = "chapter";
                sectLevel = 1;
            } else if (sectStyle.startsWith("sect") && Rx.matches(Rx.SECTION_LEVEL_STYLE_RX, sectStyle)) {
                sectName = "section";
            } else {
                sectName = sectStyle;
                sectSpecial = true;
                if (sectLevel == 0) {
                    sectLevel = 1;
                }
                sectNumbered = sectName.equals("appendix");
            }
        } else if (book) {
            sectName = sectLevel == 0 ? "part" : (sectLevel > 1 ? "section" : "chapter");
        } else if ("manpage".equals(doctype) && sectTitle.equalsIgnoreCase("synopsis")) {
            sectName = "synopsis";
            sectSpecial = true;
        } else {
            sectName = "section";
        }

        if (sectionTitle.reftext() != null) {
            attributes.put("reftext", sectionTitle.reftext());
        }
        Section section = new Section(parent, sectLevel);
        section.setId(sectionTitle.id());
        section.setTitle(sectTitle);
        section.setSectionName(sectName);
        section.setSourceLocation(sourceLocation);
        if (sectSpecial) {
            section.setSpecial(true);
            if (sectNumbered) {
                section.setNumbered(true);
            } else if ("all".equals(document.getAttributes().get("sectnums"))) {
                section.setNumbered(book && sectLevel == 1 ? "chapter" : true);
            }
        } else if (document.getAttributes().get("sectnums") != null && sectLevel > 0) {
            // NOTE a special section here is guaranteed to be nested in another section
            section.setNumbered(section.isSpecial() ? parent instanceof Section ps && ps.isNumbered() : true);
        } else if (book && sectLevel == 0 && document.getAttributes().get("partnums") != null) {
            section.setNumbered(true);
        }

        // generate an ID if one was not embedded or specified as anchor above section title
        String id = section.getId();
        if (id != null) {
            if (id.isEmpty()) {
                section.setId(id = null);
            } else if (sectTitle.contains(ATTR_REF_HEAD)) {
                // convert title to resolve attributes while in scope
                section.getTitle();
            }
        } else if (document.getAttributes().containsKey("sectids")) {
            section.setId(id = Section.generateId(section.getTitle(), document));
        }
        if (id != null && document.registerRef(id, section) == null) {
            logger().warn(messageWithContext("id assigned to section already in use: " + id,
                    reader.cursorAtLine(reader.getLineNumber() - (sectionTitle.atx() ? 1 : 2))));
        }

        section.updateAttributes(attributes);
        reader.skipBlankLines();

        return section;
    }

    /**
     * Internal: Checks if the next line on the Reader is a section title.
     *
     * @param reader the source Reader
     * @param attributes a Map of attributes collected above the current line
     * @return the Integer section level if the Reader is positioned at a section title or null otherwise
     */
    public static Integer isNextLineSection(Reader reader, Map<String, Object> attributes) {
        Object style = attributes.get("1");
        if (style != null && ("discrete".equals(style) || "float".equals(style))) {
            return null;
        }
        if (Compliance.isUnderlineStyleSectionTitles()) {
            List<String> nextLines = reader.peekLines(2, style != null && style.equals("comment"));
            return isSectionTitle(nextLines.isEmpty() ? "" : nextLines.get(0),
                    nextLines.size() > 1 ? nextLines.get(1) : null);
        }
        String nextLine = reader.peekLine();
        return isAtxSectionTitle(nextLine == null ? "" : nextLine);
    }

    /**
     * Internal: Convenience API for checking if the next line on the Reader is the document title.
     *
     * @param reader the source Reader
     * @param attributes a Map of attributes collected above the current line
     * @param leveloffset an Integer (or integer String value) the represents the current leveloffset
     * @return true if the Reader is positioned at the document title, false otherwise
     */
    public static boolean isNextLineDoctitle(Reader reader, Map<String, Object> attributes, Object leveloffset) {
        if (leveloffset != null) {
            Integer sectLevel = isNextLineSection(reader, attributes);
            return sectLevel != null && sectLevel + Strings.toInt(leveloffset) == 0;
        }
        Integer sectLevel = isNextLineSection(reader, attributes);
        return sectLevel != null && sectLevel == 0;
    }

    /**
     * Checks whether the lines given are an atx or setext section title.
     *
     * @param line1 candidate title.
     * @param line2 candidate underline (default: null).
     * @return the section level if these lines are a section title, otherwise null.
     */
    public static Integer isSectionTitle(String line1, String line2) {
        Integer level = isAtxSectionTitle(line1);
        if (level != null) {
            return level;
        }
        return Strings.isNullOrEmpty(line2) ? null : isSetextSectionTitle(line1, line2);
    }

    public static Integer isSectionTitle(String line1) {
        return isSectionTitle(line1, null);
    }

    /**
     * Checks whether the line given is an atx section title.
     *
     * <p>The level returned is 1 less than number of leading markers.
     *
     * @param line candidate title with leading atx marker.
     * @return the section level if this line is an atx section title, otherwise null.
     */
    public static Integer isAtxSectionTitle(String line) {
        Matcher m;
        if (Compliance.isMarkdownSyntax()
                ? ((line.startsWith("=") || line.startsWith("#"))
                        && (m = Rx.match(Rx.EXT_ATX_SECTION_TITLE_RX, line)) != null)
                : (line.startsWith("=") && (m = Rx.match(Rx.ATX_SECTION_TITLE_RX, line)) != null)) {
            return m.group(1).length() - 1;
        }
        return null;
    }

    /**
     * Checks whether the lines given are an setext section title.
     *
     * @param line1 candidate title
     * @param line2 candidate underline
     * @return the section level if these lines are an setext section title, otherwise null.
     */
    public static Integer isSetextSectionTitle(String line1, String line2) {
        String line2Ch0 = Strings.chr(line2);
        Integer level = SETEXT_SECTION_LEVELS.get(line2Ch0);
        int line2Len = line2.length();
        if (level != null && isUniform(line2, line2Ch0.charAt(0), line2Len)
                && Rx.matches(Rx.SETEXT_SECTION_TITLE_RX, line1) && Math.abs(line1.length() - line2Len) < 2) {
            return level;
        }
        return null;
    }

    /**
     * Internal: Parse the section title from the current position of the reader.
     *
     * <p>Parse an atx (single-line) or setext (underlined) section title. After this method is called, the Reader will
     * be positioned at the line after the section title.
     *
     * <p>For efficiency, we don't reuse methods internally that check for a section title.
     *
     * @param reader the source Reader, positioned at a section title.
     * @param document the current Document.
     * @param sectId the id of the section, if already known
     * @return the id, reftext, title, level and a flag indicating whether an atx section title was matched.
     */
    public static SectionTitle parseSectionTitle(Reader reader, Document document, String sectId) {
        String sectReftext = null;
        String line1 = reader.readLine();
        int sectLevel;
        String sectTitle;
        boolean atx;
        Matcher m;
        String line2;
        if (Compliance.isMarkdownSyntax()
                ? ((line1.startsWith("=") || line1.startsWith("#"))
                        && (m = Rx.match(Rx.EXT_ATX_SECTION_TITLE_RX, line1)) != null)
                : (line1.startsWith("=") && (m = Rx.match(Rx.ATX_SECTION_TITLE_RX, line1)) != null)) {
            // NOTE level is 1 less than number of line markers
            sectLevel = m.group(1).length() - 1;
            sectTitle = m.group(2);
            atx = true;
            if (sectId == null) {
                Matcher am;
                if (sectTitle.endsWith("]]") && (am = Rx.match(Rx.INLINE_SECTION_ANCHOR_RX, sectTitle)) != null
                        && am.group(1) == null) { // escaped
                    sectTitle = sectTitle.substring(0, sectTitle.length() - am.group().length());
                    sectId = am.group(2);
                    sectReftext = am.group(3);
                }
            }
        } else if (Compliance.isUnderlineStyleSectionTitles() && (line2 = reader.peekLine(true)) != null
                && !line2.isEmpty() && SETEXT_SECTION_LEVELS.containsKey(Strings.chr(line2))
                && isUniform(line2, line2.charAt(0), line2.length())
                && (m = Rx.match(Rx.SETEXT_SECTION_TITLE_RX, line1)) != null
                && Math.abs(line1.length() - line2.length()) < 2) {
            sectLevel = SETEXT_SECTION_LEVELS.get(Strings.chr(line2));
            sectTitle = m.group(1);
            atx = false;
            if (sectId == null) {
                Matcher am;
                if (sectTitle.endsWith("]]") && (am = Rx.match(Rx.INLINE_SECTION_ANCHOR_RX, sectTitle)) != null
                        && am.group(1) == null) { // escaped
                    sectTitle = sectTitle.substring(0, sectTitle.length() - am.group().length());
                    sectId = am.group(2);
                    sectReftext = am.group(3);
                }
            }
            reader.shift();
        } else {
            throw new IllegalStateException("Unrecognized section at " + reader.cursorAtPrevLine());
        }
        if (document.hasAttribute("leveloffset")) {
            sectLevel += Strings.toInt(document.getAttribute("leveloffset"));
            if (sectLevel < 0) {
                sectLevel = 0;
            }
        }
        return new SectionTitle(sectId, sectReftext, sectTitle, sectLevel, atx);
    }

    /**
     * Consume and parse the two header lines (line 1 = author info, line 2 = revision info).
     *
     * <p>Returns the Map of header metadata. If a Document object is supplied, the metadata is applied directly to the
     * attributes of the Document.
     *
     * <pre>
     *   data = ["Author Name &lt;author@example.org&gt;", "v1.0, 2012-12-21: Coincide w/ end of world."];
     *   parseHeaderMetadata(new Reader(data, null, true));
     *   // => { "author" => "Author Name", "firstname" => "Author", "lastname" => "Name",
     *   //      "email" => "author@example.org", "revnumber" => "1.0", "revdate" => "2012-12-21",
     *   //      "revremark" => "Coincide w/ end of world." }
     * </pre>
     *
     * @param reader the Reader holding the source lines of the document
     * @param document the Document we are building (default: null)
     * @param retrieve whether to return the metadata (default: true)
     * @return the Map of header metadata (if retrieve is true), otherwise null
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseHeaderMetadata(Reader reader, Document document, boolean retrieve) {
        Map<String, Object> docAttrs = document != null ? document.getAttributes() : null;
        // NOTE this will discard any comment lines, but not skip blank lines
        processAttributeEntries(reader, document, null);

        Map<String, Object> implicitAuthorMetadata;
        Map<String, Object> revMetadata = null;
        Object authorcount = null;
        Object implicitAuthor = null;
        Object implicitAuthorinitials = null;
        Object implicitAuthors = null;
        if (reader.hasMoreLines() && !reader.isNextLineEmpty()) {
            implicitAuthorMetadata = processAuthors(reader.readLine(), false, true);
            authorcount = implicitAuthorMetadata.remove("authorcount");
            if (document != null) {
                docAttrs.put("authorcount", authorcount);
                if (Strings.toInt(authorcount) > 0) {
                    for (Map.Entry<String, Object> entry : implicitAuthorMetadata.entrySet()) {
                        // apply header subs and assign to document; attributes substitution only relevant for email
                        if (!docAttrs.containsKey(entry.getKey())) {
                            docAttrs.put(entry.getKey(), document.applyHeaderSubs(String.valueOf(entry.getValue())));
                        }
                    }
                    implicitAuthor = docAttrs.get("author");
                    implicitAuthorinitials = docAttrs.get("authorinitials");
                    implicitAuthors = docAttrs.get("authors");
                }
            }
            implicitAuthorMetadata.put("authorcount", authorcount);

            // NOTE this will discard any comment lines, but not skip blank lines
            processAttributeEntries(reader, document, null);

            if (reader.hasMoreLines() && !reader.isNextLineEmpty()) {
                String revLine = reader.readLine();
                Matcher match = Rx.match(Rx.REVISION_INFO_LINE_RX, revLine);
                if (match != null) {
                    revMetadata = new LinkedHashMap<>();
                    if (match.group(1) != null) {
                        revMetadata.put("revnumber", Strings.rstrip(match.group(1)));
                    }
                    String component = Strings.strip(match.group(2));
                    if (!component.isEmpty()) {
                        // version must begin with 'v' if date is absent
                        if (match.group(1) == null && component.startsWith("v")) {
                            revMetadata.put("revnumber", component.substring(1));
                        } else {
                            revMetadata.put("revdate", component);
                        }
                    }
                    if (match.group(3) != null) {
                        revMetadata.put("revremark", Strings.rstrip(match.group(3)));
                    }
                    if (document != null && !revMetadata.isEmpty()) {
                        // apply header subs and assign to document
                        for (Map.Entry<String, Object> entry : revMetadata.entrySet()) {
                            if (!docAttrs.containsKey(entry.getKey())) {
                                docAttrs.put(entry.getKey(), document.applyHeaderSubs(String.valueOf(
                                        entry.getValue())));
                            }
                        }
                    }
                } else {
                    // throw it back
                    reader.unshiftLine(revLine);
                }
            }

            // NOTE this will discard any comment lines, but not skip blank lines
            processAttributeEntries(reader, document, null);

            reader.skipBlankLines();
        } else {
            implicitAuthorMetadata = new LinkedHashMap<>();
        }

        // process author attribute entries that override (or stand in for) the implicit author line
        Map<String, Object> authorMetadata = null;
        if (document != null) {
            Object authorLine;
            if (docAttrs.containsKey("author") && !java.util.Objects.equals(authorLine = docAttrs.get("author"),
                    implicitAuthor)) {
                // do not allow multiple, process as names only
                authorMetadata = processAuthors(authorLine, true, false);
                if (!java.util.Objects.equals(docAttrs.get("authorinitials"), implicitAuthorinitials)) {
                    authorMetadata.remove("authorinitials");
                }
            } else if (docAttrs.containsKey("authors") && !java.util.Objects.equals(authorLine = docAttrs.get(
                    "authors"), implicitAuthors)) {
                // allow multiple, process as names only
                authorMetadata = processAuthors(authorLine, true, true);
            } else {
                List<String> authors = new ArrayList<>();
                int authorIdx = 1;
                String authorKey = "author_1";
                boolean explicit = false;
                boolean sparse = false;
                while (docAttrs.containsKey(authorKey)) {
                    // only use indexed author attribute if value is different
                    // leaves corner case if line matches with underscores converted to spaces; use double space to
                    // force
                    Object authorOverride = docAttrs.get(authorKey);
                    if (java.util.Objects.equals(authorOverride, implicitAuthorMetadata.get(authorKey))) {
                        authors.add(null);
                        sparse = true;
                    } else {
                        authors.add(Strings.valueOf(authorOverride));
                        explicit = true;
                    }
                    authorKey = "author_" + (++authorIdx);
                }
                if (explicit) {
                    // rebuild implicit author names to reparse
                    if (sparse) {
                        for (int idx = 0; idx < authors.size(); idx++) {
                            if (authors.get(idx) != null) {
                                continue;
                            }
                            int nameIdx = idx + 1;
                            List<String> nameParts = new ArrayList<>();
                            for (String key : List.of("firstname_" + nameIdx, "middlename_" + nameIdx,
                                    "lastname_" + nameIdx)) {
                                Object part = implicitAuthorMetadata.get(key);
                                if (part != null) {
                                    nameParts.add(part.toString().replace(' ', '_'));
                                }
                            }
                            authors.set(idx, String.join(" ", nameParts));
                        }
                    }
                    // process as names only
                    authorMetadata = processAuthors(authors, true, false);
                } else {
                    authorMetadata = new LinkedHashMap<>();
                    authorMetadata.put("authorcount", 0);
                }
            }

            if (Strings.toInt(authorMetadata.get("authorcount")) == 0) {
                if (authorcount != null) {
                    authorMetadata = null;
                } else {
                    docAttrs.put("authorcount", 0);
                }
            } else {
                docAttrs.putAll(authorMetadata);

                // special case
                if (!docAttrs.containsKey("email") && docAttrs.containsKey("email_1")) {
                    docAttrs.put("email", docAttrs.get("email_1"));
                }
            }
        }

        if (retrieve) {
            Map<String, Object> result = new LinkedHashMap<>(implicitAuthorMetadata);
            if (revMetadata != null) {
                result.putAll(revMetadata);
            }
            if (authorMetadata != null) {
                result.putAll(authorMetadata);
            }
            return result;
        }
        return null;
    }

    public static Map<String, Object> parseHeaderMetadata(Reader reader, Document document) {
        return parseHeaderMetadata(reader, document, true);
    }

    public static Map<String, Object> parseHeaderMetadata(Reader reader) {
        return parseHeaderMetadata(reader, null, true);
    }

    /**
     * Internal: Parse the author line into a Map of author metadata.
     *
     * @param authorLine the String author line (or a List of author entries)
     * @param namesOnly a Boolean flag that indicates whether to process line as names only or names with emails
     *            (default: false)
     * @param multiple a Boolean flag that indicates whether to process multiple semicolon-separated entries in the
     *            author line (default: true)
     * @return a Map of author metadata
     */
    public static Map<String, Object> processAuthors(Object authorLine, boolean namesOnly, boolean multiple) {
        Map<String, Object> authorMetadata = new LinkedHashMap<>();
        int authorIdx = 0;
        List<String> entries;
        if (authorLine instanceof List<?> list) {
            entries = new ArrayList<>();
            for (Object entry : list) {
                entries.add(String.valueOf(entry));
            }
        } else {
            String line = String.valueOf(authorLine);
            entries = multiple && line.contains(";") ? java.util.Arrays.asList(Rx.AUTHOR_DELIMITER_RX.split(line))
                    : List.of(line);
        }
        for (String authorEntry : entries) {
            if (authorEntry.isEmpty()) {
                continue;
            }
            Map<String, String> keyMap = new LinkedHashMap<>();
            if (++authorIdx == 1) {
                for (String key : AUTHOR_KEYS) {
                    keyMap.put(key, key);
                }
            } else {
                for (String key : AUTHOR_KEYS) {
                    keyMap.put(key, key + "_" + authorIdx);
                }
            }

            List<String> segments = null;
            if (namesOnly) { // when parsing an attribute value
                // QUESTION should we rstrip author_entry?
                if (authorEntry.contains("<")) {
                    authorMetadata.put(keyMap.get("author"), authorEntry.replace('_', ' '));
                    authorEntry = Rx.gsub(Rx.XML_SANITIZE_RX, authorEntry, "");
                }
                // NOTE split names and collapse repeating whitespace (split drops any leading whitespace)
                segments = Strings.splitOnWhitespace(authorEntry, 3);
                if (segments.size() == 3) {
                    segments.add(Strings.squeeze(segments.remove(segments.size() - 1), ' '));
                }
            } else {
                Matcher match = Rx.match(Rx.AUTHOR_INFO_LINE_RX, authorEntry);
                if (match != null) {
                    segments = new ArrayList<>();
                    for (int i = 1; i <= match.groupCount(); i++) {
                        segments.add(match.group(i));
                    }
                }
            }

            if (segments != null && !segments.isEmpty()) {
                String fname = segments.get(0).replace('_', ' ');
                String author = fname;
                authorMetadata.put(keyMap.get("firstname"), fname);
                authorMetadata.put(keyMap.get("authorinitials"), Strings.chr(fname));
                if (segments.size() > 1 && segments.get(1) != null) {
                    if (segments.size() > 2 && segments.get(2) != null) {
                        String mname = segments.get(1).replace('_', ' ');
                        String lname = segments.get(2).replace('_', ' ');
                        authorMetadata.put(keyMap.get("middlename"), mname);
                        authorMetadata.put(keyMap.get("lastname"), lname);
                        author = fname + " " + mname + " " + lname;
                        authorMetadata.put(keyMap.get("authorinitials"), Strings.chr(fname) + Strings.chr(mname)
                                + Strings.chr(lname));
                    } else {
                        String lname = segments.get(1).replace('_', ' ');
                        authorMetadata.put(keyMap.get("lastname"), lname);
                        author = fname + " " + lname;
                        authorMetadata.put(keyMap.get("authorinitials"), Strings.chr(fname) + Strings.chr(lname));
                    }
                }
                if (authorMetadata.get(keyMap.get("author")) == null) {
                    authorMetadata.put(keyMap.get("author"), author);
                }
                if (!namesOnly && segments.size() > 3 && segments.get(3) != null) {
                    authorMetadata.put(keyMap.get("email"), segments.get(3));
                }
            } else {
                String fname = Strings.strip(Strings.squeeze(authorEntry, ' '));
                authorMetadata.put(keyMap.get("author"), fname);
                authorMetadata.put(keyMap.get("firstname"), fname);
                authorMetadata.put(keyMap.get("authorinitials"), Strings.chr(fname));
            }

            if (authorIdx == 1) {
                authorMetadata.put("authors", authorMetadata.get(keyMap.get("author")));
            } else {
                // only assign the _1 attributes once we see the second author
                if (authorIdx == 2) {
                    for (String key : AUTHOR_KEYS) {
                        if (authorMetadata.containsKey(key)) {
                            authorMetadata.put(key + "_1", authorMetadata.get(key));
                        }
                    }
                }
                authorMetadata.put("authors", authorMetadata.get("authors") + ", "
                        + authorMetadata.get(keyMap.get("author")));
            }
        }

        authorMetadata.put("authorcount", authorIdx);
        return authorMetadata;
    }

    /**
     * Internal: Parse lines of metadata until a line of metadata is not found.
     *
     * <p>This method processes sequential lines containing block metadata, ignoring blank lines and comments.
     *
     * @param reader the source reader
     * @param document the current Document
     * @param attributes a Map of attributes in which any metadata found will be stored (default: {})
     * @param textOnly indicates that parser is only looking for text content and thus the block title should not be
     *            captured
     * @return the Map of attributes including any metadata found
     */
    public static Map<String, Object> parseBlockMetadataLines(Reader reader, Document document,
            Map<String, Object> attributes, boolean textOnly) {
        while (parseBlockMetadataLine(reader, document, attributes, textOnly)) {
            // discard the line just processed
            reader.shift();
            if (reader.skipBlankLines() == null) {
                break;
            }
        }
        return attributes;
    }

    /**
     * Internal: Parse the next line if it contains metadata for the following block.
     *
     * <p>This method handles lines with the following content:
     *
     * <ul>
     * <li>line or block comment</li>
     * <li>anchor</li>
     * <li>attribute list</li>
     * <li>block title</li>
     * </ul>
     *
     * <p>Any attributes found will be inserted into the attributes argument. If the line contains block metadata, the
     * method returns true, otherwise false.
     *
     * @param reader the source reader
     * @param document the current Document
     * @param attributes a Map of attributes in which any metadata found will be stored
     * @param textOnly indicates the parser is only looking for text content, thus neither a block title or attribute
     *            entry should be captured
     * @return true if the line contains metadata, otherwise false
     */
    public static boolean parseBlockMetadataLine(Reader reader, Document document, Map<String, Object> attributes,
            boolean textOnly) {
        String nextLine = reader.peekLine();
        if (nextLine == null) {
            return false;
        }
        boolean normal = false;
        if (textOnly ? !(nextLine.startsWith("[") || nextLine.startsWith("/"))
                : !(normal = Strings.startsWithAny(nextLine, "[", ".", "/", ":"))) {
            return false;
        }
        Matcher m;
        if (nextLine.startsWith("[")) {
            if (nextLine.startsWith("[[")) {
                if (nextLine.endsWith("]]") && (m = Rx.match(Rx.BLOCK_ANCHOR_RX, nextLine)) != null) {
                    // NOTE registration of id and reftext is deferred until block is processed
                    attributes.put("id", m.group(1));
                    String reftext = m.group(2);
                    if (reftext != null) {
                        attributes.put("reftext", reftext.contains(ATTR_REF_HEAD) ? document.subAttributes(reftext)
                                : reftext);
                    }
                    return true;
                }
            } else if (nextLine.endsWith("]") && (m = Rx.match(Rx.BLOCK_ATTRIBUTE_LIST_RX, nextLine)) != null) {
                Object currentStyle = attributes.get("1");
                // extract id, role, and options from first positional attribute and remove, if present
                if (document.parseAttributes(m.group(1), List.of(), EnumSet.of(ParseOption.SUB_INPUT,
                        ParseOption.SUB_RESULT), attributes).get("1") != null) {
                    String parsedStyle = parseStyleAttribute(attributes, reader);
                    attributes.put("1", parsedStyle != null ? parsedStyle : currentStyle);
                }
                return true;
            }
        } else if (normal && nextLine.startsWith(".")) {
            if ((m = Rx.match(Rx.BLOCK_TITLE_RX, nextLine)) != null) {
                // NOTE title doesn't apply to section, but we need to stash it for the first block
                // TODO should issue an error if this is found above the document title
                attributes.put("title", m.group(1));
                return true;
            }
        } else if (!normal || nextLine.startsWith("/")) {
            if (nextLine.startsWith("//")) {
                int ll;
                if (nextLine.equals("//")) {
                    return true;
                } else if (normal && isUniform(nextLine, '/', ll = nextLine.length())) {
                    if (ll != 3) {
                        reader.readLinesUntil(ReadOptions.create().terminator(nextLine).skipFirstLine()
                                .preserveLastLine().skipProcessing().context("comment"));
                        return true;
                    }
                } else if (!nextLine.startsWith("///")) {
                    return true;
                }
            }
        // NOTE the final condition can be consolidated into single line
        } else if (normal && nextLine.startsWith(":") && (m = Rx.match(Rx.ATTRIBUTE_ENTRY_RX, nextLine)) != null) {
            processAttributeEntry(reader, document, attributes, m);
            return true;
        }
        return false;
    }

    /**
     * Process consecutive attribute entry lines, ignoring adjacent line comments and comment blocks.
     */
    public static void processAttributeEntries(Reader reader, Document document, Map<String, Object> attributes) {
        reader.skipCommentLines();
        while (processAttributeEntry(reader, document, attributes, null)) {
            // discard line just processed
            reader.shift();
            reader.skipCommentLines();
        }
    }

    public static boolean processAttributeEntry(Reader reader, Document document, Map<String, Object> attributes,
            Matcher match) {
        if (match == null) {
            if (!reader.hasMoreLines() || (match = Rx.match(Rx.ATTRIBUTE_ENTRY_RX, reader.peekLine())) == null) {
                return false;
            }
        }
        String value = match.group(2);
        if (Strings.isNullOrEmpty(value)) {
            value = "";
        } else if (value.endsWith(LINE_CONTINUATION) || value.endsWith(LINE_CONTINUATION_LEGACY)) {
            String con = value.substring(value.length() - 2);
            value = Strings.rstrip(value.substring(0, value.length() - 2));
            String nextLine;
            while (reader.advance() && !(nextLine = defaultString(reader.peekLine())).isEmpty()) {
                nextLine = Strings.lstrip(nextLine);
                boolean keepOpen = nextLine.endsWith(con);
                if (keepOpen) {
                    nextLine = Strings.rstrip(nextLine.substring(0, nextLine.length() - 2));
                }
                value = value + (value.endsWith(HARD_LINE_BREAK) ? LF : " ") + nextLine;
                if (!keepOpen) {
                    break;
                }
            }
        }

        storeAttribute(match.group(1), value, document, attributes);
        return true;
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }

    public static AttributeEntryResult storeAttribute(String name, String value) {
        return storeAttribute(name, value, null, new LinkedHashMap<>());
    }

    public static AttributeEntryResult storeAttribute(String name, String value, Document doc) {
        return storeAttribute(name, value, doc, new LinkedHashMap<>());
    }

    /**
     * Store the attribute in the document and register attribute entry if accessible.
     *
     * @param name the String name of the attribute to store; if name begins or ends with !, it signals to remove the
     *            attribute with that root name
     * @param value the String value of the attribute to store
     * @param doc the Document being parsed
     * @param attrs the attributes for the current context
     * @return the resolved attribute name (minus the ! indicator) and value
     */
    public static AttributeEntryResult storeAttribute(String name, String value, Document doc,
            Map<String, Object> attrs) {
        // TODO move processing of attribute value to utility method
        if (name.endsWith("!")) {
            // a null value signals the attribute should be deleted (unset)
            name = Strings.chop(name);
            value = null;
        } else if (name.startsWith("!")) {
            // a null value signals the attribute should be deleted (unset)
            name = name.substring(1);
            value = null;
        }

        name = sanitizeAttributeName(name);
        if (name.equals("numbered")) {
            name = "sectnums";
        } else if (name.equals("hardbreaks")) {
            name = "hardbreaks-option";
        } else if (name.equals("showtitle")) {
            storeAttribute("notitle", value != null ? null : "", doc, attrs);
        }

        if (doc != null) {
            if (value != null) {
                if (name.equals("leveloffset")) {
                    // support relative leveloffset values
                    if (value.startsWith("+")) {
                        value = String.valueOf(Strings.toInt(doc.getAttribute("leveloffset", 0))
                                + Strings.toInt(value.substring(1)));
                    } else if (value.startsWith("-")) {
                        value = String.valueOf(Strings.toInt(doc.getAttribute("leveloffset", 0))
                                - Strings.toInt(value.substring(1)));
                    }
                }
                // QUESTION should we set value to locked value if set_attribute returns false?
                String resolvedValue = doc.setDocumentAttribute(name, value);
                if (resolvedValue != null) {
                    value = resolvedValue;
                    if (attrs != null) {
                        new Document.AttributeEntry(name, value).saveTo(attrs);
                    }
                }
            } else if (doc.deleteDocumentAttribute(name) && attrs != null) {
                new Document.AttributeEntry(name, value).saveTo(attrs);
            }
        } else if (attrs != null) {
            new Document.AttributeEntry(name, value).saveTo(attrs);
        }

        return new AttributeEntryResult(name, value);
    }

    /**
     * Internal: Resolve the 0-index marker for this list item.
     *
     * <p>For ordered lists, match the marker used for this list item against the known list markers and determine which
     * marker is the first (0-index) marker in its number series.
     *
     * <p>For callout lists, return &lt;1&gt;.
     *
     * <p>For bulleted lists, return the marker as passed to this method.
     *
     * @param listType The String context of the list
     * @param marker The String marker for this list item
     * @return the String 0-index marker for this list item
     */
    public static String resolveListMarker(String listType, String marker) {
        return switch (listType) {
            case "ulist" -> marker;
            case "olist" -> resolveOrderedListMarker(marker, 0, false, null).marker();
            default -> "<1>"; // colist
        };
    }

    /**
     * Internal: Resolve the 0-index marker for this ordered list item.
     *
     * <p>Match the marker used for this ordered list item against the known ordered list markers and determine which
     * marker is the first (0-index) marker in its number series.
     *
     * <p>The purpose of this method is to normalize the implicit numbered markers so that they can be compared against
     * other list items.
     *
     * <pre>
     *   Parser.resolveOrderedListMarker("B.", 1, true, reader);
     *   // => ("A.", upperalpha)
     *
     *   Parser.resolveOrderedListMarker(".", 1, true, reader);
     *   // => (".")
     * </pre>
     *
     * @param marker The marker used for this list item
     * @param ordinal The 0-based index of the list item; only used if validate is true
     * @param validate Perform validation that the marker provided is the proper marker in the sequence (default:
     *            false)
     * @return the String of the first marker in this number series and the implicit list style, if applicable
     */
    public static OrderedListMarker resolveOrderedListMarker(String marker, int ordinal, boolean validate,
            Reader reader) {
        if (marker.startsWith(".")) {
            return new OrderedListMarker(marker, null);
        }
        // NOTE case statement is guaranteed to match one of the conditions
        String style = null;
        for (String s : ORDERED_LIST_STYLES) {
            if (Rx.matches(Rx.ORDERED_LIST_MARKER_RX_MAP.get(s), marker)) {
                style = s;
                break;
            }
        }
        String expected = null;
        String actual = null;
        if (style != null) {
            switch (style) {
                case "arabic" -> {
                    if (validate) {
                        expected = String.valueOf(ordinal + 1);
                        actual = String.valueOf(Strings.toInt(marker)); // remove trailing . and coerce to int
                    }
                    marker = "1.";
                }
                case "loweralpha" -> {
                    if (validate) {
                        expected = String.valueOf((char) ('a' + ordinal));
                        actual = Strings.chop(marker); // remove trailing .
                    }
                    marker = "a.";
                }
                case "upperalpha" -> {
                    if (validate) {
                        expected = String.valueOf((char) ('A' + ordinal));
                        actual = Strings.chop(marker); // remove trailing .
                    }
                    marker = "A.";
                }
                case "lowerroman" -> {
                    if (validate) {
                        expected = Helpers.intToRoman(ordinal + 1).toLowerCase();
                        actual = Strings.chop(marker); // remove trailing )
                    }
                    marker = "i)";
                }
                default -> { // upperroman
                    if (validate) {
                        expected = Helpers.intToRoman(ordinal + 1);
                        actual = Strings.chop(marker); // remove trailing )
                    }
                    marker = "I)";
                }
            }
        }

        if (validate && !java.util.Objects.equals(expected, actual)) {
            logger().warn(messageWithContext("list item index: expected " + expected + ", got " + actual,
                    reader.cursor()));
        }

        return new OrderedListMarker(marker, style);
    }

    /**
     * Internal: Resolve the start value of the ordered list from the marker of its first item.
     *
     * @param marker The marker used for the first list item
     * @return the int start value of the list (1 if the marker is implicit)
     */
    public static int resolveOrderedListStart(String marker) {
        int start = 1;
        if (marker.startsWith(".")) {
            return start;
        }
        String style = null;
        for (String s : ORDERED_LIST_STYLES) {
            if (Rx.matches(Rx.ORDERED_LIST_MARKER_RX_MAP.get(s), marker)) {
                style = s;
                break;
            }
        }
        if (style != null) {
            switch (style) {
                case "arabic" -> start = Strings.toInt(marker); // remove trailing . and coerce to int
                case "loweralpha" -> start = Strings.chop(marker).charAt(0) - 96; // remove trailing .
                case "upperalpha" -> start = Strings.chop(marker).charAt(0) - 64; // remove trailing .
                case "lowerroman" -> start = Helpers.romanToInt(Strings.chop(marker).toUpperCase()); // remove trailing )
                default -> start = Helpers.romanToInt(Strings.chop(marker)); // remove trailing )
            }
        }
        return start;
    }

    /**
     * Internal: Determine whether the this line is a sibling list item according to the list type and trait (marker)
     * provided.
     *
     * @param line The String line to check
     * @param listType The context of the list (olist, ulist, colist, dlist)
     * @param siblingTrait The String marker for the list or the Pattern to match a sibling
     * @return a Boolean indicating whether this line is a sibling list item given the criteria provided
     */
    public static boolean isSiblingListItem(String line, String listType, Object siblingTrait) {
        if (siblingTrait instanceof Pattern pattern) {
            return Rx.matches(pattern, line);
        }
        Matcher m = Rx.match(Rx.LIST_RX_MAP.get(listType), line);
        return m != null && java.util.Objects.equals(siblingTrait, resolveListMarker(listType, m.group(1)));
    }

    /**
     * Internal: Parse the table contained in the provided Reader.
     *
     * @param tableReader a Reader containing the source lines of an AsciiDoc table
     * @param parent the parent Block of this Table
     * @param attributes attributes captured from above this Block
     * @return an instance of Table parsed from the provided reader
     */
    public static Table parseTable(Reader tableReader, AbstractBlock parent, Map<String, Object> attributes) {
        Table table = new Table(parent, attributes);
        boolean explicitColspecs = false;

        List<Map<String, Object>> colspecs;
        if (attributes.containsKey("cols")
                && !(colspecs = parseColspecs(Strings.valueOf(attributes.get("cols")))).isEmpty()) {
            table.createColumns(colspecs);
            explicitColspecs = true;
        }

        Integer skippedVal = tableReader.skipBlankLines();
        int skipped = skippedVal == null ? 0 : skippedVal;
        boolean implicitHeader = false;
        if (attributes.get("header-option") != null) {
            table.setHasHeaderOption(true);
        } else if (skipped == 0 && attributes.get("noheader-option") == null) {
            // NOTE assume table has header until we know otherwise; if it doesn't (null), cells in first row get
            // reprocessed
            table.setHasHeaderOption("implicit");
            implicitHeader = true;
        }
        Table.ParserContext parserCtx = new Table.ParserContext(tableReader, table, attributes);
        String format = parserCtx.getFormat();
        int loopIdx = -1;
        Integer implicitHeaderBoundary = null;

        String line;
        while ((line = tableReader.readLine()) != null) {
            boolean beyondFirst = ++loopIdx > 0;
            if (beyondFirst && line.isEmpty()) {
                line = null;
                if (implicitHeaderBoundary != null) {
                    implicitHeaderBoundary++;
                }
            } else if (format.equals("psv")) {
                if (parserCtx.startsWithDelimiter(line)) {
                    line = line.substring(1);
                    // push empty cell spec if cell boundary appears at start of line
                    parserCtx.closeOpenCell();
                    implicitHeaderBoundary = null;
                } else {
                    CellSpec cellSpec = parseCellspec(line, "start", parserCtx.getDelimiter());
                    line = cellSpec.rest();
                    // if cellspec is not null, we're at a cell boundary
                    if (cellSpec.spec() != null) {
                        parserCtx.closeOpenCell(cellSpec.spec());
                        implicitHeaderBoundary = null;
                    // otherwise, the cell continues from previous line
                    } else if (implicitHeaderBoundary != null && implicitHeaderBoundary == loopIdx) {
                        table.setHasHeaderOption(null);
                        implicitHeader = false;
                        implicitHeaderBoundary = null;
                    }
                }
            }

            if (!beyondFirst) {
                tableReader.mark();
                // NOTE implicit header is offset by at least one blank line; implicit_header_boundary tracks size of
                // gap
                if (implicitHeader) {
                    if (tableReader.hasMoreLines() && tableReader.peekLine().isEmpty()) {
                        implicitHeaderBoundary = 1;
                    } else {
                        table.setHasHeaderOption(null);
                        implicitHeader = false;
                    }
                }
            }

            // this loop is used for flow control; internal logic controls how many times it executes
            cellLoop:
            while (true) {
                Matcher m;
                if (line != null && (m = parserCtx.matchDelimiter(line)) != null) {
                    String preMatch = line.substring(0, m.start());
                    String postMatch = line.substring(m.end());
                    switch (format) {
                        case "csv" -> {
                            if (parserCtx.isBufferHasUnclosedQuotes(preMatch)) {
                                parserCtx.skipPastDelimiter(preMatch);
                                if ((line = postMatch).isEmpty()) {
                                    break cellLoop;
                                }
                                continue cellLoop;
                            }
                            parserCtx.setBuffer(parserCtx.getBuffer() + preMatch);
                        }
                        case "dsv" -> {
                            if (preMatch.endsWith("\\")) {
                                parserCtx.skipPastEscapedDelimiter(preMatch);
                                if ((line = postMatch).isEmpty()) {
                                    parserCtx.setBuffer(parserCtx.getBuffer() + LF);
                                    parserCtx.keepCellOpen();
                                    break cellLoop;
                                }
                                continue cellLoop;
                            }
                            parserCtx.setBuffer(parserCtx.getBuffer() + preMatch);
                        }
                        default -> { // psv
                            if (preMatch.endsWith("\\")) {
                                parserCtx.skipPastEscapedDelimiter(preMatch);
                                if ((line = postMatch).isEmpty()) {
                                    parserCtx.setBuffer(parserCtx.getBuffer() + LF);
                                    parserCtx.keepCellOpen();
                                    break cellLoop;
                                }
                                continue cellLoop;
                            }
                            CellSpec cellSpec = parseCellspec(preMatch, "end", null);
                            parserCtx.pushCellspec(cellSpec.spec());
                            parserCtx.setBuffer(parserCtx.getBuffer() + cellSpec.rest());
                        }
                    }
                    // don't break if empty to preserve empty cell found at end of line (see issue #1106)
                    if ((line = postMatch).isEmpty()) {
                        line = null;
                    }
                    parserCtx.closeCell();
                } else {
                    // no other delimiters to see here; suck up this line into the buffer and move on
                    parserCtx.setBuffer(parserCtx.getBuffer() + (line == null ? "" : line) + LF);
                    switch (format) {
                        case "csv" -> {
                            if (parserCtx.isBufferHasUnclosedQuotes()) {
                                if (implicitHeaderBoundary != null && loopIdx == 0) {
                                    table.setHasHeaderOption(null);
                                    implicitHeader = false;
                                    implicitHeaderBoundary = null;
                                }
                                parserCtx.keepCellOpen();
                            } else {
                                parserCtx.closeCell(true);
                            }
                        }
                        case "dsv" -> parserCtx.closeCell(true);
                        default -> parserCtx.keepCellOpen(); // psv
                    }
                    break cellLoop;
                }
            }

            // NOTE cell may already be closed if table format is csv or dsv
            if (parserCtx.isCellOpen()) {
                if (!tableReader.hasMoreLines()) {
                    parserCtx.closeCell(true);
                }
            } else if (tableReader.skipBlankLines() == null) {
                break;
            }
        }

        parserCtx.closeTable();
        Object colcount = table.getAttributes().get("colcount");
        if (colcount == null) {
            table.getAttributes().put("colcount", colcount = table.getColumns().size());
        }
        if (!(Strings.toInt(colcount) == 0 || explicitColspecs)) {
            table.assignColumnWidths();
        }
        if (implicitHeader) {
            table.setHasHeaderOption(true);
        }
        table.partitionHeaderFooter(attributes);

        return table;
    }

    /**
     * Internal: Parse the column specs for this table.
     *
     * <p>The column specs dictate the number of columns, relative width of columns, default alignments for cells in
     * each column, and/or default styles or filters applied to the cells in the column.
     *
     * <p>Every column spec is guaranteed to have a width.
     *
     * @return a List of Maps of attributes that specify how to format and layout the cells in the table.
     */
    public static List<Map<String, Object>> parseColspecs(String records) {
        if (records.contains(" ")) {
            records = records.replace(" ", "");
        }
        List<Map<String, Object>> specs = new ArrayList<>();
        // check for deprecated syntax: single number, equal column spread
        if (records.equals(String.valueOf(Strings.toLong(records)))) {
            int count = Strings.toInt(records);
            for (int i = 0; i < count; i++) {
                specs.add(Options.of("width", 1));
            }
            return specs;
        }

        // NOTE -1 argument ensures we don't drop empty records
        for (String record : records.contains(",") ? Strings.split(records, ",", -1)
                : Strings.split(records, ";", -1)) {
            if (record.isEmpty()) {
                specs.add(Options.of("width", 1));
            // TODO might want to use scan rather than this mega-regexp
            } else {
                Matcher m = Rx.match(Rx.COLUMN_SPEC_RX, record);
                if (m != null) {
                    Map<String, Object> spec = new LinkedHashMap<>();
                    if (m.group(2) != null) {
                        // make this an operation
                        List<String> parts = Strings.split(m.group(2), ".");
                        String colspec = parts.isEmpty() ? null : parts.get(0);
                        String rowspec = parts.size() > 1 ? parts.get(1) : null;
                        if (!Strings.isNullOrEmpty(colspec) && TABLE_CELL_HORZ_ALIGNMENTS.containsKey(colspec)) {
                            spec.put("halign", TABLE_CELL_HORZ_ALIGNMENTS.get(colspec));
                        }
                        if (!Strings.isNullOrEmpty(rowspec) && TABLE_CELL_VERT_ALIGNMENTS.containsKey(rowspec)) {
                            spec.put("valign", TABLE_CELL_VERT_ALIGNMENTS.get(rowspec));
                        }
                    }

                    String width = m.group(3);
                    if (width != null) {
                        // to_i will strip the optional %
                        spec.put("width", width.equals("~") ? -1 : Strings.toInt(width));
                    } else {
                        spec.put("width", 1);
                    }

                    // make this an operation
                    if (m.group(4) != null && TABLE_CELL_STYLES.containsKey(m.group(4))) {
                        spec.put("style", TABLE_CELL_STYLES.get(m.group(4)));
                    }

                    if (m.group(1) != null) {
                        int repeat = Strings.toInt(m.group(1));
                        for (int i = 0; i < repeat; i++) {
                            specs.add(new LinkedHashMap<>(spec));
                        }
                    } else {
                        specs.add(spec);
                    }
                }
            }
        }
        return specs;
    }

    /**
     * Internal: Parse the cell specs for the current cell.
     *
     * <p>The cell specs dictate the cell's alignments, styles or filters, colspan, rowspan and/or repeating content.
     *
     * <p>The default spec when pos == "end" is {} since we already know we're at a delimiter. When pos == "start", we
     * *may* be at a delimiter, null indicates we're not.
     *
     * @return the Map of attributes that indicate how to layout and style this cell in the table.
     */
    public static CellSpec parseCellspec(String line, String pos, String delimiter) {
        Matcher m;
        String rest;

        if ("start".equals(pos)) {
            if (!line.contains(delimiter)) {
                return new CellSpec(null, line);
            }
            String[] parts = Strings.partition(line, delimiter);
            String specPart = parts[0];
            rest = parts[2];
            if ((m = Rx.match(Rx.CELL_SPEC_START_RX, specPart)) == null) {
                return new CellSpec(null, line);
            }
            if (m.group().isEmpty()) {
                return new CellSpec(new LinkedHashMap<>(), rest);
            }
        } else if ((m = Rx.match(Rx.CELL_SPEC_END_RX, line)) != null) { // when pos == "end"
            // NOTE return the line stripped of trailing whitespace if no cellspec is found in this case
            if (Strings.lstrip(m.group()).isEmpty()) {
                return new CellSpec(new LinkedHashMap<>(), Strings.rstrip(line));
            }
            rest = line.substring(0, m.start());
        } else {
            return new CellSpec(new LinkedHashMap<>(), line);
        }

        Map<String, Object> spec = new LinkedHashMap<>();
        if (m.group(1) != null) {
            List<String> parts = Strings.split(m.group(1), ".");
            String colspecStr = parts.isEmpty() ? null : parts.get(0);
            String rowspecStr = parts.size() > 1 ? parts.get(1) : null;
            int colspec = Strings.isNullOrEmpty(colspecStr) ? 1 : Strings.toInt(colspecStr);
            int rowspec = Strings.isNullOrEmpty(rowspecStr) ? 1 : Strings.toInt(rowspecStr);
            switch (m.group(2)) {
                case "+" -> {
                    if (colspec != 1) {
                        spec.put("colspan", colspec);
                    }
                    if (rowspec != 1) {
                        spec.put("rowspan", rowspec);
                    }
                }
                case "*" -> {
                    if (colspec != 1) {
                        spec.put("repeatcol", colspec);
                    }
                }
                default -> {
                }
            }
        }

        if (m.group(3) != null) {
            List<String> parts = Strings.split(m.group(3), ".");
            String colspec = parts.isEmpty() ? null : parts.get(0);
            String rowspec = parts.size() > 1 ? parts.get(1) : null;
            if (!Strings.isNullOrEmpty(colspec) && TABLE_CELL_HORZ_ALIGNMENTS.containsKey(colspec)) {
                spec.put("halign", TABLE_CELL_HORZ_ALIGNMENTS.get(colspec));
            }
            if (!Strings.isNullOrEmpty(rowspec) && TABLE_CELL_VERT_ALIGNMENTS.containsKey(rowspec)) {
                spec.put("valign", TABLE_CELL_VERT_ALIGNMENTS.get(rowspec));
            }
        }

        if (m.group(4) != null && TABLE_CELL_STYLES.containsKey(m.group(4))) {
            spec.put("style", TABLE_CELL_STYLES.get(m.group(4)));
        }

        return new CellSpec(spec, rest);
    }

    /**
     * Parse the first positional attribute and assign named attributes.
     *
     * <p>Parse the first positional attribute to extract the style, role and id parts, assign the values to their
     * corresponding attribute keys and return the parsed style from the first positional attribute.
     *
     * <pre>
     *   attributes
     *   // => { "1" => "abstract#intro.lead%fragment", "style" => "preamble" }
     *
     *   parseStyleAttribute(attributes)
     *   // => "abstract"
     *
     *   attributes
     *   // => { "1" => "abstract#intro.lead%fragment", "style" => "abstract", "id" => "intro",
     *   //      "role" => "lead", "options" => "fragment", "fragment-option" => "" }
     * </pre>
     *
     * @param attributes The Map of attributes to process and update
     * @param reader the source Reader (used for reporting warnings) (optional)
     * @return the String style parsed from the first positional attribute
     */
    @SuppressWarnings("unchecked")
    public static String parseStyleAttribute(Map<String, Object> attributes, Reader reader) {
        // NOTE spaces are not allowed in shorthand, so if we detect one, this ain't no shorthand
        String rawStyle = Strings.valueOf(attributes.get("1"));
        if (rawStyle != null && !rawStyle.contains(" ") && Compliance.isShorthandPropertySyntax()) {
            String name = null;
            StringBuilder accum = new StringBuilder();
            Map<String, Object> parsedAttrs = new LinkedHashMap<>();

            for (int i = 0, len = rawStyle.length(); i < len; i++) {
                char c = rawStyle.charAt(i);
                switch (c) {
                    case '.' -> {
                        yieldBufferedAttribute(parsedAttrs, name, accum.toString(), reader);
                        accum.setLength(0);
                        name = "role";
                    }
                    case '#' -> {
                        yieldBufferedAttribute(parsedAttrs, name, accum.toString(), reader);
                        accum.setLength(0);
                        name = "id";
                    }
                    case '%' -> {
                        yieldBufferedAttribute(parsedAttrs, name, accum.toString(), reader);
                        accum.setLength(0);
                        name = "option";
                    }
                    default -> accum.append(c);
                }
            }

            // small optimization if no shorthand is found
            if (name != null) {
                yieldBufferedAttribute(parsedAttrs, name, accum.toString(), reader);

                String parsedStyle = (String) parsedAttrs.get("style");
                if (parsedStyle != null) {
                    attributes.put("style", parsedStyle);
                }

                if (parsedAttrs.containsKey("id")) {
                    attributes.put("id", parsedAttrs.get("id"));
                }

                if (parsedAttrs.containsKey("role")) {
                    Object existingRole = attributes.get("role");
                    String roles = String.join(" ", (List<String>) parsedAttrs.get("role"));
                    attributes.put("role", Strings.isNullOrEmpty(existingRole) ? roles : existingRole + " " + roles);
                }

                if (parsedAttrs.containsKey("option")) {
                    for (String opt : (List<String>) parsedAttrs.get("option")) {
                        attributes.put(opt + "-option", "");
                    }
                }

                return parsedStyle;
            }
            attributes.put("style", rawStyle);
            return rawStyle;
        }
        attributes.put("style", rawStyle);
        return rawStyle;
    }

    public static String parseStyleAttribute(Map<String, Object> attributes) {
        return parseStyleAttribute(attributes, null);
    }

    /**
     * Internal: Save the collected attribute (id, option, role, or null for style) in the attribute Map.
     */
    @SuppressWarnings("unchecked")
    private static void yieldBufferedAttribute(Map<String, Object> attrs, String name, String value, Reader reader) {
        if (name != null) {
            if (value.isEmpty()) {
                if (reader != null) {
                    logger().warn(messageWithContext("invalid empty " + name + " detected in style attribute",
                            reader.cursorAtPrevLine()));
                } else {
                    logger().warn("invalid empty " + name + " detected in style attribute");
                }
            } else if (name.equals("id")) {
                if (attrs.containsKey("id")) {
                    if (reader != null) {
                        logger().warn(messageWithContext("multiple ids detected in style attribute",
                                reader.cursorAtPrevLine()));
                    } else {
                        logger().warn("multiple ids detected in style attribute");
                    }
                }
                attrs.put(name, value);
            } else {
                ((List<String>) attrs.computeIfAbsent(name, k -> new ArrayList<String>())).add(value);
            }
        } else if (!value.isEmpty()) {
            attrs.put("style", value);
        }
    }

    /**
     * Remove the block indentation (the amount of whitespace of the least indented line), replace tabs with spaces
     * (using proper tab expansion logic) and, finally, indent the lines by the margin width. Modifies the input List
     * directly.
     *
     * <p>This method preserves the significant indentation (that exceeding the block indent) on each line.
     *
     * @param lines The List of String lines to process (no trailing newlines)
     * @param indentSize The number of spaces to re-add to the start of non-empty lines after removing the indentation.
     *            If this value is &lt; 0, the existing indentation is preserved (optional, default: 0)
     * @param tabSize the number of spaces to use in place of a tab. A value of &lt;= 0 disables the replacement
     *            (optional, default: 0)
     */
    public static void adjustIndentation(List<String> lines, int indentSize, int tabSize) {
        if (lines == null || lines.isEmpty()) {
            return;
        }

        // expand tabs if a tab character is detected and tab_size > 0
        if (tabSize > 0 && lines.stream().anyMatch(line -> line.contains(TAB_CHAR))) {
            String fullTabSpace = " ".repeat(tabSize);
            lines.replaceAll(line -> {
                int tabIdx;
                if (line.isEmpty() || (tabIdx = line.indexOf('\t')) < 0) {
                    return line;
                }
                if (tabIdx == 0) {
                    int leadingTabs = 0;
                    while (leadingTabs < line.length() && line.charAt(leadingTabs) == '\t') {
                        leadingTabs++;
                    }
                    line = fullTabSpace.repeat(leadingTabs) + line.substring(leadingTabs);
                    if (!line.contains(TAB_CHAR)) {
                        return line;
                    }
                }
                // keeps track of how many spaces were added to adjust offset in match data
                int spacesAdded = 0;
                int idx = 0;
                StringBuilder result = new StringBuilder();
                for (int i = 0, len = line.length(); i < len; i++) {
                    char c = line.charAt(i);
                    if (c == '\t') {
                        // calculate how many spaces this tab represents, then replace tab with spaces
                        int offset = idx + spacesAdded;
                        if (offset % tabSize == 0) {
                            spacesAdded += tabSize - 1;
                            result.append(fullTabSpace);
                        } else {
                            int spaces = tabSize - offset % tabSize;
                            if (spaces != 1) {
                                spacesAdded += spaces - 1;
                            }
                            result.append(" ".repeat(spaces));
                        }
                    } else {
                        result.append(c);
                    }
                    idx++;
                }
                return result.toString();
            });
        }

        // skip block indent adjustment if indent_size is < 0
        if (indentSize < 0) {
            return;
        }

        // determine block indent (assumes no whitespace-only lines are present)
        Integer blockIndent = null;
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            int lineIndent = line.length() - Strings.lstrip(line).length();
            if (lineIndent == 0) {
                blockIndent = null;
                break;
            }
            if (!(blockIndent != null && blockIndent < lineIndent)) {
                blockIndent = lineIndent;
            }
        }

        // remove block indent then apply indent_size if specified
        // NOTE block_indent is > 0 if not null
        Integer indent = blockIndent;
        if (indentSize == 0) {
            if (indent != null) {
                lines.replaceAll(line -> line.isEmpty() ? line : line.substring(indent));
            }
        } else {
            String newBlockIndent = " ".repeat(indentSize);
            if (indent != null) {
                lines.replaceAll(line -> line.isEmpty() ? line : newBlockIndent + line.substring(indent));
            } else {
                lines.replaceAll(line -> line.isEmpty() ? line : newBlockIndent + line);
            }
        }
    }

    public static void adjustIndentation(List<String> lines) {
        adjustIndentation(lines, 0, 0);
    }

    public static void adjustIndentation(List<String> lines, int indentSize) {
        adjustIndentation(lines, indentSize, 0);
    }

    public static boolean isUniform(String str, char chr, int len) {
        return Strings.count(str, chr) == len;
    }

    /**
     * Internal: Convert a string to a legal attribute name.
     *
     * <pre>
     *   sanitizeAttributeName("Foo Bar")
     *   // => "foobar"
     *
     *   sanitizeAttributeName("foo")
     *   // => "foo"
     *
     *   sanitizeAttributeName("Foo 3 #-Billy")
     *   // => "foo3-billy"
     * </pre>
     *
     * @param name the String name of the attribute
     * @return a String with the legal AsciiDoc attribute name.
     */
    public static String sanitizeAttributeName(String name) {
        return Rx.gsub(Rx.INVALID_ATTRIBUTE_NAME_CHARS_RX, name, "").toLowerCase();
    }
}
