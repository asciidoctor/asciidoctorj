package org.asciidoctor.extension;

import java.util.List;
import java.util.Map;

import org.asciidoctor.Options;
import org.asciidoctor.dom.AbstractBlock;
import org.asciidoctor.dom.Block;
import org.asciidoctor.dom.Document;
import org.asciidoctor.dom.DocumentRuby;
import org.asciidoctor.dom.Inline;
import org.asciidoctor.internal.JRubyRuntimeContext;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class Processor {

    protected Document document;
    protected Ruby rubyRuntime;

    public Processor(DocumentRuby documentRuby) {
        this.rubyRuntime = JRubyRuntimeContext.get();
        this.document = new Document(documentRuby, rubyRuntime);
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
