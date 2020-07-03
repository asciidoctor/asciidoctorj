package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.internal.RubyBlockListDecorator;
import org.asciidoctor.jruby.internal.RubyHashUtil;
import org.asciidoctor.jruby.internal.RubyUtils;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;
import java.util.Map;

public class StructuralNodeImpl extends ContentNodeImpl implements StructuralNode {

    public StructuralNodeImpl(IRubyObject blockDelegate) {
        super(blockDelegate);
    }

    @Override
    @Deprecated
    public String title() {
        return getTitle();
    }

    @Override
    public String getTitle() {
        return getString("title");
    }

    @Override
    public void setTitle(String title) {
        setString("title", title);
    }

    @Override
    public String getCaption() {
        return getString("caption");
    }

    @Override
    public void setCaption(String caption) {
        setString("caption", caption);
    }

    @Override
    @Deprecated
    public String style() {
        return getStyle();
    }

    @Override
    public String getStyle() {
        return getString("style");
    }

    @Override
    public void setStyle(String style) {
        setString("style", style);
    }

    @Override
    @Deprecated
    public List<StructuralNode> blocks() {
        return getBlocks();
    }

    @Override
    public List<StructuralNode> getBlocks() {
        RubyArray rubyBlocks = (RubyArray) getRubyProperty("blocks");
        return new RubyBlockListDecorator<>(rubyBlocks);
    }

    @Override
    public void append(StructuralNode block) {
        getRubyObject().callMethod(runtime.getCurrentContext(), "<<", ((StructuralNodeImpl) block).getRubyObject());
    }

    @Override
    @Deprecated
    public Object content() {
        return getContent();
    }

    @Override
    public Object getContent() {
        return getProperty("content");
    }

    @Override
    public String convert() {
        return getString("convert");
    }

    @Override
    public int getLevel() {
        return getInt("level");
    }

    @Override
    public void setLevel(int level) {
        setInt("level", level);
    }

    @Override
    public Cursor getSourceLocation() {
        IRubyObject object = getRubyProperty("source_location");
        if (object == null || object.isNil()) {
            return null;
        }
        return new CursorImpl(object);
    }

    @Override
    public String getContentModel() {
    	return getString("content_model");
    }

    @Override
    public List<String> getSubstitutions() {
        return getList("subs", String.class);
    }

    @Override
    public boolean isSubstitutionEnabled(String substitution) {
        return getBoolean("sub?", RubyUtils.toSymbol(getRuntime(), substitution));
    }

    @Override
    public void removeSubstitution(String substitution) {
        getRubyProperty("remove_sub", RubyUtils.toSymbol(getRuntime(), substitution));
    }

    @Override
    public void addSubstitution(String substitution) {
        RubyArray subs = (RubyArray) getRubyProperty("@subs");
        subs.add(RubyUtils.toSymbol(getRuntime(), substitution));
    }

    @Override
    public void prependSubstitution(String substitution) {
        RubyArray subs = (RubyArray) getRubyProperty("@subs");
        subs.insert(getRuntime().newFixnum(0), RubyUtils.toSymbol(getRuntime(), substitution));
    }

    @Override
    public void setSubstitutions(String... substitutions) {
        RubyArray subs = (RubyArray) getRubyProperty("@subs");
        subs.clear();
        if (substitutions != null) {
            for (String substitution : substitutions) {
                subs.add(RubyUtils.toSymbol(getRuntime(), substitution));
            }
        }
    }

    @Override
    public List<StructuralNode> findBy(Map<Object, Object> selector) {
        RubyArray rubyBlocks = (RubyArray) getRubyProperty("find_by", RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime, selector));
        return new RubyBlockListDecorator<>(rubyBlocks);
    }

}
