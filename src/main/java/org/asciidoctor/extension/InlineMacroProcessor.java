package org.asciidoctor.extension;

import java.util.Map;

import org.jruby.RubyRegexp;
import org.jruby.util.ByteList;

public abstract class InlineMacroProcessor extends MacroProcessor {

    protected RubyRegexp regexp;
    
    public InlineMacroProcessor(String macroName, Map<String, Object> config) {
        super(macroName, config);
        ByteList pattern = ByteList.create(macroName + ":(\\S+?)\\[.*?\\]");
        regexp = RubyRegexp.newRegexp(rubyRuntime, pattern);
    }
    
    public RubyRegexp regexp() {
        return regexp;
    }

}
