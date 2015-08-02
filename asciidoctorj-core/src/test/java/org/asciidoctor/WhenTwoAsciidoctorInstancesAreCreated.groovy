package org.asciidoctor

import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import spock.lang.Specification

class WhenTwoAsciidoctorInstancesAreCreated extends Specification {

    private static final String PARAGRAPH = 'paragraph'

    private static final String TEST_STRING = 'Hello World'

    def "then every Asciidoctor instance has its own extension registry"() {

        given:
        String document = '''= Test document

testmacro::Test[]
'''

        when:
        Asciidoctor asciidoctor1 = Asciidoctor.Factory.create(getClass().classLoader)
        Asciidoctor asciidoctor2 = Asciidoctor.Factory.create(getClass().classLoader)

        asciidoctor1.javaExtensionRegistry().blockMacro('testmacro', TestBlockMacroProcessor)

        then:
        asciidoctor1.convert(document, OptionsBuilder.options().headerFooter(false)).contains(TEST_STRING)
        !asciidoctor2.convert(document, OptionsBuilder.options().headerFooter(false)).contains(TEST_STRING)
    }


    static class TestBlockMacroProcessor extends BlockMacroProcessor {
        TestBlockMacroProcessor(String macroName) {
            super(macroName)
        }

        @Override
        Object process(StructuralNode parent, String target, Map<String, Object> attributes) {
            createBlock(parent, PARAGRAPH, TEST_STRING)
        }
    }
}
