package org.asciidoctor.ast;

public interface DescriptionListEntry {

    java.util.List<ListItem> getTerms();

    ListItem getDescription();

    /**
     * Sets a new description for a description list item.
     * Description list items are ordinary ListItems and can be created using the factory methods of
     * a processor:
     * <code><pre>
     * class MyTreeprocessor extends Treeprocessor() {
     *   public Document process(Document doc) {
     *     final String newDescription = "A new description for this entry.";
     *     DescriptionList dl = ...
     *     DescriptionListEntry dlEntry = ...
     *     dlEntry.setDescription(createListItem(dl, newDescription));
     *     return doc;
     *   }
     * }</pre></code>
     * @param description The new description for this description list entry,
     */
    void setDescription(final ListItem description);

}
