package org.asciidoctor.ast;

public interface Section extends StructuralNode {

    /**
     * @return the 0-based index order of this section within the parent block
     */
    int getIndex();

    /**
     * Section numeral.
     * <p>
     * This is a roman numeral for book parts, arabic numeral for regular sections,
     * and a letter for special sections.
     *
     * @return the section numeral
     */
    String getNumeral();

    /**
     * @return the section name of this section
     */
    String getSectionName();

    /**
     * @return Get the flag to indicate whether this is a special section or a child of one
     */
    boolean isSpecial();

    /**
     * @return the state of the numbered attribute at this section (need to preserve for creating TOC)
     */
    boolean isNumbered();

    /**
     * Get the section number for the current Section.
     * <p>
     * The section number is a dot-separated String that uniquely describes the position of this
     * Section in the document. Each entry represents a level of nesting. The value of each entry is
     * the 1-based outline number of the Section amongst its numbered sibling Sections.
     */
    String getSectnum();

    /**
     * Get the section number for the current Section.
     * <p>
     * The section number is a dot-separated String that uniquely describes the position of this
     * Section in the document. Each entry represents a level of nesting. The value of each entry is
     * the 1-based outline number of the Section amongst its numbered sibling Sections.
     *
     * @param delimiter the delimiter to separate the number for each level
     */
    String getSectnum(String delimiter);
}
