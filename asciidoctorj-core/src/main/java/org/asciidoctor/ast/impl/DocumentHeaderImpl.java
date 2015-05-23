package org.asciidoctor.ast.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.Author;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.ast.RevisionInfo;
import org.asciidoctor.ast.Title;
import org.asciidoctor.internal.CaseInsensitiveMap;

public class DocumentHeaderImpl implements DocumentHeader {

    private Title documentTitle;
    private String pageTitle;
    private AuthorImpl author;
    private List<AuthorImpl> authors = new ArrayList<AuthorImpl>();
    private RevisionInfo revisionInfo;

    private Map<String, Object> attributes;

    private DocumentHeaderImpl() {
        super();
    }

    @Override
    public List<AuthorImpl> getAuthors() {
        return this.authors;
    }

    @Override
    public Title getDocumentTitle() {
        return documentTitle;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public AuthorImpl getAuthor() {
        return author;
    }

    @Override
    public RevisionInfo getRevisionInfo() {
        return revisionInfo;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static DocumentHeader createDocumentHeader(Title documentTitle, String pageTitle,
            Map<String, Object> attributes) {

        DocumentHeaderImpl documentHeader = new DocumentHeaderImpl();

        documentHeader.documentTitle = documentTitle;
        documentHeader.pageTitle = pageTitle;
        documentHeader.attributes = new CaseInsensitiveMap<String, Object>(attributes);

        documentHeader.author = getAuthor(attributes);
        documentHeader.revisionInfo = geRevisionInfo(attributes);
        documentHeader.authors.addAll(getAuthors(attributes));

        return documentHeader;
    }

    private static List<AuthorImpl> getAuthors(Map<String, Object> attributes) {
        return AuthorImpl.getAuthors(attributes);
    }

    private static AuthorImpl getAuthor(Map<String, Object> attributes) {
        return AuthorImpl.getInstance(attributes);
    }

    private static RevisionInfoImpl geRevisionInfo(Map<String, Object> attributes) {
        return RevisionInfoImpl.getInstance(attributes);
    }

}
