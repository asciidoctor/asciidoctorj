package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith
import spock.lang.Issue
import spock.lang.Specification

@Issue('https://github.com/asciidoctor/asciidoctorj/issues/450')
@RunWith(ArquillianSputnik)
class WhenAJavaExtensionChecksAttributes extends Specification {


    private static final String DOCUMENT = '''= Test document

[checkattributes,avalue]
Check me

'''

    @ArquillianResource
    private Asciidoctor asciidoctor

    def "a BlockProcessor should only get String attribute keys"() {
        when:
        asciidoctor.javaExtensionRegistry().block(AttributeCheckingBlockProcessor)
        asciidoctor.convert(DOCUMENT, OptionsBuilder.options().headerFooter(true).safe(SafeMode.SERVER))

        then:
        noExceptionThrown()
    }
}
