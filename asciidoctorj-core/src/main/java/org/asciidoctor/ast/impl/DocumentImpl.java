package org.asciidoctor.ast.impl;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Title;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;

public class DocumentImpl extends BlockNodeImpl implements Document {

    private Document documentDelegate;

    public DocumentImpl(Document document, Ruby runtime) {
        super(document, runtime);
        this.documentDelegate = document;
    }

    public Document getDocumentRuby() {
        return documentDelegate;
    }

    @Override
    public boolean basebackend(String backend) {
        return documentDelegate.basebackend(backend);
    }

    @Override
    public Map<Object, Object> getOptions() {
        Map<Object, Object> options = (Map<Object, Object>)documentDelegate.getOptions();
        return RubyHashUtil.convertRubyHashMapToMap(options);
    }

    @Override
    public Object doctitle(Map<Object, Object> opts) {
        RubyHash mapWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime, opts);

        Object doctitle = documentDelegate.doctitle(mapWithSymbols);

        if (doctitle instanceof IRubyObject) {
            doctitle = RubyUtils.rubyToJava(runtime, (IRubyObject) doctitle, Title.class);
        }

        return doctitle;
    }

    public String doctitle() {
        return (String) doctitle(new HashMap<Object, Object>());
    }

}
