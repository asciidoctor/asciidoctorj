package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class NumberLinesPreprocessor extends Preprocessor {

    public NumberLinesPreprocessor() {
    }

    @Override
    public Reader process(Document document, PreprocessorReader reader) {
        assertThat(reader.getLineNumber()).isEqualTo(1);
        return reader;
    }
}
