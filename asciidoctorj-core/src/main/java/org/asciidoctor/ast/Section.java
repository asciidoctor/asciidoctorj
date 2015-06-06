package org.asciidoctor.ast;

public interface Section extends AbstractBlock {

    /**
     * @deprecated Please use {@link #getIndex()}
     * @return the 0-based index order of this section within the parent block
     */
    int index();

    /**
     * @return the 0-based index order of this section within the parent block
     */
    int getIndex();

    /**
     * @deprecated Please use {@link #getNumber()}
     * @return the number of this section within the parent block
     */
    int number();

    /**
     * @return the number of this section within the parent block
     */
    int getNumber();

    /**
     * @deprecated Please use {@link #getSectionName()}
     * @return the section name of this section
     */
    String sectname();

    /**
     * @return the section name of this section
     */
    String getSectionName();

    /**
     * @deprecated Please use {@link #isSpecial()}
     * @return Get the flag to indicate whether this is a special section or a child of one
     */
    boolean special();

    /**
     * @return Get the flag to indicate whether this is a special section or a child of one
     */
    boolean isSpecial();

    /**
     * @deprecated Please use {@link #isNumbered()}
     * @return the state of the numbered attribute at this section (need to preserve for creating TOC)
     */
    boolean numbered();

    /**
     * @return the state of the numbered attribute at this section (need to preserve for creating TOC)
     */
    boolean isNumbered();

}
