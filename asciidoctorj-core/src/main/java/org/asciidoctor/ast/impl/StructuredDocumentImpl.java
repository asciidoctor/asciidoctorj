package org.asciidoctor.ast.impl;

import org.asciidoctor.ast.DocumentHeader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Structure document containing header and content chunks
 * 
 * @author marek
 * 
 */
public class StructuredDocumentImpl implements org.asciidoctor.ast.StructuredDocument {

    private DocumentHeader header;

    private List<ContentPartImpl> parts;

    private StructuredDocumentImpl() {
        super();
    }

    @Override
    public List<ContentPartImpl> getParts() {
        return parts;
    }

    @Override
    public DocumentHeader getHeader() {
        return header;
    }

    /**
     * Return first matched part by id
     * 
     * @param id The id to match
     * @return The ContentPart if the id is not null and the document
     *         contains a block-level element with this id or null otherwise.
     */
    @Override
    public ContentPartImpl getPartById(String id) {
        if (id != null) {
            for (ContentPartImpl part : parts) {
                if (id.equals(part.getId())) {
                    return part;
                }
            }
        }
        return null;
    }

    /**
     * Return first matched part by style
     * 
     * @param style The style to match
     * @return The first ContentPart if the style is not null and the document
     *         contains a block-level element with this style name or null otherwise.
     */
    @Override
    public ContentPartImpl getPartByStyle(String style) {
        if (style != null) {
            for (ContentPartImpl part : parts) {
                if (style.equals(part.getStyle())) {
                    return part;
                }
            }
        }
        return null;
    }

    /**
     * Return first matched part by role
     * 
     * @param role The role to match
     * @return The first ContentPart if the role is not null and the document
     *         contains a block-level element with this role name or null otherwise.
     */
    @Override
    public ContentPartImpl getPartByRole(String role) {
        if (role != null) {
            for (ContentPartImpl part : parts) {
                if (role.equals(part.getRole())) {
                    return part;
                }
            }
        }
        return null;
    }

    /**
     * Return all parts that match specified context
     * 
     * @param context The context to match
     * @return A list of ContentPart items that match the context if the
     *         context is not null or an empty collection.
     */
    @Override
    public List<ContentPartImpl> getPartsByContext(String context) {
        if (context != null) {
            List<ContentPartImpl> filteredParts = new ArrayList<ContentPartImpl>();
            for (ContentPartImpl part : parts) {
                if (context.equals(part.getContext())) {
                    filteredParts.add(part);
                }
            }
            return filteredParts;
        }
        return Collections.emptyList();
    }

    /**
     * Return all parts that match specified style
     * 
     * @param style The style to match
     * @return A list of ContentPart items that match the style if the
     *         style is not null or an empty collection.
     */
    @Override
    public List<ContentPartImpl> getPartsByStyle(String style) {
        if (style != null) {
            List<ContentPartImpl> filteredParts = new ArrayList<ContentPartImpl>();
            for (ContentPartImpl part : parts) {
                if (style.equals(part.getStyle())) {
                    filteredParts.add(part);
                }
            }
            return filteredParts;
        }
        return Collections.emptyList();
    }

    /**
     * Return all parts that match specified role
     * 
     * @param role The role to match
     * @return A list of ContentPart items that match the role if the
     *         role is not null or an empty collection.
     */
    @Override
    public List<ContentPartImpl> getPartsByRole(String role) {
        if (role != null) {
            List<ContentPartImpl> filteredParts = new ArrayList<ContentPartImpl>();
            for (ContentPartImpl part : parts) {
                if (role.equals(part.getRole())) {
                    filteredParts.add(part);
                }
            }
            return filteredParts;
        }
        return Collections.emptyList();
    }

    public static StructuredDocumentImpl createStructuredDocument(DocumentHeader header, List<ContentPartImpl> parts) {
        StructuredDocumentImpl document = new StructuredDocumentImpl();
        document.header = header;
        document.parts = parts;

        return document;
    }
}
