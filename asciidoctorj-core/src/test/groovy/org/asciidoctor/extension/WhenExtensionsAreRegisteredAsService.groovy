package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.asciidoctor.util.ClasspathHelper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.TempDir

class WhenExtensionsAreRegisteredAsService extends Specification {

    static final String FILENAME_HTML = 'rendersample.html'

    ClasspathHelper classpath
    Asciidoctor asciidoctor
    ClassLoader originalTCCL

    @TempDir
    File testFolder

    def setup() {
        asciidoctor = Asciidoctor.Factory.create()
        classpath = new ClasspathHelper()
        classpath.classloader = WhenExtensionsAreRegisteredAsService
        originalTCCL = Thread.currentThread().contextClassLoader
    }

    def cleanup() {
        Thread.currentThread().contextClassLoader = originalTCCL
    }


    @Ignore('Test is ignored because currently it is not possible to register two block extensions in same instance. This may require deep changes on Asciidoctor Extensions API')
    def 'extensions should be correctly added'() throws IOException {

        when:
        //To avoid registering the same extension over and over for all tests, service is instantiated manually.
        new ArrowsAndBoxesExtension().register(asciidoctor)

        File renderedFile = new File(testFolder, FILENAME_HTML)
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE).build()

        asciidoctor.convertFile(classpath.getResource('arrows-and-boxes-example.ad'), options)

        then:
        Document doc = Jsoup.parse(renderedFile, 'UTF-8')

        Element arrowsJs = doc.select('script[src=http://www.headjump.de/javascripts/arrowsandboxes.js').first()
        arrowsJs != null

        Element arrowsCss = doc.select('link[href=http://www.headjump.de/stylesheets/arrowsandboxes.css').first()
        arrowsCss != null

        Element arrowsAndBoxes = doc.select('pre[class=arrows-and-boxes').first()
        arrowsAndBoxes != null

    }

    def "the active TCCL when creating the Asciidoctor instance should be used to load extensions"() {

        when:
        URLClassLoader tccl1 = new URLClassLoader(classpath.getResource('serviceloadertest/1').toURI().toURL() as URL[])
        Thread.currentThread().contextClassLoader = tccl1
        Asciidoctor asciidoctor1 = Asciidoctor.Factory.create()

        URLClassLoader tccl2 = new URLClassLoader(classpath.getResource('serviceloadertest/2').toURI().toURL() as URL[])
        Thread.currentThread().contextClassLoader = tccl2
        Asciidoctor asciidoctor2 = Asciidoctor.Factory.create()

        then:
        ExtensionRegistryExecutor1.callCount == 1
        ExtensionRegistryExecutor1.asciidoctor == asciidoctor1

        ExtensionRegistryExecutor2.callCount == 1
        ExtensionRegistryExecutor2.asciidoctor == asciidoctor2
    }

}
