package org.asciidoctor.internal;

import java.util.List;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyObject;

public class Document implements DocumentRuby {

    private DocumentRuby documentRuby;
    private Ruby rubyRuntime;
    
    public Document(DocumentRuby documentRuby, Ruby rubyRuntime) {
        this.documentRuby = documentRuby;
        this.rubyRuntime = rubyRuntime;
    }
    
    public DocumentRuby getDocumentRuby() {
        return documentRuby;
    }
    
    @Override
    public String doctitle() {
        return this.documentRuby.doctitle();
    }

    @Override
    public String title() {
        return this.documentRuby.title();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.documentRuby.getAttributes();
    }

    @Override
    public boolean basebackend(String backend) {
        return this.documentRuby.basebackend(backend);
    }

    @Override
    public List<Block> blocks() {
        List<Block> rubyBlocks = this.documentRuby.blocks();
        
        for (int i = 0; i < rubyBlocks.size(); i++) {
            rubyBlocks.set(i, RubyUtils.rubyToJava(rubyRuntime, (RubyObject)rubyBlocks.get(i), Block.class));
        }
        
        return rubyBlocks;
    }

}
