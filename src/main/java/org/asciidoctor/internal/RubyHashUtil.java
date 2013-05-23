package org.asciidoctor.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyObject;
import org.jruby.RubySymbol;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class RubyHashUtil {

	private RubyHashUtil() {
		super();
	}
	
	public static RubyHash convertMapToRubyHashWithSymbols(Ruby rubyRuntime, Map<String, Object> options) {
		
		RubyHash rubyHash = new RubyHash(rubyRuntime);
		
		Set<String> keySet = options.keySet();
		
		for (String key : keySet) {
			
			Object value = options.get(key);
			
			RubySymbol newSymbol = toSymbol(rubyRuntime, key);
			IRubyObject iRubyValue = toRubyObject(rubyRuntime, value);
			
			rubyHash.put(newSymbol, iRubyValue);
			
		}
		
		return rubyHash;
		
	}

	private static Object toJavaObject(IRubyObject rubyObject) {
		return JavaEmbedUtils.rubyToJava(rubyObject);
	}
	
	private static RubySymbol toSymbol(Ruby rubyRuntime, String key) {
		RubySymbol newSymbol = RubySymbol.newSymbol(rubyRuntime, key);
		return newSymbol;
	}

	private static IRubyObject toRubyObject(Ruby rubyRuntime, Object value) {
		return JavaEmbedUtils.javaToRuby(rubyRuntime, value);
	}
	
}