package org.asciidoctor.jruby.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Create case insensitive wrapper map for attributes handling.
 * 
 * @author marek
 * 
 * @param <K>
 * @param <V>
 */
public class CaseInsensitiveMap<K extends String, V> implements Map<K, V> {

    private Map<K, V> map;

    public CaseInsensitiveMap() {
        this(new HashMap<K, V>());
    }

    public CaseInsensitiveMap(Map<K, V> map) {
        this.map = map;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            return map.containsKey(((String) key).toLowerCase());
        } else {
            return map.containsKey(key);
        }
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public V get(Object key) {
        if (key instanceof String) {
            return map.get(((String) key).toLowerCase());
        } else {
            return map.get(key);
        }
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public V put(K key, V value) {
        return map.put((K) key.toLowerCase(), value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends String, ? extends V> entry : m.entrySet()) {
            map.put((K) entry.getKey().toLowerCase(), entry.getValue());
        }
    }

    @Override
    public V remove(Object key) {
        if (key instanceof String) {
            return map.remove(((String) key).toLowerCase());
        } else {
            return map.remove(key);
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

}
