package org.asciidoctor.converter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.asciidoctor.arquillian.api.Unshared
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenWritingConverterIsRegistered extends Specification {

    public static final String BACKEND_NAME_42 = '42'

    @ArquillianResource(Unshared)
    private Asciidoctor asciidoctor

    @ArquillianResource
    private TemporaryFolder tmp

    def cleanup() {
        WritingTextConverter.targetFile = null
    }


    def "should_write_in_writable_converter_write_method"() throws Exception {
        given:
        // Register as default converter
        asciidoctor.javaConverterRegistry().register(WritingTextConverter)

        File correctFile = tmp.newFile('realtarget.txt')
        File wrongFile = tmp.newFile('wrongtarget.txt')

        String document = '''== Hello

World!

- a
- b
'''

        when:

        WritingTextConverter.targetFile = correctFile

        asciidoctor.convert(document, OptionsBuilder.options().backend(WritingTextConverter.DEFAULT_FORMAT).toFile(wrongFile).safe(SafeMode.UNSAFE))

        then:
        correctFile.text == '''== Hello ==

World!

-> a
-> b
'''

        wrongFile.text == ''
    }

    def 'should convert to object format'() {
        given:
        String document = '''= Don't care'''
        asciidoctor.javaConverterRegistry().register(ObjectConverter)

        when:
        ObjectConverterResult result = asciidoctor.convert(document, OptionsBuilder.options().backend(BACKEND_NAME_42).asMap(), ObjectConverterResult)

        then:
        result == new ObjectConverterResult(x: ObjectConverter.FIXED_RESULT)
    }

    def 'should invoke write for object format'() {
        given:
        String document = '''= Still don't care'''
        asciidoctor.javaConverterRegistry().register(ObjectConverter)
        File targetFile = tmp.newFile('target.html')

        when:
        ObjectConverterResult result = asciidoctor.convert(document, OptionsBuilder.options().backend(BACKEND_NAME_42).toFile(targetFile).safe(SafeMode.UNSAFE).asMap(), ObjectConverterResult)

        then:
        result == null
        ObjectConverter.result == new ObjectConverterResult(x: ObjectConverter.FIXED_RESULT)

        cleanup:
        ObjectConverter.result = null
    }


}
