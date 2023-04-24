package org.asciidoctor

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.Section
import org.asciidoctor.converter.StringConverter
import spock.lang.Specification

class WhenADocumentIsRenderedToStream extends Specification {

    public static final String HTML5_BACKEND = 'html5'
    public static final String HELLO_WORLD = 'Hello World'
    public static final String EMPTY_STRING = ''

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    static final String TEST_DOC = '''= Test

A test document

== A Section

Hello World
'''

    def 'should render HTML to ByteArrayOutputStream'() throws Exception {

        given:
        def out = new ByteArrayOutputStream()

        when:
        asciidoctor.convert(TEST_DOC,
                OptionsBuilder.options()
                        .backend(HTML5_BACKEND)
                        .toStream(out))

        String result = new String(out.toByteArray())

        then:
        result != null && result != EMPTY_STRING
        result.contains(HELLO_WORLD)
    }

    def 'should render twice HTML to ByteArrayOutputStream'() throws Exception {

        given:
        def out1 = new ByteArrayOutputStream()
        def out2 = new ByteArrayOutputStream()

        when:
        asciidoctor.convert(TEST_DOC,
                OptionsBuilder.options()
                        .backend(HTML5_BACKEND)
                        .toStream(out1))
        asciidoctor.convert(TEST_DOC,
                OptionsBuilder.options()
                        .backend(HTML5_BACKEND)
                        .toStream(out2))

        String result1 = new String(out1.toByteArray())
        String result2 = new String(out2.toByteArray())

        then:
        result1 != null && result1 != EMPTY_STRING && result1.contains(HELLO_WORLD)
        result2 != null && result2 != EMPTY_STRING && result2.contains(HELLO_WORLD)
    }

    def 'should render with custom Java converter to ByteArrayOutputStream'() throws Exception {

        given:
        def out = new ByteArrayOutputStream()
        asciidoctor.javaConverterRegistry().register(TestConverter, TestConverter.TEST_BACKEND_NAME)
        def testdoc = '''== Test

Hello, World!
'''

        when:
        asciidoctor.convert(testdoc,
                OptionsBuilder.options()
                        .backend(TestConverter.TEST_BACKEND_NAME)
                        .toStream(out))

        String result = new String(out.toByteArray())

        then:
        result != null && result != EMPTY_STRING
        result.contains('-- Test --')
        result.contains('Hello, World!')
    }


    static class TestConverter extends StringConverter {

        public static final String TEST_BACKEND_NAME = 'test'

        private static final String NEWLINE = '\n'
        private static final String NEWLINE_2 = NEWLINE * 2

        TestConverter(String backend, Map<String, Object> opts) {
            super(backend, opts)
        }

        @Override
        String convert(ContentNode node, String transform, Map<Object, Object> opts) {
            if (transform == null) {
                transform = node.nodeName
            }
            this."convert${transform.capitalize()}"(node)
        }

        Object convertEmbedded(Document node) {
            String result = node.doctitle.toUpperCase()
            String content = node.content
            if (content && content.trim().length() > 0) {
                result += NEWLINE_2 + content
            }
            result
        }

        Object convertSection(Section section) {
            String result = "-- ${section.title} --"
            String content = section.content
            if (content && content.trim().length() > 0) {
                result += NEWLINE_2 + content
            }
            result
        }

        Object convertParagraph(Block block) {
            block.getLines().join(NEWLINE)
        }

    }

}
