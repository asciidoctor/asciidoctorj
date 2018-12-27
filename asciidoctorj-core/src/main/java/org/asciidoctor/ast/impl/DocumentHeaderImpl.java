package org.asciidoctor.ast.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.asciidoctor.api.ast.Author;
import org.asciidoctor.api.ast.DocumentHeader;
import org.asciidoctor.api.ast.RevisionInfo;
import org.asciidoctor.api.ast.Title;
import org.asciidoctor.internal.CaseInsensitiveMap;

public class DocumentHeaderImpl implements DocumentHeader {

    private Title documentTitle;
    private String pageTitle;
    private Author author;
    private List<Author> authors = new ArrayList<Author>();
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
        return author;
    }

    public RevisionInfo getRevisionInfo() {
        return revisionInfo;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static DocumentHeaderImpl createDocumentHeader(Title documentTitle, String pageTitle,
            Map<String, Object> attributes) {

        DocumentHeaderImpl documentHeader = new DocumentHeaderImpl();

        documentHeader.documentTitle = documentTitle;
        documentHeader.pageTitle = pageTitle;
        documentHeader.attributes = new CaseInsensitiveMap<String, Object>(attributes);

        documentHeader.author = getAuthor(attributes);
        documentHeader.revisionInfo = getRevisionInfo(attributes);
        documentHeader.authors.addAll(getAuthors(attributes));

        return documentHeader;
    }

    private static List<Author> getAuthors(Map<String, Object> attributes) {
        return AuthorImpl.getAuthors(attributes);
    }

    private static Author getAuthor(Map<String, Object> attributes) {
        return AuthorImpl.getInstance(attributes);
    }

    private static RevisionInfo getRevisionInfo(Map<String, Object> attributes) {
        return RevisionInfoImpl.getInstance(attributes);
    }

}
