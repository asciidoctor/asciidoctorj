package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.jruby.internal.RubyObjectWrapper;
import org.jruby.runtime.builtin.IRubyObject;

public class CursorImpl extends RubyObjectWrapper implements Cursor {

    public CursorImpl(IRubyObject rubyNode) {
        super(rubyNode);
    }

    @Override
    public int getLineNumber() {
        return getInt("lineno");
    }

    @Override
    public String getPath() {
        final Object result = getProperty("path");
        return result == null ? null : result.toString();
    }

    @Override
    public String getDir() {
        final Object result = getProperty("dir");
        return result == null ? null : result.toString();
    }

    @Override
    public String getFile() {
        final Object result = getProperty("file");
        return result == null ? null : result.toString();
    }

    @Override
    public String toString() {
        return getString("to_s");
    }
}
