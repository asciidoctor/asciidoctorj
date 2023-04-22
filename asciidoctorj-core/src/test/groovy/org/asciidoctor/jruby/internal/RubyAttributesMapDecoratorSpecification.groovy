package org.asciidoctor.jruby.internal

import org.asciidoctor.Asciidoctor
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Document
import spock.lang.Specification

class RubyAttributesMapDecoratorSpecification extends Specification {

    public static final String ATTR_ONE = '1'

    public static final String ATTR_NAME_ROLE = 'role'
    public static final String ATTR_NAME_ID = 'id'

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    String attrValue = 'example#idname.rolename'
    String blockStyle = 'example'

    String documentWithPositionalAttribute = """
[${attrValue}]
Lorem ipsum dolor
"""

    def "should consistently show positional attributes as string keys"() {

        when:
        Document document = asciidoctor.load(documentWithPositionalAttribute, new HashMap<String, Object>())

        Block block = (Block) document.getBlocks().get(0)
        Map<String, Object> attributes = block.getAttributes()

        then:
        !attributes.containsKey(1L)
        attributes.containsKey(ATTR_ONE)
        attributes.keySet().contains(ATTR_ONE)
        attributes.get(ATTR_ONE) == blockStyle


    }

    def "should remove positional attributes by string keys"() {

        given: 'a block with a positional attribute'
        Document document = asciidoctor.load(documentWithPositionalAttribute, new HashMap<String, Object>())

        Block block = (Block) document.getBlocks().get(0)

        when: 'I remove the attribute with the name "1"'
        Map<String, Object> attributes = block.getAttributes()
        def oldValue = attributes.remove(ATTR_ONE)

        then: 'The key with the positional attribute is gone'
        !attributes.containsKey(ATTR_ONE)

        and: 'remove returned the previous attribute value'
        oldValue == blockStyle

        and: 'The attributes derived from the positional attribute are still there'
        attributes.containsKey(ATTR_NAME_ROLE)
        attributes.containsKey(ATTR_NAME_ID)
    }

    def "should return previous value on put"() {
        given: 'a block with a positional attribute'
        Document document = asciidoctor.load(documentWithPositionalAttribute, new HashMap<String, Object>())

        Block block = (Block) document.getBlocks().get(0)

        when: 'I put another value for the positional attribute 1'
        def attributes = block.getAttributes()
        def newValue = 42
        def previousValue = attributes.put(ATTR_ONE, newValue)

        then: 'put returned the previous value'
        previousValue.startsWith(blockStyle)

        and: 'the block has the new attribute value'
        attributes.get(ATTR_ONE) == newValue
    }
}
