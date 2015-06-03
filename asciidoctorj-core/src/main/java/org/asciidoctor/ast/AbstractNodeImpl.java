package org.asciidoctor.ast;

import org.asciidoctor.internal.RubyAttributesMapDecorator;
import org.asciidoctor.internal.RubyHashMapDecorator;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyBoolean;
import org.jruby.RubyFixnum;
import org.jruby.RubyHash;
import org.jruby.RubyNil;
import org.jruby.RubyNumeric;
import org.jruby.RubyString;
import org.jruby.RubySymbol;
import org.jruby.java.proxies.RubyObjectHolderProxy;
import org.jruby.runtime.builtin.IRubyObject;

import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractNodeImpl implements AbstractNode {

    protected Ruby runtime;
    protected IRubyObject rubyNode;

    public AbstractNodeImpl(IRubyObject rubyNode) {
        this.rubyNode = rubyNode;
        this.runtime = rubyNode.getRuntime();
    }

    @Override
    public String id() {
        return getString("id");
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
        return NodeConverter.createASTNode(getProperty("parent"));
    }

    @Override
    public DocumentRuby document() {
        return getDocument();
    }

    @Override
    public DocumentRuby getDocument() {
        return (DocumentRuby) NodeConverter.createASTNode(getProperty("document"));
    }

    public IRubyObject getRubyObject() {
        return rubyNode;
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
        return new RubyAttributesMapDecorator((RubyHash) getProperty("attributes"));
    }

    @Override
    public Object getAttr(Object name, Object defaultValue, boolean inherit) {
        return JavaEmbedUtils.rubyToJava(getProperty("attr", name, defaultValue, inherit));
    }

    @Override
    public Object getAttr(Object name, Object defaultValue) {
        return JavaEmbedUtils.rubyToJava(getProperty("attr", name, defaultValue));
    }

    @Override
    public Object getAttr(Object name) {
        return JavaEmbedUtils.rubyToJava(getProperty("attr", name));
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

    @Override
    public String getStyle() {
        return getString("@style");
    }

    @Override
    public String listMarkerKeyword() {
        return getString("list_marker_keyword");
    }

    @Override
    public String listMarkerKeyword(String listType) {
        return getString("list_marker_keyword", listType);
    }


    protected String getString(String propertyName, Object... args) {
        IRubyObject result = getProperty(propertyName, args);

        if (result instanceof RubyNil) {
            return null;
        } else if (result instanceof RubySymbol) {
            return ((RubySymbol) result).asJavaString();
        } else {
            return ((RubyString) result).asJavaString();
        }
    }

    protected boolean getBoolean(String propertyName, Object... args) {
        IRubyObject result = getProperty(propertyName, args);
        if (result instanceof RubyNil) {
            return false;
        } else {
            return ((RubyBoolean) result).isTrue();
        }
    }

    protected int getInt(String propertyName, Object... args) {
        IRubyObject result = getProperty(propertyName, args);
        if (result instanceof RubyNil) {
            return 0;
        } else {
            return (int) ((RubyNumeric) result).getLongValue();
        }
    }

    protected <T> List<T> getList(String propertyName, Class<T> elementClass, Object... args) {
        IRubyObject result = getProperty(propertyName, args);
        if (result instanceof RubyNil) {
            return null;
        } else {
            List<T> ret = new ArrayList<T>();
            RubyArray array = (RubyArray) result;
            for (int i = 0; i < array.size(); i++) {
                ret.add(RubyUtils.rubyToJava(runtime, array.at(RubyFixnum.newFixnum(runtime, i)), elementClass));
            }
            return ret;
        }
    }


    protected IRubyObject getProperty(String propertyName, Object... args) {
        ThreadContext threadContext = runtime.getThreadService().getCurrentContext();

        IRubyObject result = null;
        if (propertyName.startsWith("@")) {
            if (args != null) {
                throw new IllegalArgumentException("No args allowed for direct field access");
            }
            result = rubyNode.getInstanceVariables().getInstanceVariable(propertyName);
        } else {
            if (args == null) {
                result = rubyNode.callMethod(threadContext, propertyName);
            } else {
                IRubyObject[] rubyArgs = new IRubyObject[args.length];
                for (int i = 0; i < args.length; i++) {
                    rubyArgs[i] = JavaEmbedUtils.javaToRuby(runtime, args[i]);
                }
                result = rubyNode.callMethod(threadContext, propertyName, rubyArgs);
            }
        }
        return result;
    }

}
