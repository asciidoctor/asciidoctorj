package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import spock.lang.Issue
import spock.lang.Specification

@Issue('https://github.com/asciidoctor/asciidoctorj/issues/450')
class WhenAJavaExtensionChecksAttributes extends Specification {


    private static final String DOCUMENT = '''= Test document

[checkattributes,avalue]
Check me

'''

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    def "a BlockProcessor should only get String attribute keys"() {
        when:
        asciidoctor.javaExtensionRegistry().block(AttributeCheckingBlockProcessor)
        asciidoctor.convert(DOCUMENT, OptionsBuilder.options().standalone(true).safe(SafeMode.SERVER))

        then:
        noExceptionThrown()
    }
}
