package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.Block;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Arrays;
import java.util.List;

public class BlockImpl extends StructuralNodeImpl implements Block {

    public BlockImpl(IRubyObject blockDelegate) {
        super(blockDelegate);
    }

    @Override
    public List<String> getLines() {
        return getList("lines", String.class);
    }

    @Override
    public void setLines(List<String> lines) {
        RubyArray newLines = getRuntime().newArray(lines.size());
        for (String line : lines) {
            newLines.add(getRuntime().newString(line));
        }
        setRubyProperty("lines", newLines);
    }

    @Override
    public String getSource() {
        return getString("source");
    }

    @Override
    public void setSource(String source) {
        setLines(Arrays.asList(source.split("\n")));
    }
}
