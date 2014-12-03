package org.asciidoctor.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.asciidoctor.internal.CaseInsensitiveMap;

public class DocumentHeader {

    private Title documentTitle;
    private String pageTitle;
    private Author author;
    private List<Author> authors = new ArrayList<Author>();
    private RevisionInfo revisionInfo;

    private Map<String, Object> attributes;

    private DocumentHeader() {
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

    public static DocumentHeader createDocumentHeader(Title documentTitle, String pageTitle,
            Map<String, Object> attributes) {

        DocumentHeader documentHeader = new DocumentHeader();

        documentHeader.documentTitle = documentTitle;
        documentHeader.pageTitle = pageTitle;
        documentHeader.attributes = new CaseInsensitiveMap<String, Object>(attributes);

        documentHeader.author = getAuthor(attributes);
        documentHeader.revisionInfo = geRevisionInfo(attributes);
        documentHeader.authors.addAll(getAuthors(attributes));

        return documentHeader;
    }

    private static List<Author> getAuthors(Map<String, Object> attributes) {
        return Author.getAuthors(attributes);
    }

    private static Author getAuthor(Map<String, Object> attributes) {
        return Author.getInstance(attributes);
    }

    private static RevisionInfo geRevisionInfo(Map<String, Object> attributes) {
        return RevisionInfo.getInstance(attributes);
    }

}
