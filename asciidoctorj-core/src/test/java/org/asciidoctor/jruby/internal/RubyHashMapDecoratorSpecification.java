package org.asciidoctor.jruby.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyString;
import org.jruby.embed.ScriptingContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RubyHashMapDecoratorSpecification {

    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String KEY_VALUE_MAPPING = "{:key => \"value\"}";
    public static final String A = "A";
    public static final String VALUE_B = "B";
    public static final String VALUE_D = "D";
    public static final String CREATE_EMPTY_RUBY_HASH = "$ruby_hash = Hash.new";

    private final Ruby ruby = new ScriptingContainer().getProvider().getRuntime();

    @AfterEach
    void cleanup() {
        ruby.tearDown();
    }

    @Test
    void when_a_RubyHash_is_empty_the_Map_should_be_empty_too() {
        // given: An empty RubyHash
        RubyHash rubyHash = RubyHash.newHash(ruby);

        // when: A RubyHashMapDecorator wraps it
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash);

        // then: The decorator is empty too
        assertThat(map.isEmpty()).isTrue();
        assertThat(map.size()).isZero();
        assertThat(map.keySet()).isEmpty();
        assertThat(map.keySet().size()).isZero();
        assertThat(map.values()).isEmpty();
        assertThat(map.values().size()).isZero();
        assertThat(map.entrySet().size()).isZero();
        assertThat(map.entrySet()).isEmpty();
    }

    @Test
    void when_a_RubyHash_is_not_empty_with_a_symbol_key_the_Map_should_show_the_same_key_as_a_string_and_value() {
        // given: An RubyHash with a value
        RubyHash rubyHash = (RubyHash) ruby.evalScriptlet(KEY_VALUE_MAPPING);

        // when: A RubyHashMapDecorator wraps it
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash);

        // then: The decorator shows the key as a string
        assertThat(map.isEmpty()).isFalse();
        assertThat(map.size()).isEqualTo(1);
        assertThat(map.keySet()).isNotEmpty();
        assertThat(map.keySet().size()).isEqualTo(1);
        assertThat(map.containsKey(KEY)).isTrue();
        assertThat(map.keySet()).contains(KEY);
        assertThat(map.values().size()).isEqualTo(1);
        assertThat(map.values()).isNotEmpty();
        assertThat(map.values()).contains(VALUE);
        assertThat(map.containsValue(VALUE)).isTrue();
        assertThat(map.entrySet().size()).isEqualTo(1);
        assertThat(map.entrySet()).isNotEmpty();
        assertThat(map.get(KEY)).isEqualTo(VALUE);
        assertThat(map.keySet().iterator().next()).isEqualTo(KEY);
        assertThat(map.values().iterator().next()).isEqualTo(VALUE);
    }

    @Test
    void when_an_entry_is_added_to_a_RubyHashMapDecorator_the_RubyHash_should_contain_the_value_afterwards() {
        // given: An empty RubyHash
        RubyHash rubyHash = (RubyHash) ruby.evalScriptlet(CREATE_EMPTY_RUBY_HASH);
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash);

        // when: a single key value pair is added to the RubyHashMapDecorator
        map.put(KEY, VALUE);

        // then: The key is visible in both the RubyHashMapDecorator as well as the RubyHash
        assertThat(map.get(KEY)).isEqualTo(VALUE);
        assertThat(ruby.evalScriptlet("$ruby_hash[:key]")).isEqualTo(RubyString.newString(ruby, VALUE));
    }

    @Test
    void when_an_entry_is_removed_from_a_RubyHashMapDecorator_it_should_be_removed_from_the_RubyHash_as_well() {
        // given: An RubyHash with a single key value pair
        RubyHash rubyHash = (RubyHash) ruby.evalScriptlet(KEY_VALUE_MAPPING);
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash);

        // when: The key is removed
        map.remove(KEY);

        // then: The RubyHash and the RubyHashMapDecorator are empty
        assertThat(map.isEmpty()).isTrue();
        assertThat(rubyHash.isEmpty()).isTrue();
    }

    @Test
    void when_multiple_values_are_added_via_putAll_they_are_all_visible_in_the_RubyHashMapDecorator() {
        // given: An empty RubyHash and a RubyHashMapDecorator that wraps it
        RubyHash rubyHash = (RubyHash) ruby.evalScriptlet(CREATE_EMPTY_RUBY_HASH);
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash);

        // when: a collection of key value pairs is added to the RubyHashMapDecorator
        map.putAll(Map.of("A", VALUE_B, "C", VALUE_D));

        // then: the RubyHash and the RubyHashMapDecorator both contain all key pair value pairs
        assertThat(map.get(A)).isEqualTo(VALUE_B);
        assertThat(map.get("C")).isEqualTo(VALUE_D);
        assertThat(ruby.evalScriptlet("$ruby_hash[:A]")).isEqualTo(RubyString.newString(ruby, VALUE_B));
        assertThat(ruby.evalScriptlet("$ruby_hash[:C]")).isEqualTo(RubyString.newString(ruby, VALUE_D));
    }

    @Test
    void when_a_string_value_beginning_with_a_colon_is_added_to_a_RubyHashMapDecorator_a_symbol_value_is_added_to_the_RubyHash() {
        // given: An empty RubyHash and a RubyHashMapDecorator that wraps it
        RubyHash rubyHash = (RubyHash) ruby.evalScriptlet(CREATE_EMPTY_RUBY_HASH);
        RubyHashMapDecorator map = new RubyHashMapDecorator(rubyHash);

        // when: a string beginning with a colon is added as value to the RubyHashMapDecorator
        map.put("format", ":short");

        // then: The RubyHash contains a symbol as value
        assertThat(ruby.evalScriptlet("$ruby_hash[:format]")).isEqualTo(ruby.getSymbolTable().getSymbol("short"));
    }
}
