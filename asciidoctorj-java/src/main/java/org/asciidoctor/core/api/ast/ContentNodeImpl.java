package org.asciidoctor.core.api.ast;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.AbstractNode;

/**
 * The {@link ContentNode} of the AsciidoctorJ API, which delegates to a node of the implementation.
 *
 * @param <T> the type of the node of the implementation
 */
public class ContentNodeImpl<T extends AbstractNode> implements ContentNode {
    protected final T delegate;

    public ContentNodeImpl(T delegate) {
        this.delegate = delegate;
    }

    /**
     * @return the node of the implementation
     */
    public T getDelegate() {
        return delegate;
    }

    /**
     * Converts the name of an attribute or an option (which may be any object in the API) to a String.
     */
    static String name(Object name) {
        return name == null ? null : name.toString();
    }

    /**
     * Converts a Map of options with Object keys (e.g., :subs or subs) to a Map with String keys.
     */
    static Map<String, Object> options(Map<?, ?> options) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (options != null) {
            options.forEach((key, value) -> result.put(String.valueOf(key).replaceFirst("^:", ""), value));
        }
        return result;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void setId(String id) {
        delegate.setId(id);
    }

    @Override
    public String getNodeName() {
        return delegate.getNodeName();
    }

    @Override
    public ContentNode getParent() {
        return NodeConverter.wrap(delegate.getParent());
    }

    @Override
    public String getContext() {
        return delegate.getContext();
    }

    @Override
    public void setContext(String context) {
        if (delegate instanceof AbstractBlock block) {
            block.setContext(context);
        } else {
            throw new UnsupportedOperationException("The context of a " + getNodeName() + " node can't be changed");
        }
    }

    @Override
    public Document getDocument() {
        return NodeConverter.wrap(delegate.getDocument());
    }

    @Override
    public boolean isInline() {
        return delegate.isInline();
    }

    @Override
    public boolean isBlock() {
        return delegate.isBlock();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Object getAttribute(Object name, Object defaultValue, boolean inherit) {
        return delegate.getAttribute(name(name), defaultValue, inherit);
    }

    @Override
    public Object getAttribute(Object name, Object defaultValue) {
        return delegate.getAttribute(name(name), defaultValue);
    }

    @Override
    public Object getAttribute(Object name) {
        return delegate.getAttribute(name(name));
    }

    @Override
    public boolean hasAttribute(Object name) {
        return delegate.hasAttribute(name(name));
    }

    @Override
    public boolean hasAttribute(Object name, boolean inherited) {
        return delegate.hasAttribute(name(name), inherited);
    }

    @Override
    public boolean isAttribute(Object name, Object expected) {
        return delegate.isAttribute(name(name), expected);
    }

    @Override
    public boolean isAttribute(Object name, Object expected, boolean inherit) {
        return delegate.isAttribute(name(name), expected, inherit);
    }

    @Override
    public boolean setAttribute(Object name, Object value, boolean overwrite) {
        return delegate.setAttribute(name(name), value, overwrite);
    }

    @Override
    public boolean isOption(Object name) {
        return delegate.isOption(name(name));
    }

    @Override
    public boolean isRole() {
        return delegate.isRole();
    }

    @Override
    public boolean hasRole(String role) {
        return delegate.hasRole(role);
    }

    @Override
    public String getRole() {
        return delegate.getRole();
    }

    @Override
    public List<String> getRoles() {
        return delegate.getRoles();
    }

    @Override
    public void addRole(String role) {
        delegate.addRole(role);
    }

    @Override
    public void removeRole(String role) {
        delegate.removeRole(role);
    }

    @Override
    public boolean isReftext() {
        return delegate.isReftext();
    }

    @Override
    public String getReftext() {
        return delegate.getReftext();
    }

    @Override
    public String iconUri(String name) {
        return delegate.iconUri(name);
    }

    @Override
    public String mediaUri(String target) {
        return delegate.mediaUri(target);
    }

    @Override
    public String imageUri(String targetImage) {
        return delegate.imageUri(targetImage);
    }

    @Override
    public String imageUri(String targetImage, String assetDirKey) {
        return delegate.imageUri(targetImage, assetDirKey);
    }

    @Override
    public String readAsset(String path, Map<Object, Object> opts) {
        return delegate.readAsset(path, options(opts));
    }

    @Override
    public String normalizeWebPath(String path, String start, boolean preserveUriTarget) {
        return delegate.normalizeWebPath(path, start, preserveUriTarget);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
