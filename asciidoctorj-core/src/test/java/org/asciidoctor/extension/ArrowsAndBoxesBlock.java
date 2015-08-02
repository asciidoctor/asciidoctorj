package org.asciidoctor.extension;

import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Document;

public class ArrowsAndBoxesBlock extends BlockProcessor {

//    static {
//        config.put("contexts", Arrays.asList(":paragraph"));
//        config.put("content_model", ":simple");
//    }

    public ArrowsAndBoxesBlock(String context, Document document) {
        //super(context, documentRuby);
        super(null, null);
    }

    @Override
    public Object process(StructuralNode parent, Reader reader,
            Map<String, Object> attributes) {

        List<String> lines = reader.lines();

        StringBuilder outputLines = new StringBuilder();
        outputLines.append("<pre class=\"arrows-and-boxes\">");

        for (String line : lines) {
            outputLines.append(line);
        }

        outputLines.append("</pre>");
        attributes.put("!subs", "");

        return null;
//        return createBlock(document, "pass", Arrays.asList(outputLines.toString()),
//                attributes, new HashMap<String, Object>() {
//                    {
//                        put("content_model", ":raw");
//                    }
//                });

    }
}
