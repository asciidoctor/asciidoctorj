package org.asciidoctor.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TableConverterTest {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private static List<String> linesWithoutTrailingSpaces(String content) {
        return content.lines().map(line -> line.replaceAll("\\s+$", "")).collect(Collectors.toList());
    }

    @Test
    @DisplayName("should convert a table with horizontal alignment")
    void shouldConvertATableWithHorizontalAlignment() {
        // given:
        String document = """

                = Hello Table

                [chapter]
                == A chapter

                .Simple table with single text rows and a header
                [options="header,footer"]
                [cols="<,^,^"]
                |====
                |       | Column A | Column B

                | Row 1 |   A1     |    B1
                | Row 2 |   A2     |    B2

                | Sum   | Sum A    | Sum B
                |====
                """;
        asciidoctor.javaConverterRegistry().register(TableTestConverter.class, "tabletestconverter");

        // when:
        Options options = Options.builder().standalone(false).backend("tabletestconverter").build();
        String content = asciidoctor.convert(document, options);

        // then:
        assertThat(linesWithoutTrailingSpaces(content)).isEqualTo("""
                HELLO TABLE

                -- A chapter --

                               |   Column A    |   Column B
                ---------------+---------------+---------------
                Row 1          |      A1       |      B1
                Row 2          |      A2       |      B2
                ===============#===============#===============
                Sum            |     Sum A     |     Sum B""".lines().collect(Collectors.toList()));
    }

    @Test
    @DisplayName("the columns of a loaded table should be numbered starting at 1")
    void theColumnsOfALoadedTableShouldBeNumberedStartingAtOne() {
        // given:
        String document = """
                = Hello Table

                |====
                | A | B | C
                | 1 | 2 | 3
                |====
                """;

        // when:
        org.asciidoctor.ast.Document loaded = asciidoctor.load(document, Options.builder().standalone(false).build());
        Table table = (Table) loaded.findBy(Map.of("context", ":table")).get(0);

        // then:
        assertThat(table.getColumns()).extracting(Column::getColumnNumber).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("should convert a table with nested asciidoctor content")
    void shouldConvertATableWithNestedAsciidoctorContent() {

        // given:
        String document = """

                = Hello Asciidoctor Table

                [cols="2"]
                |====
                a|
                = A header
                | Second column
                |====
                """;
        asciidoctor.javaConverterRegistry().register(TableTestConverter.class, "tabletestconverter");

        // when:
        Options options = Options.builder().standalone(false).backend("tabletestconverter").build();
        String content = asciidoctor.convert(document, options);

        // then:
        assertThat(linesWithoutTrailingSpaces(content)).isEqualTo("""
                HELLO ASCIIDOCTOR TABLE

                A HEADER       |Second column""".lines().collect(Collectors.toList()));

    }
}
