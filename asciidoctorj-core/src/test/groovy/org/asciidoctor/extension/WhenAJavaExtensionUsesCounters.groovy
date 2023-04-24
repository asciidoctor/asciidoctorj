package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.Section
import org.asciidoctor.ast.StructuralNode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import spock.lang.Specification

import static org.asciidoctor.ast.ContentModel.SIMPLE

@SuppressWarnings('DuplicateNumberLiteral')
class WhenAJavaExtensionUsesCounters extends Specification {

    public static final String UTF8 = 'UTF-8'

    public static final String PARAGRAPH = 'paragraph'

    public static final String PARAGRAPH_SELECTOR = '.paragraph'

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()


    def 'using the counter macro should continue the counters set in macros but doesnt'() {
        given:
        String document = '''= Test document

== Test section

testmacro::countera[]

testmacro::counterb[]

testmacro::countera[]

The attribute for countera: {counter:countera}

The attribute for counterb: {counter:counterb}

'''
        when:
        asciidoctor.javaExtensionRegistry().blockMacro(TestBlockMacroProcessor)
        String result = asciidoctor.convert(document, OptionsBuilder.options().standalone(true).safe(SafeMode.SERVER))

        then:
        Document doc = Jsoup.parse(result, UTF8)
        Elements paragraphs = doc.select(PARAGRAPH_SELECTOR)
        paragraphs.get(0).text() == 'This is macro call 1 for countera'
        paragraphs.get(1).text() == 'This is macro call 1 for counterb'
        paragraphs.get(2).text() == 'This is macro call 2 for countera'

        paragraphs.get(3).text() == 'The attribute for countera: 1'
        paragraphs.get(4).text() == 'The attribute for counterb: 1'
    }

    def 'using a treeprocessor should continue the counters set in macros'() {
        given:
        String document = '''= Test document

== Test section

[.count]
countera

[.count]
counterb

The attribute for countera: {counter:countera}

The attribute for counterb: {counter:counterb}

'''
        when:
        asciidoctor.javaExtensionRegistry().treeprocessor(TestTreeProcessor)
        String result = asciidoctor.convert(document, OptionsBuilder.options().standalone(true).safe(SafeMode.SERVER))

        then:
        Document doc = Jsoup.parse(result, UTF8)
        Elements paragraphs = doc.select(PARAGRAPH_SELECTOR)
        paragraphs.get(0).text() == 'Counter countera has value 1'
        paragraphs.get(1).text() == 'Counter counterb has value 1'
        paragraphs.get(2).text() == 'The attribute for countera: 2'
        paragraphs.get(3).text() == 'The attribute for counterb: 2'
    }

    def 'an initial seed should be used'() {
        given:
        String document = '''= Test document

== Test section

testmacro::countera[]

testmacro::counterb[]

testmacro::countera[]

'''
        when:
        asciidoctor.javaExtensionRegistry().blockMacro(TestBlockMacroWithInitialCounterProcessor)
        String result = asciidoctor.convert(document, OptionsBuilder.options().standalone(true).safe(SafeMode.SERVER))

        then:
        Document doc = Jsoup.parse(result, UTF8)
        Elements paragraphs = doc.select(PARAGRAPH_SELECTOR)
        paragraphs.get(0).text() == 'This is macro call 42 for countera'
        paragraphs.get(1).text() == 'This is macro call 42 for counterb'
        paragraphs.get(2).text() == 'This is macro call 43 for countera'
    }

    @CompileStatic
    @Name('testmacro')
    @Contexts(Contexts.PARAGRAPH)
    @ContentModel(SIMPLE)
    static class TestBlockMacroProcessor extends BlockMacroProcessor {

        @Override
        StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {

            String text = "This is macro call ${parent.document.getAndIncrementCounter(target)} for ${target}"
            // Have to do this to interact successfully with counters also used in the document
            // String text = "This is macro call {counter:${target}} for ${target}"

            createBlock(parent, PARAGRAPH, text)
        }
    }

    @CompileStatic
    @Name('testmacro')
    @Contexts(Contexts.PARAGRAPH)
    @ContentModel(SIMPLE)
    static class TestBlockMacroWithInitialCounterProcessor extends BlockMacroProcessor {

        @Override
        StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {

            String text = "This is macro call ${parent.document.getAndIncrementCounter(target, 42)} for ${target}"

            createBlock(parent, PARAGRAPH, text)
        }
    }

    static class TestTreeProcessor extends Treeprocessor {

        @Override
        org.asciidoctor.ast.Document process(org.asciidoctor.ast.Document document) {
            List<StructuralNode> newNodes = document.blocks.collect {
                if (it instanceof Section) {
                    processSection((Section) it)
                } else {
                    it
                }
            }
            document.blocks.clear()
            document.blocks.addAll(newNodes)
            document
        }

        Section processSection(Section section) {
            List<StructuralNode> newNodes = section.blocks.collect {
                if (it instanceof Block && it.getRoles().contains('count')) {
                    createBlock(section, PARAGRAPH, "Counter ${it.content} has value ${it.document.getAndIncrementCounter(it.source)}")
                } else {
                    it
                }
            }
            section.blocks.clear()
            section.blocks.addAll(newNodes)
            section
        }
    }
}
