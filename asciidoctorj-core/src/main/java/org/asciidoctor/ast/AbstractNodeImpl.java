package org.asciidoctor.ast;

import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.java.proxies.RubyObjectHolderProxy;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;
import java.util.Map;

public abstract class AbstractNodeImpl implements AbstractNode {

    protected Ruby runtime;
    protected AbstractNode abstractNode;

    public AbstractNodeImpl(AbstractNode abstractNode, Ruby ruby) {
        if (!(abstractNode instanceof IRubyObject) && !(abstractNode instanceof RubyObjectHolderProxy)) {
            throw new IllegalArgumentException("Node delegate must be an IRubyObject or RubyObjectHolderProxy!");
        }
        this.runtime = ruby;
        this.abstractNode = abstractNode;
    }

    @Override
    public String id() {
        return this.abstractNode.id();
    }

    @Override
    public String context() {
        return getContext();
    }

    @Override
    public String getContext() {
        return this.abstractNode.getContext();
    }

    @Override
    public AbstractNode parent() {
        return getParent();
    }

    @Override
    public AbstractNode getParent() {
        return NodeConverter.createASTNode(this.abstractNode.getParent());
    }

    @Override
    public DocumentRuby document() {
        return getDocument();
    }

    @Override
    public DocumentRuby getDocument() {
        return (DocumentRuby) NodeConverter.createASTNode(this.abstractNode.getDocument());
    }

    @Override
    public String getNodeName() {
        return this.abstractNode.getNodeName();
    }

    @Override
    public boolean isInline() {
        return this.abstractNode.isInline();
    }

    @Override
    public boolean isBlock() {
        return this.abstractNode.isBlock();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.abstractNode.getAttributes();
    }

    @Override
    public Object getAttr(Object name, Object defaultValue, boolean inherit) {
        return this.abstractNode.getAttr(name, defaultValue, inherit);
    }

    @Override
    public Object getAttr(Object name, Object defaultValue) {
        return this.abstractNode.getAttr(name, defaultValue, true);
    }

    @Override
    public Object getAttr(Object name) {
        return this.abstractNode.getAttr(name, null, true);
    }

    @Override
    public boolean isAttr(Object name, Object expected, boolean inherit) {
        return this.abstractNode.isAttr(name, expected, inherit);
    }

    @Override
    public boolean isAttr(Object name, Object expected) {
        return this.abstractNode.isAttr(name, expected, true);
    }

    @Override
    public boolean setAttr(Object name, Object value, boolean overwrite) {
        return this.abstractNode.setAttr(name, value, overwrite);
    }

    @Override
    public boolean isOption(Object name) {
        return this.abstractNode.isOption(name);
    }

    @Override
    public boolean isRole() {
        return this.abstractNode.isRole();
    }

    @Override
    public String getRole() {
        return this.abstractNode.getRole();
    }

    @Override
    public String role() {
        return this.abstractNode.role();
    }

    @Override
    public List<String> getRoles() {
        return this.abstractNode.getRoles();
    }

    @Override
    public boolean hasRole(String role) {
        return this.abstractNode.hasRole(role);
    }

    @Override
    public boolean isReftext() {
        return this.abstractNode.isReftext();
    }

    @Override
    public String getReftext() {
        return this.abstractNode.getReftext();
    }

    @Override
    public String iconUri(String name) {
        return this.abstractNode.iconUri(name);
    }

    @Override
    public String mediaUri(String target) {
        return this.abstractNode.mediaUri(target);
    }

    @Override
    public String imageUri(String targetImage) {
        return this.abstractNode.imageUri(targetImage);
    }

    @Override
    public String imageUri(String targetImage, String assetDirKey) {
        return this.abstractNode.imageUri(targetImage, assetDirKey);
    }

    @Override
    public String readAsset(String path, Map<Object, Object> opts) {
        return this.abstractNode.readAsset(path, RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime, opts));
    }

    @Override
    public String normalizeWebPath(String path, String start, boolean preserveUriTarget) {
        return this.abstractNode.normalizeWebPath(path, start, preserveUriTarget);
    }

    @Override
    public String getStyle() {

        IRubyObject style = ((RubyObjectHolderProxy) this.abstractNode).__ruby_object()
                .getInstanceVariables()
                .getInstanceVariable("@style");

        if (style == null) {
            return null;
        } else {
            return RubyUtils.rubyToJava(runtime, style, String.class);
        }
    }

    @Override
    public String listMarkerKeyword() {
        return abstractNode.listMarkerKeyword();
    }

    @Override
    public String listMarkerKeyword(String listType) {
        return abstractNode.listMarkerKeyword(listType);
    }

    /**
     * @return The nodes delegate in the Ruby world
     */
    public AbstractNode getDelegate() {
        return abstractNode;
    }
}
