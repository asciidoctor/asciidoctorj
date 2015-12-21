package org.asciidoctor.ast;

public interface DescriptionListEntry {

    java.util.List<ListItem> getTerms();

    ListItem getDescription();

}
