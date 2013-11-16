package org.asciidoctor.extension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.internal.AbstractBlock;
import org.asciidoctor.internal.DocumentRuby;
import org.asciidoctor.internal.Reader;

public class ArrowsAndBoxesBlock extends BlockProcessor {

    static {
        config.put("contexts", Arrays.asList(":paragraph"));
        config.put("content_model", ":simple");
    }

    public ArrowsAndBoxesBlock(String context, DocumentRuby documentRuby) {
        super(context, documentRuby);
    }

    @Override
    public Object process(AbstractBlock parent, Reader reader,
            Map<String, Object> attributes) {

        List<String> lines = reader.lines();

        StringBuilder outputLines = new StringBuilder();
        outputLines.append("<pre class=\"arrows-and-boxes\">");

        for (String line : lines) {
            outputLines.append(line);
        }

        outputLines.append("</pre>");
        attributes.put("!subs", "");

        return createBlock(document, "pass", Arrays.asList(outputLines.toString()),
                attributes, new HashMap<String, Object>() {
                    {
                        put("content_model", ":raw");
                    }
                });

    }
}
