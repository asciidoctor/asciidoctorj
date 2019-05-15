package org.asciidoctor.jruby.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyString;
import org.jruby.RubySymbol;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class RubyHashUtil {

    private RubyHashUtil() {
        super();
    }

    public static RubyHash convertMapToRubyHashWithSymbolsIfNecessary(Ruby rubyRuntime, Map<Object, Object> options) {

        RubyHash rubyHash = new RubyHash(rubyRuntime);

        Set<Entry<Object, Object>> optionsSet = options.entrySet();

        for (Entry<Object, Object> entry : optionsSet) {

            Object keyType = entry.getKey();

            if (isNotARubySymbol(keyType)) {
                CharSequence key = (CharSequence) keyType;
                Object value = entry.getValue();

                RubySymbol newSymbol = RubyUtils.toSymbol(rubyRuntime, key.toString());
                IRubyObject iRubyValue = toRubyObject(rubyRuntime, value);

                rubyHash.put(newSymbol, iRubyValue);
            } else if (keyType instanceof RubySymbol) {
                Object value = entry.getValue();
                IRubyObject iRubyValue = toRubyObject(rubyRuntime, value);
                rubyHash.put(keyType, iRubyValue);
            }

        }

        return rubyHash;

    }

    public static RubyHash convertMapToRubyHashWithSymbols(Ruby rubyRuntime, Map<String, Object> options) {

        if (options instanceof RubyHashMapDecorator) {
            return ((RubyHashMapDecorator) options).getRubyHash();
        }

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

    public static RubyHash convertMapToRubyHashWithStrings(Ruby rubyRuntime, Map<String, Object> attributes) {

        if (attributes instanceof RubyHashMapDecorator) {
            return ((RubyHashMapDecorator) attributes).getRubyHash();
        } else if (attributes instanceof RubyHash) {
            return (RubyHash) attributes;
        }

        RubyHash rubyHash = new RubyHash(rubyRuntime);

        Set<Entry<String, Object>> optionsSet = attributes.entrySet();

        for (Entry<String, Object> entry : optionsSet) {

            String key = entry.getKey();
            Object value = entry.getValue();

            RubyString newKey = rubyRuntime.newString(key);
            IRubyObject iRubyValue = toRubyObject(rubyRuntime, value);

            rubyHash.put(newKey, iRubyValue);
        }
        return rubyHash;
    }

    private static boolean isNotARubySymbol(Object keyType) {
        return keyType instanceof CharSequence;
    }

    public static Map<Object, Object> convertRubyHashMapToMap(Map<Object, Object> rubyHashMap) {

        Map<Object, Object> map = new HashMap<Object, Object>();

        Set<Entry<Object, Object>> elements = rubyHashMap.entrySet();

        for (Entry<Object, Object> element : elements) {
            if(element.getKey() instanceof RubySymbol) {
                map.put(toJavaString((RubySymbol)element.getKey()), toJavaObject(element.getValue()));
            } else {
                map.put(toJavaObject(element.getKey()).toString(), toJavaObject(element.getValue()));
            }
        }

        return map;

    }

    public static Map<String, Object> convertRubyHashMapToStringObjectMap(RubyHash rubyHashMap) {

        Map<String, Object> map = new HashMap<String, Object>();

        Set<Entry<Object, Object>> elements = rubyHashMap.entrySet();

        for (Entry<Object, Object> element : elements) {
            if(element.getKey() instanceof RubySymbol) {
                map.put(toJavaString((RubySymbol)element.getKey()), toJavaObject(element.getValue()));
            } else if (element.getKey() instanceof RubyString) {
                map.put(((RubyString) element.getKey()).asJavaString(), toJavaObject(element.getValue()));
            } else if (element.getKey() instanceof String) {
                map.put((String) element.getKey(), toJavaObject(element.getValue()));
            }
        }

        return map;

    }

    private static String toJavaString(RubySymbol element) {
        return element.asJavaString();
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
        } else if (value instanceof Map) {
        	return convertMapToRubyHashWithStrings(rubyRuntime, (Map<String, Object>) value);
        } else {

            if (isNotARubySymbol(value)) {
                CharSequence stringValue = ((CharSequence) value);

                if (stringValue.length() > 0 && stringValue.charAt(0) == ':') {
                    return RubyUtils.toSymbol(rubyRuntime, stringValue.subSequence(1, stringValue.length()).toString());
                }
            }

            IRubyObject iRubyObject = JavaEmbedUtils.javaToRuby(rubyRuntime, value);
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

    public static RubyHash toNoneSymbolsRubyHash(Ruby rubyRuntime, Map<String, Object> map) {

        RubyHash rubyHash = new RubyHash(rubyRuntime);

        Set<Entry<String, Object>> entrySet = map.entrySet();

        for (Entry<String, Object> entry : entrySet) {
            rubyHash.put(toJavaObject(entry.getKey()), toJavaObject(entry.getValue()));
        }

        return rubyHash;

    }

}
