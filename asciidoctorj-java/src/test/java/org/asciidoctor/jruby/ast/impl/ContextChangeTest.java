package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ContextChangeTest  {

    private final Asciidoctor asciiDoctor = Asciidoctor.Factory.create();

    static String orderedListSample() {
        return "= Document Title\n\n" +
                "== Section A\n\n" +
                ". This it item 1 in an ordered list\n\n" +
                ". This is item 2 in an ordered list\n\n" +
                ". This is item 3 in and ordered list\n\n";

    }

    @Test
    public void get_context_of_ordered_list(){

        Document document = loadDocument(orderedListSample());

        assertThat(document).isNotNull();
        assertThat(document.getBlocks().size()).isEqualTo(1);

        StructuralNode orderedList = document.getBlocks().get(0).getBlocks().get(0);
        assertThat(orderedList).isNotNull();

        // Odd – I expected this to send back :'olist'
        assertThat(orderedList.getContext()).isEqualTo("olist");

        // But can you change it?

        orderedList.setContext("colist");

        assertThat(orderedList.getContext()).isEqualTo("colist");

    }

    private Document loadDocument(String source) {
        Attributes attributes = Attributes.builder().sectionNumbers(false).build();
        Options options = Options.builder().attributes(attributes).build();
        return asciiDoctor.load(source, options);
    }
}
