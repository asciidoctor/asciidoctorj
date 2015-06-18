package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.AbstractNode;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.ListNode;
import org.asciidoctor.ast.Section;

import java.util.Map;

public class DummyConverter extends AbstractConverter {

    public DummyConverter(String backend, Map<Object, Object> opts) {
        super(backend, opts);
    }
    
    @Override
    public Object convert(AbstractNode node, String transform, Map<Object, Object> o) {
        return "Dummy";
    }

}
