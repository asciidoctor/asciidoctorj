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
     * @param id
     * @return
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
     * @param style
     * @return
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
     * @param style
     * @return
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
     * @param style
     * @return
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
     * @param style
     * @return
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
     * @param style
     * @return
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
