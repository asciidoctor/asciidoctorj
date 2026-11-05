package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Regression tests for differences with Asciidoctor Ruby found by tools/compare (no Ruby counterpart).
 */
@DisplayName("HTML5 converter")
public class Html5ConverterTest extends TestHelper {
    @Test
    @DisplayName("should not output null in footer when version-label is unset")
    void shouldNotOutputNullInFooterWhenVersionLabelIsUnset() throws Exception {
        var input = """
                = Document Title
                :revnumber: LPR55
                :version-label!:

                content
                """;
        var result = convertString(input);
        assertXpath("//*[@id=\"footer-text\"][normalize-space(text())=\"LPR55\"]", result, 1);
        assertFalse(result.contains("null"));
    }

    @Test
    @DisplayName("should not output null in toc entry href when section has no id")
    void shouldNotOutputNullInTocEntryHrefWhenSectionHasNoId() throws Exception {
        var input = """
                = Document Title
                :toc:
                :sectids!:

                == Section

                content
                """;
        var result = convertString(input);
        assertXpath("//*[@id=\"toc\"]//a[@href=\"#\"][text()=\"Section\"]", result, 1);
        assertFalse(result.contains("#null"));
    }

    @Test
    @DisplayName("should report AsciidoctorJ and its version as generator")
    void shouldReportAsciidoctorJAndItsVersionAsGenerator() throws Exception {
        var result = convertString("= Document Title\n\ncontent");
        assertXpath("//meta[@name=\"generator\"][@content=\"AsciidoctorJ " + Asciidoctor.VERSION + "\"]", result, 1);
        var manpage = (String) Asciidoctor.convert("= cmd(1)\n:doctype: manpage\n\n== Name\n\ncmd - does things\n",
                Options.of("backend", "manpage", "standalone", true));
        assertIncludes(manpage, ".\\\" Generator: AsciidoctorJ " + Asciidoctor.VERSION + "\n");
    }
}
