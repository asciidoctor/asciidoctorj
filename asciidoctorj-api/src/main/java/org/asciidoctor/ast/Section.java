package org.asciidoctor.ast;

public interface Section extends StructuralNode {

    /**
     * @deprecated Please use {@link #getIndex()}
     * @return the 0-based index order of this section within the parent block
     */
    @Deprecated
    int index();

    /**
     * @return the 0-based index order of this section within the parent block
     */
    int getIndex();

    /**
     * @deprecated Please use {@link #getNumber()}
     * @return the number of this section within the parent block
     */
    @Deprecated
    int number();

    /**
     * @deprecated Please use {@link #getNumeral()}
     * @return the number of this section within the parent block
     */
    @Deprecated
    int getNumber();

    /**
     * Section numeral.
     *
     * This is a roman numeral for book parts, arabic numeral for regular sections,
     * and a letter for special sections.
     *
     * @return the section numeral
     */
    String getNumeral();

    /**
     * @deprecated Please use {@link #getSectionName()}
     * @return the section name of this section
     */
    @Deprecated
    String sectname();

    /**
     * @return the section name of this section
     */
    String getSectionName();

    /**
     * @deprecated Please use {@link #isSpecial()}
     * @return Get the flag to indicate whether this is a special section or a child of one
     */
    @Deprecated
    boolean special();

    /**
     * @return Get the flag to indicate whether this is a special section or a child of one
     */
    boolean isSpecial();

    /**
     * @deprecated Please use {@link #isNumbered()}
     * @return the state of the numbered attribute at this section (need to preserve for creating TOC)
     */
    @Deprecated
    boolean numbered();

    /**
     * @return the state of the numbered attribute at this section (need to preserve for creating TOC)
     */
    boolean isNumbered();

}
