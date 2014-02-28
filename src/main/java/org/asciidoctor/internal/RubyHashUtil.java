package org.asciidoctor.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubySymbol;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class RubyHashUtil {

	private RubyHashUtil() {
		super();
	}

	public static RubyHash convertMapToRubyHashWithSymbolsIfNecessary(Ruby rubyRuntime,
			Map<Object, Object> options) {

		RubyHash rubyHash = new RubyHash(rubyRuntime);

		Set<Entry<Object, Object>> optionsSet = options.entrySet();

		for (Entry<Object, Object> entry : optionsSet) {

			Object keyType = entry.getKey();

			if (isNotARubySymbol(keyType)) {
				String key = (String) keyType;
				Object value = entry.getValue();

				RubySymbol newSymbol = RubyUtils.toSymbol(rubyRuntime, key);
				IRubyObject iRubyValue = toRubyObject(rubyRuntime, value);

				rubyHash.put(newSymbol, iRubyValue);
			}

		}

		return rubyHash;

	}

	public static RubyHash convertMapToRubyHashWithSymbols(Ruby rubyRuntime,
			Map<String, Object> options) {

		RubyHash rubyHash = new RubyHash(rubyRuntime);

		Set<Entry<String, Object>> optionsSet = options.entrySet();

		for (Entry<String, Object> entry : optionsSet) {

			String key = entry.getKey();
			Object value = entry.getValue();

			RubySymbol newSymbol = RubyUtils.toSymbol(rubyRuntime, key);
			IRubyObject iRubyValue = toRubyObject(rubyRuntime, value);

			rubyHash.put(newSymbol, iRubyValue);

		}

		return rubyHash;

	}

	private static boolean isNotARubySymbol(Object keyType) {
		return keyType instanceof String;
	}

	public static Map<String, Object> convertRubyHashMapToMap(
			Map<RubySymbol, Object> rubyHashMap) {

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

	private static IRubyObject toRubyObject(Ruby rubyRuntime, Object value) {

		if (value instanceof List) {
			return toRubyArray(rubyRuntime, (List<Object>) value);
		} else {

			if (isNotARubySymbol(value)) {
				String stringValue = ((String) value);

				if (stringValue.startsWith(":")) {
					return RubyUtils.toSymbol(rubyRuntime,
							stringValue.substring(1));
				}
			}

			IRubyObject iRubyObject = JavaEmbedUtils.javaToRuby(rubyRuntime,
					value);
			return iRubyObject;
		}
	}

	private static IRubyObject toRubyArray(Ruby rubyRuntime, List<Object> values) {

		RubyArray rubyArray = RubyArray.newArray(rubyRuntime, values.size());

		for (Object value : values) {
			rubyArray.add(toRubyObject(rubyRuntime, value));
		}

		return rubyArray;
	}

	public static RubyHash toNoneSymbolsRubyHash(Ruby rubyRuntime,
			Map<String, Object> map) {

		RubyHash rubyHash = new RubyHash(rubyRuntime);

		Set<Entry<String, Object>> entrySet = map.entrySet();

		for (Entry<String, Object> entry : entrySet) {
			rubyHash.put(toJavaObject(entry.getKey()),
					toJavaObject(entry.getValue()));
		}

		return rubyHash;

	}

}
