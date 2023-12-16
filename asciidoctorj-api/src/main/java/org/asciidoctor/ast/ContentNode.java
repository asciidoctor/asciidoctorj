package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface ContentNode {

    /**
     * @return A unique ID for this node
     */
    String getId();

    void setId(String id);

    String getNodeName();

    ContentNode getParent();

    String getContext();

    void setContext(String context);

    Document getDocument();

    boolean isInline();

    boolean isBlock();

    Map<String, Object> getAttributes();

    Object getAttribute(Object name, Object defaultValue, boolean inherit);

    Object getAttribute(Object name, Object defaultValue);

    Object getAttribute(Object name);

    /**
     * @param name
     * @return {@code true} if this node or the document has an attribute with the given name
     */
    boolean hasAttribute(Object name);

    /**
     * @param name
     * @param inherited
     * @return {@code true} if the current node or depending on the inherited parameter the document has an attribute with the given name.
     */
    boolean hasAttribute(Object name, boolean inherited);

    /**
     * @param name
     * @param expected
     * @return
     */
    boolean isAttribute(Object name, Object expected);

    /**
     * @param name
     * @param expected
     * @param inherit
     * @return
     */
    boolean isAttribute(Object name, Object expected, boolean inherit);

    /**
     * @param name
     * @param value
     * @param overwrite
     * @return
     */
    boolean setAttribute(Object name, Object value, boolean overwrite);


    boolean isOption(Object name);

    //boolean isRole(String expect); cannot be implemented because it tries to use the non argument isRole.
    boolean isRole();

    boolean hasRole(String role);

    String getRole();

    List<String> getRoles();

    void addRole(String role);

    void removeRole(String role);

    boolean isReftext();

    String getReftext();

    String iconUri(String name);

    String mediaUri(String target);

    String imageUri(String targetImage);

    String imageUri(String targetImage, String assetDirKey);

    String readAsset(String path, Map<Object, Object> opts);

    String normalizeWebPath(String path, String start, boolean preserveUriTarget);

}
