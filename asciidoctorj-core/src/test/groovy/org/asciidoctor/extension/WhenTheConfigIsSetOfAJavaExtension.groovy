package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.jruby.internal.AsciidoctorCoreException
import spock.lang.Specification

class WhenTheConfigIsSetOfAJavaExtension extends Specification {

    String document = '''[modify]
Parsing will crash when processing this block
'''

    def 'setConfig should throw no Exception as long as the processor is not used by Asciidoctor'() {

        given:
        BlockProcessor blockProcessor = new ConfigModifyingBlockProcessor()
        Map<String, Object> config = [:]
        config[Contexts.KEY] = [Contexts.PARAGRAPH]
        config[ContentModel.KEY] = ContentModel.SIMPLE
        blockProcessor.config = config

        expect:
        blockProcessor.config[ContentModel.KEY] == ContentModel.SIMPLE
    }

    def 'setConfig should throw an IllegalStateException in Processor_process when a processor instance is registered'() {

        given:
        Asciidoctor asciidoctor = Asciidoctor.Factory.create()

        BlockProcessor blockProcessor = new ConfigModifyingBlockProcessor()
        Map<String, Object> config = [:]
        config[Contexts.KEY] = [Contexts.PARAGRAPH]
        config[ContentModel.KEY] = ContentModel.SIMPLE
        blockProcessor.config = config

        asciidoctor.javaExtensionRegistry().block(blockProcessor)

        when:
        asciidoctor.convert(document, OptionsBuilder.options().toFile(false))

        then:
        Exception e = thrown()
        e instanceof AsciidoctorCoreException || e instanceof IllegalStateException
    }

    def 'setConfig should throw an IllegalStateException in Processor_process when a processor class is registered'() {

        given:
        Asciidoctor asciidoctor = Asciidoctor.Factory.create()

        asciidoctor.javaExtensionRegistry().block('modify', ConfigModifyingBlockProcessor)

        when:
        asciidoctor.convert(document, OptionsBuilder.options().toFile(false))

        then:
        Exception e = thrown()
        e instanceof AsciidoctorCoreException || e instanceof IllegalStateException
    }

}
