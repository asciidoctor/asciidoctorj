package org.asciidoctor.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubySymbol;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class RubyHashUtil {

	private RubyHashUtil() {
		super();
	}
	
	public static RubyHash convertMapToRubyHashWithSymbols(Ruby rubyRuntime, Map<String, Object> options) {
		
		RubyHash rubyHash = new RubyHash(rubyRuntime);
		
		Set<Entry<String, Object>> optionsSet = options.entrySet();
		
		for (Entry<String, Object> entry : optionsSet) {
			
			String key = entry.getKey();
			Object value = entry.getValue();
			
			RubySymbol newSymbol = toSymbol(rubyRuntime, key);
			IRubyObject iRubyValue = toRubyObject(rubyRuntime, value);
			
			rubyHash.put(newSymbol, iRubyValue);
			
		}
		
		return rubyHash;
		
	}

	public static Map<String, Object> convertRubyHashMapToMap(Map<RubySymbol, Object> rubyHashMap) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		Set<Entry<RubySymbol, Object>> elements = rubyHashMap.entrySet();
		
		for (Entry<RubySymbol, Object> element : elements) {
			map.put(toJavaString(element), toJavaObject(element.getValue()));
		}
		
		return map;
		
	}

	private static String toJavaString(Entry<RubySymbol, Object> element) {
		return element.getKey().asJavaString();
	}
	
	private static Object toJavaObject(Object rubyObject) {
		if (rubyObject instanceof IRubyObject) {
			IRubyObject iRubyObject = (IRubyObject) rubyObject;
			return JavaEmbedUtils.rubyToJava(iRubyObject);
		}
		
		return rubyObject;
	}
	
	private static RubySymbol toSymbol(Ruby rubyRuntime, String key) {
		RubySymbol newSymbol = RubySymbol.newSymbol(rubyRuntime, key);
		return newSymbol;
	}

	private static IRubyObject toRubyObject(Ruby rubyRuntime, Object value) {
		
		if (value instanceof Map) {
			return toNoneSymbolsRubyHash(rubyRuntime, (Map<String, Object>) value);
		} else {
			IRubyObject iRubyObject = JavaEmbedUtils.javaToRuby(rubyRuntime, value);
			return iRubyObject;
		}
	}
	
	public static RubyHash toNoneSymbolsRubyHash(Ruby rubyRuntime, Map<String, Object> map) {
		
		RubyHash rubyHash = new RubyHash(rubyRuntime);
		
		Set<Entry<String, Object>> entrySet = map.entrySet();
		
		for (Entry<String, Object> entry : entrySet) {
			rubyHash.put(toJavaObject(entry.getKey()), toJavaObject(entry.getValue()));
		}
		
		return rubyHash;
		
	}
	
}