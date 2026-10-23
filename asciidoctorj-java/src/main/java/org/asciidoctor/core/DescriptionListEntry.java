package org.asciidoctor.core;

import java.util.ArrayList;

/**
 * An entry in a description list (dlist), which consists of one or more terms and an optional description.
 *
 * <p>The terms and the description are ListItem nodes whose parent is the description list.
 */
public class DescriptionListEntry extends AbstractBlock {
    private final java.util.List<ListItem> terms;
    private ListItem description;

    public DescriptionListEntry(List parent, java.util.List<ListItem> terms, ListItem description) {
        super(parent, "dlist_item");
        this.terms = new ArrayList<>(terms);
        this.description = description;
    }

    /**
     * @return the terms of this entry
     */
    public java.util.List<ListItem> getTerms() {
        return terms;
    }

    /**
     * @return the description of this entry, or null if there's no description
     */
    public ListItem getDescription() {
        return description;
    }

    public void setDescription(ListItem description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "#<" + getClass().getName() + "@" + System.identityHashCode(this) + " {terms: " + terms.size()
                + ", description: " + description + "}>";
    }
}
