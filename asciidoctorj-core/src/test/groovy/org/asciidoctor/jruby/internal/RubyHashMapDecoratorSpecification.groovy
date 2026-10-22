package org.asciidoctor.jruby.internal

import org.jruby.Ruby
import org.jruby.RubyHash
import org.jruby.RubyString
import org.jruby.embed.ScriptingContainer
import spock.lang.Specification

class RubyHashMapDecoratorSpecification extends Specification {

    public static final String KEY = 'key'
    public static final String VALUE = 'value'
    public static final String KEY_VALUE_MAPPING = '{:key => "value"}'
    public static final String A = 'A'
    public static final String VALUE_B = 'B'
    public static final String VALUE_D = 'D'
    public static final String CREATE_EMPTY_RUBY_HASH = '$ruby_hash = Hash.new'
    Ruby ruby = new ScriptingContainer().getProvider().getRuntime()

    def cleanup() {
        ruby.tearDown()
    }

    def 'when a RubyHash is empty the Map should be empty too'() {
        given: 'An empty RubyHash'
        RubyHash rubyHash = RubyHash.newHash(ruby)

        when: 'A RubyHashMapDecorator wraps it'
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash)

        then: 'The decorator is empty too'
        map.isEmpty()
        map.size() == 0
        map.keySet().empty
        map.keySet().size() == 0
        map.values().empty
        map.values().size() == 0
        map.entrySet().size() == 0
        map.entrySet().empty
    }

    def 'when a RubyHash is not empty with a symbol key the Map should show the same key as a string and value'() {
        given: 'An RubyHash with a value'
        RubyHash rubyHash = ruby.evalScriptlet(KEY_VALUE_MAPPING)

        when: 'A RubyHashMapDecorator wraps it'
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash)

        then: 'The decorator shows the key as a string'
        !map.isEmpty()
        map.size() == 1
        !map.keySet().empty
        map.keySet().size() == 1
        map.containsKey(KEY)
        map.keySet().contains(KEY)
        map.values().size() == 1
        !map.values().empty
        map.values().contains(VALUE)
        map.containsValue(VALUE)
        map.entrySet().size() == 1
        !map.entrySet().empty
        map.get(KEY) == VALUE
        map.keySet().iterator().next() == KEY
        map.values().iterator().next() == VALUE
    }

    def 'when an entry is added to a RubyHashMapDecorator the RubyHash should contain the value afterwards'() {
        given: 'An empty RubyHash'
        RubyHash rubyHash = ruby.evalScriptlet(CREATE_EMPTY_RUBY_HASH)
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash)

        when: 'a single key value pair is added to the RubyHashMapDecorator'
        map.put(KEY, VALUE)

        then: 'The key is visible in both the RubyHashMapDecorator as well as the RubyHash'
        map.get(KEY) == VALUE
        ruby.evalScriptlet('$ruby_hash[:key]') == RubyString.newString(ruby, VALUE)
    }

    def 'when an entry is removed from a RubyHashMapDecorator it should be removed from the RubyHash as well'() {
        given: 'An RubyHash with a single key value pair'
        RubyHash rubyHash = ruby.evalScriptlet(KEY_VALUE_MAPPING)
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash)

        when: 'The key is removed'
        map.remove(KEY)

        then: 'The RubyHash and the RubyHashMapDecorator are empty'
        map.isEmpty()
        rubyHash.isEmpty()
    }

    def 'when multiple values are added via putAll they are all visible in the RubyHashMapDecorator'() {
        given: 'An empty RubyHash and a RubyHashMapDecorator that wraps it'
        RubyHash rubyHash = ruby.evalScriptlet(CREATE_EMPTY_RUBY_HASH)
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash)

        when: 'a collection of key value pairs is added to the RubyHashMapDecorator'
        map.putAll(['A': VALUE_B, 'C': VALUE_D])

        then: 'the RubyHash and the RubyHashMapDecorator both contain all key pair value pairs'
        map.get(A) == VALUE_B
        map.get('C') == VALUE_D
        ruby.evalScriptlet('$ruby_hash[:A]') == RubyString.newString(ruby, VALUE_B)
        ruby.evalScriptlet('$ruby_hash[:C]') == RubyString.newString(ruby, VALUE_D)
    }

    def 'when a string value beginning with a colon is added to a RubyHashMapDecorator a symbol value is added to the RubyHash'() {
        given: 'An empty RubyHash and a RubyHashMapDecorator that wraps it'
        RubyHash rubyHash = ruby.evalScriptlet(CREATE_EMPTY_RUBY_HASH)
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash)

        when: 'a string beginning with a colon is added as value to the RubyHashMapDecorator'
        map.put('format', ':short')

        then: 'The RubyHash contains a symbol as value'
        ruby.evalScriptlet('$ruby_hash[:format]') == ruby.getSymbolTable().getSymbol('short')
    }
}
