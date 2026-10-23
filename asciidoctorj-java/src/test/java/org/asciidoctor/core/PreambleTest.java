package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

@DisplayName("Preamble")
public class PreambleTest extends TestHelper {

    @Test
    @DisplayName("title and single paragraph preamble before section")
    void titleAndSingleParagraphPreambleBeforeSection() throws Exception {
        var input = """
                = Title

                Preamble paragraph 1.

                == First Section

                Section paragraph 1.
                """;
        var result = convertString(input);
        assertXpath("//p", result, 2);
        assertXpath("//*[@id=\"preamble\"]", result, 1);
        assertXpath("//*[@id=\"preamble\"]//p", result, 1);
        assertXpath("//*[@id=\"preamble\"]/following-sibling::*//h2[@id=\"_first_section\"]", result, 1);
        assertXpath("//*[@id=\"preamble\"]/following-sibling::*//p", result, 1);
    }

    @Test
    @DisplayName("title of preface is blank by default in DocBook output")
    void titleOfPrefaceIsBlankByDefaultInDocBookOutput() throws Exception {
        var input = """
                = Document Title
                :doctype: book

                Preface content.

                == First Section

                Section content.
                """;
        var result = convertString(input, Options.of("backend", "docbook"));
        assertXpath("//preface/title", result, 1);
        var titleNode = xmlnodesAtXpath("//preface/title", result, 1);
        assertEquals("", titleNode.getText());
    }

    @Test
    @DisplayName("preface-title attribute is assigned as title of preface in DocBook output")
    void prefaceTitleAttributeIsAssignedAsTitleOfPrefaceInDocBookOutput() throws Exception {
        var input = """
                = Document Title
                :doctype: book
                :preface-title: Preface

                Preface content.

                == First Section

                Section content.
                """;
        var result = convertString(input, Options.of("backend", "docbook"));
        assertXpath("//preface/title[text()=\"Preface\"]", result, 1);
    }

    @Test
    @DisplayName("title and multi-paragraph preamble before section")
    void titleAndMultiParagraphPreambleBeforeSection() throws Exception {
        var input = """
                = Title

                Preamble paragraph 1.

                Preamble paragraph 2.

                == First Section

                Section paragraph 1.
                """;
        var result = convertString(input);
        assertXpath("//p", result, 3);
        assertXpath("//*[@id=\"preamble\"]", result, 1);
        assertXpath("//*[@id=\"preamble\"]//p", result, 2);
        assertXpath("//*[@id=\"preamble\"]/following-sibling::*//h2[@id=\"_first_section\"]", result, 1);
        assertXpath("//*[@id=\"preamble\"]/following-sibling::*//p", result, 1);
    }

    @Test
    @DisplayName("should not wrap content in preamble if document has title but no sections")
    void shouldNotWrapContentInPreambleIfDocumentHasTitleButNoSections() throws Exception {
        var input = """
                = Title

                paragraph
                """;
        var result = convertString(input);
        assertXpath("//p", result, 1);
        assertXpath("//*[@id=\"content\"]/*[@class=\"paragraph\"]/p", result, 1);
        assertXpath("//*[@id=\"content\"]/*[@class=\"paragraph\"]/following-sibling::*", result, 0);
    }

    @Test
    @DisplayName("title and section without preamble")
    void titleAndSectionWithoutPreamble() throws Exception {
        var input = """
                = Title

                == First Section

                Section paragraph 1.
                """;
        var result = convertString(input);
        assertXpath("//p", result, 1);
        assertXpath("//*[@id=\"preamble\"]", result, 0);
        assertXpath("//h2[@id=\"_first_section\"]", result, 1);
    }

    @Test
    @DisplayName("no title with preamble and section")
    void noTitleWithPreambleAndSection() throws Exception {
        var input = """
                Preamble paragraph 1.

                == First Section

                Section paragraph 1.
                """;
        var result = convertString(input);
        assertXpath("//p", result, 2);
        assertXpath("//*[@id=\"preamble\"]", result, 0);
        assertXpath("//h2[@id=\"_first_section\"]/preceding::p", result, 1);
    }

    @Test
    @DisplayName("preamble in book doctype")
    void preambleInBookDoctype() throws Exception {
        var input = """
                = Book
                :doctype: book

                Back then...

                = Chapter One

                [partintro]
                It was a dark and stormy night...

                == Scene One

                Someone's gonna get axed.

                = Chapter Two

                [partintro]
                They couldn't believe their eyes when...

                == Scene One

                The axe came swinging.
                """;
        var d = documentFromString(input);
        assertEquals("book", d.getDoctype());
        var output = d.convert();
        assertXpath("//h1", output, 3);
        assertXpath(("//*[@id=\"preamble\"]//p[text() = \"Back then" + str(decodeChar(8230)) + str(decodeChar(8203)) + "\"]"), output, 1);
    }

    @Test
    @DisplayName("should output table of contents in preamble if toc-placement attribute value is preamble")
    void shouldOutputTableOfContentsInPreambleIfTocPlacementAttributeValueIsPreamble() throws Exception {
        var input = """
                = Article
                :toc:
                :toc-placement: preamble

                Once upon a time...

                == Section One

                It was a dark and stormy night...

                == Section Two

                They couldn't believe their eyes when...
                """;
        var output = convertString(input);
        assertXpath("//*[@id=\"preamble\"]/*[@id=\"toc\"]", output, 1);
    }

    @Test
    @DisplayName("should move abstract in implicit preface to info tag when converting to DocBook")
    void shouldMoveAbstractInImplicitPrefaceToInfoTagWhenConvertingToDocBook() throws Exception {
        var input = """
                = Document Title

                [abstract]
                This is the abstract.

                == Fin
                """;
        for (var doctype : list("article", "book")) {
            var output = convertString(input, Options.of("backend", "docbook", "doctype", doctype));
            assertXpath("//abstract", output, 1);
            assertXpath(("/" + str(doctype) + "/info/abstract"), output, 1);
        }
    }

    @Test
    @DisplayName("should move abstract as first section to info tag when converting to DocBook")
    void shouldMoveAbstractAsFirstSectionToInfoTagWhenConvertingToDocBook() throws Exception {
        var input = """
                = Document Title

                [abstract]
                == Abstract

                This is the abstract.

                == Fin
                """;
        var output = convertString(input, Options.of("backend", "docbook"));
        assertXpath("//abstract", output, 1);
        assertXpath("/article/info/abstract", output, 1);
    }

    @Test
    @DisplayName("should move abstract in preface section to info tag when converting to DocBook")
    void shouldMoveAbstractInPrefaceSectionToInfoTagWhenConvertingToDocBook() throws Exception {
        var input = """
                = Document Title
                :doctype: book

                [preface]
                == Preface

                [abstract]
                This is the abstract.

                == Fin
                """;
        var output = convertString(input, Options.of("backend", "docbook"));
        assertXpath("//abstract", output, 1);
        assertXpath("/book/info/abstract", output, 1);
        assertXpath("//preface", output, 0);
    }
}
