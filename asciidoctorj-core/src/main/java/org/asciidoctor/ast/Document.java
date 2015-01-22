package org.asciidoctor.ast;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;

public class Document extends AbstractBlockImpl implements DocumentRuby {

    private DocumentRuby documentDelegate;

    public Document(DocumentRuby documentRuby, Ruby runtime) {
        super(documentRuby, runtime);
        this.documentDelegate = documentRuby;
    }

    public DocumentRuby getDocumentRuby() {
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
