package org.asciidoctor.core.api.extension;

import java.util.LinkedHashMap;
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.core.api.ast.NodeConverter;
import org.asciidoctor.extension.PreprocessorReader;

/**
 * The {@link PreprocessorReader} of the AsciidoctorJ API.
 */
public class PreprocessorReaderImpl extends ReaderImpl implements PreprocessorReader {

    public PreprocessorReaderImpl(org.asciidoctor.core.PreprocessorReader delegate) {
        super(delegate);
    }

    private org.asciidoctor.core.PreprocessorReader preprocessorReader() {
        return (org.asciidoctor.core.PreprocessorReader) delegate;
    }

    @Override
    public void pushInclude(String data, String file, String path, int lineNumber, Map<String, Object> attributes) {
        preprocessorReader().pushInclude(data, file, path, lineNumber,
                attributes == null ? null : new LinkedHashMap<>(attributes));
    }

    @Override
    public Document getDocument() {
        return NodeConverter.wrap(preprocessorReader().getDocument());
    }
}
