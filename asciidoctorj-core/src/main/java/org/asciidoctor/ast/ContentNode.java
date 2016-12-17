package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface ContentNode {

    /**
     * @deprecated Please use {@link #getId()}
     * @return A unique ID for this node
     */
    String id();

    /**
     * @return A unique ID for this node
     */
    String getId();

    void setId(String id);

    String getNodeName();
    /**
     * @deprecated Use {@linkplain #getParent()}  instead.
     */
    ContentNode parent();
    ContentNode getParent();
    /**
     * @deprecated Use {@linkplain #getContext()}  instead.
     */
    String context();
    String getContext();
    /**
     * @deprecated Use {@linkplain #getDocument()}  instead.
     */
    Document document();
    Document getDocument();
    boolean isInline();
    boolean isBlock();
    Map<String, Object> getAttributes();
    Object getAttr(Object name, Object defaultValue, boolean inherit);
    Object getAttr(Object name, Object defaultValue);
    Object getAttr(Object name);

    /**
     * @param name
     * @return {@code true} if this node or the document has an attribute with the given name
     */
    boolean hasAttr(Object name);

    /**
     *
     * @param name
     * @param inherited
     * @return {@code true} if the current node or depending on the inherited parameter the document has an attribute with the given name.
     */
    boolean hasAttr(Object name, boolean inherited);
    boolean isAttr(Object name, Object expected);
    boolean isAttr(Object name, Object expected, boolean inherit);
    boolean setAttr(Object name, Object value, boolean overwrite);
    boolean isOption(Object name);
    //boolean isRole(String expect); cannot be implemented because it tries to use the non argument isRole.
    boolean isRole();
    boolean hasRole(String role);
    String getRole();
    /**
     * @deprecated Use {@linkplain #getRole()}  instead.
     */
    String role();
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
