package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import spock.lang.Issue
import spock.lang.Specification

class WhenATreeProcessorCreatesNodes extends Specification {

    public static final String NEW_CODE = 'puts "World"'

    public static final String OLD_CODE = 'puts "Hello"'

    public static final String ATTR_VALUE_LANGUAGE_RUBY = 'language-ruby'

    public static final String ELEM_CODE = 'code'

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    @Issue('https://github.com/asciidoctor/asciidoctorj/issues/513')
    def 'it should be able to copy attributes from another node'() {

        given: 'A Asciidoctor document with one Ruby source block'
        String document = '''== Test doc

[source,ruby]
----
puts "Hello"
----
'''

        and: 'a Treeprocessor that creates an additional source block passing the same attributes'
        Treeprocessor tp = new Treeprocessor() {
            @Override
            Document process(Document doc) {
                StructuralNode newSourceBlock = createBlock(doc.blocks[0], 'listing', NEW_CODE, doc.blocks[0].blocks[0].attributes)
                // The style 'source' is visible in the attributes, but for new blocks it has to be set individually as a object property.
                newSourceBlock.style = 'source'
                doc.blocks[0].append(newSourceBlock)
            }
        }
        asciidoctor.javaExtensionRegistry().treeprocessor(tp)

        when: 'The document is converted'
        String html = asciidoctor.convert(document, OptionsBuilder.options().standalone(true))

        then: 'The second source block uses the same value of the language attribute and highlight as Ruby'
        org.jsoup.nodes.Document htmlDocument = Jsoup.parse(html)
        Elements elements = htmlDocument.getElementsByTag(ELEM_CODE)
        elements.size() == 2
        elements.get(0).hasClass(ATTR_VALUE_LANGUAGE_RUBY)
        elements.get(0).text() == OLD_CODE
        elements.get(1).hasClass(ATTR_VALUE_LANGUAGE_RUBY)
        elements.get(1).text() == NEW_CODE
    }

}
