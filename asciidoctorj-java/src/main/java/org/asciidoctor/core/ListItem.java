package org.asciidoctor.core;

import static org.asciidoctor.core.Asciidoctor.LF;

import java.util.ArrayList;

/**
 * Methods for managing items for AsciiDoc olists, ulist, and dlists.
 */
public class ListItem extends AbstractBlock {
    private String marker;
    private String text;

    /**
     * Initialize a ListItem object.
     *
     * @param parent The parent list block for this list item
     * @param text the String text (default null)
     */
    public ListItem(AbstractBlock parent, String text) {
        super(parent, "list_item");
        this.text = text;
        this.level = parent.getLevel();
        this.subs = new ArrayList<>(NORMAL_SUBS);
    }

    public ListItem(AbstractBlock parent) {
        this(parent, null);
    }

    /**
     * A contextual alias for the list parent node; counterpart to the items alias on List.
     */
    public AbstractBlock getList() {
        return (AbstractBlock) parent;
    }

    /**
     * @return the String used to mark this list item
     */
    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    /**
     * A convenience method that checks whether the text of this list item is not blank (i.e., not null or empty
     * string).
     */
    public boolean hasText() {
        return !Strings.isNullOrEmpty(text);
    }

    /**
     * Get the String text of this ListItem with substitutions applied.
     *
     * <p>By default, normal substitutions are applied to the text. The substitutions can be modified by altering the
     * subs property of this object.
     *
     * @return the converted String text for this ListItem
     */
    public String getText() {
        // NOTE text can be null if dd node only has block content
        return text == null ? null : applySubs(text, subs);
    }

    /**
     * Set the String text assigned to this ListItem.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the source (unconverted) text of this list item
     */
    public String getSource() {
        return text;
    }

    /**
     * Check whether this list item has simple content (no nested blocks aside from a single outline list). Primarily
     * relevant for outline lists.
     *
     * @return true if the list item contains no blocks or it contains a single outline list. Otherwise, return false.
     */
    public boolean isSimple() {
        return blocks.isEmpty() || (blocks.size() == 1 && blocks.get(0) instanceof List list && list.isOutline());
    }

    /**
     * Check whether this list item has compound content (nested blocks aside from a single outline list). Primarily
     * relevant for outline lists.
     *
     * @return true if the list item contains blocks other than a single outline list. Otherwise, return false.
     */
    public boolean isCompound() {
        return !isSimple();
    }

    /**
     * Internal: Fold the adjacent paragraph block into the list item text.
     */
    public void foldFirst() {
        Block first = (Block) blocks.remove(0);
        text = Strings.isNullOrEmpty(text) ? first.getSource() : text + LF + first.getSource();
    }

    @Override
    public String toString() {
        return "#<" + getClass().getName() + "@" + System.identityHashCode(this) + " {list_context: "
                + parent.getContext() + ", text: " + Strings.inspect(text) + ", blocks: " + blocks.size() + "}>";
    }
}
