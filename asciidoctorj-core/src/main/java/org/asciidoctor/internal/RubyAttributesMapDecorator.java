package org.asciidoctor.internal;

import org.jruby.RubyHash;
import org.jruby.RubyString;
import org.jruby.RubySymbol;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RubyAttributesMapDecorator implements Map<String, Object> {

    private final RubyHash rubyHash;

    public RubyAttributesMapDecorator(RubyHash rubyHash) {
        this.rubyHash = rubyHash;
    }

    @Override
    public int size() {
        return rubyHash.size();
    }

    @Override
    public boolean isEmpty() {
        return rubyHash.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        return rubyHash.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return rubyHash.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        Object value = rubyHash.get(key);
        return convertRubyValue(value);
    }

    @Override
    public Object put(String key, Object value) {
        Object oldValue = get(key);
        rubyHash.put(key, convertJavaValue(value));
        return oldValue;
    }

    @Override
    public Object remove(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        Object oldValue = get(key);
        rubyHash.remove(key);
        return convertRubyValue(oldValue);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        for (Entry<? extends String, ?> entry: m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        rubyHash.clear();
    }

    @Override
    public Set<String> keySet() {
        return createJavaMap().keySet();
    }

    @Override
    public Collection<Object> values() {
        return createJavaMap().values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return createJavaMap().entrySet();
    }

    private Map<String, Object> createJavaMap() {
        Map<String, Object> copy = new HashMap<String, Object>();
        Set<Entry<Object, Object>> rubyEntrySet = rubyHash.entrySet();
        for (Entry<Object, Object> o: rubyEntrySet) {
            String key;
            Object rubyKey = o.getKey();
            Object rubyValue = o.getValue();
            if (rubyKey instanceof RubyString) {
                key = ((RubyString) rubyKey).asJavaString();
            } else if (rubyKey instanceof String) {
                key = (String) rubyKey;
            } else {
                continue;
            }
            Object value = convertRubyValue(rubyValue);
            copy.put(key, value);
        }
        return copy;
    }

    private Object convertRubyValue(Object rubyValue) {
        if (rubyValue == null) {
            return null;
        } else if (rubyValue instanceof IRubyObject) {
            return JavaEmbedUtils.rubyToJava((IRubyObject) rubyValue);
        } else {
            return rubyValue;
        }
    }

    private IRubyObject convertJavaValue(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String && ((String) value).startsWith(":")) {
            return rubyHash.getRuntime().getSymbolTable().getSymbol(((String) value).substring(1));
        } else {
            return JavaEmbedUtils.javaToRuby(rubyHash.getRuntime(), value);
        }
    }
}