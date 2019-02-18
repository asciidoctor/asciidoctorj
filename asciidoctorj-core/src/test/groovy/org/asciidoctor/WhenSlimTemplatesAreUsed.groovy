package org.asciidoctor

import org.asciidoctor.jruby.internal.JRubyAsciidoctor
import org.asciidoctor.util.ClasspathResources
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.junit.Rule
import spock.lang.Specification

import static org.asciidoctor.OptionsBuilder.options

class WhenSlimTemplatesAreUsed extends Specification {

    @Rule
    ClasspathResources classpath = new ClasspathResources()

    Asciidoctor asciidoctor = JRubyAsciidoctor.create()

    def 'the slim paragraph template should be used when rendering a document inline'() {
        given:
        Options options = options().templateDir(classpath.getResource('src/custom-backends/slim')).toFile(false).headerFooter(false).get()

        String sourceDocument = '''
= Hello World

This will be replaced by static content from the template
'''

        when:
        String renderContent = asciidoctor.convert(sourceDocument, options)

        then:
        Document doc = Jsoup.parse(renderContent, 'UTF-8')
        Element paragraph = doc.select('p').first()
        paragraph.text() == 'This is static content'
    }

}
