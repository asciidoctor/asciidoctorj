package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.ast.Document
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenAPreprocessorSetsTheSourceMapOption extends Specification {

    static final int NOT_SET = -1

    @ArquillianResource
    private Asciidoctor asciidoctor

    static class SourceMapOptionPreprocessor extends Preprocessor {
        @Override
        void process(Document document, PreprocessorReader reader) {
            document.sourcemap = true
        }
    }

    static class TestTreeprocessor extends Treeprocessor {
        int lineNoFirstSection = 0
        @Override
        Document process(Document document) {
            def blocks = document.getBlocks()
            lineNoFirstSection = blocks[0].sourceLocation?.lineNumber ?: NOT_SET
            document
        }
    }

    def DOCUMENT = '''= Test

== A Section

Some text
'''

    def 'another extension can get the source location if a preprocessor enabled the SourceMap option on the Document'() {

        given:
        def treeprocessor = new TestTreeprocessor()

        when:
        asciidoctor.javaExtensionRegistry()
                .preprocessor(new SourceMapOptionPreprocessor())
                .treeprocessor(treeprocessor)
        def doc = asciidoctor.load(DOCUMENT, OptionsBuilder.options().asMap())

        then:
        treeprocessor.lineNoFirstSection == 3
        doc.isSourcemap()
    }

    def 'another extension cannot get the source location if a preprocessor did not enable the SourceMap option on the Document'() {

        given:
        def treeprocessor = new TestTreeprocessor()

        when:
        asciidoctor.javaExtensionRegistry()
                .treeprocessor(treeprocessor)
        def doc = asciidoctor.load(DOCUMENT, OptionsBuilder.options().asMap())

        then:
        treeprocessor.lineNoFirstSection == NOT_SET
        !doc.isSourcemap()
    }

}
