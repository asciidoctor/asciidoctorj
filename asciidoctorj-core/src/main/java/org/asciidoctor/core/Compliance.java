package org.asciidoctor.core;

import java.util.Set;

/**
 * Flags to control compliance with the behavior of AsciiDoc.
 */
public final class Compliance {
    private static final Set<String> KEYS = Set.of(
            "block_terminates_paragraph",
            "strict_verbatim_paragraphs",
            "underline_style_section_titles",
            "unwrap_standalone_preamble",
            "attribute_missing",
            "attribute_undefined",
            "shorthand_property_syntax",
            "natural_xrefs",
            "unique_id_start_index",
            "markdown_syntax");

    // AsciiDoc terminates paragraphs adjacent to block content (delimiter or block attribute list)
    // This option allows this behavior to be modified
    // Compliance value: true
    private static boolean blockTerminatesParagraph = true;

    // AsciiDoc does not parse paragraphs with a verbatim style (i.e., literal, listing, source, verse) as verbatim
    // content. This options allows this behavior to be modified
    // Compliance value: false
    private static boolean strictVerbatimParagraphs = true;

    // AsciiDoc supports both atx (single-line) and setext (underlined) section titles.
    // This option can be used to disable the setext variant.
    // Compliance value: true
    private static boolean underlineStyleSectionTitles = true;

    // Asciidoctor will unwrap the content in a preamble if the document has a title and no sections, then discard the
    // empty preamble.
    // Compliance value: false
    private static boolean unwrapStandalonePreamble = true;

    // AsciiDoc drops lines that contain references to missing attributes. This behavior is not intuitive to most
    // writers. Asciidoctor allows this behavior to be configured.
    // Possible options are 'skip', 'drop', 'drop-line', and 'warn'.
    // Compliance value: 'drop-line'
    private static String attributeMissing = "skip";

    // AsciiDoc drops lines that contain an attribute unassignment.
    // This behavior may need to be tuned depending on the circumstances.
    // Compliance value: 'drop-line'
    private static String attributeUndefined = "drop-line";

    // Asciidoctor will allow the id, role and options to be set on blocks using a shorthand syntax
    // (e.g., #idname.rolename%optionname)
    // Compliance value: false
    private static boolean shorthandPropertySyntax = true;

    // Asciidoctor will attempt to resolve the target of a cross reference by matching its reference text (reftext or
    // title) (e.g., <<Section Title>>)
    // Compliance value: false
    private static boolean naturalXrefs = true;

    // Asciidoctor will start counting at the following number when creating a unique id when there is a conflict
    // Compliance value: 2
    private static int uniqueIdStartIndex = 2;

    // Asciidoctor will recognize commonly-used Markdown syntax to the degree it does not interfere with existing
    // AsciiDoc syntax and behavior.
    // Compliance value: false
    private static boolean markdownSyntax = true;

    private Compliance() {
    }

    /**
     * @return the names of the compliance keys
     */
    public static Set<String> keys() {
        return KEYS;
    }

    public static boolean isBlockTerminatesParagraph() {
        return blockTerminatesParagraph;
    }

    public static void setBlockTerminatesParagraph(boolean value) {
        blockTerminatesParagraph = value;
    }

    public static boolean isStrictVerbatimParagraphs() {
        return strictVerbatimParagraphs;
    }

    public static void setStrictVerbatimParagraphs(boolean value) {
        strictVerbatimParagraphs = value;
    }

    public static boolean isUnderlineStyleSectionTitles() {
        return underlineStyleSectionTitles;
    }

    public static void setUnderlineStyleSectionTitles(boolean value) {
        underlineStyleSectionTitles = value;
    }

    public static boolean isUnwrapStandalonePreamble() {
        return unwrapStandalonePreamble;
    }

    public static void setUnwrapStandalonePreamble(boolean value) {
        unwrapStandalonePreamble = value;
    }

    public static String getAttributeMissing() {
        return attributeMissing;
    }

    public static void setAttributeMissing(String value) {
        attributeMissing = value;
    }

    public static String getAttributeUndefined() {
        return attributeUndefined;
    }

    public static void setAttributeUndefined(String value) {
        attributeUndefined = value;
    }

    public static boolean isShorthandPropertySyntax() {
        return shorthandPropertySyntax;
    }

    public static void setShorthandPropertySyntax(boolean value) {
        shorthandPropertySyntax = value;
    }

    public static boolean isNaturalXrefs() {
        return naturalXrefs;
    }

    public static void setNaturalXrefs(boolean value) {
        naturalXrefs = value;
    }

    public static int getUniqueIdStartIndex() {
        return uniqueIdStartIndex;
    }

    public static void setUniqueIdStartIndex(int value) {
        uniqueIdStartIndex = value;
    }

    public static boolean isMarkdownSyntax() {
        return markdownSyntax;
    }

    public static void setMarkdownSyntax(boolean value) {
        markdownSyntax = value;
    }
}
