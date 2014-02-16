package org.asciidoctor.extension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FrontMatterPreprocessorExtension extends Preprocessor {

    public FrontMatterPreprocessorExtension(Map<String, Object> config) {
        super(config);
    }

    @Override
    public PreprocessorReader process(PreprocessorReader reader,
            List<String> lines) {

        final List<String> frontMatter = new ArrayList<String>();

        List<String> originalLines = new ArrayList<String>(lines);

        if (lines.size() == 0) {
            return reader;
        } else {
            if ("---".equals(lines.get(0).trim())) {
                lines.remove(0);
            }

            Iterator<String> iterator = lines.iterator();

            while (iterator.hasNext()) {
                String line = iterator.next().trim();

                if (!"---".equals(line)) {
                    iterator.remove();
                    frontMatter.add(line);
                } else {
                    break;
                }
            }

            if (lines.size() == 0 || !"---".equals(lines.get(0).trim())) {
                lines.clear();
                lines.addAll(originalLines);
            } else {
                lines.remove(0);
                /*document.getAttributes().put("front-matter",
                        frontMatter.toString());*/
            }

            for (int i = 0; i < frontMatter.size() + 2; i++) {
                reader.advance();
            }

        }

        return reader;
    }

}