package org.asciidoctor.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Structure document containing header and content chunks
 * 
 * @author marek
 * 
 */
public class StructuredDocument {

    private DocumentHeader header;

    private List<ContentPart> parts;

    private StructuredDocument() {
        super();
    }

    public List<ContentPart> getParts() {
        return parts;
    }

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
    public ContentPart getPartById(String id) {
        if (id != null) {
            for (ContentPart part : parts) {
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
    public ContentPart getPartByStyle(String style) {
        if (style != null) {
            for (ContentPart part : parts) {
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
    public ContentPart getPartByRole(String role) {
        if (role != null) {
            for (ContentPart part : parts) {
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
    public List<ContentPart> getPartsByContext(String context) {
        if (context != null) {
            List<ContentPart> filteredParts = new ArrayList<ContentPart>();
            for (ContentPart part : parts) {
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
    public List<ContentPart> getPartsByStyle(String style) {
        if (style != null) {
            List<ContentPart> filteredParts = new ArrayList<ContentPart>();
            for (ContentPart part : parts) {
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
    public List<ContentPart> getPartsByRole(String role) {
        if (role != null) {
            List<ContentPart> filteredParts = new ArrayList<ContentPart>();
            for (ContentPart part : parts) {
                if (role.equals(part.getRole())) {
                    filteredParts.add(part);
                }
            }
            return filteredParts;
        }
        return Collections.emptyList();
    }

    public static StructuredDocument createStructuredDocument(DocumentHeader header, List<ContentPart> parts) {
        StructuredDocument document = new StructuredDocument();
        document.header = header;
        document.parts = parts;

        return document;
    }
}
