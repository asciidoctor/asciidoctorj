package org.asciidoctor.jruby.internal;

import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.jruby.Ruby;
import org.jruby.RubyClass;
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

    private RubyClass abstractNodeClass;

    private Class rubyKeyType;

    public RubyHashMapDecorator(RubyHash rubyHash) {
        this(rubyHash, RubySymbol.class);
    }

    /**
     * Wrap a RubyHash map that uses a key of type `rubyKeyType`.
     *
     * Regardless of `rubyKeyType`, the wrapper will always use Java String
     * as the exposed key type and coerce internally as appropriate.
     *
     * @param rubyHash hash map to wrap
     * @param rubyKeyType key type, valid values: RubySymbol.class or String.class
     */
    public RubyHashMapDecorator(RubyHash rubyHash, Class rubyKeyType) {
        this.rubyRuntime = rubyHash.getRuntime();
        this.rubyHash = rubyHash;
        if (rubyKeyType != RubySymbol.class && rubyKeyType != String.class) {
            throw new UnsupportedOperationException("key type must be either RubySymbol or String");
        }
        this.rubyKeyType = rubyKeyType;
    }

    private Object coerceKey(String key) {
        if (rubyKeyType == RubySymbol.class) {
            return rubyHash.getRuntime().getSymbolTable().getSymbol(key);
        }
        return key;
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
        return rubyHash.containsKey(coerceKey((String) key));
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
        Object value = rubyHash.get(coerceKey((String) key));
        return convertRubyValue(value);
    }

    @Override
    public Object put(String key, Object value) {
        Object oldValue = get(key);
        rubyHash.put(coerceKey(key), convertJavaValue(value));
        return oldValue;
    }

    @Override
    public Object remove(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        Object oldValue = get(key);
        rubyHash.remove(coerceKey((String) key));
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
            IRubyObject rubyObject = (IRubyObject) rubyValue;

            if (RubyClass.KindOf.DEFAULT_KIND_OF.isKindOf(rubyObject, getAbstractNodeClass())) {
                return NodeConverter.createASTNode(rubyObject);
            }
            if (rubyObject instanceof RubySymbol) {
                return ":" + rubyObject.asString();
            }
            return JavaEmbedUtils.rubyToJava((IRubyObject) rubyValue);
        } else {
            return rubyValue;
        }
    }

    private RubyClass getAbstractNodeClass() {
        RubyClass ret = abstractNodeClass;
        if (ret == null) {
            ret = rubyRuntime.getModule("Asciidoctor").getClass("AbstractNode");
            abstractNodeClass = ret;
        }
        return ret;
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