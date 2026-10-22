package org.asciidoctor.converter

import groovy.transform.CompileStatic


@CompileStatic
@ConverterFor(value = WritingTextConverter.DEFAULT_FORMAT, suffix = '.txt')
class WritingTextConverter extends TextConverter implements OutputFormatWriter<String> {

    static final String DEFAULT_FORMAT = 'writabletext'

    static File targetFile

    static boolean called = false

    WritingTextConverter(String backend, Map<String, Object> opts) {
        super(backend, opts)
    }
    
    @Override
    void write(String output, OutputStream out) {
        called = true
        out.write(output.bytes)
    }
}
