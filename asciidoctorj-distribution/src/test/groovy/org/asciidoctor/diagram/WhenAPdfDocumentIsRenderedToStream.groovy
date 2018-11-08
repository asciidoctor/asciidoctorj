package org.asciidoctor.diagram

import org.asciidoctor.Asciidoctor
import org.asciidoctor.AttributesBuilder
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import spock.lang.Specification

import java.text.SimpleDateFormat

@RunWith(ArquillianSputnik)
class WhenAPdfDocumentIsRenderedToStream extends Specification {

    public static final String BACKEND_PDF = 'pdf'
    @ArquillianResource
    private Asciidoctor asciidoctor

    @ArquillianResource
    public TemporaryFolder testFolder

    static final String ASCIIDOCTOR_DIAGRAM = 'asciidoctor-diagram'


    def 'should render PDF to ByteArrayOutputStream'() throws Exception {

        given:
        String imageFileName = UUID.randomUUID()

        File referenceFile = new File('build/stream-test-file.pdf')
        File streamFile = new File('build/stream-test-stream.pdf')
        File imagesOutDir = new File(testFolder.root, 'images-dir')
        def createdCacheImage = new File(testFolder.root, ".asciidoctor/diagram/${imageFileName}.png.cache")

        String testDoc = """= Test

A test document

== A Section

Hello World

[ditaa,${imageFileName}]
....

+---+
| A |
+---+
....

== Another Section

Some other test

a

<<<

b

c

"""

        asciidoctor.requireLibrary(ASCIIDOCTOR_DIAGRAM)

        def out = new ByteArrayOutputStream()

        def dateTimeFormatter = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss z')

        def now = new Date()

        def attrs = AttributesBuilder.attributes()
                .attribute('outdir', testFolder.root.absolutePath)
                .attribute('imagesdir', imagesOutDir.name)
                .attribute('docdatetime', dateTimeFormatter.format(now))
                .attribute('localdatetime', dateTimeFormatter.format(now))
                .attribute('reproducible', 'true')

        when:
        asciidoctor.convert(testDoc,
                OptionsBuilder.options()
                        .backend(BACKEND_PDF)
                        .headerFooter(true)
                        .attributes(attrs)
                        .safe(SafeMode.UNSAFE)
                        .toStream(out))

        streamFile.bytes = out.toByteArray()
        def toStreamBytes = out.toByteArray()

        asciidoctor.convert(testDoc,
                OptionsBuilder.options()
                        .backend(BACKEND_PDF)
                        .headerFooter(true)
                        .attributes(attrs)
                        .safe(SafeMode.UNSAFE)
                        .toFile(referenceFile))

        def toFileBytes = referenceFile.bytes

        then:
        createdCacheImage.exists()
        toStreamBytes.length == toFileBytes.length
        Arrays.equals(toStreamBytes, toFileBytes)
    }
}
