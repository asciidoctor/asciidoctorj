package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.ast.Document
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicBoolean

class WhenAPreprocessorIsRegistered extends Specification {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    public static final int ONE = 1
    public static final int TWO = 2

    private final String firstLine = 'First line'
    private final String secondLine = 'Second line'

    private final String document = """$firstLine
$secondLine"""

    def 'should be able to peek single lines from Reader'() {
        given:

        AtomicBoolean preprocessorCalled = new AtomicBoolean(false)

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            Reader process(Document doc, PreprocessorReader reader) {
                preprocessorCalled.set(true)

                assert reader.peekLine() == firstLine
                assert reader.peekLine() == firstLine

                reader.advance()

                assert reader.peekLine() == secondLine
                assert reader.peekLine() == secondLine

                reader.advance()

                assert reader.peekLine() == null
                reader
            }
        })

        when:
        asciidoctor.convert(document, OptionsBuilder.options())

        then:
        preprocessorCalled.get()
    }

    def 'should be able to peek multiple lines from Reader'() {
        given:

        AtomicBoolean preprocessorCalled = new AtomicBoolean(false)

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            Reader process(Document doc, PreprocessorReader reader) {
                preprocessorCalled.set(true)

                assert reader.peekLines(ONE) == [firstLine]
                assert reader.peekLines(TWO) == [firstLine, secondLine]

                reader.advance()

                assert reader.peekLines(ONE) == [secondLine]
                assert reader.peekLines(TWO) == [secondLine]

                reader.advance()

                assert reader.peekLines(ONE) == []
                assert reader.peekLines(TWO) == []
                reader
            }
        })

        when:
        asciidoctor.convert(document, OptionsBuilder.options())

        then:
        preprocessorCalled.get()
    }

    def 'should be able to read line by line from Reader'() {
        given:

        AtomicBoolean preprocessorCalled = new AtomicBoolean(false)

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            Reader process(Document doc, PreprocessorReader reader) {
                preprocessorCalled.set(true)

                assert reader.readLine() == firstLine
                assert reader.readLine() == secondLine
                assert reader.readLine() == null
                reader
            }
        })

        when:
        asciidoctor.convert(document, OptionsBuilder.options())

        then:
        preprocessorCalled.get()
    }

    def 'should be able to restore a line into the Reader'() {
        given:

        AtomicBoolean preprocessorCalled = new AtomicBoolean(false)

        String anotherLine = 'Another line'

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            Reader process(Document doc, PreprocessorReader reader) {
                preprocessorCalled.set(true)

                assert reader.peekLine() == firstLine

                reader.restoreLine(anotherLine)

                assert reader.peekLine() == anotherLine
                assert reader.readLine() == anotherLine
                assert reader.peekLine() == firstLine
                assert reader.readLine() == firstLine
                reader
            }
        })

        when:
        asciidoctor.convert(document, OptionsBuilder.options())

        then:
        preprocessorCalled.get()
    }

    def 'should be able to restore multiple lines into the Reader'() {
        given:

        AtomicBoolean preprocessorCalled = new AtomicBoolean(false)

        String anotherFirstLine = 'Another first line'
        String anotherSecondLine = 'Another second line'

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            Reader process(Document doc, PreprocessorReader reader) {
                preprocessorCalled.set(true)

                assert reader.peekLine() == firstLine

                reader.restoreLines([anotherFirstLine, anotherSecondLine])

                assert reader.peekLine() == anotherFirstLine
                assert reader.readLine() == anotherFirstLine
                assert reader.peekLine() == anotherSecondLine
                assert reader.readLine() == anotherSecondLine
                assert reader.peekLine() == firstLine
                assert reader.readLine() == firstLine
                reader
            }
        })

        when:
        asciidoctor.convert(document, OptionsBuilder.options())

        then:
        preprocessorCalled.get()
    }

}
