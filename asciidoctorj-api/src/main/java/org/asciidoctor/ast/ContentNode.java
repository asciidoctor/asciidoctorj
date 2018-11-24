package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface ContentNode {

    /**
     * @deprecated Please use {@link #getId()}
     * @return A unique ID for this node
     */
    @Deprecated
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
    @Deprecated
    ContentNode parent();
    ContentNode getParent();
    /**
     * @deprecated Use {@linkplain #getContext()}  instead.
     */
    @Deprecated
    String context();
    String getContext();
    /**
     * @deprecated Use {@linkplain #getDocument()}  instead.
     */
    @Deprecated
    Document document();
    Document getDocument();
    boolean isInline();
    boolean isBlock();
    Map<String, Object> getAttributes();

    /**
     *
     * @param name
     * @param defaultValue
     * @param inherit
     * @return
     * @deprecated Use {@link #getAttribute(Object, Object, boolean)} instead
     */
    @Deprecated
    Object getAttr(Object name, Object defaultValue, boolean inherit);

    /**
     *
     * @param name
     * @param defaultValue
     * @return
     * @deprecated Use {@link #getAttribute(Object, Object)} instead
     */
    @Deprecated
    Object getAttr(Object name, Object defaultValue);

    /**
     * @param name
     * @return
     * @deprecated Use {@link #getAttribute(Object)} instead
     */
    @Deprecated
    Object getAttr(Object name);

    Object getAttribute(Object name, Object defaultValue, boolean inherit);

    Object getAttribute(Object name, Object defaultValue);

    Object getAttribute(Object name);

    /**
     * @param name
     * @return {@code true} if this node or the document has an attribute with the given name
     * @deprecated Use {@link #hasAttribute(Object)} instead
     */
    @Deprecated
    boolean hasAttr(Object name);

    /**
     *
     * @param name
     * @param inherited
     * @return {@code true} if the current node or depending on the inherited parameter the document has an attribute with the given name.
     * @deprecated Use {@link #hasAttribute(Object, boolean)} instead
     */
    @Deprecated
    boolean hasAttr(Object name, boolean inherited);

    /**
     * @param name
     * @return {@code true} if this node or the document has an attribute with the given name
     */
    boolean hasAttribute(Object name);

    /**
     *
     * @param name
     * @param inherited
     * @return {@code true} if the current node or depending on the inherited parameter the document has an attribute with the given name.
     */
    boolean hasAttribute(Object name, boolean inherited);

    /**
     *
     * @param name
     * @param expected
     * @return
     * @deprecated Use {@link #isAttribute(Object, Object)} instead.
     */
    @Deprecated
    boolean isAttr(Object name, Object expected);

    /**
     *
     * @param name
     * @param expected
     * @param inherit
     * @return
     * @deprecated Use {@link #isAttribute(Object, Object, boolean)} instead.
     */
    @Deprecated
    boolean isAttr(Object name, Object expected, boolean inherit);

    /**
     *
     * @param name
     * @param expected
     * @return
     */
    boolean isAttribute(Object name, Object expected);

    /**
     *
     * @param name
     * @param expected
     * @param inherit
     * @return
     */
    boolean isAttribute(Object name, Object expected, boolean inherit);

    /**
     *
     * @param name
     * @param value
     * @param overwrite
     * @return
     * @deprecated Use {@link #setAttribute(Object, Object, boolean)} instead.
     */
    @Deprecated
    boolean setAttr(Object name, Object value, boolean overwrite);

    boolean setAttribute(Object name, Object value, boolean overwrite);



    boolean isOption(Object name);
    //boolean isRole(String expect); cannot be implemented because it tries to use the non argument isRole.
    boolean isRole();
    boolean hasRole(String role);
    String getRole();
    /**
     * @deprecated Use {@linkplain #getRole()}  instead.
     */
    @Deprecated
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
