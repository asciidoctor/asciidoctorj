package org.asciidoctor;

import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_ATTRIBUTES;
import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_MACROS;
import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_POST_REPLACEMENTS;
import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_QUOTES;
import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_REPLACEMENTS;
import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_SPECIAL_CHARACTERS;
import static org.asciidoctor.util.OptionsTestHelper.emptyOptions;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Treeprocessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenSubstitutionsAreUsedTest {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    @DisplayName("a node should return its substitutions")
    public void aNodeShouldReturnItsSubstitutions() {

        // given:
        String document = """

                = Test document

                == Test section

                Test paragraph

                [source,java]
                ----
                System.out.println("Hello World");
                ----
                """;

        // when:
        Document doc = asciidoctor.load(document, emptyOptions());
        Block paragraph = (Block) doc.getBlocks().get(0).getBlocks().get(0);
        Block source = (Block) doc.getBlocks().get(0).getBlocks().get(1);

        // then:
        assertThat(paragraph.getSubstitutions()).isEqualTo(List.of(SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_QUOTES, SUBSTITUTION_ATTRIBUTES, SUBSTITUTION_REPLACEMENTS, SUBSTITUTION_MACROS, SUBSTITUTION_POST_REPLACEMENTS));
        assertThat(source.getSubstitutions()).isEqualTo(List.of(SUBSTITUTION_SPECIAL_CHARACTERS));
    }

    @Test
    @DisplayName("it should be possible to remove a substitution")
    public void itShouldBePossibleToRemoveASubstitution() {
        // given:
        String document = """

                = Test document
                :foo: bar

                == Test section

                First test paragraph {foo}.

                Second test paragraph {foo}

                """;

        // when:
        asciidoctor.javaExtensionRegistry().treeprocessor(TestTreeprocessor.class);
        Document doc = asciidoctor.load(document, emptyOptions());
        Block firstparagraph = (Block) doc.getBlocks().get(0).getBlocks().get(0);
        Block secondparagraph = (Block) doc.getBlocks().get(0).getBlocks().get(1);

        String html = asciidoctor.convert(document, emptyOptions());

        // then:
        assertThat(firstparagraph.getSubstitutions()).isEqualTo(List.of(SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_QUOTES, SUBSTITUTION_ATTRIBUTES, SUBSTITUTION_REPLACEMENTS, SUBSTITUTION_MACROS, SUBSTITUTION_POST_REPLACEMENTS));
        assertThat(firstparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)).isTrue();
        assertThat(secondparagraph.getSubstitutions()).isEqualTo(List.of(SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_QUOTES, SUBSTITUTION_REPLACEMENTS, SUBSTITUTION_MACROS, SUBSTITUTION_POST_REPLACEMENTS));
        assertThat(secondparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)).isFalse();

        assertThat(html).contains("First test paragraph bar.");
        assertThat(html).contains("Second test paragraph {foo}");
    }

    @Test
    @DisplayName("it should be possible to add a substitution")
    public void itShouldBePossibleToAddASubstitution() {
        // given:
        String document = """

                = Test document
                :foo: bar

                == Test section

                First test paragraph {foo}

                [source,java]
                ----
                System.out.println("{foo}");
                ----

                """;

        // when:
        asciidoctor.javaExtensionRegistry().treeprocessor(TestTreeprocessor.class);
        Document doc = asciidoctor.load(document, emptyOptions());
        Block firstparagraph = (Block) doc.getBlocks().get(0).getBlocks().get(0);
        Block secondparagraph = (Block) doc.getBlocks().get(0).getBlocks().get(1);

        String html = asciidoctor.convert(document, emptyOptions());

        // then:
        assertThat(firstparagraph.getSubstitutions()).isEqualTo(List.of(SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_QUOTES, SUBSTITUTION_ATTRIBUTES, SUBSTITUTION_REPLACEMENTS, SUBSTITUTION_MACROS, SUBSTITUTION_POST_REPLACEMENTS));
        assertThat(firstparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)).isTrue();
        assertThat(secondparagraph.getSubstitutions()).isEqualTo(List.of(SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_ATTRIBUTES));
        assertThat(secondparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)).isTrue();

        assertThat(html).contains("First test paragraph bar");
        assertThat(html).contains("System.out.println(\"bar\");");
    }

    public static class TestTreeprocessor extends Treeprocessor {
        @Override
        public Document process(Document document) {
            if (document.getBlocks().get(0).getBlocks().get(1).isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)) {
                document.getBlocks().get(0).getBlocks().get(1).removeSubstitution(SUBSTITUTION_ATTRIBUTES);
            } else {
                document.getBlocks().get(0).getBlocks().get(1).addSubstitution(SUBSTITUTION_ATTRIBUTES);
            }
            return document;
        }
    }


    @Test
    @DisplayName("it should be possible to prepend a substitution")
    public void itShouldBePossibleToPrependASubstitution() {
        // given:
        // Usually *{foo} should not be rendered as bold text as attributes are substituted after quotes.
        // But by prepending the attributes substitution we can accomplish that
        String document = """

                = Test document
                :foo: bar*

                == Test section

                First test paragraph *{foo}


                """;

        // when:
        asciidoctor.javaExtensionRegistry().treeprocessor(PrependSubstitutionTestTreeprocessor.class);
        Document doc = asciidoctor.load(document, emptyOptions());
        Block firstparagraph = (Block) doc.getBlocks().get(0).getBlocks().get(0);

        String html = asciidoctor.convert(document, emptyOptions());

        // then:
        assertThat(firstparagraph.getSubstitutions()).isEqualTo(List.of(SUBSTITUTION_ATTRIBUTES, SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_QUOTES, SUBSTITUTION_ATTRIBUTES, SUBSTITUTION_REPLACEMENTS, SUBSTITUTION_MACROS, SUBSTITUTION_POST_REPLACEMENTS));
        assertThat(firstparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)).isTrue();

        assertThat(html).contains("First test paragraph <strong>bar</strong>");
    }

    public static class PrependSubstitutionTestTreeprocessor extends Treeprocessor {
        @Override
        public Document process(Document document) {
            document.getBlocks().get(0).getBlocks().get(0).prependSubstitution(SUBSTITUTION_ATTRIBUTES);
            return document;
        }
    }

    @Test
    @DisplayName("it should be possible to set a substitution")
    public void itShouldBePossibleToSetASubstitution() {
        // given:
        // By default in the listing < and > should be substituted by &lt; and &gt; due to the special characters substitution.
        // We set only the attributes substitution instead and expect literally 'bar <=> bar' in the output.
        String document = """

                = Test document
                :foo: bar

                == Test section

                ----
                {foo} <=> bar
                ----

                """;

        // when:
        asciidoctor.javaExtensionRegistry().treeprocessor(SetSubstitutionTestTreeprocessor.class);
        Document doc = asciidoctor.load(document, emptyOptions());
        Block firstparagraph = (Block) doc.getBlocks().get(0).getBlocks().get(0);

        String html = asciidoctor.convert(document, Options.builder().build());

        // then:
        assertThat(firstparagraph.getSubstitutions()).isEqualTo(List.of(SUBSTITUTION_ATTRIBUTES));
        assertThat(firstparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)).isTrue();

        assertThat(html).contains("bar <=> bar");
    }

    public static class SetSubstitutionTestTreeprocessor extends Treeprocessor {
        @Override
        public Document process(Document document) {
            document.getBlocks().get(0).getBlocks().get(0).setSubstitutions(SUBSTITUTION_ATTRIBUTES);
            return document;
        }
    }

}
