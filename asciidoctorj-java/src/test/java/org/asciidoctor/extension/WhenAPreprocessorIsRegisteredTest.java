package org.asciidoctor.extension;

import static org.asciidoctor.util.OptionsTestHelper.emptyOptions;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenAPreprocessorIsRegisteredTest {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    public static final int ONE = 1;
    public static final int TWO = 2;

    private final String firstLine = "First line";
    private final String secondLine = "Second line";

    private final String document = firstLine + "\n" + secondLine;

    @Test
    @DisplayName("should be able to peek single lines from Reader")
    public void shouldBeAbleToPeekSingleLinesFromReader() {
        // given:

        AtomicBoolean preprocessorCalled = new AtomicBoolean(false);

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            public Reader process(Document doc, PreprocessorReader reader) {
                preprocessorCalled.set(true);

                assertThat(reader.peekLine()).isEqualTo(firstLine);
                assertThat(reader.peekLine()).isEqualTo(firstLine);

                reader.advance();

                assertThat(reader.peekLine()).isEqualTo(secondLine);
                assertThat(reader.peekLine()).isEqualTo(secondLine);

                reader.advance();

                assertThat(reader.peekLine()).isNull();
                return reader;
            }
        });

        // when:
        asciidoctor.convert(document, emptyOptions());

        // then:
        assertThat(preprocessorCalled.get()).isTrue();
    }

    @Test
    @DisplayName("should be able to peek multiple lines from Reader")
    public void shouldBeAbleToPeekMultipleLinesFromReader() {
        // given:

        AtomicBoolean preprocessorCalled = new AtomicBoolean(false);

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            public Reader process(Document doc, PreprocessorReader reader) {
                preprocessorCalled.set(true);

                assertThat(reader.peekLines(ONE)).isEqualTo(List.of(firstLine));
                assertThat(reader.peekLines(TWO)).isEqualTo(List.of(firstLine, secondLine));

                reader.advance();

                assertThat(reader.peekLines(ONE)).isEqualTo(List.of(secondLine));
                assertThat(reader.peekLines(TWO)).isEqualTo(List.of(secondLine));

                reader.advance();

                assertThat(reader.peekLines(ONE)).isEqualTo(List.of());
                assertThat(reader.peekLines(TWO)).isEqualTo(List.of());
                return reader;
            }
        });

        // when:
        asciidoctor.convert(document, emptyOptions());

        // then:
        assertThat(preprocessorCalled.get()).isTrue();
    }

    @Test
    @DisplayName("should be able to read line by line from Reader")
    public void shouldBeAbleToReadLineByLineFromReader() {
        // given:

        AtomicBoolean preprocessorCalled = new AtomicBoolean(false);

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            public Reader process(Document doc, PreprocessorReader reader) {
                preprocessorCalled.set(true);

                assertThat(reader.readLine()).isEqualTo(firstLine);
                assertThat(reader.readLine()).isEqualTo(secondLine);
                assertThat(reader.readLine()).isNull();
                return reader;
            }
        });

        // when:
        asciidoctor.convert(document, emptyOptions());

        // then:
        assertThat(preprocessorCalled.get()).isTrue();
    }

    @Test
    @DisplayName("should be able to restore a line into the Reader")
    public void shouldBeAbleToRestoreALineIntoTheReader() {
        // given:

        AtomicBoolean preprocessorCalled = new AtomicBoolean(false);

        String anotherLine = "Another line";

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            public Reader process(Document doc, PreprocessorReader reader) {
                preprocessorCalled.set(true);

                assertThat(reader.peekLine()).isEqualTo(firstLine);

                reader.restoreLine(anotherLine);

                assertThat(reader.peekLine()).isEqualTo(anotherLine);
                assertThat(reader.readLine()).isEqualTo(anotherLine);
                assertThat(reader.peekLine()).isEqualTo(firstLine);
                assertThat(reader.readLine()).isEqualTo(firstLine);
                return reader;
            }
        });

        // when:
        asciidoctor.convert(document, emptyOptions());

        // then:
        assertThat(preprocessorCalled.get()).isTrue();
    }

    @Test
    @DisplayName("should be able to restore multiple lines into the Reader")
    public void shouldBeAbleToRestoreMultipleLinesIntoTheReader() {
        // given:

        AtomicBoolean preprocessorCalled = new AtomicBoolean(false);

        String anotherFirstLine = "Another first line";
        String anotherSecondLine = "Another second line";

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            public Reader process(Document doc, PreprocessorReader reader) {
                preprocessorCalled.set(true);

                assertThat(reader.peekLine()).isEqualTo(firstLine);

                reader.restoreLines(List.of(anotherFirstLine, anotherSecondLine));

                assertThat(reader.peekLine()).isEqualTo(anotherFirstLine);
                assertThat(reader.readLine()).isEqualTo(anotherFirstLine);
                assertThat(reader.peekLine()).isEqualTo(anotherSecondLine);
                assertThat(reader.readLine()).isEqualTo(anotherSecondLine);
                assertThat(reader.peekLine()).isEqualTo(firstLine);
                assertThat(reader.readLine()).isEqualTo(firstLine);
                return reader;
            }
        });

        // when:
        asciidoctor.convert(document, emptyOptions());

        // then:
        assertThat(preprocessorCalled.get()).isTrue();
    }

    @Test
    @DisplayName("should be able to get source and source lines")
    public void shouldBeAbleToGetSourceAndSourceLines() {
        // given:

        AtomicReference<String> source = new AtomicReference<>("");
        AtomicReference<List<String>> sourceLines = new AtomicReference<>(new ArrayList<>());

        asciidoctor.javaExtensionRegistry().preprocessor(new Preprocessor() {
            @Override
            public Reader process(Document doc, PreprocessorReader reader) {
                source.set(reader.getSource());
                sourceLines.set(reader.getSourceLines());
                return reader;
            }
        });

        // when:
        asciidoctor.convert(document, emptyOptions());

        // then:
        assertThat(source.get()).isEqualTo(document);
        assertThat(sourceLines.get()).isEqualTo(List.of(firstLine, secondLine));
    }

}
