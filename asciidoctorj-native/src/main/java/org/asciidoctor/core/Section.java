package org.asciidoctor.core;

import java.util.Map;

/**
 * Methods for managing sections of AsciiDoc content in a document.
 *
 * <pre>
 *   section = new Section();
 *   section.setTitle("Section 1");
 *   section.setId("sect1");
 *
 *   section.getBlocks().size();
 *   // => 0
 *
 *   section.getId();
 *   // => "sect1"
 *
 *   section.append(newBlock);
 *   section.getBlocks().size();
 *   // => 1
 * </pre>
 */
public class Section extends AbstractBlock {
    private int index;
    private String sectname;
    private boolean special;
    private Object numbered = false;

    /**
     * Initialize a Section object.
     *
     * @param parent The parent AbstractBlock. If set, must be a Document or Section object (default: null)
     * @param level The Integer level of this section (default: 1 more than parent level or 1 if parent not defined)
     * @param numbered A Boolean indicating whether numbering is enabled for this Section (default: false)
     * @param attributes An optional Map of attributes (default: {})
     */
    public Section(AbstractBlock parent, Integer level, boolean numbered, Map<String, Object> attributes) {
        super(parent, "section", attributes);
        if (parent instanceof Section parentSection) {
            this.level = level != null ? level : parentSection.getLevel() + 1;
            this.special = parentSection.isSpecial();
        } else {
            this.level = level != null ? level : 1;
            this.special = false;
        }
        this.numbered = numbered;
        this.index = 0;
    }

    public Section(AbstractBlock parent, Integer level, boolean numbered) {
        this(parent, level, numbered, null);
    }

    public Section(AbstractBlock parent, Integer level) {
        this(parent, level, false, null);
    }

    public Section(AbstractBlock parent) {
        this(parent, null, false, null);
    }

    public Section() {
        this(null, null, false, null);
    }

    /**
     * @return the 0-based index order of this section within the parent block
     */
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the section name of this section
     */
    public String getSectionName() {
        return sectname;
    }

    public void setSectionName(String sectname) {
        this.sectname = sectname;
    }

    /**
     * @return the flag to indicate whether this is a special section or a child of one
     */
    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }

    /**
     * @return whether this section should be numbered
     */
    public boolean isNumbered() {
        return numbered != null && !Boolean.FALSE.equals(numbered);
    }

    /**
     * @return the numbering mode of this section (false, true or "chapter")
     */
    public Object getNumbered() {
        return numbered;
    }

    /**
     * Sets the flag to indicate whether this section should be numbered.
     *
     * @param numbered a Boolean or the String "chapter" to indicate the section should be numbered like a chapter
     */
    public void setNumbered(Object numbered) {
        this.numbered = numbered == null ? false : numbered;
    }

    /**
     * @return the caption for this section (only relevant for appendices)
     */
    @Override
    public String getCaption() {
        return caption;
    }

    /**
     * The name of this section, an alias of the section title.
     */
    public String getName() {
        return getTitle();
    }

    /**
     * Generate a String ID from the title of this section.
     *
     * <p>See {@link #generateId(String, Document)} for details.
     */
    public String generateId() {
        return generateId(getTitle(), document);
    }

    /**
     * Check whether this Section has any child Section objects.
     *
     * @return A Boolean to indicate whether this Section has child Section objects
     */
    @Override
    public boolean hasSections() {
        return nextSectionIndex > 0;
    }

    /**
     * Get the section number for the current Section.
     *
     * <p>The section number is a dot-separated String that uniquely describes the position of this Section in the
     * document. Each entry represents a level of nesting. The value of each entry is the 1-based outline number of the
     * Section amongst its numbered sibling Sections.
     *
     * <p>This method assumes that both the level and parent instance variables have been assigned. The method also
     * assumes that the value of parent is either a Document or Section.
     *
     * <pre>
     *   sect1.getSectnum()
     *   // => 1.
     *
     *   sect1_1.getSectnum()
     *   // => 1.1.
     *
     *   sect1_1_1.getSectnum(",", "")
     *   // => 1,1,1
     * </pre>
     *
     * @param delimiter the delimiter to separate the number for each level (default: '.')
     * @param append the String to append at the end of the section number (default: the delimiter)
     * @return the section number as a String
     */
    public String getSectnum(String delimiter, String append) {
        if (delimiter == null) {
            delimiter = ".";
        }
        if (append == null) {
            append = delimiter;
        }
        String number = numeral == null ? "" : numeral;
        return level > 1 && parent instanceof Section parentSection
                ? parentSection.getSectnum(delimiter, delimiter) + number + append
                : number + append;
    }

    public String getSectnum() {
        return getSectnum(".", null);
    }

    public String getSectnum(String delimiter) {
        return getSectnum(delimiter, null);
    }

    /**
     * Get the section number for the current Section.
     *
     * @param delimiter the delimiter to separate the number for each level
     * @param append whether the delimiter should be appended to the final level
     * @return the section number as a String
     */
    public String getSectnum(String delimiter, boolean append) {
        return getSectnum(delimiter, append ? null : "");
    }

    /**
     * Generate cross reference text (xreftext) that can be used to refer to this section.
     *
     * @see AbstractBlock#getXreftext(String)
     */
    @Override
    public String getXreftext(String xrefstyle) {
        String val = getReftext();
        if (val != null && !val.isEmpty()) {
            return val;
        } else if (xrefstyle != null) {
            if (isNumbered()) {
                switch (xrefstyle) {
                    case "full" -> {
                        String type = sectname;
                        String quotedTitle;
                        if ("chapter".equals(type) || "appendix".equals(type)) {
                            quotedTitle = subPlaceholder(subQuotes("_%s_"), getTitle());
                        } else {
                            quotedTitle = subPlaceholder(subQuotes(document.isCompatMode() ? "``%s''" : "\"`%s`\""),
                                    getTitle());
                        }
                        Object signifier = document.getAttributes().get(type + "-refsig");
                        if (signifier != null) {
                            return signifier + " " + getSectnum(".", ",") + " " + quotedTitle;
                        }
                        return getSectnum(".", ",") + " " + quotedTitle;
                    }
                    case "short" -> {
                        Object signifier = document.getAttributes().get(sectname + "-refsig");
                        if (signifier != null) {
                            return signifier + " " + getSectnum(".", "");
                        }
                        return getSectnum(".", "");
                    }
                    default -> { // 'basic'
                        return "chapter".equals(sectname) || "appendix".equals(sectname)
                                ? subPlaceholder(subQuotes("_%s_"), getTitle()) : getTitle();
                    }
                }
            }
            // apply basic styling
            return "chapter".equals(sectname) || "appendix".equals(sectname)
                    ? subPlaceholder(subQuotes("_%s_"), getTitle()) : getTitle();
        }
        return getTitle();
    }

    /**
     * Append a content block to this block's list of blocks.
     *
     * <p>If the child block is a Section, assign an index to it.
     *
     * @param block The child Block to append to this parent Block
     * @return The parent Block
     */
    @Override
    public AbstractBlock append(AbstractBlock block) {
        if ("section".equals(block.getContext()) && block instanceof Section section) {
            assignNumeral(section);
        }
        return super.append(block);
    }

    @Override
    public String toString() {
        if (title != null) {
            String formalTitle = isNumbered() ? getSectnum() + " " + title : title;
            return "#<" + getClass().getName() + "@" + System.identityHashCode(this) + " {level: " + level
                    + ", title: " + Strings.inspect(formalTitle) + ", blocks: " + blocks.size() + "}>";
        }
        return super.toString();
    }

    /**
     * Generate a String ID from the given section title.
     *
     * <p>The generated ID is prefixed with value of the 'idprefix' attribute, which is an underscore (_) by default.
     * Invalid characters are then removed and spaces are replaced with the value of the 'idseparator' attribute, which
     * is an underscore (_) by default.
     *
     * <p>If the generated ID is already in use in the document, a count is appended, offset by the separator, until a
     * unique ID is found.
     *
     * <p>Section ID generation can be disabled by unsetting the 'sectids' document attribute.
     *
     * <pre>
     *   Section.generateId("Foo", document)
     *   // => "_foo"
     * </pre>
     *
     * @return the generated String ID.
     */
    public static String generateId(String title, Document document) {
        Map<String, Object> attrs = document.getAttributes();
        Object prefixVal = attrs.get("idprefix");
        String pre = prefixVal == null ? "_" : prefixVal.toString();
        String sep;
        String sepSub = null;
        boolean noSep = false;
        Object sepVal = attrs.get("idseparator");
        if (sepVal != null) {
            sep = sepVal.toString();
            if (sep.length() == 1 || (!(noSep = sep.isEmpty()))) {
                if (sep.length() > 1) {
                    sep = Strings.chr(sep);
                    attrs.put("idseparator", sep);
                }
                sepSub = sep.equals("-") || sep.equals(".") ? " .-" : " " + sep + ".-";
            }
        } else {
            sep = "_";
            sepSub = " _.-";
        }
        String genId = pre + Rx.gsub(Rx.INVALID_SECTION_ID_CHARS_RX, title.toLowerCase(), "");
        if (noSep) {
            genId = genId.replace(" ", "");
        } else {
            // replace space with separator and remove repeating and trailing separator characters
            genId = Strings.trS(genId, sepSub, sep);
            if (genId.endsWith(sep)) {
                genId = Strings.chop(genId);
            }
            // ensure id doesn't begin with idseparator if idprefix is empty (assuming idseparator is not empty)
            if (pre.isEmpty() && genId.startsWith(sep)) {
                genId = genId.substring(1);
            }
        }
        Map<String, AbstractNode> refs = document.getCatalog().getRefs();
        if (refs.containsKey(genId)) {
            int cnt = Compliance.getUniqueIdStartIndex();
            String candidateId;
            while (refs.get(candidateId = genId + sep + cnt) != null) {
                cnt++;
            }
            return candidateId;
        }
        return genId;
    }
}
