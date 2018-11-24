package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NumberLinesPreprocessor extends Preprocessor {

    public NumberLinesPreprocessor() {
    }

    @Override
    public void process(Document document, PreprocessorReader reader) {
        assertThat(reader.getLineNumber(), is(1));
    }
}
