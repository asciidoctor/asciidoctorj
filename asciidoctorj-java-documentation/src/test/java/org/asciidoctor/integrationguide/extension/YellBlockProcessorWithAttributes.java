package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Contexts;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Reader;

import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

//tag::include[]
@Name("yell")
@Contexts({Contexts.PARAGRAPH})
@ContentModel(ContentModel.SIMPLE)
public class YellBlockProcessorWithAttributes extends BlockProcessor {

    @Override
    public Object process(
            StructuralNode parent, Reader reader, Map<String, Object> attributes) {

        String content = reader.read();
        String yellContent = content.toUpperCase();

        String loudness = (String) attributes.get("loudness"); // <1>
        if (loudness != null) {
            yellContent += IntStream.range(0, Integer.parseInt(loudness))
                    .mapToObj(i -> "!")
                    .collect(joining());
        }

        return createBlock(parent, "paragraph", yellContent, attributes);
    }

}
//end::include[]
