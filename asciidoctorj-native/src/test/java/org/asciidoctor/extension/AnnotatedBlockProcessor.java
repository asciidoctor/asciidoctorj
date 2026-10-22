package org.asciidoctor.extension;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.StructuralNode;

@Name("yell")
@Contexts({Contexts.LISTING, Contexts.OPEN})
@ContentModel(ContentModel.SIMPLE)
@DefaultAttributes({
    @DefaultAttribute(key = "key", value = "value")
})
public class AnnotatedBlockProcessor extends BlockProcessor {

    public AnnotatedBlockProcessor() {}

    // This constructor will not be called when registered as a a class
    // because 2 Strings don't match any known signature.
    public AnnotatedBlockProcessor(String dummyValue, String blockName) {
        super(blockName);
    }

    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        assert "value".equals(attributes.get("key"));
        List<String> lines = reader.readLines();
        List<String> newLines = lines.stream().map(String::toUpperCase).collect(Collectors.toList());

        return createBlock(parent, "paragraph", newLines);
    }
}
