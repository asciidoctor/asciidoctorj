package org.asciidoctor.converter

import groovy.transform.CompileStatic


@CompileStatic
@ConverterFor(value = WritingTextConverter.DEFAULT_FORMAT, suffix = '.txt')
class WritingTextConverter extends TextConverter implements WritingConverter<String> {

    static final String DEFAULT_FORMAT = 'writabletext'

    static File targetFile

    WritingTextConverter(String backend, Map<String, Object> opts) {
        super(backend, opts)
    }
    
    @Override
    void write(String output, File f) {
        if (targetFile != null) {
            targetFile.text = output
        } else {
            f.text = output
        }
    }
}
