package org.asciidoctor.integrationguide.extension;

//tag::include[]

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;

import java.io.File;
import java.util.Map;

public class LsIncludeProcessor extends IncludeProcessor {    // <1>

    @Override
    public boolean handles(String target) {                   // <2>
        return "ls".equals(target);
    }

    @Override
    public void process(Document document,                    // <3>
                        PreprocessorReader reader,
                        String target,
                        Map<String, Object> attributes) {

        StringBuilder sb = new StringBuilder();

        for (File f: new File(".").listFiles()) {
            sb.append(f.getName()).append("\n");
        }

        reader.push_include(                                  // <4>
                sb.toString(),
                target,
                new File(".").getAbsolutePath(),
                1,
                attributes);
    }
}
//end::include[]
