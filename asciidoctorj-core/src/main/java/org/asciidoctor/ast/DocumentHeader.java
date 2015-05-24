package org.asciidoctor.ast;

import java.util.Map;

public interface DocumentHeader {

    java.util.List<? extends Author> getAuthors();

    Title getDocumentTitle();

    String getPageTitle();

    Author getAuthor();

    RevisionInfo getRevisionInfo();

    Map<String, Object> getAttributes();
}
