package org.asciidoctor.extension;

import org.asciidoctor.internal.RubyObjectWrapper;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;

public class ReaderImpl extends RubyObjectWrapper implements Reader {

    public ReaderImpl(IRubyObject rubyNode) {
        super(rubyNode);
    }

    @Override
    public int getLineno() {
        return getLineNumber();
    }

    @Override
    public int getLineNumber() {
        return getInt("lineno");
    }

    @Override
    public boolean hasMoreLines() {
        return getBoolean("has_more_lines?");
    }

    @Override
    public boolean isNextLineEmpty() {
        return getBoolean("next_line_empty?");
    }

    @Override
    public String read() {
        return getString("read");
    }

    @Override
    public List<String> readLines() {
        return getList("read_lines", String.class);
    }

    @Override
    public String readLine() {
        return getString("read_line");
    }

    @Override
    public List<String> lines() {
        return getList("lines", String.class);
    }

    @Override
    public void restoreLine(String line) {
        getRubyProperty("unshift_line", line);
    }

    @Override
    public void restoreLines(List<String> lines) {
        getRubyProperty("unshift_lines", lines);
    }

    @Override
    public String peekLine() {
        return getString("peek_line");
    }

    @Override
    public List<String> peekLines(int lineCount) {
        return getList("peek_lines", String.class, lineCount);
    }

    @Override
    public boolean advance() {
        return getBoolean("advance");
    }

    @Override
    public void terminate() {
        getRubyProperty("terminate");
    }

}
