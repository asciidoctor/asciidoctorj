package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.internal.DocumentRuby;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.RubySymbol;

public abstract class BlockProcessor extends Processor {

    public BlockProcessor(DocumentRuby documentRuby) {
        super(documentRuby);
    }

    public Map<Object, Object> config() {
        
        return new HashMap<Object, Object>(){{
            RubySymbol[] values = new RubySymbol[2];
            values[0] = RubyUtils.toSymbol(rubyRuntime, "paragraph");
            values[1] = RubyUtils.toSymbol(rubyRuntime, "open");
            put(RubyUtils.toSymbol(rubyRuntime, "contexts"), values);
        }};
        
    }
    
    public abstract Object process(Object parent, Object reader, Object attributes);
    
}
