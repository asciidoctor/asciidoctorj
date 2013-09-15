package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.internal.Block;
import org.asciidoctor.internal.DocumentRuby;
import org.asciidoctor.internal.Reader;
import org.asciidoctor.internal.RubyHashUtil;
import org.jruby.Ruby;

public abstract class BlockProcessor extends Processor {

    protected static final Map<String, Object> config = new HashMap<String, Object>();
    
    public static Map<String, Object> config() {
        return config;
    }
    
    // Glue code to convert the config object to proper Ruby types using Ruby runtime
    public static void setup(Ruby rubyRuntime) {
        Map<String, Object> raw = config();
        Map<String, Object> converted = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, raw);
        raw.clear();
        raw.putAll(converted);
    }
    
    protected String context;
    protected Map<String, Object> options;
    
    public BlockProcessor(String context, DocumentRuby documentRuby) {
        super(documentRuby);
        this.context = context;
    }
    
    protected Map<String, Object> options() {
        return new HashMap<String, Object>();
    }
    
    public abstract Object process(Block parent, Reader reader, Map<String, Object> attributes);
}
