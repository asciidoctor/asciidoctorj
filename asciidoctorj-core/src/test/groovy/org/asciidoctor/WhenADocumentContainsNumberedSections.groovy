package org.asciidoctor

import org.asciidoctor.ast.Section
import spock.lang.Specification

class WhenADocumentContainsNumberedSections extends Specification {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    def "for appendix the section numeral should be a letter"() {
        given:
        final source = '''\
            = Article Title
            :sectnums:
            
            [appendix]
            == Appendix Title
            '''.stripIndent()

        when:
        def document = asciidoctor.load source, [:]

        then:
        final appendix = (Section) document.blocks[0]
        appendix.title == 'Appendix Title'
        appendix.numeral == 'A'
    }

    def "for regular section the numeral should be an arabic numeral"() {
        given:
        final source = '''\
            = Article Title
            :sectnums:
            
            == Regular Section Title
            '''.stripIndent()

        when:
        def document = asciidoctor.load source, [:]

        then:
        final section = (Section) document.blocks[0]
        section.title == 'Regular Section Title'
        section.numeral == '1'
    }

    def "for book part the section numeral should be a roman numeral"() {
        given:
        final source = '''\
            = Book Title
            :doctype: book
            :sectnums:
            :partnums:
            
            = Part Title
            
            == Chapter Title
            '''.stripIndent()

        when:
        def document = asciidoctor.load source, [:]

        then:
        final part = (Section) document.blocks[0]
        part.title == 'Part Title'
        part.numeral == 'I'
    }
}
