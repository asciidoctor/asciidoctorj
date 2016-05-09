package org.asciidoctor.internal;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyString;
import org.jruby.RubySymbol;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
public class RubyHashMapDecorator implements Map<String, Object> {

    private final RubyHash rubyHash;

    private final Ruby rubyRuntime;

    public RubyHashMapDecorator(RubyHash rubyHash) {
        this.rubyRuntime = rubyHash.getRuntime();
        this.rubyHash = rubyHash;
    }

    @Override
    public int size() {
        return createJavaMap().size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        RubySymbol symbol = rubyHash.getRuntime().getSymbolTable().getSymbol((String) key);
        return rubyHash.containsKey(symbol);
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
        RubySymbol symbol = rubyHash.getRuntime().getSymbolTable().getSymbol((String) key);
        Object value = rubyHash.get(symbol);
        return convertRubyValue(value);
    }

    @Override
    public Object put(String key, Object value) {
        Object oldValue = get(key);
        RubySymbol symbol = rubyHash.getRuntime().getSymbolTable().getSymbol(key);
        rubyHash.put(symbol, convertJavaValue(value));
        return oldValue;
    }

    @Override
    public Object remove(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        Object oldValue = get(key);
        RubySymbol symbol = rubyHash.getRuntime().getSymbolTable().getSymbol((String) key);
        rubyHash.remove(symbol);
        return convertRubyValue(oldValue);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        for (Map.Entry<? extends String, ?> entry: m.entrySet()) {
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
        for (Map.Entry<Object, Object> o: rubyEntrySet) {
            String key = null;
            Object value;
            Object rubyKey = o.getKey();
            Object rubyValue = o.getValue();
            if (rubyKey instanceof RubySymbol) {
                key = ((RubySymbol) rubyKey).asJavaString();
            } else if (rubyKey instanceof RubyString) {
                key = ((RubyString) rubyKey).asJavaString();
            } else if (rubyKey instanceof String) {
                key = (String) rubyKey;
            } else if (rubyKey instanceof Long) {
                // Skip it silently, it is a positional attribute
            } else {
                throw new IllegalStateException("Did not expect key " + rubyKey + " of type " + rubyKey.getClass());
            }
            if (key != null) {
                value = convertRubyValue(rubyValue);
                copy.put(key, value);
            }
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

    RubyHash getRubyHash() {
        return rubyHash;
    }
}