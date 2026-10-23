package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.extension.Contexts;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Reader;

import java.util.Map;

//tag::include[]
@Name("yell")                                              // <1>
@Contexts({Contexts.PARAGRAPH})                            // <2>
@ContentModel(ContentModel.SIMPLE)                         // <3>
public class YellBlockProcessor extends BlockProcessor {   // <4>

    @Override
    public Object process(                                 // <5>
            StructuralNode parent, Reader reader, Map<String, Object> attributes) {

        String content = reader.read();
        String yellContent = content.toUpperCase();

        return createBlock(parent, "paragraph", yellContent, attributes);
    }

}
//end::include[]
