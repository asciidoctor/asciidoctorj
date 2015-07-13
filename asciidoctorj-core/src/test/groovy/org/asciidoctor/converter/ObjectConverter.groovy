package org.asciidoctor.converter

import groovy.transform.CompileStatic
import org.asciidoctor.ast.AbstractNode

@CompileStatic
@ConverterFor(format = '42')
class ObjectConverter extends AbstractConverter<ObjectConverterResult> implements WritingConverter<ObjectConverterResult> {

    static final int FIXED_RESULT = 42

    ObjectConverter(String backend, Map<String, Object> opts) {
        super(backend, opts)
    }

    @Override
    ObjectConverterResult convert(AbstractNode node, String transform, Map<Object, Object> opts) {
        new ObjectConverterResult(x: FIXED_RESULT)
    }

    @Override
    void write(ObjectConverterResult output, OutputStream f) {
        f.write(output.toString().bytes)
    }

}
