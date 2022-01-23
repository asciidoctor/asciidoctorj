package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import java.util.ArrayList;
import java.util.List;

public class FrontMatterPreprocessor extends Preprocessor {

    @Override
    public void process(Document document, PreprocessorReader reader) {
        List<String> lines = reader.getLines();
        if (lines.isEmpty()) {
            return;
        }
        List<String> frontMatter = new ArrayList<>();
        if ("---".equals(lines.get(0).trim())) {
            lines.remove(0);
            while (!lines.isEmpty() && !"---".equals(lines.get(0).trim())) {
                frontMatter.add(lines.remove(0));
            }

            if (!lines.isEmpty() && "---".equals(lines.get(0).trim())) {
                lines.remove(0);
                document.setAttribute("front-matter", frontMatter, true);
                for (int i = 0; i < frontMatter.size() + 2; i++) {
                    reader.advance();
                }
            }
        }
    }
}
