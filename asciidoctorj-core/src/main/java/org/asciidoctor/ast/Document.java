package org.asciidoctor.ast;

import java.util.Map;

import org.asciidoctor.internal.RubyHashUtil;
import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyHash;
import org.jruby.RubyString;
import org.jruby.RubySymbol;
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
    public Title getStructuredDoctitle() {
        Ruby runtime = getRubyObject().getRuntime();
        RubyHash options = RubyHash.newHash(runtime);
        RubySymbol partitioned = RubySymbol.newSymbol(runtime, "partition");
        options.put(partitioned, RubyBoolean.newBoolean(runtime, true));

        Object doctitle = getRubyProperty("doctitle", options);

        return toJava((IRubyObject) doctitle, Title.class);

    }

    public String getDoctitle() {
        return getString("doctitle");
    }

    public String doctitle() {
        return getDoctitle();
    }

}
