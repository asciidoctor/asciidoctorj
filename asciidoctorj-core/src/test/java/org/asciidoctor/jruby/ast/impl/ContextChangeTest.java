package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


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

        assertThat(document, notNullValue());
        assertThat(document.getBlocks().size(), equalTo(1));

        StructuralNode orderedList = document.getBlocks().get(0).getBlocks().get(0);
        assertThat(orderedList, notNullValue());

        // Odd – I expected this to send back :'olist'
        assertThat(orderedList.getContext(), equalTo("olist"));

        // But can you change it?

        orderedList.setContext("colist");

        assertThat(orderedList.getContext(), equalTo("colist"));

    }

    private Document loadDocument(String source) {
        Attributes attributes = Attributes.builder().sectionNumbers(false).build();
        Options options = Options.builder().attributes(attributes).build();
        return asciiDoctor.load(source, options);
    }
}
