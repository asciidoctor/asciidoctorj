package org.asciidoctor.integrationguide.extension;

//tag::include[]
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

import java.util.ArrayList;
import java.util.List;

public class CommentPreprocessor extends Preprocessor {   // <1>

    @Override
    public void process(Document document, PreprocessorReader reader) {

        List<String> lines = reader.readLines();          // <2>
        List<String> newLines = new ArrayList<String>();

        boolean inComment = false;

        for (String line: lines) {                        // <3>
            if (line.trim().equals("////")) {
                if (!inComment) {
                   newLines.add("[NOTE]");
                }
                newLines.add("--");
                inComment = !inComment;
            } else {
                newLines.add(line);
            }
        }

        reader.restoreLines(newLines);                    // <4>
    }
}
//end::include[]
