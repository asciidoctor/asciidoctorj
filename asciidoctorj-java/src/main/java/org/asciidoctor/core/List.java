package org.asciidoctor.core;

/**
 * Methods for managing AsciiDoc lists (ordered, unordered and description lists).
 *
 * <p>In a description list (dlist), each item is a {@link DescriptionListEntry} that consists of a List of ListItem
 * terms and a ListItem description. If a description is not set, then the description of the entry is null.
 */
public class List extends AbstractBlock {

    public List(AbstractNode parent, String context, java.util.Map<String, Object> attributes) {
        super(parent, context, attributes);
    }

    public List(AbstractNode parent, String context) {
        super(parent, context);
    }

    /**
     * @return the items in this list (alias for blocks)
     */
    public java.util.List<AbstractBlock> getItems() {
        return blocks;
    }

    /**
     * @return whether this list has items (alias for hasBlocks)
     */
    public boolean hasItems() {
        return hasBlocks();
    }

    /**
     * Check whether this list is an outline list (unordered or ordered).
     *
     * @return true if this list is an outline list. Otherwise, return false.
     */
    public boolean isOutline() {
        return "ulist".equals(context) || "olist".equals(context);
    }

    @Override
    public String convert() {
        if ("colist".equals(context)) {
            String result = super.convert();
            document.getCallouts().nextList();
            return result;
        }
        return super.convert();
    }

    @Override
    public String toString() {
        return "#<" + getClass().getName() + "@" + System.identityHashCode(this) + " {context: " + context
                + ", style: " + Strings.inspect(style) + ", items: " + blocks.size() + "}>";
    }
}
