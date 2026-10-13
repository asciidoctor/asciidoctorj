package org.asciidoctor;

import static org.asciidoctor.util.OptionsTestHelper.emptyOptions;
import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenADocumentContainsNumberedSectionsTest {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    @DisplayName("for appendix the section numeral should be a letter")
    public void forAppendixTheSectionNumeralShouldBeALetter() {
        // given:
        final String source = """
                = Article Title
                :sectnums:

                [appendix]
                == Appendix Title
                """;

        // when:
        Document document = asciidoctor.load(source, emptyOptions());

        // then:
        final Section appendix = (Section) document.getBlocks().get(0);
        assertThat(appendix.getTitle()).isEqualTo("Appendix Title");
        assertThat(appendix.getNumeral()).isEqualTo("A");
    }

    @Test
    @DisplayName("for regular section the numeral should be an arabic numeral")
    public void forRegularSectionTheNumeralShouldBeAnArabicNumeral() {
        // given:
        final String source = """
                = Article Title
                :sectnums:

                == Regular Section Title
                """;

        // when:
        Document document = asciidoctor.load(source, emptyOptions());

        // then:
        final Section section = (Section) document.getBlocks().get(0);
        assertThat(section.getTitle()).isEqualTo("Regular Section Title");
        assertThat(section.getNumeral()).isEqualTo("1");
    }

    @Test
    @DisplayName("for book part the section numeral should be a roman numeral")
    public void forBookPartTheSectionNumeralShouldBeARomanNumeral() {
        // given:
        final String source = """
                = Book Title
                :doctype: book
                :sectnums:
                :partnums:

                = Part Title

                == Chapter Title
                """;

        // when:
        Document document = asciidoctor.load(source, emptyOptions());

        // then:
        final Section part = (Section) document.getBlocks().get(0);
        assertThat(part.getTitle()).isEqualTo("Part Title");
        assertThat(part.getNumeral()).isEqualTo("I");
    }
}
