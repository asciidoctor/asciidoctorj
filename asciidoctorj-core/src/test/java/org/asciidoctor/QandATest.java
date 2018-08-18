package org.asciidoctor;

//import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.Document;
//import org.asciidoctor.ast.StructuralNode;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class QandATest {
/*
    private static final String DOC = "== Test\n" +
        "\n" +
        ".Q & A block\n"+
        "[qanda]\n"+
        "What is Asciidoctor?::\n"+
        "  An implementation of the AsciiDoc processor in Ruby.\n"+
        "\n"+
        "What is the answer to the Ultimate Question?:: 42";

    @Test
    public void shouldParseTitle() {
        final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        final Document doc = asciidoctor.load(DOC, OptionsBuilder.options().asMap());
        final DescriptionList dl = findDescriptionList(doc);
        assertThat(dl.getTitle(), is("Q &amp; A block"));
        assertThat(dl.getStyle(), is("qanda"));
    }

    private DescriptionList findDescriptionList(StructuralNode node) {
        if (node instanceof DescriptionList) {
            return (DescriptionList) node;
        } else {
            for (StructuralNode structuralNode : node.getBlocks()) {
                DescriptionList dl = findDescriptionList(structuralNode);
                if (dl != null) {
                    return dl;
                }
            }
            return null;
        }
    }
    */
}
