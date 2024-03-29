package org.asciidoctor

import org.asciidoctor.jruby.internal.JRubyAsciidoctor
import org.asciidoctor.test.extension.ClasspathHelper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import spock.lang.Specification

class WhenSlimTemplatesAreUsed extends Specification {

    Asciidoctor asciidoctor = JRubyAsciidoctor.create()

    ClasspathHelper classpath = new ClasspathHelper(WhenSlimTemplatesAreUsed)

    def 'the slim paragraph template should be used when rendering a document inline'() {
        given:
        Options options = Options.builder()
                .templateDirs(classpath.getResource('src/custom-backends/slim'))
                .toFile(false)
                .standalone(false)
                .build()

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
