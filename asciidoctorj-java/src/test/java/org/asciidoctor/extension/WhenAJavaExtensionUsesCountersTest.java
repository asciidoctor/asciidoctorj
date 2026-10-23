package org.asciidoctor.extension;

import static org.asciidoctor.ast.ContentModel.SIMPLE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenAJavaExtensionUsesCountersTest {

    public static final String UTF8 = "UTF-8";

    public static final String PARAGRAPH = "paragraph";

    public static final String PARAGRAPH_SELECTOR = ".paragraph";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();


    @Test
    @DisplayName("using the counter macro should continue the counters set in macros but doesnt")
    public void usingTheCounterMacroShouldContinueTheCountersSetInMacrosButDoesnt() {
        // given:
        String document = """
                = Test document

                == Test section

                testmacro::countera[]

                testmacro::counterb[]

                testmacro::countera[]

                The attribute for countera: {counter:countera}

                The attribute for counterb: {counter:counterb}

                """;
        // when:
        asciidoctor.javaExtensionRegistry().blockMacro(TestBlockMacroProcessor.class);
        String result = asciidoctor.convert(document, Options.builder().standalone(true).safe(SafeMode.SERVER).build());

        // then:
        Document doc = Jsoup.parse(result, UTF8);
        Elements paragraphs = doc.select(PARAGRAPH_SELECTOR);
        assertThat(paragraphs.get(0).text()).isEqualTo("This is macro call 1 for countera");
        assertThat(paragraphs.get(1).text()).isEqualTo("This is macro call 1 for counterb");
        assertThat(paragraphs.get(2).text()).isEqualTo("This is macro call 2 for countera");

        assertThat(paragraphs.get(3).text()).isEqualTo("The attribute for countera: 1");
        assertThat(paragraphs.get(4).text()).isEqualTo("The attribute for counterb: 1");
    }

    @Test
    @DisplayName("using a treeprocessor should continue the counters set in macros")
    public void usingATreeprocessorShouldContinueTheCountersSetInMacros() {
        // given:
        String document = """
                = Test document

                == Test section

                [.count]
                countera

                [.count]
                counterb

                The attribute for countera: {counter:countera}

                The attribute for counterb: {counter:counterb}

                """;
        // when:
        asciidoctor.javaExtensionRegistry().treeprocessor(TestTreeProcessor.class);
        String result = asciidoctor.convert(document, Options.builder().standalone(true).safe(SafeMode.SERVER).build());

        // then:
        Document doc = Jsoup.parse(result, UTF8);
        Elements paragraphs = doc.select(PARAGRAPH_SELECTOR);
        assertThat(paragraphs.get(0).text()).isEqualTo("Counter countera has value 1");
        assertThat(paragraphs.get(1).text()).isEqualTo("Counter counterb has value 1");
        assertThat(paragraphs.get(2).text()).isEqualTo("The attribute for countera: 2");
        assertThat(paragraphs.get(3).text()).isEqualTo("The attribute for counterb: 2");
    }

    @Test
    @DisplayName("an initial seed should be used")
    public void anInitialSeedShouldBeUsed() {
        // given:
        String document = """
                = Test document

                == Test section

                testmacro::countera[]

                testmacro::counterb[]

                testmacro::countera[]

                """;
        // when:
        asciidoctor.javaExtensionRegistry().blockMacro(TestBlockMacroWithInitialCounterProcessor.class);
        String result = asciidoctor.convert(document, Options.builder().standalone(true).safe(SafeMode.SERVER).build());

        // then:
        Document doc = Jsoup.parse(result, UTF8);
        Elements paragraphs = doc.select(PARAGRAPH_SELECTOR);
        assertThat(paragraphs.get(0).text()).isEqualTo("This is macro call 42 for countera");
        assertThat(paragraphs.get(1).text()).isEqualTo("This is macro call 42 for counterb");
        assertThat(paragraphs.get(2).text()).isEqualTo("This is macro call 43 for countera");
    }

    @Name("testmacro")
    @Contexts(Contexts.PARAGRAPH)
    @ContentModel(SIMPLE)
    public static class TestBlockMacroProcessor extends BlockMacroProcessor {

        @Override
        public StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {

            String text = "This is macro call " + parent.getDocument().getAndIncrementCounter(target) + " for " + target;
            // Have to do this to interact successfully with counters also used in the document
            // String text = "This is macro call {counter:${target}} for ${target}"

            return createBlock(parent, PARAGRAPH, text);
        }
    }

    @Name("testmacro")
    @Contexts(Contexts.PARAGRAPH)
    @ContentModel(SIMPLE)
    public static class TestBlockMacroWithInitialCounterProcessor extends BlockMacroProcessor {

        @Override
        public StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {

            String text = "This is macro call " + parent.getDocument().getAndIncrementCounter(target, 42) + " for " + target;

            return createBlock(parent, PARAGRAPH, text);
        }
    }

    public static class TestTreeProcessor extends Treeprocessor {

        @Override
        public org.asciidoctor.ast.Document process(org.asciidoctor.ast.Document document) {
            List<StructuralNode> newNodes = document.getBlocks().stream().map(it -> {
                if (it instanceof Section) {
                    return processSection((Section) it);
                } else {
                    return it;
                }
            }).collect(Collectors.toList());
            document.getBlocks().clear();
            document.getBlocks().addAll(newNodes);
            return document;
        }

        Section processSection(Section section) {
            List<StructuralNode> newNodes = section.getBlocks().stream().map(it -> {
                if (it instanceof Block && it.getRoles().contains("count")) {
                    Block block = (Block) it;
                    return createBlock(section, PARAGRAPH, "Counter " + block.getContent() + " has value " + block.getDocument().getAndIncrementCounter(block.getSource()));
                } else {
                    return it;
                }
            }).collect(Collectors.toList());
            section.getBlocks().clear();
            section.getBlocks().addAll(newNodes);
            return section;
        }
    }
}
