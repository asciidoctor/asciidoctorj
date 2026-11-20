package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.ast.Document;

public class NumberLinesPreprocessor extends Preprocessor {

    public NumberLinesPreprocessor() {
    }

    @Override
    public Reader process(Document document, PreprocessorReader reader) {
        assertThat(reader.getLineNumber()).isEqualTo(1);
        return reader;
    }
}
