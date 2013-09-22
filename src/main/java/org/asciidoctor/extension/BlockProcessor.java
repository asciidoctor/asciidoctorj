package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.internal.DocumentRuby;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.RubyHash;
import org.jruby.RubySymbol;

public abstract class BlockProcessor extends Processor {

    public BlockProcessor(DocumentRuby documentRuby) {
        super(documentRuby);
    }

    public RubyHash config() {
        
        RubySymbol[] values = new RubySymbol[2];
        values[0] = RubyUtils.toSymbol(rubyRuntime, "paragraph");
        values[1] = RubyUtils.toSymbol(rubyRuntime, "open");
        
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("contexts", values);
        
        return RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime,context);
        
        
    }
    
    public abstract Object process(Object parent, Object reader, Object attributes);
    
}
