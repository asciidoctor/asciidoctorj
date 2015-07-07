package org.asciidoctor.ast;

import org.asciidoctor.internal.RubyObjectWrapper;
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
        return getString("path");
    }

    @Override
    public String getDir() {
        return getString("dir");
    }

    @Override
    public String getFile() {
        return getString("file");
    }

}
