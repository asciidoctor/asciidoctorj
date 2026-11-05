package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

@DisplayName("Links")
public class LinksTest extends TestHelper {

    @Test
    @DisplayName("qualified url inline with text")
    void qualifiedUrlInlineWithText() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][@class=\"bare\"][text() = \"http://asciidoc.org\"]", convertString("The AsciiDoc project is located at http://asciidoc.org."));
    }

    @Test
    @DisplayName("qualified url with role inline with text")
    void qualifiedUrlWithRoleInlineWithText() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][@class=\"bare project\"][text() = \"http://asciidoc.org\"]", convertString("The AsciiDoc project is located at http://asciidoc.org[role=project]."));
    }

    @Test
    @DisplayName("qualified http url inline with hide-uri-scheme set")
    void qualifiedHttpUrlInlineWithHideUriSchemeSet() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][@class=\"bare\"][text() = \"asciidoc.org\"]", convertString("The AsciiDoc project is located at http://asciidoc.org.", Options.of("attributes", Options.of("hide-uri-scheme", ""))));
    }

    @Test
    @DisplayName("qualified file url inline with label")
    void qualifiedFileUrlInlineWithLabel() throws Exception {
        assertXpath("//a[@href=\"file:///home/user/bookmarks.html\"][text() = \"My Bookmarks\"]", convertStringToEmbedded("file:///home/user/bookmarks.html[My Bookmarks]"));
    }

    @Test
    @DisplayName("qualified file url inline with hide-uri-scheme set")
    void qualifiedFileUrlInlineWithHideUriSchemeSet() throws Exception {
        assertXpath("//a[@href=\"file:///etc/app.conf\"][text() = \"/etc/app.conf\"]", convertString("Edit the configuration file link:file:///etc/app.conf[]", Options.of("attributes", Options.of("hide-uri-scheme", ""))));
    }

    @Test
    @DisplayName("should not hide bare URI scheme in implicit text of link macro when hide-uri-scheme is set")
    void shouldNotHideBareURISchemeInImplicitTextOfLinkMacroWhenHideUriSchemeIsSet() throws Exception {
        for (var entry : entries(smap("link:https://[]", "https://", "link:ssh://[]", "ssh://"))) {
            var input = entry.getKey();
            var expected = entry.getValue();
            assertXpath(("/a[text() = \"" + str(expected) + "\"]"), convertInlineString(input, Options.of("attributes", Options.of("hide-uri-scheme", ""))));
        }
    }

    @Test
    @DisplayName("qualified url with label")
    void qualifiedUrlWithLabel() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][text() = \"AsciiDoc\"]", convertString("We're parsing http://asciidoc.org[AsciiDoc] markup"));
    }

    @Test
    @DisplayName("qualified url with label containing escaped right square bracket")
    void qualifiedUrlWithLabelContainingEscapedRightSquareBracket() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][text() = \"[Ascii]Doc\"]", convertString("We're parsing http://asciidoc.org[[Ascii\\]Doc] markup"));
    }

    @Test
    @DisplayName("qualified url with backslash label")
    void qualifiedUrlWithBackslashLabel() throws Exception {
        assertXpath("//a[@href=\"https://google.com\"][text() = \"Google for \\\"]", convertString("I advise you to https://google.com[Google for +\\+]"));
    }

    @Test
    @DisplayName("qualified url with label using link macro")
    void qualifiedUrlWithLabelUsingLinkMacro() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][text() = \"AsciiDoc\"]", convertString("We're parsing link:http://asciidoc.org[AsciiDoc] markup"));
    }

    @Test
    @DisplayName("qualified url with role using link macro")
    void qualifiedUrlWithRoleUsingLinkMacro() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][@class=\"bare project\"][text() = \"http://asciidoc.org\"]", convertString("We're parsing link:http://asciidoc.org[role=project] markup"));
    }

    @Test
    @DisplayName("qualified url using macro syntax with multi-line label inline with text")
    void qualifiedUrlUsingMacroSyntaxWithMultiLineLabelInlineWithText() throws Exception {
        assertXpath("//a[@href='http://asciidoc.org'][text() = 'AsciiDoc\nmarkup']", convertString("We're parsing link:http://asciidoc.org[AsciiDoc\nmarkup]"));
    }

    @Test
    @DisplayName("qualified url with label containing square brackets using link macro")
    void qualifiedUrlWithLabelContainingSquareBracketsUsingLinkMacro() throws Exception {
        var str = "http://example.com[[bracket1\\]]";
        var doc = documentFromString(str, Options.of("standalone", false, "doctype", "inline"));
        assertMatch("<a href=\"http://example.com\">[bracket1]</a>", doc.convert(), 1);
        doc = documentFromString(str, Options.of("standalone", false, "backend", "docbook", "doctype", "inline"));
        assertMatch("<link xl:href=\"http://example.com\">[bracket1]</link>", doc.convert(), 1);
    }

    @Test
    @DisplayName("link macro with empty target")
    void linkMacroWithEmptyTarget() throws Exception {
        var input = "Link to link:[this page].";
        var output = convertStringToEmbedded(input);
        assertXpath("//a", output, 1);
        assertXpath("//a[@href=\"\"]", output, 1);
    }

    @Test
    @DisplayName("should not recognize link macro with double colons")
    void shouldNotRecognizeLinkMacroWithDoubleColons() throws Exception {
        var input = "The link::http://example.org[example domain] is reserved for tests and documentation.";
        var output = convertStringToEmbedded(input);
        assertIncludes(output, "link::http://example.org[example domain]");
    }

    @Test
    @DisplayName("qualified url surrounded by angled brackets")
    void qualifiedUrlSurroundedByAngledBrackets() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][@class=\"bare\"][text()=\"http://asciidoc.org\"]", convertString("<http://asciidoc.org> is the project page for AsciiDoc."), 1);
    }

    @Test
    @DisplayName("qualified url surrounded by double angled brackets should preserve outer angled brackets")
    void qualifiedUrlSurroundedByDoubleAngledBracketsShouldPreserveOuterAngledBrackets() throws Exception {
        assertIncludes(convertStringToEmbedded("<<https://asciidoc.org>>"), "&lt;<a href=\"https://asciidoc.org\" class=\"bare\">https://asciidoc.org</a>&gt;");
    }

    @Test
    @DisplayName("qualified url macro inside angled brackets")
    void qualifiedUrlMacroInsideAngledBrackets() throws Exception {
        assertIncludes(convertStringToEmbedded("<https://asciidoc.org[]>"), "&lt;<a href=\"https://asciidoc.org\" class=\"bare\">https://asciidoc.org</a>&gt;");
    }

    @Test
    @DisplayName("qualified url surrounded by angled brackets in unconstrained context")
    void qualifiedUrlSurroundedByAngledBracketsInUnconstrainedContext() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][@class=\"bare\"][text()=\"http://asciidoc.org\"]", convertString("URLは<http://asciidoc.org>。fin"), 1);
    }

    @Test
    @DisplayName("multiple qualified urls surrounded by angled brackets in unconstrained context")
    void multipleQualifiedUrlsSurroundedByAngledBracketsInUnconstrainedContext() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][@class=\"bare\"][text()=\"http://asciidoc.org\"]", convertString("URLは<http://asciidoc.org>。URLは<http://asciidoc.org>。"), 2);
    }

    @Test
    @DisplayName("qualified url surrounded by escaped angled brackets should escape form")
    void qualifiedUrlSurroundedByEscapedAngledBracketsShouldEscapeForm() throws Exception {
        assertXpath("//p[text()=\"<http://asciidoc.org>\"]", convertString("\\<http://asciidoc.org>"), 1);
    }

    @Test
    @DisplayName("escaped qualified url surrounded by angled brackets should escape autolink")
    void escapedQualifiedUrlSurroundedByAngledBracketsShouldEscapeAutolink() throws Exception {
        assertXpath("//p[text()=\"<http://asciidoc.org>\"]", convertString("<\\http://asciidoc.org>"), 1);
    }

    @Test
    @DisplayName("xref shorthand with target that starts with URL protocol and has space after comma should not crash parser")
    void xrefShorthandWithTargetThatStartsWithURLProtocolAndHasSpaceAfterCommaShouldNotCrashParser() throws Exception {
        var output = convertStringToEmbedded("<<https://example.com, Example>>");
        assertInclude("<a href=\"#https://example.com\">Example</a>", output);
    }

    @Test
    @DisplayName("xref shorthand with link macro as target should be ignored")
    void xrefShorthandWithLinkMacroAsTargetShouldBeIgnored() throws Exception {
        var output = convertStringToEmbedded("<<link:https://example.com[], Example>>");
        assertInclude("&lt;&lt;<a href=\"https://example.com\" class=\"bare\">https://example.com</a>, Example&gt;&gt;", output);
    }

    @Test
    @DisplayName("autolink containing text enclosed in angle brackets")
    void autolinkContainingTextEnclosedInAngleBrackets() throws Exception {
        var output = convertStringToEmbedded("https://github.com/<org>/");
        assertInclude("<a href=\"https://github.com/&lt;org&gt;/\" class=\"bare\">https://github.com/&lt;org&gt;/</a>", output);
    }

    @Test
    @DisplayName("qualified url surrounded by round brackets")
    void qualifiedUrlSurroundedByRoundBrackets() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][text()=\"http://asciidoc.org\"]", convertString("(http://asciidoc.org) is the project page for AsciiDoc."), 1);
    }

    @Test
    @DisplayName("qualified url with trailing period")
    void qualifiedUrlWithTrailingPeriod() throws Exception {
        var result = convertStringToEmbedded("The homepage for Asciidoctor is https://asciidoctor.org.");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(.,\".\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url with trailing explanation point")
    void qualifiedUrlWithTrailingExplanationPoint() throws Exception {
        var result = convertStringToEmbedded("Check out https://asciidoctor.org!");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(.,\"!\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url with trailing question mark")
    void qualifiedUrlWithTrailingQuestionMark() throws Exception {
        var result = convertStringToEmbedded("Is the homepage for Asciidoctor https://asciidoctor.org?");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(.,\"?\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url with trailing round bracket")
    void qualifiedUrlWithTrailingRoundBracket() throws Exception {
        var result = convertStringToEmbedded("Asciidoctor is a Ruby-based AsciiDoc processor (see https://asciidoctor.org)");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(.,\")\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url with trailing period followed by round bracket")
    void qualifiedUrlWithTrailingPeriodFollowedByRoundBracket() throws Exception {
        var result = convertStringToEmbedded("(The homepage for Asciidoctor is https://asciidoctor.org.)");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(.,\".)\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url with trailing exclamation point followed by round bracket")
    void qualifiedUrlWithTrailingExclamationPointFollowedByRoundBracket() throws Exception {
        var result = convertStringToEmbedded("(Check out https://asciidoctor.org!)");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(.,\"!)\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url with trailing question mark followed by round bracket")
    void qualifiedUrlWithTrailingQuestionMarkFollowedByRoundBracket() throws Exception {
        var result = convertStringToEmbedded("(Is the homepage for Asciidoctor https://asciidoctor.org?)");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(.,\"?)\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url with trailing semi-colon")
    void qualifiedUrlWithTrailingSemiColon() throws Exception {
        var result = convertStringToEmbedded("https://asciidoctor.org; where text gets parsed");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(.,\";\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url with trailing colon")
    void qualifiedUrlWithTrailingColon() throws Exception {
        var result = convertStringToEmbedded("https://asciidoctor.org: where text gets parsed");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(.,\":\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url in round brackets with trailing colon")
    void qualifiedUrlInRoundBracketsWithTrailingColon() throws Exception {
        var result = convertStringToEmbedded("(https://asciidoctor.org): where text gets parsed");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(.,\"):\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url with trailing round bracket followed by colon")
    void qualifiedUrlWithTrailingRoundBracketFollowedByColon() throws Exception {
        var result = convertStringToEmbedded("(from https://asciidoctor.org): where text gets parsed");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(., \"):\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url in round brackets with trailing semi-colon")
    void qualifiedUrlInRoundBracketsWithTrailingSemiColon() throws Exception {
        var result = convertStringToEmbedded("(https://asciidoctor.org); where text gets parsed");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(., \");\")]", result, 1);
    }

    @Test
    @DisplayName("qualified url with trailing round bracket followed by semi-colon")
    void qualifiedUrlWithTrailingRoundBracketFollowedBySemiColon() throws Exception {
        var result = convertStringToEmbedded("(from https://asciidoctor.org); where text gets parsed");
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]", result, 1);
        assertXpath("//a[@href=\"https://asciidoctor.org\"][text()=\"https://asciidoctor.org\"]/following-sibling::text()[starts-with(., \");\")]", result, 1);
    }

    @Test
    @DisplayName("URI scheme with trailing characters should not be converted to a link")
    void uriSchemeWithTrailingCharactersShouldNotBeConvertedToALink() throws Exception {
        var comma = ",";
        var inputSources = list("http://;", "file://:", ("irc://" + str(comma)));
        var expectedOutputs = list("http://;", "file://:", ("irc://" + str(comma)));
        var inputSourceList = inputSources;
        for (int i = 0; i < inputSourceList.size(); i++) {
            var inputSource = inputSourceList.get(i);
            var expectedOutput = get(expectedOutputs, i);
            var actual = blockFromString(inputSource);
            assertEquals(expectedOutput, actual.getContent());
        }
    }

    @Test
    @DisplayName("bare URI scheme enclosed in brackets should not be converted to link")
    void bareURISchemeEnclosedInBracketsShouldNotBeConvertedToLink() throws Exception {
        var inputSources = list("(https://)", "<ftp://>");
        var expectedOutputs = list("(https://)", "&lt;ftp://&gt;");
        var inputSourceList = inputSources;
        for (int i = 0; i < inputSourceList.size(); i++) {
            var inputSource = inputSourceList.get(i);
            var expectedOutput = get(expectedOutputs, i);
            var actual = blockFromString(inputSource);
            assertEquals(expectedOutput, actual.getContent());
        }
    }

    @Test
    @DisplayName("qualified url containing round brackets")
    void qualifiedUrlContainingRoundBrackets() throws Exception {
        assertXpath("//a[@href=\"http://jruby.org/apidocs/org/jruby/Ruby.html#addModule(org.jruby.RubyModule)\"][text()=\"addModule() adds a Ruby module\"]", convertString("http://jruby.org/apidocs/org/jruby/Ruby.html#addModule(org.jruby.RubyModule)[addModule() adds a Ruby module]"), 1);
    }

    @Test
    @DisplayName("qualified url adjacent to text in square brackets")
    void qualifiedUrlAdjacentToTextInSquareBrackets() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][text()=\"AsciiDoc\"]", convertString("]http://asciidoc.org[AsciiDoc] project page."), 1);
    }

    @Test
    @DisplayName("qualified url adjacent to text in round brackets")
    void qualifiedUrlAdjacentToTextInRoundBrackets() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][text()=\"AsciiDoc\"]", convertString(")http://asciidoc.org[AsciiDoc] project page."), 1);
    }

    @Test
    @DisplayName("qualified url following no-break space")
    void qualifiedUrlFollowingNoBreakSpace() throws Exception {
        assertXpath("//a[@href=\"http://asciidoc.org\"][text()=\"AsciiDoc\"]", convertString(("\u00a0" + "http://asciidoc.org[AsciiDoc] project page.")), 1);
    }

    @Test
    @DisplayName("qualified url following smart apostrophe")
    void qualifiedUrlFollowingSmartApostrophe() throws Exception {
        var output = convertStringToEmbedded("l&#8217;http://www.irit.fr[IRIT]");
        assertMatch(rx("l&#8217;<a href="), output);
    }

    @Test
    @DisplayName("should convert qualified url as macro enclosed in double quotes")
    void shouldConvertQualifiedUrlAsMacroEnclosedInDoubleQuotes() throws Exception {
        var output = convertStringToEmbedded("\"https://asciidoctor.org[]\"");
        assertInclude("\"<a href=\"https://asciidoctor.org\" class=\"bare\">https://asciidoctor.org</a>\"", output);
    }

    @Test
    @DisplayName("should convert qualified url as macro enclosed in single quotes")
    void shouldConvertQualifiedUrlAsMacroEnclosedInSingleQuotes() throws Exception {
        var output = convertStringToEmbedded("'https://asciidoctor.org[]'");
        assertInclude("'<a href=\"https://asciidoctor.org\" class=\"bare\">https://asciidoctor.org</a>'", output);
    }

    @Test
    @DisplayName("should convert qualified url as macro with trailing period")
    void shouldConvertQualifiedUrlAsMacroWithTrailingPeriod() throws Exception {
        var result = convertStringToEmbedded("Information about the https://symbols.example.org/.[.] character.");
        assertXpath("//a[@href=\"https://symbols.example.org/.\"][text()=\".\"]", result, 1);
    }

    @Test
    @DisplayName("qualified url using invalid link macro should not create link")
    void qualifiedUrlUsingInvalidLinkMacroShouldNotCreateLink() throws Exception {
        assertXpath("//a", convertString("link:http://asciidoc.org is the project page for AsciiDoc."), 0);
    }

    @Test
    @DisplayName("escaped inline qualified url should not create link")
    void escapedInlineQualifiedUrlShouldNotCreateLink() throws Exception {
        assertXpath("//a", convertString("\\http://asciidoc.org is the project page for AsciiDoc."), 0);
    }

    @Test
    @DisplayName("escaped inline qualified url as macro should not create link")
    void escapedInlineQualifiedUrlAsMacroShouldNotCreateLink() throws Exception {
        var output = convertString("\\http://asciidoc.org[asciidoc.org] is the project page for AsciiDoc.");
        assertXpath("//a", output, 0);
        assertXpath("//p[starts-with(text(), \"http://asciidoc.org[asciidoc.org]\")]", output, 1);
    }

    @Test
    @DisplayName("url in link macro with at (@) sign should not create mailto link")
    void urlInLinkMacroWithAtSignShouldNotCreateMailtoLink() throws Exception {
        assertXpath("//a[@href=\"http://xircles.codehaus.org/lists/dev@geb.codehaus.org\"][text()=\"subscribe\"]", convertString("http://xircles.codehaus.org/lists/dev@geb.codehaus.org[subscribe]"));
    }

    @Test
    @DisplayName("implicit url with at (@) sign should not create mailto link")
    void implicitUrlWithAtSignShouldNotCreateMailtoLink() throws Exception {
        assertXpath("//a[@href=\"http://xircles.codehaus.org/lists/dev@geb.codehaus.org\"][text()=\"http://xircles.codehaus.org/lists/dev@geb.codehaus.org\"]", convertString("http://xircles.codehaus.org/lists/dev@geb.codehaus.org"));
    }

    @Test
    @DisplayName("escaped inline qualified url using macro syntax should not create link")
    void escapedInlineQualifiedUrlUsingMacroSyntaxShouldNotCreateLink() throws Exception {
        assertXpath("//a", convertString("\\http://asciidoc.org[AsciiDoc] is the key to good docs."), 0);
    }

    @Test
    @DisplayName("inline qualified url followed by a newline should not include newline in link")
    void inlineQualifiedUrlFollowedByANewlineShouldNotIncludeNewlineInLink() throws Exception {
        assertXpath("//a[@href=\"https://github.com/asciidoctor\"]", convertString("The source code for Asciidoctor can be found at https://github.com/asciidoctor\nwhich is a GitHub organization."), 1);
    }

    @Test
    @DisplayName("qualified url divided by newline using macro syntax should not create link")
    void qualifiedUrlDividedByNewlineUsingMacroSyntaxShouldNotCreateLink() throws Exception {
        assertXpath("//a", convertString("The source code for Asciidoctor can be found at link:https://github.com/asciidoctor\n[]which is a GitHub organization."), 0);
    }

    @Test
    @DisplayName("qualified url containing whitespace using macro syntax should not create link")
    void qualifiedUrlContainingWhitespaceUsingMacroSyntaxShouldNotCreateLink() throws Exception {
        assertXpath("//a", convertString("I often need to refer to the chapter on link:http://asciidoc.org?q=attribute references[Attribute References]."), 0);
    }

    @Test
    @DisplayName("qualified url containing an encoded space using macro syntax should create a link")
    void qualifiedUrlContainingAnEncodedSpaceUsingMacroSyntaxShouldCreateALink() throws Exception {
        assertXpath("//a", convertString("I often need to refer to the chapter on link:http://asciidoc.org?q=attribute%20references[Attribute References]."), 1);
    }

    @Test
    @DisplayName("inline quoted qualified url should not consume surrounding angled brackets")
    void inlineQuotedQualifiedUrlShouldNotConsumeSurroundingAngledBrackets() throws Exception {
        assertXpath("//a[@href=\"https://github.com/asciidoctor\"]", convertString("Asciidoctor GitHub organization: <**https://github.com/asciidoctor**>"), 1);
    }

    @Test
    @DisplayName("link with quoted text should not be separated into attributes when text contains an equal sign")
    void linkWithQuotedTextShouldNotBeSeparatedIntoAttributesWhenTextContainsAnEqualSign() throws Exception {
        assertXpath("//a[@href=\"http://search.example.com\"][text()=\"Google, Yahoo, Bing = Search Engines\"]", convertStringToEmbedded("http://search.example.com[\"Google, Yahoo, Bing = Search Engines\"]"), 1);
    }

    @Test
    @DisplayName("should leave link text as is if it contains an equals sign but no attributes are found")
    void shouldLeaveLinkTextAsIsIfItContainsAnEqualsSignButNoAttributesAreFound() throws Exception {
        assertXpath("//a[@href=\"https://example.com\"][text()=\"What You Need\n= What You Get\"]", convertStringToEmbedded("https://example.com[What You Need\n= What You Get]"), 1);
    }

    @Test
    @DisplayName("link with quoted text but no equal sign should carry quotes over to output")
    void linkWithQuotedTextButNoEqualSignShouldCarryQuotesOverToOutput() throws Exception {
        assertXpath("//a[@href=\"http://search.example.com\"][text()='\"Google, Yahoo, Bing\"']", convertStringToEmbedded("http://search.example.com[\"Google, Yahoo, Bing\"]"), 1);
    }

    @Test
    @DisplayName("link with comma in text but no equal sign should not be separated into attributes")
    void linkWithCommaInTextButNoEqualSignShouldNotBeSeparatedIntoAttributes() throws Exception {
        assertXpath("//a[@href=\"http://search.example.com\"][text()=\"Google, Yahoo, Bing\"]", convertStringToEmbedded("http://search.example.com[Google, Yahoo, Bing]"), 1);
    }

    @Test
    @DisplayName("link with formatted wrapped text should not be separated into attributes")
    void linkWithFormattedWrappedTextShouldNotBeSeparatedIntoAttributes() throws Exception {
        var result = convertStringToEmbedded("https://example.com[[.role]#Foo\nBar#]");
        assertInclude("<a href=\"https://example.com\"><span class=\"role\">Foo\nBar</span></a>", result);
    }

    @Test
    @DisplayName("should process role and window attributes on link")
    void shouldProcessRoleAndWindowAttributesOnLink() throws Exception {
        assertXpath("//a[@href=\"http://google.com\"][@class=\"external\"][@target=\"_blank\"]", convertStringToEmbedded("http://google.com[Google, role=external, window=\"_blank\"]"), 1);
    }

    @Test
    @DisplayName("should parse link with wrapped text that includes attributes")
    void shouldParseLinkWithWrappedTextThatIncludesAttributes() throws Exception {
        var result = convertStringToEmbedded("https://example.com[Foo\nBar,role=foobar]");
        assertInclude("<a href=\"https://example.com\" class=\"foobar\">Foo Bar</a>", result);
    }

    @Test
    @DisplayName("link macro with attributes but no text should use URL as text")
    void linkMacroWithAttributesButNoTextShouldUseURLAsText() throws Exception {
        var url = "https://fonts.googleapis.com/css?family=Roboto:400,400italic,";
        assertXpath(("//a[@href=\"" + str(url) + "\"][text()=\"" + str(url) + "\"]"), convertStringToEmbedded(("link:" + str(url) + "[family=Roboto,weight=400]")), 1);
    }

    @Test
    @DisplayName("link macro with attributes but blank text should use URL as text")
    void linkMacroWithAttributesButBlankTextShouldUseURLAsText() throws Exception {
        var url = "https://fonts.googleapis.com/css?family=Roboto:400,400italic,";
        assertXpath(("//a[@href=\"" + str(url) + "\"][text()=\"" + str(url) + "\"]"), convertStringToEmbedded(("link:" + str(url) + "[,family=Roboto,weight=400]")), 1);
    }

    @Test
    @DisplayName("link macro with comma but no explicit attributes in text should not parse text")
    void linkMacroWithCommaButNoExplicitAttributesInTextShouldNotParseText() throws Exception {
        var url = "https://fonts.googleapis.com/css?family=Roboto:400,400italic,";
        assertXpath(("//a[@href=\"" + str(url) + "\"][text()=\"Roboto,400\"]"), convertStringToEmbedded(("link:" + str(url) + "[Roboto,400]")), 1);
    }

    @Test
    @DisplayName("link macro should support id and role attributes")
    void linkMacroShouldSupportIdAndRoleAttributes() throws Exception {
        var url = "https://fonts.googleapis.com/css?family=Roboto:400";
        assertXpath(("//a[@href=\"" + str(url) + "\"][@id=\"roboto-regular\"][@class=\"bare font\"][text()=\"" + str(url) + "\"]"), convertStringToEmbedded(("link:" + str(url) + "[,id=roboto-regular,role=font]")), 1);
    }

    @Test
    @DisplayName("link text that ends in ^ should set link window to _blank")
    void linkTextThatEndsInShouldSetLinkWindowTo_blank() throws Exception {
        assertXpath("//a[@href=\"http://google.com\"][@target=\"_blank\"]", convertStringToEmbedded("http://google.com[Google^]"), 1);
    }

    @Test
    @DisplayName("rel=noopener should be added to a link that targets the _blank window")
    void relNoopenerShouldBeAddedToALinkThatTargetsThe_blankWindow() throws Exception {
        assertXpath("//a[@href=\"http://google.com\"][@target=\"_blank\"][@rel=\"noopener\"]", convertStringToEmbedded("http://google.com[Google^]"), 1);
    }

    @Test
    @DisplayName("rel=noopener should be added to a link that targets a named window when the noopener option is set")
    void relNoopenerShouldBeAddedToALinkThatTargetsANamedWindowWhenTheNoopenerOptionIsSet() throws Exception {
        assertXpath("//a[@href=\"http://google.com\"][@target=\"name\"][@rel=\"noopener\"]", convertStringToEmbedded("http://google.com[Google,window=name,opts=noopener]"), 1);
    }

    @Test
    @DisplayName("rel=noopener should not be added to a link if it does not target a window")
    void relNoopenerShouldNotBeAddedToALinkIfItDoesNotTargetAWindow() throws Exception {
        var result = convertStringToEmbedded("http://google.com[Google,opts=noopener]");
        assertXpath("//a[@href=\"http://google.com\"]", result, 1);
        assertXpath("//a[@href=\"http://google.com\"][@rel=\"noopener\"]", result, 0);
    }

    @Test
    @DisplayName("rel=nofollow should be added to a link when the nofollow option is set")
    void relNofollowShouldBeAddedToALinkWhenTheNofollowOptionIsSet() throws Exception {
        assertXpath("//a[@href=\"http://google.com\"][@target=\"name\"][@rel=\"nofollow noopener\"]", convertStringToEmbedded("http://google.com[Google,window=name,opts=\"nofollow,noopener\"]"), 1);
    }

    @Test
    @DisplayName("id attribute on link is processed")
    void idAttributeOnLinkIsProcessed() throws Exception {
        assertXpath("//a[@href=\"http://google.com\"][@id=\"link-1\"]", convertStringToEmbedded("http://google.com[Google, id=\"link-1\"]"), 1);
    }

    @Test
    @DisplayName("title attribute on link is processed")
    void titleAttributeOnLinkIsProcessed() throws Exception {
        assertXpath("//a[@href=\"http://google.com\"][@title=\"title-1\"]", convertStringToEmbedded("http://google.com[Google, title=\"title-1\"]"), 1);
    }

    @Test
    @DisplayName("inline irc link")
    void inlineIrcLink() throws Exception {
        assertXpath("//a[@href=\"irc://irc.freenode.net\"][text()=\"irc://irc.freenode.net\"]", convertStringToEmbedded("irc://irc.freenode.net"), 1);
    }

    @Test
    @DisplayName("inline irc link with text")
    void inlineIrcLinkWithText() throws Exception {
        assertXpath("//a[@href=\"irc://irc.freenode.net\"][text()=\"Freenode IRC\"]", convertStringToEmbedded("irc://irc.freenode.net[Freenode IRC]"), 1);
    }

    @Test
    @DisplayName("inline ref")
    void inlineRef() throws Exception {
        var variations = list("[[tigers]]", "anchor:tigers[]");
        for (var anchor : variations) {
            var doc = documentFromString(("Here you can read about tigers." + str(anchor)));
            var output = doc.convert();
            assertInstanceOf(Inline.class, get(doc.getCatalog().getRefs(), "tigers"));
            assertNull(((Inline) get(doc.getCatalog().getRefs(), "tigers")).getText());
            assertXpath("//a[@id=\"tigers\"]", output, 1);
            assertXpath("//a[@id=\"tigers\"]/child::text()", output, 0);
        }
    }

    @Test
    @DisplayName("escaped inline ref")
    void escapedInlineRef() throws Exception {
        var variations = list("[[tigers]]", "anchor:tigers[]");
        for (var anchor : variations) {
            var doc = documentFromString(("Here you can read about tigers.\\" + str(anchor)));
            var output = doc.convert();
            assertFalsy(doc.getCatalog().getRefs().containsKey("tigers"));
            assertXpath("//a[@id=\"tigers\"]", output, 0);
        }
    }

    @Test
    @DisplayName("inline ref can start with colon")
    void inlineRefCanStartWithColon() throws Exception {
        var input = "[[:idname]] text";
        var output = convertStringToEmbedded(input);
        assertXpath("//a[@id=\":idname\"]", output, 1);
    }

    @Test
    @DisplayName("inline ref cannot start with digit")
    void inlineRefCannotStartWithDigit() throws Exception {
        var input = "[[1-install]] text";
        var output = convertStringToEmbedded(input);
        assertIncludes(output, "[[1-install]]");
        assertXpath("//a[@id = \"1-install\"]", output, 0);
    }

    @Test
    @DisplayName("reftext of shorthand inline ref cannot resolve to empty")
    void reftextOfShorthandInlineRefCannotResolveToEmpty() throws Exception {
        var input = "[[no-such-id,{empty}]]text";
        var doc = documentFromString(input);
        assertEmpty(doc.getCatalog().getRefs());
        var output = doc.convert(Options.of("standalone", false));
        assertIncludes(output, sub(input, "{empty}", ""));
    }

    @Test
    @DisplayName("reftext of macro inline ref can resolve to empty")
    void reftextOfMacroInlineRefCanResolveToEmpty() throws Exception {
        var input = "anchor:id-only[{empty}]text\\n\\nsee <<id-only>>";
        var doc = documentFromString(input);
        assertTruthy(doc.getCatalog().getRefs().containsKey("id-only"));
        var output = doc.convert(Options.of("standalone", false));
        assertXpath("//a[@id=\"id-only\"]", output, 1);
        assertXpath("//a[@href=\"#id-only\"]", output, 1);
        assertXpath("//a[@href=\"#id-only\"][text()=\"[id-only]\"]", output, 1);
    }

    @Test
    @DisplayName("inline ref with reftext")
    void inlineRefWithReftext() throws Exception {
        for (var anchor : list("[[tigers,Tigers]]", "anchor:tigers[Tigers]")) {
            var doc = documentFromString(("Here you can read about tigers." + str(anchor)));
            var output = doc.convert();
            assertInstanceOf(Inline.class, get(doc.getCatalog().getRefs(), "tigers"));
            assertEquals("Tigers", ((Inline) get(doc.getCatalog().getRefs(), "tigers")).getText());
            assertXpath("//a[@id=\"tigers\"]", output, 1);
            assertXpath("//a[@id=\"tigers\"]/child::text()", output, 0);
        }
    }

    @Test
    @DisplayName("should encode double quotes in reftext of anchor macro in DocBook output")
    void shouldEncodeDoubleQuotesInReftextOfAnchorMacroInDocBookOutput() throws Exception {
        var input = "anchor:uncola[the \"un\"-cola]";
        var result = convertInlineString(input, Options.of("backend", "docbook"));
        assertEquals("<anchor xml:id=\"uncola\" xreflabel=\"the &quot;un&quot;-cola\"/>", result);
    }

    @Test
    @DisplayName("should substitute attribute references in reftext when registering inline ref")
    void shouldSubstituteAttributeReferencesInReftextWhenRegisteringInlineRef() throws Exception {
        for (var anchor : list("[[tigers,{label-tigers}]]", "anchor:tigers[{label-tigers}]")) {
            var doc = documentFromString(("Here you can read about tigers." + str(anchor)), Options.of("attributes", Options.of("label-tigers", "Tigers")));
            doc.convert();
            assertInstanceOf(Inline.class, get(doc.getCatalog().getRefs(), "tigers"));
            assertEquals("Tigers", ((Inline) get(doc.getCatalog().getRefs(), "tigers")).getText());
        }
    }

    @Test
    @DisplayName("inline ref with reftext converted to DocBook")
    void inlineRefWithReftextConvertedToDocBook() throws Exception {
        for (var anchor : list("[[tigers,<Tigers>]]", "anchor:tigers[<Tigers>]")) {
            var doc = documentFromString(("Here you can read about tigers." + str(anchor)), Options.of("backend", "docbook"));
            var output = doc.convert(Options.of("standalone", false));
            assertInstanceOf(Inline.class, get(doc.getCatalog().getRefs(), "tigers"));
            assertEquals("<Tigers>", ((Inline) get(doc.getCatalog().getRefs(), "tigers")).getText());
            assertIncludes(output, "<anchor xml:id=\"tigers\" xreflabel=\"&lt;Tigers&gt;\"/>");
        }
    }

    @Test
    @DisplayName("does not match bibliography anchor in prose when scanning for inline anchor")
    void doesNotMatchBibliographyAnchorInProseWhenScanningForInlineAnchor() throws Exception {
        var doc = documentFromString("Use [[[label]]] to assign a label to a bibliography entry, but not in a paragraph.");
        assertFalsy(doc.getCatalog().getRefs().containsKey("label"));
    }

    @Test
    @DisplayName("repeating inline anchor macro with empty reftext")
    void repeatingInlineAnchorMacroWithEmptyReftext() throws Exception {
        var input = "anchor:one[] anchor:two[] anchor:three[]";
        var result = convertInlineString(input);
        assertEquals("<a id=\"one\"></a> <a id=\"two\"></a> <a id=\"three\"></a>", result);
    }

    @Test
    @DisplayName("mixed inline anchor macro and anchor shorthand with empty reftext")
    void mixedInlineAnchorMacroAndAnchorShorthandWithEmptyReftext() throws Exception {
        var input = "anchor:one[][[two]]anchor:three[][[four]]anchor:five[]";
        var result = convertInlineString(input);
        assertEquals("<a id=\"one\"></a><a id=\"two\"></a><a id=\"three\"></a><a id=\"four\"></a><a id=\"five\"></a>", result);
    }

    @Test
    @DisplayName("assigns xreflabel value for anchor macro without reftext in DocBook output")
    void assignsXreflabelValueForAnchorMacroWithoutReftextInDocBookOutput() throws Exception {
        for (var input : list("anchor:foo[]bar", "[[foo]]bar")) {
            var result = convertInlineString(input, Options.of("backend", "docbook"));
            assertEquals("<anchor xml:id=\"foo\" xreflabel=\"[foo]\"/>bar", result);
        }
    }

    @Test
    @DisplayName("should remove trailing space on reftext of inline anchor shorthand")
    void shouldRemoveTrailingSpaceOnReftextOfInlineAnchorShorthand() throws Exception {
        var input = """
                see <<foo>>

                [[foo,[FOO] ]]foo
                """;
        var result = convertStringToEmbedded(input);
        assertIncludes(result, "see <a href=\"#foo\">[FOO]</a>");
    }

    @Test
    @DisplayName("should remove trailing space on reftext of inline anchor shorthand when converting to DocBook")
    void shouldRemoveTrailingSpaceOnReftextOfInlineAnchorShorthandWhenConvertingToDocBook() throws Exception {
        var input = """
                see <<foo>>

                [[foo,[FOO] ]]foo
                """;
        var result = convertStringToEmbedded(input, Options.of("backend", "docbook"));
        assertIncludes(result, " xreflabel=\"[FOO]\"");
    }

    @Test
    @DisplayName("unescapes square bracket in reftext of anchor macro")
    void unescapesSquareBracketInReftextOfAnchorMacro() throws Exception {
        var input = """
                see <<foo>>

                anchor:foo[b[a\\]r]tex
                """;
        var result = convertStringToEmbedded(input);
        assertIncludes(result, "see <a href=\"#foo\">b[a]r</a>");
    }

    @Test
    @DisplayName("unescapes square bracket in reftext of anchor macro in DocBook output")
    void unescapesSquareBracketInReftextOfAnchorMacroInDocBookOutput() throws Exception {
        var input = "anchor:foo[b[a\\]r]";
        var result = convertInlineString(input, Options.of("backend", "docbook"));
        assertEquals("<anchor xml:id=\"foo\" xreflabel=\"b[a]r\"/>", result);
    }

    @Test
    @DisplayName("xref using angled bracket syntax")
    void xrefUsingAngledBracketSyntax() throws Exception {
        var doc = documentFromString("<<tigers>>");
        doc.register("refs", list("tigers", new Inline(doc, "anchor", "[tigers]", Options.of("type", "ref", "target", "tigers")), "[tigers]"));
        assertXpath("//a[@href=\"#tigers\"][text() = \"[tigers]\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with explicit hash")
    void xrefUsingAngledBracketSyntaxWithExplicitHash() throws Exception {
        var doc = documentFromString("<<#tigers>>");
        doc.register("refs", list("tigers", new Inline(doc, "anchor", "Tigers", Options.of("type", "ref", "target", "tigers")), "Tigers"));
        assertXpath("//a[@href=\"#tigers\"][text() = \"Tigers\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with label")
    void xrefUsingAngledBracketSyntaxWithLabel() throws Exception {
        var input = """
                <<tigers,About Tigers>>

                [#tigers]
                == Tigers
                """;
        assertXpath("//a[@href=\"#tigers\"][text() = \"About Tigers\"]", convertString(input), 1);
    }

    @Test
    @DisplayName("xref should use title of target as link text when no explicit reftext is specified")
    void xrefShouldUseTitleOfTargetAsLinkTextWhenNoExplicitReftextIsSpecified() throws Exception {
        var input = """
                <<tigers>>

                [#tigers]
                == Tigers
                """;
        assertXpath("//a[@href=\"#tigers\"][text() = \"Tigers\"]", convertString(input), 1);
    }

    @Test
    @DisplayName("xref should use title of target as link text when explicit link text is empty")
    void xrefShouldUseTitleOfTargetAsLinkTextWhenExplicitLinkTextIsEmpty() throws Exception {
        var input = """
                <<tigers,>>

                [#tigers]
                == Tigers
                """;
        assertXpath("//a[@href=\"#tigers\"][text() = \"Tigers\"]", convertString(input), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with quoted label")
    void xrefUsingAngledBracketSyntaxWithQuotedLabel() throws Exception {
        var input = """
                <<tigers,"About Tigers">>

                [#tigers]
                == Tigers
                """;
        assertXpath("//a[@href=\"#tigers\"][text() = '\"About Tigers\"']", convertString(input), 1);
    }

    @Test
    @DisplayName("should not interpret path sans extension in xref with angled bracket syntax in compat mode")
    void shouldNotInterpretPathSansExtensionInXrefWithAngledBracketSyntaxInCompatMode() throws Exception {
        try (var logger = usingMemoryLogger()) {
            var doc = documentFromString("<<tigers#>>", Options.of("standalone", false, "attributes", Options.of("compat-mode", "")));
            assertXpath("//a[@href=\"#tigers#\"][text() = \"[tigers#]\"]", doc.convert(), 1);
            assertEmpty(logger);
        }
    }

    @Test
    @DisplayName("xref using angled bracket syntax with path sans extension")
    void xrefUsingAngledBracketSyntaxWithPathSansExtension() throws Exception {
        var doc = documentFromString("<<tigers#>>", Options.of("standalone", false));
        assertXpath("//a[@href=\"tigers.html\"][text() = \"tigers.html\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("inter-document xref shorthand syntax should assume AsciiDoc extension if AsciiDoc extension not present")
    void interDocumentXrefShorthandSyntaxShouldAssumeAsciiDocExtensionIfAsciiDocExtensionNotPresent() throws Exception {
        for (var entry : entries(smap("using-.net-web-services#", "Using .NET web services", "asciidoctor.1#", "Asciidoctor Manual", "path/to/document#", "Document Title"))) {
            var target = entry.getKey();
            var text = entry.getValue();
            var result = convertStringToEmbedded(("<<" + str(target) + "," + str(text) + ">>"));
            assertXpath(("//a[@href=\"" + str(Strings.chop(target)) + ".html\"][text()=\"" + str(text) + "\"]"), result, 1);
        }
    }

    @Test
    @DisplayName("xref macro with explicit inter-document target should assume implicit AsciiDoc file extension if no file extension is present")
    void xrefMacroWithExplicitInterDocumentTargetShouldAssumeImplicitAsciiDocFileExtensionIfNoFileExtensionIsPresent() throws Exception {
        for (var entry : entries(smap("using-.net-web-services#", "Using .NET web services", "asciidoctor.1#", "Asciidoctor Manual"))) {
            var target = entry.getKey();
            var text = entry.getValue();
            var result = convertStringToEmbedded(("xref:" + str(target) + "[" + str(text) + "]"));
            assertXpath(("//a[@href=\"" + str(Strings.chop(target)) + "\"][text()=\"" + str(text) + "\"]"), result, 1);
        }
        for (var entry : entries(smap("document#", "Document Title", "path/to/document#", "Document Title", "include.d/document#", "Document Title"))) {
            var target = entry.getKey();
            var text = entry.getValue();
            var result = convertStringToEmbedded(("xref:" + str(target) + "[" + str(text) + "]"));
            assertXpath(("//a[@href=\"" + str(Strings.chop(target)) + ".html\"][text()=\"" + str(text) + "\"]"), result, 1);
        }
    }

    @Test
    @DisplayName("xref macro with implicit inter-document target should preserve path with file extension")
    void xrefMacroWithImplicitInterDocumentTargetShouldPreservePathWithFileExtension() throws Exception {
        for (var entry : entries(smap("refcard.pdf", "Refcard", "asciidoctor.1", "Asciidoctor Manual"))) {
            var path = entry.getKey();
            var text = entry.getValue();
            var result = convertStringToEmbedded(("xref:" + str(path) + "[" + str(text) + "]"));
            assertXpath(("//a[@href=\"" + str(path) + "\"][text()=\"" + str(text) + "\"]"), result, 1);
        }
        for (var entry : entries(smap("sections.d/first", "First Section"))) {
            var path = entry.getKey();
            var text = entry.getValue();
            var result = convertStringToEmbedded(("xref:" + str(path) + "[" + str(text) + "]"));
            assertXpath(("//a[@href=\"#" + str(path) + "\"][text()=\"" + str(text) + "\"]"), result, 1);
        }
    }

    @Test
    @DisplayName("inter-document xref should only remove the file extension part if the path contains a period elsewhere")
    void interDocumentXrefShouldOnlyRemoveTheFileExtensionPartIfThePathContainsAPeriodElsewhere() throws Exception {
        var result = convertStringToEmbedded("<<using-.net-web-services.adoc#,Using .NET web services>>");
        assertXpath("//a[@href=\"using-.net-web-services.html\"][text() = \"Using .NET web services\"]", result, 1);
    }

    @Test
    @DisplayName("xref macro target containing dot should be interpreted as a path unless prefixed by #")
    void xrefMacroTargetContainingDotShouldBeInterpretedAsAPathUnlessPrefixedBy() throws Exception {
        var result = convertStringToEmbedded("xref:using-.net-web-services[Using .NET web services]");
        assertXpath("//a[@href=\"using-.net-web-services\"][text() = \"Using .NET web services\"]", result, 1);
        result = convertStringToEmbedded("xref:#using-.net-web-services[Using .NET web services]");
        assertXpath("//a[@href=\"#using-.net-web-services\"][text() = \"Using .NET web services\"]", result, 1);
    }

    @Test
    @DisplayName("should not interpret double underscore in target of xref macro if sequence is preceded by a backslash")
    void shouldNotInterpretDoubleUnderscoreInTargetOfXrefMacroIfSequenceIsPrecededByABackslash() throws Exception {
        var result = convertStringToEmbedded("xref:doc\\__with_double__underscore.adoc[text]");
        assertXpath("//a[@href=\"doc__with_double__underscore.html\"][text() = \"text\"]", result, 1);
    }

    @Test
    @DisplayName("should not interpret double underscore in target of xref shorthand if sequence is preceded by a backslash")
    void shouldNotInterpretDoubleUnderscoreInTargetOfXrefShorthandIfSequenceIsPrecededByABackslash() throws Exception {
        var result = convertStringToEmbedded("<<doc\\__with_double__underscore.adoc#,text>>");
        assertXpath("//a[@href=\"doc__with_double__underscore.html\"][text() = \"text\"]", result, 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with path sans extension using docbook backend")
    void xrefUsingAngledBracketSyntaxWithPathSansExtensionUsingDocbookBackend() throws Exception {
        var doc = documentFromString("<<tigers#>>", Options.of("standalone", false, "backend", "docbook"));
        assertMatch("<link xl:href=\"tigers.xml\">tigers.xml</link>", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with ancestor path sans extension")
    void xrefUsingAngledBracketSyntaxWithAncestorPathSansExtension() throws Exception {
        var doc = documentFromString("<<../tigers#,tigers>>", Options.of("standalone", false));
        assertXpath("//a[@href=\"../tigers.html\"][text() = \"tigers\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with absolute path sans extension")
    void xrefUsingAngledBracketSyntaxWithAbsolutePathSansExtension() throws Exception {
        var doc = documentFromString("<</path/to/tigers#,tigers>>", Options.of("standalone", false));
        assertXpath("//a[@href=\"/path/to/tigers.html\"][text() = \"tigers\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with path and extension")
    void xrefUsingAngledBracketSyntaxWithPathAndExtension() throws Exception {
        try (var logger = usingMemoryLogger()) {
            var doc = documentFromString("<<tigers.adoc>>", Options.of("standalone", false));
            assertXpath("//a[@href=\"#tigers.adoc\"][text() = \"[tigers.adoc]\"]", doc.convert(), 1);
            assertEmpty(logger);
        }
    }

    @Test
    @DisplayName("xref using angled bracket syntax with path and extension with hash")
    void xrefUsingAngledBracketSyntaxWithPathAndExtensionWithHash() throws Exception {
        var doc = documentFromString("<<tigers.adoc#>>", Options.of("standalone", false));
        assertXpath("//a[@href=\"tigers.html\"][text() = \"tigers.html\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with path and extension with fragment")
    void xrefUsingAngledBracketSyntaxWithPathAndExtensionWithFragment() throws Exception {
        var doc = documentFromString("<<tigers.adoc#id>>", Options.of("standalone", false));
        assertXpath("//a[@href=\"tigers.html#id\"][text() = \"tigers.html\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using macro syntax with path and extension in compat mode")
    void xrefUsingMacroSyntaxWithPathAndExtensionInCompatMode() throws Exception {
        try (var logger = usingMemoryLogger()) {
            var doc = documentFromString("xref:tigers.adoc[]", Options.of("standalone", false, "attributes", Options.of("compat-mode", "")));
            assertXpath("//a[@href=\"#tigers.adoc\"][text() = \"[tigers.adoc]\"]", doc.convert(), 1);
            assertEmpty(logger);
        }
    }

    @Test
    @DisplayName("xref using macro syntax with path and extension")
    void xrefUsingMacroSyntaxWithPathAndExtension() throws Exception {
        var doc = documentFromString("xref:tigers.adoc[]", Options.of("standalone", false));
        assertXpath("//a[@href=\"tigers.html\"][text() = \"tigers.html\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with path and fragment")
    void xrefUsingAngledBracketSyntaxWithPathAndFragment() throws Exception {
        var doc = documentFromString("<<tigers#about>>", Options.of("standalone", false));
        assertXpath("//a[@href=\"tigers.html#about\"][text() = \"tigers.html\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with path, fragment and text")
    void xrefUsingAngledBracketSyntaxWithPathFragmentAndText() throws Exception {
        var doc = documentFromString("<<tigers#about,About Tigers>>", Options.of("standalone", false));
        assertXpath("//a[@href=\"tigers.html#about\"][text() = \"About Tigers\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with path and custom relfilesuffix and outfilesuffix")
    void xrefUsingAngledBracketSyntaxWithPathAndCustomRelfilesuffixAndOutfilesuffix() throws Exception {
        var attributes = Options.of("relfileprefix", "../", "outfilesuffix", "/");
        var doc = documentFromString("<<tigers#about,About Tigers>>", Options.of("standalone", false, "attributes", attributes));
        assertXpath("//a[@href=\"../tigers/#about\"][text() = \"About Tigers\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with path and custom relfilesuffix")
    void xrefUsingAngledBracketSyntaxWithPathAndCustomRelfilesuffix() throws Exception {
        var attributes = Options.of("relfilesuffix", "/");
        var doc = documentFromString("<<tigers#about,About Tigers>>", Options.of("standalone", false, "attributes", attributes));
        assertXpath("//a[@href=\"tigers/#about\"][text() = \"About Tigers\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with path which has been included in this document")
    void xrefUsingAngledBracketSyntaxWithPathWhichHasBeenIncludedInThisDocument() throws Exception {
        try (var logger = usingMemoryLogger()) {
            try (var verbose = inVerboseMode()) {
                var doc = documentFromString("<<tigers#about,About Tigers>>", Options.of("standalone", false));
                doc.getCatalog().getIncludes().put("tigers", true);
                var output = doc.convert();
                assertXpath("//a[@href=\"#about\"][text() = \"About Tigers\"]", output, 1);
                assertMessage(logger, "INFO", "possible invalid reference: about");
            }
        }
    }

    @Test
    @DisplayName("xref using angled bracket syntax with nested path which has been included in this document")
    void xrefUsingAngledBracketSyntaxWithNestedPathWhichHasBeenIncludedInThisDocument() throws Exception {
        try (var logger = usingMemoryLogger()) {
            try (var verbose = inVerboseMode()) {
                var doc = documentFromString("<<part1/tigers#about,About Tigers>>", Options.of("standalone", false));
                doc.getCatalog().getIncludes().put("part1/tigers", true);
                var output = doc.convert();
                assertXpath("//a[@href=\"#about\"][text() = \"About Tigers\"]", output, 1);
                assertMessage(logger, "INFO", "possible invalid reference: about");
            }
        }
    }

    @Test
    @DisplayName("xref using angled bracket syntax inline with text")
    void xrefUsingAngledBracketSyntaxInlineWithText() throws Exception {
        var input = """
                Want to learn <<tigers,about tigers>>?

                [#tigers]
                == Tigers
                """;
        assertXpath("//a[@href=\"#tigers\"][text() = \"about tigers\"]", convertString(input), 1);
    }

    @Test
    @DisplayName("xref using angled bracket syntax with multi-line label inline with text")
    void xrefUsingAngledBracketSyntaxWithMultiLineLabelInlineWithText() throws Exception {
        var input = """
                Want to learn <<tigers,about
                tigers>>?

                [#tigers]
                == Tigers
                """;
        assertXpath("//a[@href=\"#tigers\"][normalize-space(text()) = \"about tigers\"]", convertString(input), 1);
    }

    @Test
    @DisplayName("xref with escaped text")
    void xrefWithEscapedText() throws Exception {
        var input = """
                See the <<tigers, `+[tigers]+`>> section for details about tigers.

                [#tigers]
                == Tigers
                """;
        var output = convertStringToEmbedded(input);
        assertXpath("//a[@href=\"#tigers\"]/code[text()=\"[tigers]\"]", output, 1);
    }

    @Test
    @DisplayName("xref with target that begins with attribute reference in title")
    void xrefWithTargetThatBeginsWithAttributeReferenceInTitle() throws Exception {
        for (var xref : list("<<{lessonsdir}/lesson-1#,Lesson 1>>", "xref:{lessonsdir}/lesson-1.adoc[Lesson 1]")) {
            var input = (":lessonsdir: lessons\n\n[#lesson-1-listing]\n== " + str(xref) + "\n\nA summary of the first lesson.\n");
            var output = convertStringToEmbedded(input);
            assertXpath("//h2/a[@href=\"lessons/lesson-1.html\"]", output, 1);
        }
    }

    @Test
    @DisplayName("xref using macro syntax")
    void xrefUsingMacroSyntax() throws Exception {
        var doc = documentFromString("xref:tigers[]");
        doc.register("refs", list("tigers", new Inline(doc, "anchor", "[tigers]", Options.of("type", "ref", "target", "tigers")), "[tigers]"));
        assertXpath("//a[@href=\"#tigers\"][text() = \"[tigers]\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("multiple xref macros with implicit text in single line")
    void multipleXrefMacrosWithImplicitTextInSingleLine() throws Exception {
        var input = """
                This document has two sections, xref:sect-a[] and xref:sect-b[].

                [#sect-a]
                == Section A

                [#sect-b]
                == Section B
                """;
        var result = convertStringToEmbedded(input);
        assertXpath("//a[@href=\"#sect-a\"][text() = \"Section A\"]", result, 1);
        assertXpath("//a[@href=\"#sect-b\"][text() = \"Section B\"]", result, 1);
    }

    @Test
    @DisplayName("xref using macro syntax with explicit hash")
    void xrefUsingMacroSyntaxWithExplicitHash() throws Exception {
        var doc = documentFromString("xref:#tigers[]");
        doc.register("refs", list("tigers", new Inline(doc, "anchor", "Tigers", Options.of("type", "ref", "target", "tigers")), "Tigers"));
        assertXpath("//a[@href=\"#tigers\"][text() = \"Tigers\"]", doc.convert(), 1);
    }

    @Test
    @DisplayName("xref using macro syntax with label")
    void xrefUsingMacroSyntaxWithLabel() throws Exception {
        var input = """
                xref:tigers[About Tigers]

                [#tigers]
                == Tigers
                """;
        assertXpath("//a[@href=\"#tigers\"][text() = \"About Tigers\"]", convertString(input), 1);
    }

    @Test
    @DisplayName("xref using macro syntax inline with text")
    void xrefUsingMacroSyntaxInlineWithText() throws Exception {
        var input = """
                Want to learn xref:tigers[about tigers]?

                [#tigers]
                == Tigers
                """;
        assertXpath("//a[@href=\"#tigers\"][text() = \"about tigers\"]", convertString(input), 1);
    }

    @Test
    @DisplayName("xref using macro syntax with multi-line label inline with text")
    void xrefUsingMacroSyntaxWithMultiLineLabelInlineWithText() throws Exception {
        var input = """
                Want to learn xref:tigers[about
                tigers]?

                [#tigers]
                == Tigers
                """;
        assertXpath("//a[@href=\"#tigers\"][normalize-space(text()) = \"about tigers\"]", convertString(input), 1);
    }

    @Test
    @DisplayName("xref using macro syntax with text that ends with an escaped closing bracket")
    void xrefUsingMacroSyntaxWithTextThatEndsWithAnEscapedClosingBracket() throws Exception {
        var input = """
                xref:tigers[[tigers\\]]

                [#tigers]
                == Tigers
                """;
        assertXpath("//a[@href=\"#tigers\"][text() = \"[tigers]\"]", convertStringToEmbedded(input), 1);
    }

    @Test
    @DisplayName("xref using macro syntax with text that contains an escaped closing bracket")
    void xrefUsingMacroSyntaxWithTextThatContainsAnEscapedClosingBracket() throws Exception {
        var input = """
                xref:tigers[[tigers\\] are cats]

                [#tigers]
                == Tigers
                """;
        assertXpath("//a[@href=\"#tigers\"][text() = \"[tigers] are cats\"]", convertStringToEmbedded(input), 1);
    }

    @Test
    @DisplayName("unescapes square bracket in reftext used by xref")
    void unescapesSquareBracketInReftextUsedByXref() throws Exception {
        var input = """
                anchor:foo[b[a\\]r]about

                see <<foo>>
                """;
        var result = convertStringToEmbedded(input);
        assertXpath("//a[@href=\"#foo\"]", result, 1);
        assertXpath("//a[@href=\"#foo\"][text()=\"b[a]r\"]", result, 1);
    }

    @Test
    @DisplayName("xref using invalid macro syntax does not create link")
    void xrefUsingInvalidMacroSyntaxDoesNotCreateLink() throws Exception {
        var doc = documentFromString("xref:tigers");
        doc.register("refs", list("tigers", new Inline(doc, "anchor", "Tigers", Options.of("type", "ref", "target", "tigers")), "Tigers"));
        assertXpath("//a", doc.convert(), 0);
    }

    @Test
    @DisplayName("should warn and create link if verbose flag is set and reference is not found")
    void shouldWarnAndCreateLinkIfVerboseFlagIsSetAndReferenceIsNotFound() throws Exception {
        var input = """
                [#foobar]
                == Foobar

                == Section B

                See <<foobaz>>.
                """;
        try (var logger = usingMemoryLogger()) {
            try (var verbose = inVerboseMode()) {
                var output = convertStringToEmbedded(input);
                assertXpath("//a[@href=\"#foobaz\"][text() = \"[foobaz]\"]", output, 1);
                assertMessage(logger, "INFO", "possible invalid reference: foobaz");
            }
        }
    }

    @Test
    @DisplayName("should not warn if verbose flag is set and reference is found in compat mode")
    void shouldNotWarnIfVerboseFlagIsSetAndReferenceIsFoundInCompatMode() throws Exception {
        var input = """
                [[foobar]]
                == Foobar

                == Section B

                See <<foobar>>.
                """;
        try (var logger = usingMemoryLogger()) {
            try (var verbose = inVerboseMode()) {
                var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("compat-mode", "")));
                assertXpath("//a[@href=\"#foobar\"][text() = \"Foobar\"]", output, 1);
                assertEmpty(logger);
            }
        }
    }

    @Test
    @DisplayName("should warn and create link if verbose flag is set and reference using # notation is not found")
    void shouldWarnAndCreateLinkIfVerboseFlagIsSetAndReferenceUsingNotationIsNotFound() throws Exception {
        var input = """
                [#foobar]
                == Foobar

                == Section B

                See <<#foobaz>>.
                """;
        try (var logger = usingMemoryLogger()) {
            try (var verbose = inVerboseMode()) {
                var output = convertStringToEmbedded(input);
                assertXpath("//a[@href=\"#foobaz\"][text() = \"[foobaz]\"]", output, 1);
                assertMessage(logger, "INFO", "possible invalid reference: foobaz");
            }
        }
    }

    @Test
    @DisplayName("should produce an internal anchor from an inter-document xref to file included into current file")
    void shouldProduceAnInternalAnchorFromAnInterDocumentXrefToFileIncludedIntoCurrentFile() throws Exception {
        var input = """
                = Book Title
                :doctype: book

                [#ch1]
                == Chapter 1

                So it begins.

                Read <<other-chapters.adoc#ch2>> to find out what happens next!

                include::other-chapters.adoc[]
                """;
        var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", fixturedir()));
        assertTruthy(doc.getCatalog().getIncludes().containsKey("other-chapters"));
        assertTruthy(get(doc.getCatalog().getIncludes(), "other-chapters"));
        var output = doc.convert();
        assertXpath("//a[@href=\"#ch2\"][text()=\"Chapter 2\"]", output, 1);
    }

    @Test
    @DisplayName("should produce an internal anchor from an inter-document xref to file included entirely into current file using tags")
    void shouldProduceAnInternalAnchorFromAnInterDocumentXrefToFileIncludedEntirelyIntoCurrentFileUsingTags() throws Exception {
        var input = """
                = Book Title
                :doctype: book

                [#ch1]
                == Chapter 1

                So it begins.

                Read <<other-chapters.adoc#ch2>> to find out what happens next!

                include::other-chapters.adoc[tags=**]
                """;
        var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", fixturedir()));
        assertXpath("//a[@href=\"#ch2\"][text()=\"Chapter 2\"]", output, 1);
    }

    @Test
    @DisplayName("should not produce an internal anchor for inter-document xref to file partially included into current file")
    void shouldNotProduceAnInternalAnchorForInterDocumentXrefToFilePartiallyIncludedIntoCurrentFile() throws Exception {
        var input = """
                = Book Title
                :doctype: book

                [#ch1]
                == Chapter 1

                So it begins.

                Read <<other-chapters.adoc#ch2,the next chapter>> to find out what happens next!

                include::other-chapters.adoc[tags=ch2]
                """;
        var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", fixturedir()));
        assertTruthy(doc.getCatalog().getIncludes().containsKey("other-chapters"));
        assertFalsy(get(doc.getCatalog().getIncludes(), "other-chapters"));
        var output = doc.convert();
        assertXpath("//a[@href=\"other-chapters.html#ch2\"][text()=\"the next chapter\"]", output, 1);
    }

    @Test
    @DisplayName("should produce an internal anchor for inter-document xref to file included fully and partially")
    void shouldProduceAnInternalAnchorForInterDocumentXrefToFileIncludedFullyAndPartially() throws Exception {
        var input = """
                = Book Title
                :doctype: book

                [#ch1]
                == Chapter 1

                So it begins.

                Read <<other-chapters.adoc#ch2,the next chapter>> to find out what happens next!

                include::other-chapters.adoc[]

                include::other-chapters.adoc[tag=ch2-noid]
                """;
        var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", fixturedir()));
        assertTruthy(doc.getCatalog().getIncludes().containsKey("other-chapters"));
        assertTruthy(get(doc.getCatalog().getIncludes(), "other-chapters"));
        var output = doc.convert();
        assertXpath("//a[@href=\"#ch2\"][text()=\"the next chapter\"]", output, 1);
    }

    @Test
    @DisplayName("should warn and create link if debug mode is enabled, inter-document xref points to current doc, and reference not found")
    void shouldWarnAndCreateLinkIfDebugModeIsEnabledInterDocumentXrefPointsToCurrentDocAndReferenceNotFound() throws Exception {
        var input = """
                [#foobar]
                == Foobar

                == Section B

                See <<test.adoc#foobaz>>.
                """;
        try (var logger = usingMemoryLogger()) {
            try (var verbose = inVerboseMode()) {
                var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("docname", "test")));
                assertXpath("//a[@href=\"#foobaz\"][text() = \"[foobaz]\"]", output, 1);
                assertMessage(logger, "INFO", "possible invalid reference: foobaz");
            }
        }
    }

    @Test
    @DisplayName("should use doctitle as fallback link text if inter-document xref points to current doc and no link text is provided")
    void shouldUseDoctitleAsFallbackLinkTextIfInterDocumentXrefPointsToCurrentDocAndNoLinkTextIsProvided() throws Exception {
        var input = """
                = Links & Stuff at https://example.org

                See xref:test.adoc[]
                """;
        var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("docname", "test")));
        assertInclude("<a href=\"#\">Links &amp; Stuff at https://example.org</a>", output);
    }

    @Test
    @DisplayName("should use doctitle of root document as fallback link text for inter-document xref in AsciiDoc table cell that resolves to current doc")
    void shouldUseDoctitleOfRootDocumentAsFallbackLinkTextForInterDocumentXrefInAsciiDocTableCellThatResolvesToCurrentDoc() throws Exception {
        var input = """
                = Document Title

                |===
                a|See xref:test.adoc[]
                |===
                """;
        var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("docname", "test")));
        assertInclude("<a href=\"#\">Document Title</a>", output);
    }

    @Test
    @DisplayName("should use reftext on document as fallback link text if inter-document xref points to current doc and no link text is provided")
    void shouldUseReftextOnDocumentAsFallbackLinkTextIfInterDocumentXrefPointsToCurrentDocAndNoLinkTextIsProvided() throws Exception {
        var input = """
                [reftext="Links and Stuff"]
                = Links & Stuff

                See xref:test.adoc[]
                """;
        var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("docname", "test")));
        assertInclude("<a href=\"#\">Links and Stuff</a>", output);
    }

    @Test
    @DisplayName("should use reftext on document as fallback link text if xref points to empty fragment and no link text is provided")
    void shouldUseReftextOnDocumentAsFallbackLinkTextIfXrefPointsToEmptyFragmentAndNoLinkTextIsProvided() throws Exception {
        var input = """
                [reftext="Links and Stuff"]
                = Links & Stuff

                See xref:#[]
                """;
        var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("docname", "test")));
        assertInclude("<a href=\"#\">Links and Stuff</a>", output);
    }

    @Test
    @DisplayName("should use fallback link text if inter-document xref points to current doc without header and no link text is provided")
    void shouldUseFallbackLinkTextIfInterDocumentXrefPointsToCurrentDocWithoutHeaderAndNoLinkTextIsProvided() throws Exception {
        var input = "See xref:test.adoc[]\n";
        var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("docname", "test")));
        assertInclude("<a href=\"#\">[^top]</a>", output);
    }

    @Test
    @DisplayName("should use fallback link text if fragment of internal xref is empty and no link text is provided")
    void shouldUseFallbackLinkTextIfFragmentOfInternalXrefIsEmptyAndNoLinkTextIsProvided() throws Exception {
        var input = "See xref:#[]\n";
        var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("docname", "test")));
        assertInclude("<a href=\"#\">[^top]</a>", output);
    }

    @Test
    @DisplayName("should use document id as linkend for self xref in DocBook backend")
    void shouldUseDocumentIdAsLinkendForSelfXrefInDocBookBackend() throws Exception {
        var input = """
                [#docid]
                = Document Title

                See xref:test.adoc[]
                """;
        var output = convertStringToEmbedded(input, Options.of("backend", "docbook", "attributes", Options.of("docname", "test")));
        assertInclude("<xref linkend=\"docid\"/>", output);
    }

    @Test
    @DisplayName("should auto-generate document id to use as linkend for self xref in DocBook backend")
    void shouldAutoGenerateDocumentIdToUseAsLinkendForSelfXrefInDocBookBackend() throws Exception {
        var input = """
                = Document Title

                See xref:test.adoc[]
                """;
        var doc = documentFromString(input, Options.of("backend", "docbook", "attributes", Options.of("docname", "test")));
        assertNull(doc.getId());
        var output = doc.convert();
        assertNull(doc.getId());
        assertInclude(" xml:id=\"__article-root__\"", output);
        assertInclude("<xref linkend=\"__article-root__\"/>", output);
    }

    @Test
    @DisplayName("should produce an internal anchor for inter-document xref to file outside of base directory")
    void shouldProduceAnInternalAnchorForInterDocumentXrefToFileOutsideOfBaseDirectory() throws Exception {
        var input = """
                = Document Title

                See <<../section-a.adoc#section-a>>.

                include::../section-a.adoc[]
                """;
        var doc = documentFromString(input, Options.of("safe", "unsafe", "base_dir", joinPath(fixturedir(), "subdir")));
        assertIncludes(doc.getCatalog().getIncludes(), "../section-a");
        var output = doc.convert(Options.of("standalone", false));
        assertXpath("//a[@href=\"#section-a\"][text()=\"Section A\"]", output, 1);
    }

    @Test
    @DisplayName("xref uses title of target as label for forward and backward references in html output")
    void xrefUsesTitleOfTargetAsLabelForForwardAndBackwardReferencesInHtmlOutput() throws Exception {
        var input = """
                == Section A

                <<_section_b>>

                == Section B

                <<_section_a>>
                """;
        var output = convertStringToEmbedded(input);
        assertXpath("//h2[@id=\"_section_a\"][text()=\"Section A\"]", output, 1);
        assertXpath("//a[@href=\"#_section_a\"][text()=\"Section A\"]", output, 1);
        assertXpath("//h2[@id=\"_section_b\"][text()=\"Section B\"]", output, 1);
        assertXpath("//a[@href=\"#_section_b\"][text()=\"Section B\"]", output, 1);
    }

    @Test
    @DisplayName("should not fail to resolve broken xref in title of block with ID")
    void shouldNotFailToResolveBrokenXrefInTitleOfBlockWithID() throws Exception {
        var input = """
                [#p1]
                .<<DNE>>
                paragraph text
                """;
        var output = convertStringToEmbedded(input);
        assertXpath("//*[@class=\"title\"]/a[@href=\"#DNE\"][text()=\"[DNE]\"]", output, 1);
    }

    @Test
    @DisplayName("should resolve forward xref in title of block with ID")
    void shouldResolveForwardXrefInTitleOfBlockWithID() throws Exception {
        var input = """
                [#p1]
                .<<conclusion>>
                paragraph text

                [#conclusion]
                == Conclusion
                """;
        var output = convertStringToEmbedded(input);
        assertXpath("//*[@class=\"title\"]/a[@href=\"#conclusion\"][text()=\"Conclusion\"]", output, 1);
    }

    @Test
    @DisplayName("should not fail to resolve broken xref in section title")
    void shouldNotFailToResolveBrokenXrefInSectionTitle() throws Exception {
        var input = """
                [#s1]
                == <<DNE>>

                == <<s1>>
                """;
        var output = convertStringToEmbedded(input);
        assertXpath("//h2[@id=\"s1\"]/a[@href=\"#DNE\"][text()=\"[DNE]\"]", output, 1);
        assertXpath("//h2/a[@href=\"#s1\"][text()=\"[DNE]\"]", output, 1);
    }

    @Test
    @DisplayName("should break circular xref reference in section title")
    void shouldBreakCircularXrefReferenceInSectionTitle() throws Exception {
        var input = """
                [#a]
                == A <<b>>

                [#b]
                == B <<a>>
                """;
        var output = convertStringToEmbedded(input);
        assertIncludes(output, "<h2 id=\"a\">A <a href=\"#b\">B [a]</a></h2>");
        assertIncludes(output, "<h2 id=\"b\">B <a href=\"#a\">[a]</a></h2>");
    }

    @Test
    @DisplayName("should drop nested anchor in xreftext")
    void shouldDropNestedAnchorInXreftext() throws Exception {
        var input = """
                [#a]
                == See <<b>>

                [#b]
                == Consult https://google.com[Google]
                """;
        var output = convertStringToEmbedded(input);
        assertIncludes(output, "<h2 id=\"a\">See <a href=\"#b\">Consult Google</a></h2>");
    }

    @Test
    @DisplayName("should not resolve forward xref evaluated during parsing")
    void shouldNotResolveForwardXrefEvaluatedDuringParsing() throws Exception {
        var input = """
                [#s1]
                == <<forward>>

                == <<s1>>

                [#forward]
                == Forward
                """;
        var output = convertStringToEmbedded(input);
        assertXpath("//a[@href=\"#forward\"][text()=\"Forward\"]", output, 0);
    }

    @Test
    @DisplayName("should not resolve forward natural xref evaluated during parsing")
    void shouldNotResolveForwardNaturalXrefEvaluatedDuringParsing() throws Exception {
        var input = """
                :idprefix:

                [#s1]
                == <<Forward>>

                == <<s1>>

                == Forward
                """;
        var output = convertStringToEmbedded(input);
        assertXpath("//a[@href=\"#forward\"][text()=\"Forward\"]", output, 0);
    }

    @Test
    @DisplayName("should resolve first matching natural xref")
    void shouldResolveFirstMatchingNaturalXref() throws Exception {
        var input = """
                see <<Section Title>>

                [#s1]
                == Section Title

                [#s2]
                == Section Title
                """;
        var output = convertStringToEmbedded(input);
        assertXpath("//a[@href=\"#s1\"]", output, 1);
        assertXpath("//a[@href=\"#s1\"][text()=\"Section Title\"]", output, 1);
    }

    @Test
    @DisplayName("should not match numeric character references while searching for fragment in xref target")
    void shouldNotMatchNumericCharacterReferencesWhileSearchingForFragmentInXrefTarget() throws Exception {
        var input = """
                see <<Cub => Tiger>>

                == Cub => Tiger
                """;
        var output = convertStringToEmbedded(input);
        assertXpath("//a[@href=\"#_cub_tiger\"]", output, 1);
        assertXpath(("//a[@href=\"#_cub_tiger\"][text()=\"Cub " + str(decodeChar(8658)) + " Tiger\"]"), output, 1);
    }

    @Test
    @DisplayName("should not match numeric character references in path of interdocument xref")
    void shouldNotMatchNumericCharacterReferencesInPathOfInterdocumentXref() throws Exception {
        var input = "see xref:{cpp}[{cpp}].\n";
        var output = convertStringToEmbedded(input);
        assertIncludes(output, "<a href=\"#C&#43;&#43;\">C&#43;&#43;</a>");
    }

    @Test
    @DisplayName("anchor creates reference")
    void anchorCreatesReference() throws Exception {
        var doc = documentFromString("[[tigers]]Tigers roam here.");
        var ref = get(doc.getCatalog().getRefs(), "tigers");
        assertNotNull(ref);
        assertNull(ref.getReftext());
    }

    @Test
    @DisplayName("anchor with label creates reference")
    void anchorWithLabelCreatesReference() throws Exception {
        var doc = documentFromString("[[tigers,Tigers]]Tigers roam here.");
        var ref = get(doc.getCatalog().getRefs(), "tigers");
        assertNotNull(ref);
        assertEquals("Tigers", ref.getReftext());
    }

    @Test
    @DisplayName("anchor with quoted label creates reference with quoted label text")
    void anchorWithQuotedLabelCreatesReferenceWithQuotedLabelText() throws Exception {
        var doc = documentFromString("[[tigers,\"Tigers roam here\"]]Tigers roam here.");
        var ref = get(doc.getCatalog().getRefs(), "tigers");
        assertNotNull(ref);
        assertEquals("\"Tigers roam here\"", ref.getReftext());
    }

    @Test
    @DisplayName("anchor with label containing a comma creates reference")
    void anchorWithLabelContainingACommaCreatesReference() throws Exception {
        var doc = documentFromString("[[tigers,Tigers, scary tigers, roam here]]Tigers roam here.");
        var ref = get(doc.getCatalog().getRefs(), "tigers");
        assertNotNull(ref);
        assertEquals("Tigers, scary tigers, roam here", ref.getReftext());
    }
}
