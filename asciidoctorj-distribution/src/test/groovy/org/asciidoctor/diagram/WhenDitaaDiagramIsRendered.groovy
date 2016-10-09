package org.asciidoctor.diagram

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenDitaaDiagramIsRendered extends Specification {

    static final String ASCIIDOCTOR_DIAGRAM = 'asciidoctor-diagram'

    static final String BUILD_DIR = 'build'

    @ArquillianResource
    private Asciidoctor asciidoctor

    def 'should render ditaa diagram to HTML'() throws Exception {

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

        asciidoctor.requireLibrary(ASCIIDOCTOR_DIAGRAM)

        when:
        String result = asciidoctor.convert(document, OptionsBuilder.options().toFile(false))

        then:
        result.contains("""src="${imageFileName}.png""")
        new File("${imageFileName}.png").exists()
        new File(".asciidoctor/diagram/${imageFileName}.png.cache").exists()
    }

    def 'should render ditaa diagram to PDF'() throws Exception {

        given:
        String destinationFileName = 'build/test.pdf'
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

        asciidoctor.requireLibrary(ASCIIDOCTOR_DIAGRAM)

        when:
        asciidoctor.convert(document, OptionsBuilder.options()
                .toFile(new File(destinationFileName))
                .backend('pdf'))

        then:
        new File(destinationFileName).exists()
        File png = new File("build/${imageFileName}.png")
        File pngCache = new File("build/.asciidoctor/diagram/${imageFileName}.png.cache")
        png.exists()
        pngCache.exists()

        cleanup:
        png.delete()
        pngCache.delete()

    }

}
