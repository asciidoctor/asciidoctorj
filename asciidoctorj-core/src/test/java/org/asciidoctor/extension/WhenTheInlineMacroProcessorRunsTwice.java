package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class WhenTheInlineMacroProcessorRunsTwice {

    // See https://github.com/asciidoctor/asciidoctorj/issues/926
    @Test
    public void inlineMacroAttributes() {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().inlineMacro(new InlineMacroProcessor("example") {

            @Override
            public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
                return createPhraseNode(parent, "quoted", attributes.toString(), attributes, new HashMap<>());
            }

        });
        assertThat(convert(asciidoctor), containsString("{format=test}"));
        assertThat(convert(asciidoctor), containsString("{format=test}"));
    }

    private String convert(Asciidoctor asciidoctor) {
        return asciidoctor.convert("example:alpha.bravo[format=test]", Collections.emptyMap());
    }

}
