package org.asciidoctor.ast;

import org.jruby.runtime.builtin.IRubyObject;

public class ColumnImpl extends AbstractNodeImpl implements Column {

    public ColumnImpl(IRubyObject rubyNode) {
        super(rubyNode);
    }

    @Override
    public String getStyle() {
        return getString("style");
    }

    @Override
    public Table getTable() {
        return (Table) getParent();
    }

    @Override
    public int getColumnNumber() {
        return Integer.parseInt((String) getAttr("colnumber"));
    }

    @Override
    public int getWidth() {
        return ((Number) getAttr("width")).intValue();
    }

    @Override
    public String getHAlign() {
        return (String) getAttr("halign");
    }

    @Override
    public String getVAlign() {
        return (String) getAttr("valign");
    }
}
