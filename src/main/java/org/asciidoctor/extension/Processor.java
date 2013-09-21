package org.asciidoctor.extension;

import java.util.Map;

import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.internal.Block;
import org.asciidoctor.internal.Document;
import org.asciidoctor.internal.DocumentRuby;
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
        this.rubyRuntime = (Ruby) documentRuby.getAttributes()
                .get(Attributes.JRUBY);
        this.document = new Document(documentRuby, rubyRuntime);
    }

    public Block createBlock(Document parent, String context, String content, Map<String, Object> attributes,
            Map<String, Object> options) {

        options.put(Options.SOURCE, content);
        options.put(Options.ATTRIBUTES, attributes);        
        
        IRubyObject rubyClass = rubyRuntime.evalScriptlet("Asciidoctor::Block");
        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime,
                options);
        //convertMapToRubyHashWithSymbols.put(RubyUtils.toSymbol(rubyRuntime, "content_model"), RubyUtils.toSymbol(rubyRuntime, "verbatim"));
        //convertMapToRubyHashWithSymbols.put(RubyUtils.toSymbol(rubyRuntime, "subs"), new Object[] {});
        Object[] parameters = {
                parent.getDocumentRuby(),
                RubyUtils.toSymbol(rubyRuntime, context),
                convertMapToRubyHashWithSymbols };
        return (Block) JavaEmbedUtils.invokeMethod(rubyRuntime, rubyClass,
                "new", parameters, Block.class);
    }

}
