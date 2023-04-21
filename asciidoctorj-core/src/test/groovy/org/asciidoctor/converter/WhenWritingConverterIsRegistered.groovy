package org.asciidoctor.converter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.asciidoctor.arquillian.api.Unshared
import org.asciidoctor.converter.ObjectConverter.ObjectConverterResult
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
        WritingTextConverter.called = false
        // Register as default converter
        asciidoctor.javaConverterRegistry().register(WritingTextConverter)

        File file = tmp.newFile('target.txt')

        String document = '''== Hello

World!

- a
- b
'''

        when:
        asciidoctor.convert(document, Options.builder().backend(WritingTextConverter.DEFAULT_FORMAT).toFile(file).safe(SafeMode.UNSAFE).build())

        then:
        file.text == '''== Hello ==

World!

-> a
-> b
'''

        WritingTextConverter.called

        cleanup:
        WritingTextConverter.called = false
    }

    private static final String DONT_CARE_DOCUMENT = '''= Don't care'''

    def 'should convert to object format'() {
        given:
        asciidoctor.javaConverterRegistry().register(ObjectConverter)

        when:
        ObjectConverterResult result = asciidoctor.convert(DONT_CARE_DOCUMENT, Options.builder().backend(BACKEND_NAME_42).build(), ObjectConverterResult)

        then:
        result.value == ObjectConverter.FIXED_RESULT
        result instanceof ObjectConverterResult
    }

    def 'should write to stream'() {
        given:
        asciidoctor.javaConverterRegistry().register(ObjectConverter)

        ByteArrayOutputStream bout = new ByteArrayOutputStream()

        when:
        ObjectConverterResult result = asciidoctor.convert(DONT_CARE_DOCUMENT, Options.builder().backend(BACKEND_NAME_42).toStream(bout).build(), ObjectConverterResult)

        then:
        new String(bout.toByteArray()) == new ObjectConverterResult(ObjectConverter.FIXED_RESULT).toString()
        result == null
    }

}
