package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface DocumentHeader {

    List<Author> getAuthors();

    Title getDocumentTitle();

    String getPageTitle();

    Author getAuthor();

    RevisionInfo getRevisionInfo();

    Map<String, Object> getAttributes();
}
