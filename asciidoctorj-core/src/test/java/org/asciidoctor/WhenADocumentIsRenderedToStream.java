package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.converter.StringConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WhenADocumentIsRenderedToStream {

    public static final String HTML5_BACKEND = "html5";
    public static final String HELLO_WORLD = "Hello World";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    static final String TEST_DOC = """
            = Test

            A test document

            == A Section

            Hello World
            """;

    @Test
    @DisplayName("should render HTML to ByteArrayOutputStream")
    void shouldRenderHTMLToByteArrayOutputStream() throws Exception {

        // given:
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // when:
        asciidoctor.convert(TEST_DOC,
                Options.builder()
                        .backend(HTML5_BACKEND)
                        .toStream(out)
                        .build());

        String result = new String(out.toByteArray());

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).contains(HELLO_WORLD);
    }

    @Test
    @DisplayName("should render twice HTML to ByteArrayOutputStream")
    void shouldRenderTwiceHTMLToByteArrayOutputStream() throws Exception {

        // given:
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();

        // when:
        asciidoctor.convert(TEST_DOC,
                Options.builder()
                        .backend(HTML5_BACKEND)
                        .toStream(out1)
                        .build());
        asciidoctor.convert(TEST_DOC,
                Options.builder()
                        .backend(HTML5_BACKEND)
                        .toStream(out2)
                        .build());

        String result1 = new String(out1.toByteArray());
        String result2 = new String(out2.toByteArray());

        // then:
        assertThat(result1).isNotEmpty().contains(HELLO_WORLD);
        assertThat(result2).isNotEmpty().contains(HELLO_WORLD);
    }

    @Test
    @DisplayName("should render with custom Java converter to ByteArrayOutputStream")
    void shouldRenderWithCustomJavaConverterToByteArrayOutputStream() throws Exception {

        // given:
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        asciidoctor.javaConverterRegistry().register(TestConverter.class, TestConverter.TEST_BACKEND_NAME);
        String testDoc = """
                == Test

                Hello, World!
                """;

        // when:
        asciidoctor.convert(testDoc,
                Options.builder()
                        .backend(TestConverter.TEST_BACKEND_NAME)
                        .toStream(out)
                        .build());

        String result = new String(out.toByteArray());

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).contains("-- Test --");
        assertThat(result).contains("Hello, World!");
    }


    public static class TestConverter extends StringConverter {

        public static final String TEST_BACKEND_NAME = "test";

        private static final String NEWLINE = "\n";
        private static final String NEWLINE_2 = NEWLINE.repeat(2);

        public TestConverter(String backend, Map<String, Object> opts) {
            super(backend, opts);
        }

        @Override
        public String convert(ContentNode node, String transform, Map<Object, Object> opts) {
            if (transform == null) {
                transform = node.getNodeName();
            }
            switch (transform) {
                case "embedded":
                    return convertEmbedded((Document) node);
                case "section":
                    return convertSection((Section) node);
                case "paragraph":
                    return convertParagraph((Block) node);
                default:
                    throw new IllegalArgumentException("No method convert" + transform);
            }
        }

        String convertEmbedded(Document node) {
            String result = node.getDoctitle().toUpperCase();
            String content = (String) node.getContent();
            if (content != null && content.trim().length() > 0) {
                result += NEWLINE_2 + content;
            }
            return result;
        }

        String convertSection(Section section) {
            String result = "-- " + section.getTitle() + " --";
            String content = (String) section.getContent();
            if (content != null && content.trim().length() > 0) {
                result += NEWLINE_2 + content;
            }
            return result;
        }

        String convertParagraph(Block block) {
            return String.join(NEWLINE, block.getLines());
        }

    }

}
