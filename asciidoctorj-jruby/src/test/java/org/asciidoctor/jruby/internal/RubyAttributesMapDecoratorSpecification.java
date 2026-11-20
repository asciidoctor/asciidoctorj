package org.asciidoctor.jruby.internal;

import static org.asciidoctor.util.OptionsTestHelper.emptyOptions;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.Test;

class RubyAttributesMapDecoratorSpecification {

    public static final String ATTR_ONE = "1";

    public static final String ATTR_NAME_ROLE = "role";
    public static final String ATTR_NAME_ID = "id";

    private static final String ATTR_VALUE = "example#idname.rolename";
    private static final String BLOCK_STYLE = "example";

    private static final String DOCUMENT_WITH_POSITIONAL_ATTRIBUTE = """

            [%s]
            Lorem ipsum dolor
            """.formatted(ATTR_VALUE);

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    void should_consistently_show_positional_attributes_as_string_keys() {
        Document document = asciidoctor.load(DOCUMENT_WITH_POSITIONAL_ATTRIBUTE, emptyOptions());

        Block block = (Block) document.getBlocks().get(0);
        Map<String, Object> attributes = block.getAttributes();

        assertThat(attributes.containsKey(1L)).isFalse();
        assertThat(attributes).containsKey(ATTR_ONE);
        assertThat(attributes.keySet()).contains(ATTR_ONE);
        assertThat(attributes.get(ATTR_ONE)).isEqualTo(BLOCK_STYLE);
    }

    @Test
    void should_remove_positional_attributes_by_string_keys() {
        // given: a block with a positional attribute
        Document document = asciidoctor.load(DOCUMENT_WITH_POSITIONAL_ATTRIBUTE, emptyOptions());

        Block block = (Block) document.getBlocks().get(0);

        // when: I remove the attribute with the name "1"
        Map<String, Object> attributes = block.getAttributes();
        Object oldValue = attributes.remove(ATTR_ONE);

        // then: The key with the positional attribute is gone
        assertThat(attributes).doesNotContainKey(ATTR_ONE);

        // and: remove returned the previous attribute value
        assertThat(oldValue).isEqualTo(BLOCK_STYLE);

        // and: The attributes derived from the positional attribute are still there
        assertThat(attributes).containsKey(ATTR_NAME_ROLE);
        assertThat(attributes).containsKey(ATTR_NAME_ID);
    }

    @Test
    void should_return_previous_value_on_put() {
        // given: a block with a positional attribute
        Document document = asciidoctor.load(DOCUMENT_WITH_POSITIONAL_ATTRIBUTE, emptyOptions());

        Block block = (Block) document.getBlocks().get(0);

        // when: I put another value for the positional attribute 1
        Map<String, Object> attributes = block.getAttributes();
        int newValue = 42;
        String previousValue = (String) attributes.put(ATTR_ONE, newValue);

        // then: put returned the previous value
        assertThat(previousValue).startsWith(BLOCK_STYLE);

        // and: the block has the new attribute value (JRuby converts the Integer to a Long)
        assertThat(attributes.get(ATTR_ONE)).isEqualTo((long) newValue);
    }
}
