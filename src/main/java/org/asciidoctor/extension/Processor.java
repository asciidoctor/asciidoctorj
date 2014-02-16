package org.asciidoctor.extension;

import java.util.List;
import java.util.Map;

import org.asciidoctor.Options;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.ast.Inline;
import org.asciidoctor.internal.JRubyRuntimeContext;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class Processor {

	protected RubyHash config;
    protected Ruby rubyRuntime;

    public Processor(Map<String, Object> config) {
        this.rubyRuntime = JRubyRuntimeContext.get();
        this.config = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, config);
    }

    public void update_config(Map<Object, Object> config) {
    	this.config.putAll(config);
    }
    
    public RubyHash config() {
    	return this.config;
    }
    
    public Block createBlock(AbstractBlock parent, String context, String content, Map<String, Object> attributes,
            Map<String, Object> options) {

        options.put(Options.SOURCE, content);
        options.put(Options.ATTRIBUTES, attributes);        
        
        return createBlock(parent, context, options);
    }
    
    public Block createBlock(AbstractBlock parent, String context, List<String> content, Map<String, Object> attributes,
            Map<String, Object> options) {

        options.put(Options.SOURCE, content);
        options.put(Options.ATTRIBUTES, attributes);        
        
        return createBlock(parent, context, options);
    }

    public Inline createInline(AbstractBlock parent, String context, List<String> text, Map<String, Object> attributes, Map<String, Object> options) {
        
        options.put(Options.ATTRIBUTES, attributes);
        
        IRubyObject rubyClass = rubyRuntime.evalScriptlet("Asciidoctor::Inline");
        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime,
                options);
        Object[] parameters = {
                parent.delegate(),
                RubyUtils.toSymbol(rubyRuntime, context),
                text,
                convertMapToRubyHashWithSymbols };
        return (Inline) JavaEmbedUtils.invokeMethod(rubyRuntime, rubyClass,
                "new", parameters, Inline.class);
    }
    
    public Inline createInline(AbstractBlock parent, String context, String text, Map<String, Object> attributes, Map<String, Object> options) {
        
        options.put(Options.ATTRIBUTES, attributes);
        
        IRubyObject rubyClass = rubyRuntime.evalScriptlet("Asciidoctor::Inline");
        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime,
                options);
        Object[] parameters = {
                parent.delegate(),
                RubyUtils.toSymbol(rubyRuntime, context),
                text,
                convertMapToRubyHashWithSymbols };
        return (Inline) JavaEmbedUtils.invokeMethod(rubyRuntime, rubyClass,
                "new", parameters, Inline.class);
    }
    
    protected Document document(DocumentRuby documentRuby) {
    	return new Document(documentRuby, rubyRuntime);
    }
    
    private Block createBlock(AbstractBlock parent, String context,
            Map<String, Object> options) {
        IRubyObject rubyClass = rubyRuntime.evalScriptlet("Asciidoctor::Block");
        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime,
                options);
        Object[] parameters = {
                parent.delegate(),
                RubyUtils.toSymbol(rubyRuntime, context),
                convertMapToRubyHashWithSymbols };
        return (Block) JavaEmbedUtils.invokeMethod(rubyRuntime, rubyClass,
                "new", parameters, Block.class);
    }

}
