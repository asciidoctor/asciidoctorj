package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.Author;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.ast.RevisionInfo;
import org.asciidoctor.ast.Title;
import org.asciidoctor.jruby.internal.CaseInsensitiveMap;

import java.util.List;
import java.util.Map;

public class DocumentHeaderImpl implements DocumentHeader {

    private Title documentTitle;
    private String pageTitle;
    private List<Author> authors;
    private RevisionInfo revisionInfo;

    private Map<String, Object> attributes;

    private DocumentHeaderImpl() {
        super();
    }

    public List<Author> getAuthors() {
        return this.authors;
    }

    public Title getDocumentTitle() {
        return documentTitle;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public Author getAuthor() {
        return authors == null || authors.size() == 0 ? null : authors.get(0);
    }

    public RevisionInfo getRevisionInfo() {
        return revisionInfo;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static DocumentHeaderImpl createDocumentHeader(Title documentTitle, String pageTitle,
                                                          List<Author> authors, Map<String, Object> attributes) {

        DocumentHeaderImpl documentHeader = new DocumentHeaderImpl();

        documentHeader.documentTitle = documentTitle;
        documentHeader.pageTitle = pageTitle;
        documentHeader.attributes = new CaseInsensitiveMap<>(attributes);
        documentHeader.authors = authors;
        documentHeader.revisionInfo = getRevisionInfo(attributes);

        return documentHeader;
    }

    private static RevisionInfo getRevisionInfo(Map<String, Object> attributes) {
        return RevisionInfoImpl.getInstance(attributes);
    }

}
