package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HardBreakPreprocessor extends Preprocessor {

    @Override
    public Reader process(Document document, PreprocessorReader reader) {
        List<String> lines = reader.getLines();
        return newReader(lines.stream()
                .map(line -> line.isEmpty() || line.startsWith("=") ? line : line + " +")
                .collect(Collectors.toList()));
    }
}
