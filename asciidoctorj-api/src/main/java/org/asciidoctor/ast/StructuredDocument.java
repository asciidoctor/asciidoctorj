package org.asciidoctor.ast;

import java.util.List;

/**
 * Structure document containing header and content chunks
 * 
 * @author marek
 * 
 */
public interface StructuredDocument {

    List<? extends ContentPart> getParts();

    DocumentHeader getHeader();

    /**
     * Return first matched part by id
     * 
     * @param id The id to match
     * @return The ContentPart if the id is not null and the document
     *         contains a block-level element with this id or null otherwise.
     */
    ContentPart getPartById(String id);

    /**
     * Return first matched part by style
     * 
     * @param style The style to match
     * @return The first ContentPart if the style is not null and the document
     *         contains a block-level element with this style name or null otherwise.
     */
    ContentPart getPartByStyle(String style);

    /**
     * Return first matched part by role
     * 
     * @param role The role to match
     * @return The first ContentPart if the role is not null and the document
     *         contains a block-level element with this role name or null otherwise.
     */
    ContentPart getPartByRole(String role);

    /**
     * Return all parts that match specified context
     * 
     * @param context The context to match
     * @return A list of ContentPart items that match the context if the
     *         context is not null or an empty collection.
     */
    List<? extends ContentPart> getPartsByContext(String context);

    /**
     * Return all parts that match specified style
     * 
     * @param style The style to match
     * @return A list of ContentPart items that match the style if the
     *         style is not null or an empty collection.
     */
    List<? extends ContentPart> getPartsByStyle(String style);

    /**
     * Return all parts that match specified role
     * 
     * @param role The role to match
     * @return A list of ContentPart items that match the role if the
     *         role is not null or an empty collection.
     */
    List<? extends ContentPart> getPartsByRole(String role);
}
