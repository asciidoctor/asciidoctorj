package org.asciidoctor.ast;

import org.asciidoctor.internal.RubyAttributesMapDecorator;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyObjectWrapper;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;

import org.jruby.javasupport.JavaEmbedUtils;

import java.util.List;
import java.util.Map;

public abstract class AbstractNodeImpl extends RubyObjectWrapper implements AbstractNode {

    public AbstractNodeImpl(IRubyObject rubyNode) {
        super(rubyNode);
    }

    @Override
    public String id() {
        return getId();
    }

    @Override
    public String getId() {
        return getString("id");
    }

    @Override
    public void setId(String id) {
        setString("id", id);
    }

    @Override
    public String context() {
        return getContext();
    }

    @Override
    public String getContext() {
        return getString("context");
    }

    @Override
    public AbstractNode parent() {
        return getParent();
    }

    @Override
    public AbstractNode getParent() {
        return NodeConverter.createASTNode(getRubyProperty("parent"));
    }

    @Override
    public Document document() {
        return getDocument();
    }

    @Override
    public Document getDocument() {
        return (Document) NodeConverter.createASTNode(getRubyProperty("document"));
    }

    @Override
    public String getNodeName() {
        return getString("node_name");
    }

    @Override
    public boolean isInline() {
        return getBoolean("inline?");
    }

    @Override
    public boolean isBlock() {
        return getBoolean("block?");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return new RubyAttributesMapDecorator((RubyHash) getRubyProperty("attributes"));
    }

    @Override
    public Object getAttr(Object name, Object defaultValue, boolean inherit) {
        return JavaEmbedUtils.rubyToJava(getRubyProperty("attr", name, defaultValue, inherit));
    }

    @Override
    public Object getAttr(Object name, Object defaultValue) {
        return JavaEmbedUtils.rubyToJava(getRubyProperty("attr", name, defaultValue));
    }

    @Override
    public Object getAttr(Object name) {
        return JavaEmbedUtils.rubyToJava(getRubyProperty("attr", name));
    }

    @Override
    public boolean isAttr(Object name, Object expected, boolean inherit) {
        return getBoolean("attr?", name, expected, inherit);
    }

    @Override
    public boolean isAttr(Object name, Object expected) {
        return getBoolean("attr?", name, expected);
    }

    @Override
    public boolean setAttr(Object name, Object value, boolean overwrite) {
        return getBoolean("set_attr", name, value, overwrite);
    }

    @Override
    public boolean isOption(Object name) {
        return getBoolean("option?", name);
    }

    @Override
    public boolean isRole() {
        return getBoolean("role?");
    }

    @Override
    public String getRole() {
        return getString("role");
    }

    @Override
    public String role() {
        return getRole();
    }

    @Override
    public List<String> getRoles() {
        return getList("roles", String.class);
    }

    @Override
    public boolean hasRole(String role) {
        return getBoolean("role?", role);
    }

    @Override
    public boolean isReftext() {
        return getBoolean("reftext?");
    }

    @Override
    public String getReftext() {
        return getString("reftext");
    }

    @Override
    public String iconUri(String name) {
        return getString("icon_uri", name);
    }

    @Override
    public String mediaUri(String target) {
        return getString("media_uri", target);
    }

    @Override
    public String imageUri(String targetImage) {
        return getString("image_uri", targetImage);
    }

    @Override
    public String imageUri(String targetImage, String assetDirKey) {
        return getString("image_uri", targetImage, assetDirKey);
    }

    @Override
    public String readAsset(String path, Map<Object, Object> opts) {
        return getString("read_asset", path, RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime, opts));
    }

    @Override
    public String normalizeWebPath(String path, String start, boolean preserveUriTarget) {
        return getString("normalize_web_path", path, start, preserveUriTarget);
    }

}
