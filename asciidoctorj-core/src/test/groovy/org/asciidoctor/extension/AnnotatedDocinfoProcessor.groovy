package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.ast.Document

@CompileStatic
@Location(LocationType.FOOTER)
class AnnotatedDocinfoProcessor extends DocinfoProcessor {

    public static final String META_TAG = '<meta name="robots" content="index, follow"/>'

    AnnotatedDocinfoProcessor() {}

    AnnotatedDocinfoProcessor(LocationType location) {
        config['location'] = location.optionValue()
    }

    @Override
    String process(Document document) {
        META_TAG
    }
}
