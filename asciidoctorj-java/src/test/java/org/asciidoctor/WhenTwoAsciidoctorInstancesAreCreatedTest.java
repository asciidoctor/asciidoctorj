package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenTwoAsciidoctorInstancesAreCreatedTest {

    private static final String PARAGRAPH = "paragraph";

    private static final String TEST_STRING = "Hello World";

    @Test
    @DisplayName("then every Asciidoctor instance has its own extension registry")
    public void thenEveryAsciidoctorInstanceHasItsOwnExtensionRegistry() {
        // given:
        String document = """
                = Test document

                testmacro::Test[]
                """;

        // when:
        Asciidoctor asciidoctor1 = Asciidoctor.Factory.create();
        Asciidoctor asciidoctor2 = Asciidoctor.Factory.create();

        asciidoctor1.javaExtensionRegistry().blockMacro("testmacro", TestBlockMacroProcessor.class);

        // then:
        Options standaloneDisabledOptions = Options.builder().standalone(false).build();
        assertThat(asciidoctor1.convert(document, standaloneDisabledOptions)).contains(TEST_STRING);
        assertThat(asciidoctor2.convert(document, standaloneDisabledOptions)).doesNotContain(TEST_STRING);
    }

    public static class TestBlockMacroProcessor extends BlockMacroProcessor {
        public TestBlockMacroProcessor(String macroName) {
            super(macroName);
        }

        @Override
        public Block process(StructuralNode parent, String target, Map<String, Object> attributes) {
            return createBlock(parent, PARAGRAPH, TEST_STRING);
        }
    }
}
