package org.asciidoctor.extension;

import org.asciidoctor.internal.RubyUtils;
import org.jruby.RubyRegexp;
import org.jruby.util.RegexpOptions;

import java.util.HashMap;
import java.util.Map;

public abstract class InlineMacroProcessor extends MacroProcessor {

    protected RubyRegexp regexp;
    
    public InlineMacroProcessor(String macroName) {
        this(macroName, new HashMap<String, Object>());
    }
    
    public InlineMacroProcessor(String macroName, Map<String, Object> config) {
        super(macroName, config);
    }
}
