package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenAPreprocessorSetsTheSourceMapOptionTest {

    static final int NOT_SET = -1;

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    public static class SourceMapOptionPreprocessor extends Preprocessor {
        @Override
        public Reader process(Document document, PreprocessorReader reader) {
            document.setSourcemap(true);
            return reader;
        }
    }

    public static class TestTreeprocessor extends Treeprocessor {
        int lineNoFirstSection = 0;

        @Override
        public Document process(Document document) {
            List<StructuralNode> blocks = document.getBlocks();
            Cursor sourceLocation = blocks.get(0).getSourceLocation();
            // NOTE like the Elvis operator in Groovy, 0 (the line number of a cursor) is falsy
            lineNoFirstSection = sourceLocation != null && sourceLocation.getLineNumber() != 0 ? sourceLocation.getLineNumber() : NOT_SET;
            return document;
        }
    }

    String DOCUMENT = """
            = Test

            == A Section

            Some text
            """;

    @Test
    @DisplayName("another extension can get the source location if a preprocessor enabled the SourceMap option on the Document")
    public void anotherExtensionCanGetTheSourceLocationIfAPreprocessorEnabledTheSourceMapOption() {

        // given:
        TestTreeprocessor treeprocessor = new TestTreeprocessor();

        // when:
        asciidoctor.javaExtensionRegistry()
                .preprocessor(new SourceMapOptionPreprocessor())
                .treeprocessor(treeprocessor);
        Document doc = asciidoctor.load(DOCUMENT, Options.builder().build());

        // then:
        assertThat(treeprocessor.lineNoFirstSection).isEqualTo(3);
        assertThat(doc.isSourcemap()).isTrue();
    }

    @Test
    @DisplayName("another extension cannot get the source location if a preprocessor did not enable the SourceMap option on the Document")
    public void anotherExtensionCannotGetTheSourceLocationIfAPreprocessorDidNotEnableTheSourceMapOption() {

        // given:
        TestTreeprocessor treeprocessor = new TestTreeprocessor();

        // when:
        asciidoctor.javaExtensionRegistry()
                .treeprocessor(treeprocessor);
        Document doc = asciidoctor.load(DOCUMENT, Options.builder().build());

        // then:
        assertThat(treeprocessor.lineNoFirstSection).isEqualTo(NOT_SET);
        assertThat(doc.isSourcemap()).isFalse();
    }

}
