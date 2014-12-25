package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractBlock;
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
    public Object convert(AbstractBlock node, String transform, Map<Object, Object> o) {

        assertThat(node.getClass().getPackage().getName(), is("org.asciidoctor.ast"));

        if (transform == null) {
            transform = node.getNodeName();
        }
 
        if (node instanceof Document) {
            return node.content();
        } else if (node instanceof Section) {
            return new StringBuilder().append("== ").append(node.title()).append(" ==").append(LINE_SEPARATOR).append(LINE_SEPARATOR).append(node.content()).toString();
        } else if (transform.equals("paragraph")) {
            String content = (String) node.content();
            return new StringBuilder(content.replaceAll(LINE_SEPARATOR, " ")).append('\n');
        } else {
            return node.content();
        }
    }

}
