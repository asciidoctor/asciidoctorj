package org.asciidoctor.ast;

import java.util.Map;

import org.asciidoctor.internal.RubyHashUtil;
import org.jruby.RubyHash;
import org.jruby.RubyString;
import org.jruby.runtime.builtin.IRubyObject;

public class Document extends AbstractBlockImpl implements DocumentRuby {

    public Document(IRubyObject documentRuby) {
        super(documentRuby);
    }

    @Override
    public boolean isBasebackend(String backend) {
        return getBoolean("basebackend?", backend);
    }

    @Override
    public boolean basebackend(String backend) {
        return isBasebackend(backend);
    }

    @Override
    public Map<Object, Object> getOptions() {
        return RubyHashUtil.convertRubyHashMapToMap((RubyHash) getRubyProperty("options"));
    }

    @Override
    public Object doctitle(Map<Object, Object> opts) {
        RubyHash mapWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime, opts);

        Object doctitle = getRubyProperty("doctitle", mapWithSymbols);

        if (doctitle instanceof RubyString) {
            return ((RubyString) doctitle).asJavaString();
        } else if (doctitle instanceof String) {
            return doctitle;
        } else {
            return toJava((IRubyObject) doctitle, Title.class);
        }

    }

    public String doctitle() {
        return getString("doctitle");
    }

}
