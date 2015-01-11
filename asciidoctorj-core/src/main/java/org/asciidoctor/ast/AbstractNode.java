package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface AbstractNode {

    String id();
    String getNodeName();
    /**
     * @deprecated Use {@linkplain #getParent()}  instead.
     */
    AbstractNode parent();
    AbstractNode getParent();
    /**
     * @deprecated Use {@linkplain #getContext()}  instead.
     */
    String context();
    String getContext();
    /**
     * @deprecated Use {@linkplain #getDocument()}  instead.
     */
    DocumentRuby document();
    DocumentRuby getDocument();
    boolean isInline();
    boolean isBlock();
    Map<String, Object> getAttributes();
    Object getAttr(Object name, Object defaultValue, boolean inherit);
    Object getAttr(Object name, Object defaultValue);
    Object getAttr(Object name);
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
    boolean isReftext();
    String getReftext();
    String iconUri(String name);
    String mediaUri(String target);
    String imageUri(String targetImage);
    String imageUri(String targetImage, String assetDirKey);
    String readAsset(String path, Map<Object, Object> opts);
    String normalizeWebPath(String path, String start, boolean preserveUriTarget);

    String getStyle();

    String listMarkerKeyword();
    String listMarkerKeyword(String listType);
}
