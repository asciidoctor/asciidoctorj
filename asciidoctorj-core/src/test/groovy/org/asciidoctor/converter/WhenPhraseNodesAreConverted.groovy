package org.asciidoctor.converter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.asciidoctor.arquillian.api.Unshared
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenPhraseNodesAreConverted extends Specification {

    @ArquillianResource(Unshared)
    private Asciidoctor asciidoctor

    def "should_write_in_writable_converter_write_method"() throws Exception {
        given:
        // Register as default converter
        asciidoctor.javaConverterRegistry().register(PhraseNodeConverter)

        String document = '''
Hello `+{backtick}+`
'''

        when:
        String html = asciidoctor.convert(document, OptionsBuilder.options().backend(PhraseNodeConverter.DEFAULT_FORMAT).safe(SafeMode.UNSAFE))

        then:
        html == '''Hello >>>{backtick}<<<'''
    }

}
