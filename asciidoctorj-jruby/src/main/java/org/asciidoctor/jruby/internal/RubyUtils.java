package org.asciidoctor.jruby.internal;

import org.jruby.Ruby;
import org.jruby.RubySymbol;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.InputStream;

public class RubyUtils {

    public static <T> T rubyToJava(Ruby runtime, IRubyObject rubyObject, Class<T> returnType) {
        return (T) org.jruby.javasupport.JavaEmbedUtils.rubyToJava(runtime, rubyObject, returnType);
    }

    public static RubySymbol toSymbol(Ruby rubyRuntime, String key) {
        return RubySymbol.newSymbol(rubyRuntime, key);
    }

    public static void requireLibrary(Ruby rubyRuntime, String require) {
        rubyRuntime.evalScriptlet(String.format("require '%s'", require));
    }

    public static void loadRubyClass(Ruby rubyRuntime, InputStream rubyClassDefinition) {
        String script = IOUtils.readFull(rubyClassDefinition);
        rubyRuntime.evalScriptlet(script);
    }

    public static void setGlobalVariable(Ruby rubyRuntime, String variableName, Object variableValue) {
        String script = String.format("$%s = %s", variableName, variableValue);
        rubyRuntime.evalScriptlet(script);
    }

}
