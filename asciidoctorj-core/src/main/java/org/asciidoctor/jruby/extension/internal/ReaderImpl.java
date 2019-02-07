package org.asciidoctor.jruby.extension.internal;

import java.util.List;

import org.asciidoctor.extension.Reader;
import org.asciidoctor.jruby.internal.RubyObjectWrapper;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.runtime.builtin.IRubyObject;

public class ReaderImpl extends RubyObjectWrapper implements Reader {

    public ReaderImpl(IRubyObject rubyNode) {
        super(rubyNode);
    }

    static ReaderImpl createReader(Ruby runtime, List<String> lines) {
        RubyArray rubyLines = runtime.newArray(lines.size());
        for (String line : lines) {
            rubyLines.add(runtime.newString(line));
        }

        RubyClass readerClass = runtime.getModule("Asciidoctor").getClass("Reader");
        return new ReaderImpl(readerClass.callMethod("new", rubyLines));
    }

    @Override
    @Deprecated
    public int getLineno() {
        return getLineNumber();
    }

    @Override
    public int getLineNumber() {
        return getInt("lineno");
    }

    public String getFile() {
        IRubyObject rObj = getRubyProperty("file");
        return rObj.toString();
    }

    public String getDir() {
        IRubyObject rObj = getRubyProperty("dir");
        return rObj.toString();
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
