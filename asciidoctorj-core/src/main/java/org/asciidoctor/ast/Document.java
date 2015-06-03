package org.asciidoctor.ast;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.internal.RubyHashMapDecorator;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyString;
import org.jruby.runtime.builtin.IRubyObject;

public class Document extends AbstractBlockImpl implements DocumentRuby {

    public Document(IRubyObject documentRuby) {
        super(documentRuby);
    }

    @Override
    public boolean basebackend(String backend) {
        return getBoolean("basebackend?", backend);
    }

    @Override
    public Map<Object, Object> getOptions() {
        return RubyHashUtil.convertRubyHashMapToMap((RubyHash) getProperty("options"));
    }

    @Override
    public Object doctitle(Map<Object, Object> opts) {
        RubyHash mapWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime, opts);

        Object doctitle = getProperty("doctitle", mapWithSymbols);

        if (doctitle instanceof RubyString) {
            return ((RubyString) doctitle).asJavaString();
        } else if (doctitle instanceof String) {
            return doctitle;
        } else {
            return RubyUtils.rubyToJava(runtime, (IRubyObject) doctitle, Title.class);
        }

    }

    public String doctitle() {
        return getString("doctitle");
    }

}
