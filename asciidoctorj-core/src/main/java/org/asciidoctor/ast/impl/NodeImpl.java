package org.asciidoctor.ast.impl;

import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.Node;
import org.asciidoctor.ast.Document;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.java.proxies.RubyObjectHolderProxy;
import org.jruby.runtime.builtin.IRubyObject;

public abstract class NodeImpl implements Node {

    protected Ruby runtime;
    protected Node node;

    public NodeImpl(Node node, Ruby ruby) {
        this.runtime = ruby;
        this.node = node;
    }

    @Override
    public String id() {
        return this.node.id();
    }

    @Override
    public String context() {
        return getContext();
    }

    @Override
    public String getContext() {
        return this.node.getContext();
    }

    @Override
    public Node parent() {
        return getParent();
    }

    @Override
    public Node getParent() {
        return this.node.getParent();
    }

    @Override
    public Document document() {
        return getDocument();
    }

    @Override
    public Document getDocument() {
        return this.node.getDocument();
    }

    @Override
    public String getNodeName() {
        return this.node.getNodeName();
    }

    @Override
    public boolean isInline() {
        return this.node.isInline();
    }

    @Override
    public boolean isBlock() {
        return this.node.isBlock();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.node.getAttributes();
    }

    @Override
    public Object getAttr(Object name, Object defaultValue, boolean inherit) {
        return this.node.getAttr(name, defaultValue, inherit);
    }

    @Override
    public Object getAttr(Object name, Object defaultValue) {
        return this.node.getAttr(name, defaultValue, true);
    }

    @Override
    public Object getAttr(Object name) {
        return this.node.getAttr(name, null, true);
    }

    @Override
    public boolean isAttr(Object name, Object expected, boolean inherit) {
        return this.node.isAttr(name, expected, inherit);
    }

    @Override
    public boolean isAttr(Object name, Object expected) {
        return this.node.isAttr(name, expected, true);
    }

    @Override
    public boolean setAttr(Object name, Object value, boolean overwrite) {
        return this.node.setAttr(name, value, overwrite);
    }

    @Override
    public boolean isOption(Object name) {
        return this.node.isOption(name);
    }

    @Override
    public boolean isRole() {
        return this.node.isRole();
    }

    @Override
    public String getRole() {
        return this.node.getRole();
    }

    @Override
    public String role() {
        return this.node.role();
    }

    @Override
    public List<String> getRoles() {
        return this.node.getRoles();
    }

    @Override
    public boolean hasRole(String role) {
        return this.node.hasRole(role);
    }

    @Override
    public boolean isReftext() {
        return this.node.isReftext();
    }

    @Override
    public String getReftext() {
        return this.node.getReftext();
    }

    @Override
    public String iconUri(String name) {
        return this.node.iconUri(name);
    }

    @Override
    public String mediaUri(String target) {
        return this.node.mediaUri(target);
    }

    @Override
    public String imageUri(String targetImage) {
        return this.node.imageUri(targetImage);
    }

    @Override
    public String imageUri(String targetImage, String assetDirKey) {
        return this.node.imageUri(targetImage, assetDirKey);
    }

    @Override
    public String readAsset(String path, Map<Object, Object> opts) {
        return this.node.readAsset(path, RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime, opts));
    }

    @Override
    public String normalizeWebPath(String path, String start, boolean preserveUriTarget) {
        return this.node.normalizeWebPath(path, start, preserveUriTarget);
    }

    @Override
    public String getStyle() {

        IRubyObject style = ((RubyObjectHolderProxy) this.node).__ruby_object()
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
        return node.listMarkerKeyword();
    }

    @Override
    public String listMarkerKeyword(String listType) {
        return node.listMarkerKeyword(listType);
    }
}
