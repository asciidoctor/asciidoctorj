package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

@Location(LocationType.FOOTER)
public class AnnotatedDocinfoProcessor extends DocinfoProcessor {

    public static final String META_TAG = "<meta name=\"robots\" content=\"index, follow\"/>";

    public AnnotatedDocinfoProcessor() {}

    public AnnotatedDocinfoProcessor(LocationType location) {
        getConfig().put("location", location.optionValue());
    }

    @Override
    public String process(Document document) {
        return META_TAG;
    }
}
