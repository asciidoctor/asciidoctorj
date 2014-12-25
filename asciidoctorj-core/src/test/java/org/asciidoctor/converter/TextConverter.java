package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractNode;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TextConverter extends AbstractConverter {

    private String LINE_SEPARATOR = "\n";

    public TextConverter(String backend, Map<Object, Object> opts) {
        super(backend, opts);
    }
    
    @Override
    public Object convert(AbstractNode node, String transform, Map<Object, Object> o) {

        assertThat(node.getClass().getPackage().getName(), is("org.asciidoctor.ast"));

        if (transform == null) {
            transform = node.getNodeName();
        }
 
        if (node instanceof Document) {
            Document document = (Document) node;
            return document.content();
        } else if (node instanceof Section) {
            Section section = (Section) node;
            return new StringBuilder()
                    .append("== ").append(section.title()).append(" ==")
                    .append(LINE_SEPARATOR).append(LINE_SEPARATOR)
                    .append(section.content()).toString();
        } else if (transform.equals("paragraph")) {
            Block block = (Block) node;
            String content = (String) block.content();
            return new StringBuilder(content.replaceAll(LINE_SEPARATOR, " ")).append('\n');
        } else if (node instanceof Block) {
            Block block = (Block) node;
            return block.content();
        }
        return null;
    }

}
