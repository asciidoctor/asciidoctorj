package org.asciidoctor.internal;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubySymbol;
import org.jruby.javasupport.JavaClass;
import org.jruby.runtime.builtin.IRubyObject;

public class RubyUtils {

    public static <T> T rubyToJava(Ruby runtime, IRubyObject rubyObject,
            Class<T> returnType) {
        return (T) org.jruby.javasupport.JavaEmbedUtils.rubyToJava(runtime,
                rubyObject, returnType);
    }

    public static RubySymbol toSymbol(Ruby rubyRuntime, String key) {
        RubySymbol newSymbol = RubySymbol.newSymbol(rubyRuntime, key);
        return newSymbol;
    }

    public static RubyClass toRubyClass(Ruby rubyRuntime, Class<?> rubyClass) {
    	return JavaClass.get(rubyRuntime, rubyClass).getProxyClass();
    }
    
}
