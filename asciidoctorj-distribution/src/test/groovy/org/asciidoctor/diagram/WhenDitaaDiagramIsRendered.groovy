package org.asciidoctor.diagram

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenDitaaDiagramIsRendered extends Specification {

    @ArquillianResource
    private Asciidoctor asciidoctor

    def 'should render ditaa diagram'() throws Exception {

        given:

        String imageFileName = UUID.randomUUID()

        String document = """= Document Title

Hello World

[ditaa,${imageFileName}]
....

+---+
| A |
+---+
....

"""

        asciidoctor.requireLibrary('asciidoctor-diagram')

        when:
        String result = asciidoctor.convert(document, OptionsBuilder.options().toFile(false).attributes(['imagesoutdir': 'build']))

        then:
        result.contains("""src="${imageFileName}.png""")
        new File("build/${imageFileName}.png").exists()
        new File("build/${imageFileName}.png.cache").exists()
    }
}
