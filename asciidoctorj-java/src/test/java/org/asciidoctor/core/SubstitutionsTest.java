package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.*;

import org.junit.jupiter.api.*;

@DisplayName("Substitutions")
public class SubstitutionsTest extends TestHelper {
    static final String BACKSLASH = "\\";

    @Nested
    @DisplayName("Dispatcher")
    class Dispatcher {

        @Test
        @DisplayName("apply normal substitutions")
        void applyNormalSubstitutions() throws Exception {
            var para = (Block) blockFromString("[blue]_http://asciidoc.org[AsciiDoc]_ & [red]*Ruby*\n&#167; Making +++<u>documentation</u>+++ together +\nsince (C) {inception_year}.");
            para.getDocument().getAttributes().put("inception_year", "2012");
            var result = para.applySubs(para.getSource());
            assertEquals("<em class=\"blue\"><a href=\"http://asciidoc.org\">AsciiDoc</a></em> &amp; <strong class=\"red\">Ruby</strong>\n&#167; Making <u>documentation</u> together<br>\nsince &#169; 2012.", result);
        }

        @Test
        @DisplayName("apply_subs should not modify string directly")
        void apply_subsShouldNotModifyStringDirectly() throws Exception {
            var input = "<html> -- the root of all web";
            var para = (Block) blockFromString(input);
            var paraSource = para.getSource();
            var result = para.applySubs(paraSource);
            assertEquals("&lt;html&gt;&#8201;&#8212;&#8201;the root of all web", result);
            assertEquals(input, paraSource);
        }

        @Test
        @DisplayName("should not drop trailing blank lines when performing substitutions")
        void shouldNotDropTrailingBlankLinesWhenPerformingSubstitutions() throws Exception {
            var para = (Block) blockFromString("[%hardbreaks]\nthis\nis\n-> {program}");
            lines(para).add("");
            lines(para).add("");
            para.getDocument().getAttributes().put("program", "Asciidoctor");
            Object result = para.applySubs(lines(para));
            assertEquals(list("this<br>", "is<br>", "&#8594; Asciidoctor<br>", "<br>", ""), result);
            result = para.applySubs(join(lines(para), "\n"));
            assertEquals("this<br>\nis<br>\n&#8594; Asciidoctor<br>\n<br>\n", result);
        }

        @Test
        @DisplayName("should expand subs passed to expand_subs")
        void shouldExpandSubsPassedToExpand_subs() throws Exception {
            var para = (Block) blockFromString("{program}\n*bold*\n2 > 1");
            para.getDocument().getAttributes().put("program", "Asciidoctor");
            assertEquals(list(Substitution.SPECIAL_CHARACTERS), para.expandSubs(list("specialchars")));
            assertFalsy(para.expandSubs(list("none")));
            assertEquals(list(Substitution.SPECIAL_CHARACTERS, Substitution.QUOTES, Substitution.ATTRIBUTES, Substitution.REPLACEMENTS, Substitution.MACROS, Substitution.POST_REPLACEMENTS), para.expandSubs(list("normal")));
        }

        @Test
        @DisplayName("apply_subs should allow the subs argument to be nil")
        void apply_subsShouldAllowTheSubsArgumentToBeNil() throws Exception {
            var block = (Block) blockFromString("[pass]\n*raw*");
            var result = block.applySubs(block.getSource(), null);
            assertEquals("*raw*", result);
        }
    }

    @Nested
    @DisplayName("Quotes")
    class Quotes {

        @Test
        @DisplayName("single-line double-quoted string")
        void singleLineDoubleQuotedString() throws Exception {
            var para = (Block) blockFromString("``a few quoted words''", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("&#8220;a few quoted words&#8221;", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("\"`a few quoted words`\"");
            assertEquals("&#8220;a few quoted words&#8221;", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("\"`a few quoted words`\"", Options.of("backend", "docbook"));
            assertEquals("<quote role=\"double\">a few quoted words</quote>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line double-quoted string")
        void escapedSingleLineDoubleQuotedString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "``a few quoted words''"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("&#8216;`a few quoted words&#8217;'", para.subQuotes(para.getSource()));
            para = (Block) blockFromString((str(BACKSLASH.repeat(2)) + "``a few quoted words''"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("``a few quoted words''", para.subQuotes(para.getSource()));
            para = (Block) blockFromString((str(BACKSLASH) + "\"`a few quoted words`\""));
            assertEquals("\"`a few quoted words`\"", para.subQuotes(para.getSource()));
            para = (Block) blockFromString((str(BACKSLASH.repeat(2)) + "\"`a few quoted words`\""));
            assertEquals((str(BACKSLASH) + "\"`a few quoted words`\""), para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("multi-line double-quoted string")
        void multiLineDoubleQuotedString() throws Exception {
            var para = (Block) blockFromString("``a few\nquoted words''", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("&#8220;a few\nquoted words&#8221;", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("\"`a few\nquoted words`\"");
            assertEquals("&#8220;a few\nquoted words&#8221;", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("double-quoted string with inline single quote")
        void doubleQuotedStringWithInlineSingleQuote() throws Exception {
            var para = (Block) blockFromString("``Here's Johnny!''", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("&#8220;Here's Johnny!&#8221;", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("\"`Here's Johnny!`\"");
            assertEquals("&#8220;Here's Johnny!&#8221;", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("double-quoted string with inline backquote")
        void doubleQuotedStringWithInlineBackquote() throws Exception {
            var para = (Block) blockFromString("``Here`s Johnny!''", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("&#8220;Here`s Johnny!&#8221;", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("\"`Here`s Johnny!`\"");
            assertEquals("&#8220;Here`s Johnny!&#8221;", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("double-quoted string around monospaced text")
        void doubleQuotedStringAroundMonospacedText() throws Exception {
            var para = (Block) blockFromString("\"``E=mc^2^` is the solution!`\"");
            assertEquals("&#8220;`E=mc<sup>2</sup>` is the solution!&#8221;", para.applySubs(para.getSource()));
            para = (Block) blockFromString("\"```E=mc^2^`` is the solution!`\"");
            assertEquals("&#8220;<code>E=mc<sup>2</sup></code> is the solution!&#8221;", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("single-line single-quoted string")
        void singleLineSingleQuotedString() throws Exception {
            var para = (Block) blockFromString("`a few quoted words'", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("&#8216;a few quoted words&#8217;", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("'`a few quoted words`'");
            assertEquals("&#8216;a few quoted words&#8217;", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("'`a few quoted words`'", Options.of("backend", "docbook"));
            assertEquals("<quote role=\"single\">a few quoted words</quote>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line single-quoted string")
        void escapedSingleLineSingleQuotedString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "`a few quoted words'"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("`a few quoted words'", para.subQuotes(para.getSource()));
            para = (Block) blockFromString((str(BACKSLASH) + "'`a few quoted words`'"));
            assertEquals("'`a few quoted words`'", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("multi-line single-quoted string")
        void multiLineSingleQuotedString() throws Exception {
            var para = (Block) blockFromString("`a few\nquoted words'", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("&#8216;a few\nquoted words&#8217;", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("'`a few\nquoted words`'");
            assertEquals("&#8216;a few\nquoted words&#8217;", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-quoted string with inline single quote")
        void singleQuotedStringWithInlineSingleQuote() throws Exception {
            var para = (Block) blockFromString("`That isn't what I did.'", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("&#8216;That isn't what I did.&#8217;", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("'`That isn't what I did.`'");
            assertEquals("&#8216;That isn't what I did.&#8217;", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-quoted string with inline backquote")
        void singleQuotedStringWithInlineBackquote() throws Exception {
            var para = (Block) blockFromString("`Here`s Johnny!'", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("&#8216;Here`s Johnny!&#8217;", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("'`Here`s Johnny!`'");
            assertEquals("&#8216;Here`s Johnny!&#8217;", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-line constrained marked string")
        void singleLineConstrainedMarkedString() throws Exception {
            var para = (Block) blockFromString("#a few words#");
            assertEquals("<mark>a few words</mark>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line constrained marked string")
        void escapedSingleLineConstrainedMarkedString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "#a few words#"));
            assertEquals("#a few words#", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("multi-line constrained marked string")
        void multiLineConstrainedMarkedString() throws Exception {
            var para = (Block) blockFromString("#a few\nwords#");
            assertEquals("<mark>a few\nwords</mark>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("constrained marked string should not match entity references")
        void constrainedMarkedStringShouldNotMatchEntityReferences() throws Exception {
            var para = (Block) blockFromString("111 #mark a# 222 \"`quote a`\" 333 #mark b# 444");
            assertEquals("111 <mark>mark a</mark> 222 &#8220;quote a&#8221; 333 <mark>mark b</mark> 444", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-line unconstrained marked string")
        void singleLineUnconstrainedMarkedString() throws Exception {
            var para = (Block) blockFromString("##--anything goes ##");
            assertEquals("<mark>--anything goes </mark>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line unconstrained marked string")
        void escapedSingleLineUnconstrainedMarkedString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + str(BACKSLASH) + "##--anything goes ##"));
            assertEquals("##--anything goes ##", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("multi-line unconstrained marked string")
        void multiLineUnconstrainedMarkedString() throws Exception {
            var para = (Block) blockFromString("##--anything\ngoes ##");
            assertEquals("<mark>--anything\ngoes </mark>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-line constrained marked string with role")
        void singleLineConstrainedMarkedStringWithRole() throws Exception {
            var para = (Block) blockFromString("[statement]#a few words#");
            assertEquals("<span class=\"statement\">a few words</span>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("does not recognize attribute list with left square bracket on formatted text")
        void doesNotRecognizeAttributeListWithLeftSquareBracketOnFormattedText() throws Exception {
            var para = (Block) blockFromString("key: [ *before [.redacted]#redacted# after* ]");
            assertEquals("key: [ <strong>before <span class=\"redacted\">redacted</span> after</strong> ]", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("should ignore enclosing square brackets when processing formatted text with attribute list")
        void shouldIgnoreEnclosingSquareBracketsWhenProcessingFormattedTextWithAttributeList() throws Exception {
            var doc = documentFromString("nums = [1, 2, 3, [.blue]#4#]", Options.of("doctype", "inline"));
            assertEquals("nums = [1, 2, 3, <span class=\"blue\">4</span>]", doc.convert());
        }

        @Test
        @DisplayName("single-line constrained strong string")
        void singleLineConstrainedStrongString() throws Exception {
            var para = (Block) blockFromString("*a few strong words*");
            assertEquals("<strong>a few strong words</strong>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line constrained strong string")
        void escapedSingleLineConstrainedStrongString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "*a few strong words*"));
            assertEquals("*a few strong words*", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("multi-line constrained strong string")
        void multiLineConstrainedStrongString() throws Exception {
            var para = (Block) blockFromString("*a few\nstrong words*");
            assertEquals("<strong>a few\nstrong words</strong>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("constrained strong string containing an asterisk")
        void constrainedStrongStringContainingAnAsterisk() throws Exception {
            var para = (Block) blockFromString("*bl*ck*-eye");
            assertEquals("<strong>bl*ck</strong>-eye", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("constrained strong string containing an asterisk and multibyte word chars")
        void constrainedStrongStringContainingAnAsteriskAndMultibyteWordChars() throws Exception {
            var para = (Block) blockFromString("*黑*眼圈*");
            assertEquals("<strong>黑*眼圈</strong>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-line constrained quote variation emphasized string")
        void singleLineConstrainedQuoteVariationEmphasizedString() throws Exception {
            var para = (Block) blockFromString("_a few emphasized words_");
            assertEquals("<em>a few emphasized words</em>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line constrained quote variation emphasized string")
        void escapedSingleLineConstrainedQuoteVariationEmphasizedString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "_a few emphasized words_"));
            assertEquals("_a few emphasized words_", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single quoted string")
        void escapedSingleQuotedString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "'a few emphasized words'"));
            assertEquals((str(BACKSLASH) + "'a few emphasized words'"), para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("multi-line constrained emphasized quote variation string")
        void multiLineConstrainedEmphasizedQuoteVariationString() throws Exception {
            var para = (Block) blockFromString("_a few\nemphasized words_");
            assertEquals("<em>a few\nemphasized words</em>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-quoted string containing an emphasized phrase")
        void singleQuotedStringContainingAnEmphasizedPhrase() throws Exception {
            var para = (Block) blockFromString("`I told him, 'Just go for it!''", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("&#8216;I told him, <em>Just go for it!</em>&#8217;", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("'`I told him, 'Just go for it!'`'");
            assertEquals("&#8216;I told him, 'Just go for it!'&#8217;", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-quotes inside emphasized words are restored")
        void escapedSingleQuotesInsideEmphasizedWordsAreRestored() throws Exception {
            var para = (Block) blockFromString(("'Here" + str(BACKSLASH) + "'s Johnny!'"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("<em>Here's Johnny!</em>", para.applySubs(para.getSource()));
            para = (Block) blockFromString(("'Here" + str(BACKSLASH) + "'s Johnny!'"));
            assertEquals("'Here's Johnny!'", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("single-line constrained emphasized underline variation string")
        void singleLineConstrainedEmphasizedUnderlineVariationString() throws Exception {
            var para = (Block) blockFromString("_a few emphasized words_");
            assertEquals("<em>a few emphasized words</em>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line constrained emphasized underline variation string")
        void escapedSingleLineConstrainedEmphasizedUnderlineVariationString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "_a few emphasized words_"));
            assertEquals("_a few emphasized words_", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("multi-line constrained emphasized underline variation string")
        void multiLineConstrainedEmphasizedUnderlineVariationString() throws Exception {
            var para = (Block) blockFromString("_a few\nemphasized words_");
            assertEquals("<em>a few\nemphasized words</em>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-line constrained monospaced string")
        void singleLineConstrainedMonospacedString() throws Exception {
            var para = (Block) blockFromString("`a few <{monospaced}> words`", Options.of("attributes", Options.of("monospaced", "monospaced", "compat-mode", "")));
            assertEquals("<code>a few &lt;{monospaced}&gt; words</code>", para.applySubs(para.getSource()));
            para = (Block) blockFromString("`a few <{monospaced}> words`", Options.of("attributes", Options.of("monospaced", "monospaced")));
            assertEquals("<code>a few &lt;monospaced&gt; words</code>", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("single-line constrained monospaced string with role")
        void singleLineConstrainedMonospacedStringWithRole() throws Exception {
            var para = (Block) blockFromString("[input]`a few <{monospaced}> words`", Options.of("attributes", Options.of("monospaced", "monospaced", "compat-mode", "")));
            assertEquals("<code class=\"input\">a few &lt;{monospaced}&gt; words</code>", para.applySubs(para.getSource()));
            para = (Block) blockFromString("[input]`a few <{monospaced}> words`", Options.of("attributes", Options.of("monospaced", "monospaced")));
            assertEquals("<code class=\"input\">a few &lt;monospaced&gt; words</code>", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line constrained monospaced string")
        void escapedSingleLineConstrainedMonospacedString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "`a few <monospaced> words`"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("`a few &lt;monospaced&gt; words`", para.applySubs(para.getSource()));
            para = (Block) blockFromString((str(BACKSLASH) + "`a few <monospaced> words`"));
            assertEquals("`a few &lt;monospaced&gt; words`", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line constrained monospaced string with role")
        void escapedSingleLineConstrainedMonospacedStringWithRole() throws Exception {
            var para = (Block) blockFromString(("[input]" + str(BACKSLASH) + "`a few <monospaced> words`"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("[input]`a few &lt;monospaced&gt; words`", para.applySubs(para.getSource()));
            para = (Block) blockFromString(("[input]" + str(BACKSLASH) + "`a few <monospaced> words`"));
            assertEquals("[input]`a few &lt;monospaced&gt; words`", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("escaped role on single-line constrained monospaced string")
        void escapedRoleOnSingleLineConstrainedMonospacedString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "[input]`a few <monospaced> words`"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("[input]<code>a few &lt;monospaced&gt; words</code>", para.applySubs(para.getSource()));
            para = (Block) blockFromString((str(BACKSLASH) + "[input]`a few <monospaced> words`"));
            assertEquals("[input]<code>a few &lt;monospaced&gt; words</code>", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("escaped role on escaped single-line constrained monospaced string")
        void escapedRoleOnEscapedSingleLineConstrainedMonospacedString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "[input]" + str(BACKSLASH) + "`a few <monospaced> words`"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals((str(BACKSLASH) + "[input]`a few &lt;monospaced&gt; words`"), para.applySubs(para.getSource()));
            para = (Block) blockFromString((str(BACKSLASH) + "[input]" + str(BACKSLASH) + "`a few <monospaced> words`"));
            assertEquals((str(BACKSLASH) + "[input]`a few &lt;monospaced&gt; words`"), para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("should ignore role that ends with transitional role on constrained monospace span")
        void shouldIgnoreRoleThatEndsWithTransitionalRoleOnConstrainedMonospaceSpan() throws Exception {
            var para = (Block) blockFromString("[foox-]`leave it alone`");
            assertEquals("<code class=\"foox-\">leave it alone</code>", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line constrained monospace string with forced compat role")
        void escapedSingleLineConstrainedMonospaceStringWithForcedCompatRole() throws Exception {
            var para = (Block) blockFromString(("[x-]" + str(BACKSLASH) + "`leave it alone`"));
            assertEquals("[x-]`leave it alone`", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("escaped forced compat role on single-line constrained monospace string")
        void escapedForcedCompatRoleOnSingleLineConstrainedMonospaceString() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "[x-]`just *mono*`"));
            assertEquals("[x-]<code>just <strong>mono</strong></code>", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("multi-line constrained monospaced string")
        void multiLineConstrainedMonospacedString() throws Exception {
            var para = (Block) blockFromString("`a few\n<{monospaced}> words`", Options.of("attributes", Options.of("monospaced", "monospaced", "compat-mode", "")));
            assertEquals("<code>a few\n&lt;{monospaced}&gt; words</code>", para.applySubs(para.getSource()));
            para = (Block) blockFromString("`a few\n<{monospaced}> words`", Options.of("attributes", Options.of("monospaced", "monospaced")));
            assertEquals("<code>a few\n&lt;monospaced&gt; words</code>", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("single-line unconstrained strong chars")
        void singleLineUnconstrainedStrongChars() throws Exception {
            var para = (Block) blockFromString("**Git**Hub");
            assertEquals("<strong>Git</strong>Hub", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line unconstrained strong chars")
        void escapedSingleLineUnconstrainedStrongChars() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "**Git**Hub"));
            assertEquals("<strong>*Git</strong>*Hub", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("multi-line unconstrained strong chars")
        void multiLineUnconstrainedStrongChars() throws Exception {
            var para = (Block) blockFromString("**G\ni\nt\n**Hub");
            assertEquals("<strong>G\ni\nt\n</strong>Hub", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("unconstrained strong chars with inline asterisk")
        void unconstrainedStrongCharsWithInlineAsterisk() throws Exception {
            var para = (Block) blockFromString("**bl*ck**-eye");
            assertEquals("<strong>bl*ck</strong>-eye", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("unconstrained strong chars with role")
        void unconstrainedStrongCharsWithRole() throws Exception {
            var para = (Block) blockFromString("Git[blue]**Hub**");
            assertEquals("Git<strong class=\"blue\">Hub</strong>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped unconstrained strong chars with role")
        void escapedUnconstrainedStrongCharsWithRole() throws Exception {
            var para = (Block) blockFromString(("Git" + str(BACKSLASH) + "[blue]**Hub**"));
            assertEquals("Git[blue]<strong>*Hub</strong>*", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-line unconstrained emphasized chars")
        void singleLineUnconstrainedEmphasizedChars() throws Exception {
            var para = (Block) blockFromString("__Git__Hub");
            assertEquals("<em>Git</em>Hub", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line unconstrained emphasized chars")
        void escapedSingleLineUnconstrainedEmphasizedChars() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "__Git__Hub"));
            assertEquals("__Git__Hub", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line unconstrained emphasized chars around word")
        void escapedSingleLineUnconstrainedEmphasizedCharsAroundWord() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + str(BACKSLASH) + "__GitHub__"));
            assertEquals("__GitHub__", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("multi-line unconstrained emphasized chars")
        void multiLineUnconstrainedEmphasizedChars() throws Exception {
            var para = (Block) blockFromString("__G\ni\nt\n__Hub");
            assertEquals("<em>G\ni\nt\n</em>Hub", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("unconstrained emphasis chars with role")
        void unconstrainedEmphasisCharsWithRole() throws Exception {
            var para = (Block) blockFromString("[gray]__Git__Hub");
            assertEquals("<em class=\"gray\">Git</em>Hub", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped unconstrained emphasis chars with role")
        void escapedUnconstrainedEmphasisCharsWithRole() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "[gray]__Git__Hub"));
            assertEquals("[gray]__Git__Hub", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-line constrained monospaced chars")
        void singleLineConstrainedMonospacedChars() throws Exception {
            var para = (Block) blockFromString("call +save()+ to persist the changes", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("call <code>save()</code> to persist the changes", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("call [x-]+save()+ to persist the changes");
            assertEquals("call <code>save()</code> to persist the changes", para.applySubs(para.getSource()));
            para = (Block) blockFromString("call `save()` to persist the changes");
            assertEquals("call <code>save()</code> to persist the changes", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-line constrained monospaced chars with role")
        void singleLineConstrainedMonospacedCharsWithRole() throws Exception {
            var para = (Block) blockFromString("call [method]+save()+ to persist the changes", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("call <code class=\"method\">save()</code> to persist the changes", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("call [method x-]+save()+ to persist the changes");
            assertEquals("call <code class=\"method\">save()</code> to persist the changes", para.applySubs(para.getSource()));
            para = (Block) blockFromString("call [method]`save()` to persist the changes");
            assertEquals("call <code class=\"method\">save()</code> to persist the changes", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line constrained monospaced chars")
        void escapedSingleLineConstrainedMonospacedChars() throws Exception {
            var para = (Block) blockFromString(("call " + str(BACKSLASH) + "+save()+ to persist the changes"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("call +save()+ to persist the changes", para.subQuotes(para.getSource()));
            para = (Block) blockFromString(("call " + str(BACKSLASH) + "`save()` to persist the changes"));
            assertEquals("call `save()` to persist the changes", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line constrained monospaced chars with role")
        void escapedSingleLineConstrainedMonospacedCharsWithRole() throws Exception {
            var para = (Block) blockFromString(("call [method]" + str(BACKSLASH) + "+save()+ to persist the changes"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("call [method]+save()+ to persist the changes", para.subQuotes(para.getSource()));
            para = (Block) blockFromString(("call [method]" + str(BACKSLASH) + "`save()` to persist the changes"));
            assertEquals("call [method]`save()` to persist the changes", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped role on single-line constrained monospaced chars")
        void escapedRoleOnSingleLineConstrainedMonospacedChars() throws Exception {
            var para = (Block) blockFromString(("call " + str(BACKSLASH) + "[method]+save()+ to persist the changes"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("call [method]<code>save()</code> to persist the changes", para.subQuotes(para.getSource()));
            para = (Block) blockFromString(("call " + str(BACKSLASH) + "[method]`save()` to persist the changes"));
            assertEquals("call [method]<code>save()</code> to persist the changes", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped role on escaped single-line constrained monospaced chars")
        void escapedRoleOnEscapedSingleLineConstrainedMonospacedChars() throws Exception {
            var para = (Block) blockFromString(("call " + str(BACKSLASH) + "[method]" + str(BACKSLASH) + "+save()+ to persist the changes"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals(("call " + str(BACKSLASH) + "[method]+save()+ to persist the changes"), para.subQuotes(para.getSource()));
            para = (Block) blockFromString(("call " + str(BACKSLASH) + "[method]" + str(BACKSLASH) + "`save()` to persist the changes"));
            assertEquals(("call " + str(BACKSLASH) + "[method]`save()` to persist the changes"), para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line constrained passthrough string with forced compat role")
        void escapedSingleLineConstrainedPassthroughStringWithForcedCompatRole() throws Exception {
            var para = (Block) blockFromString(("[x-]" + str(BACKSLASH) + "+leave it alone+"));
            assertEquals("[x-]+leave it alone+", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("single-line unconstrained monospaced chars")
        void singleLineUnconstrainedMonospacedChars() throws Exception {
            var para = (Block) blockFromString("Git++Hub++", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("Git<code>Hub</code>", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("Git[x-]++Hub++");
            assertEquals("Git<code>Hub</code>", para.applySubs(para.getSource()));
            para = (Block) blockFromString("Git``Hub``");
            assertEquals("Git<code>Hub</code>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line unconstrained monospaced chars")
        void escapedSingleLineUnconstrainedMonospacedChars() throws Exception {
            var para = (Block) blockFromString(("Git" + str(BACKSLASH) + "++Hub++"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("Git+<code>Hub</code>+", para.subQuotes(para.getSource()));
            para = (Block) blockFromString(("Git" + str(BACKSLASH.repeat(2)) + "++Hub++"), Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("Git++Hub++", para.subQuotes(para.getSource()));
            para = (Block) blockFromString(("Git" + str(BACKSLASH) + "``Hub``"));
            assertEquals("Git``Hub``", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("multi-line unconstrained monospaced chars")
        void multiLineUnconstrainedMonospacedChars() throws Exception {
            var para = (Block) blockFromString("Git++\nH\nu\nb++", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("Git<code>\nH\nu\nb</code>", para.subQuotes(para.getSource()));
            para = (Block) blockFromString("Git[x-]++\nH\nu\nb++");
            assertEquals("Git<code>\nH\nu\nb</code>", para.applySubs(para.getSource()));
            para = (Block) blockFromString("Git``\nH\nu\nb``");
            assertEquals("Git<code>\nH\nu\nb</code>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("single-line superscript chars")
        void singleLineSuperscriptChars() throws Exception {
            var para = (Block) blockFromString("x^2^ = x * x, e = mc^2^, there's a 1^st^ time for everything");
            assertEquals("x<sup>2</sup> = x * x, e = mc<sup>2</sup>, there's a 1<sup>st</sup> time for everything", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line superscript chars")
        void escapedSingleLineSuperscriptChars() throws Exception {
            var para = (Block) blockFromString(("x" + str(BACKSLASH) + "^2^ = x * x"));
            assertEquals("x^2^ = x * x", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("does not match superscript across whitespace")
        void doesNotMatchSuperscriptAcrossWhitespace() throws Exception {
            var para = (Block) blockFromString("x^(n\n-\n1)^");
            assertEquals(para.getSource(), para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("allow spaces in superscript if spaces are inserted using an attribute reference")
        void allowSpacesInSuperscriptIfSpacesAreInsertedUsingAnAttributeReference() throws Exception {
            var para = (Block) blockFromString("Night ^A{sp}poem{sp}by{sp}Jane{sp}Kondo^.");
            assertEquals("Night <sup>A poem by Jane Kondo</sup>.", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("allow spaces in superscript if text is wrapped in a passthrough")
        void allowSpacesInSuperscriptIfTextIsWrappedInAPassthrough() throws Exception {
            var para = (Block) blockFromString("Night ^+A poem by Jane Kondo+^.");
            assertEquals("Night <sup>A poem by Jane Kondo</sup>.", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("does not match adjacent superscript chars")
        void doesNotMatchAdjacentSuperscriptChars() throws Exception {
            var para = (Block) blockFromString("a ^^ b");
            assertEquals("a ^^ b", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("does not confuse superscript and links with blank window shorthand")
        void doesNotConfuseSuperscriptAndLinksWithBlankWindowShorthand() throws Exception {
            var para = (Block) blockFromString("http://localhost[Text^] on the 21^st^ and 22^nd^");
            assertEquals("<a href=\"http://localhost\" target=\"_blank\" rel=\"noopener\">Text</a> on the 21<sup>st</sup> and 22<sup>nd</sup>", para.getContent());
        }

        @Test
        @DisplayName("single-line subscript chars")
        void singleLineSubscriptChars() throws Exception {
            var para = (Block) blockFromString("H~2~O");
            assertEquals("H<sub>2</sub>O", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("escaped single-line subscript chars")
        void escapedSingleLineSubscriptChars() throws Exception {
            var para = (Block) blockFromString(("H" + str(BACKSLASH) + "~2~O"));
            assertEquals("H~2~O", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("does not match subscript across whitespace")
        void doesNotMatchSubscriptAcrossWhitespace() throws Exception {
            var para = (Block) blockFromString("project~ view\non\nGitHub~");
            assertEquals(para.getSource(), para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("does not match adjacent subscript chars")
        void doesNotMatchAdjacentSubscriptChars() throws Exception {
            var para = (Block) blockFromString("a ~~ b");
            assertEquals("a ~~ b", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("does not match subscript across distinct URLs")
        void doesNotMatchSubscriptAcrossDistinctURLs() throws Exception {
            var para = (Block) blockFromString("http://www.abc.com/~def[DEF] and http://www.abc.com/~ghi[GHI]");
            assertEquals(para.getSource(), para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("quoted text with role shorthand")
        void quotedTextWithRoleShorthand() throws Exception {
            var para = (Block) blockFromString("[.white.red-background]#alert#");
            assertEquals("<span class=\"white red-background\">alert</span>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("quoted text with id shorthand")
        void quotedTextWithIdShorthand() throws Exception {
            var para = (Block) blockFromString("[#bond]#007#");
            assertEquals("<span id=\"bond\">007</span>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("quoted text with id and role shorthand")
        void quotedTextWithIdAndRoleShorthand() throws Exception {
            var para = (Block) blockFromString("[#bond.white.red-background]#007#");
            assertEquals("<span id=\"bond\" class=\"white red-background\">007</span>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("quoted text with id and role shorthand with roles before id")
        void quotedTextWithIdAndRoleShorthandWithRolesBeforeId() throws Exception {
            var para = (Block) blockFromString("[.white.red-background#bond]#007#");
            assertEquals("<span id=\"bond\" class=\"white red-background\">007</span>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("quoted text with id and role shorthand with roles around id")
        void quotedTextWithIdAndRoleShorthandWithRolesAroundId() throws Exception {
            var para = (Block) blockFromString("[.white#bond.red-background]#007#");
            assertEquals("<span id=\"bond\" class=\"white red-background\">007</span>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("quoted text with id and role shorthand using docbook backend")
        void quotedTextWithIdAndRoleShorthandUsingDocbookBackend() throws Exception {
            var para = (Block) blockFromString("[#bond.white.red-background]#007#", Options.of("backend", "docbook"));
            assertEquals("<anchor xml:id=\"bond\"/><phrase role=\"white red-background\">007</phrase>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("should not assign role attribute if shorthand style has no roles")
        void shouldNotAssignRoleAttributeIfShorthandStyleHasNoRoles() throws Exception {
            var para = (Block) blockFromString("[#idname]*blah*");
            assertEquals("<strong id=\"idname\">blah</strong>", para.getContent());
        }

        @Test
        @DisplayName("should remove trailing spaces from role defined using shorthand")
        void shouldRemoveTrailingSpacesFromRoleDefinedUsingShorthand() throws Exception {
            var para = (Block) blockFromString("[.rolename ]*blah*");
            assertEquals("<strong class=\"rolename\">blah</strong>", para.getContent());
        }

        @Test
        @DisplayName("should allow role to be defined using attribute reference")
        void shouldAllowRoleToBeDefinedUsingAttributeReference() throws Exception {
            var input = "[{rolename}]#phrase#";
            var result = convertStringToEmbedded(input, Options.of("doctype", "inline", "attributes", Options.of("rolename", "red")));
            assertEquals("<span class=\"red\">phrase</span>", result);
        }

        @Test
        @DisplayName("should ignore attributes after comma")
        void shouldIgnoreAttributesAfterComma() throws Exception {
            var para = (Block) blockFromString("[red, foobar]#alert#");
            assertEquals("<span class=\"red\">alert</span>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("should remove leading and trailing spaces around role after ignoring attributes after comma")
        void shouldRemoveLeadingAndTrailingSpacesAroundRoleAfterIgnoringAttributesAfterComma() throws Exception {
            var para = (Block) blockFromString("[ red , foobar]#alert#");
            assertEquals("<span class=\"red\">alert</span>", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("should not assign role if value before comma is empty")
        void shouldNotAssignRoleIfValueBeforeCommaIsEmpty() throws Exception {
            var para = (Block) blockFromString("[,]#anonymous#");
            assertEquals("anonymous", para.subQuotes(para.getSource()));
        }

        @Test
        @DisplayName("inline passthrough with id and role set using shorthand")
        void inlinePassthroughWithIdAndRoleSetUsingShorthand() throws Exception {
            for (var attrlist : list("#idname.rolename", ".rolename#idname")) {
                var para = (Block) blockFromString(("[" + str(attrlist) + "]+pass+"));
                assertEquals("<span id=\"idname\" class=\"rolename\">pass</span>", para.getContent());
            }
        }
    }

    @Nested
    @DisplayName("Macros")
    class Macros {

        @Test
        @DisplayName("a single-line link macro should be interpreted as a link")
        void aSingleLineLinkMacroShouldBeInterpretedAsALink() throws Exception {
            var para = (Block) blockFromString("link:/home.html[]");
            assertEquals("<a href=\"/home.html\" class=\"bare\">/home.html</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a single-line link macro with text should be interpreted as a link")
        void aSingleLineLinkMacroWithTextShouldBeInterpretedAsALink() throws Exception {
            var para = (Block) blockFromString("link:/home.html[Home]");
            assertEquals("<a href=\"/home.html\">Home</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a mailto macro should be interpreted as a mailto link")
        void aMailtoMacroShouldBeInterpretedAsAMailtoLink() throws Exception {
            var para = (Block) blockFromString("mailto:doc.writer@asciidoc.org[]");
            assertEquals("<a href=\"mailto:doc.writer@asciidoc.org\">doc.writer@asciidoc.org</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a mailto macro with text should be interpreted as a mailto link")
        void aMailtoMacroWithTextShouldBeInterpretedAsAMailtoLink() throws Exception {
            var para = (Block) blockFromString("mailto:doc.writer@asciidoc.org[Doc Writer]");
            assertEquals("<a href=\"mailto:doc.writer@asciidoc.org\">Doc Writer</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a mailto macro with text and subject should be interpreted as a mailto link")
        void aMailtoMacroWithTextAndSubjectShouldBeInterpretedAsAMailtoLink() throws Exception {
            var para = (Block) blockFromString("mailto:doc.writer@asciidoc.org[Doc Writer, Pull request]");
            assertEquals("<a href=\"mailto:doc.writer@asciidoc.org?subject=Pull%20request\">Doc Writer</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a mailto macro with text, subject and body should be interpreted as a mailto link")
        void aMailtoMacroWithTextSubjectAndBodyShouldBeInterpretedAsAMailtoLink() throws Exception {
            var para = (Block) blockFromString("mailto:doc.writer@asciidoc.org[Doc Writer, Pull request, Please accept my pull request]");
            assertEquals("<a href=\"mailto:doc.writer@asciidoc.org?subject=Pull%20request&amp;body=Please%20accept%20my%20pull%20request\">Doc Writer</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a mailto macro with subject and body only should use e-mail as text")
        void aMailtoMacroWithSubjectAndBodyOnlyShouldUseEMailAsText() throws Exception {
            var para = (Block) blockFromString("mailto:doc.writer@asciidoc.org[,Pull request,Please accept my pull request]");
            assertEquals("<a href=\"mailto:doc.writer@asciidoc.org?subject=Pull%20request&amp;body=Please%20accept%20my%20pull%20request\">doc.writer@asciidoc.org</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a mailto macro supports id and role attributes")
        void aMailtoMacroSupportsIdAndRoleAttributes() throws Exception {
            var para = (Block) blockFromString("mailto:doc.writer@asciidoc.org[,id=contact,role=icon]");
            assertEquals("<a href=\"mailto:doc.writer@asciidoc.org\" id=\"contact\" class=\"icon\">doc.writer@asciidoc.org</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("should recognize inline email addresses")
        void shouldRecognizeInlineEmailAddresses() throws Exception {
            for (var input : list("doc.writer@asciidoc.org", "author+website@4fs.no", "john@domain.uk.co", "name@somewhere.else.com", "joe_bloggs@mail_server.com", "joe-bloggs@mail-server.com", "joe.bloggs@mail.server.com", "FOO@BAR.COM", "docs@writing.ninja")) {
                var para = (Block) blockFromString(input);
                assertEquals(("<a href=\"mailto:" + str(input) + "\">" + str(input) + "</a>"), para.subMacros(para.getSource()));
            }
        }

        @Test
        @DisplayName("should recognize inline email address containing an ampersand")
        void shouldRecognizeInlineEmailAddressContainingAnAmpersand() throws Exception {
            var para = (Block) blockFromString("bert&ernie@sesamestreet.com");
            assertEquals("<a href=\"mailto:bert&amp;ernie@sesamestreet.com\">bert&amp;ernie@sesamestreet.com</a>", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("should recognize inline email address surrounded by angle brackets")
        void shouldRecognizeInlineEmailAddressSurroundedByAngleBrackets() throws Exception {
            var para = (Block) blockFromString("<doc.writer@asciidoc.org>");
            assertEquals("&lt;<a href=\"mailto:doc.writer@asciidoc.org\">doc.writer@asciidoc.org</a>&gt;", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("should ignore escaped inline email address")
        void shouldIgnoreEscapedInlineEmailAddress() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "doc.writer@asciidoc.org"));
            assertEquals("doc.writer@asciidoc.org", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a single-line raw url should be interpreted as a link")
        void aSingleLineRawUrlShouldBeInterpretedAsALink() throws Exception {
            var para = (Block) blockFromString("http://google.com");
            assertEquals("<a href=\"http://google.com\" class=\"bare\">http://google.com</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a single-line raw url with text should be interpreted as a link")
        void aSingleLineRawUrlWithTextShouldBeInterpretedAsALink() throws Exception {
            var para = (Block) blockFromString("http://google.com[Google]");
            assertEquals("<a href=\"http://google.com\">Google</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a multi-line raw url with text should be interpreted as a link")
        void aMultiLineRawUrlWithTextShouldBeInterpretedAsALink() throws Exception {
            var para = (Block) blockFromString("http://google.com[Google\nHomepage]");
            assertEquals("<a href=\"http://google.com\">Google\nHomepage</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a single-line raw url with attribute as text should be interpreted as a link with resolved attribute")
        void aSingleLineRawUrlWithAttributeAsTextShouldBeInterpretedAsALinkWithResolvedAttribute() throws Exception {
            var para = (Block) blockFromString("http://google.com[{google_homepage}]");
            para.getDocument().getAttributes().put("google_homepage", "Google Homepage");
            assertEquals("<a href=\"http://google.com\">Google Homepage</a>", para.subMacros(para.subAttributes(para.getSource())));
        }

        @Test
        @DisplayName("should not resolve an escaped attribute in link text")
        void shouldNotResolveAnEscapedAttributeInLinkText() throws Exception {
            for (var entry : entries(smap("http://google.com", ("http://google.com[" + str(BACKSLASH) + "{google_homepage}]"), "http://google.com?q=,", ("link:http://google.com?q=,[" + str(BACKSLASH) + "{google_homepage}]")))) {
                var uri = entry.getKey();
                var macro = entry.getValue();
                var para = (Block) blockFromString(macro);
                para.getDocument().getAttributes().put("google_homepage", "Google Homepage");
                assertEquals(("<a href=\"" + str(uri) + "\">{google_homepage}</a>"), para.subMacros(para.subAttributes(para.getSource())));
            }
        }

        @Test
        @DisplayName("a single-line escaped raw url should not be interpreted as a link")
        void aSingleLineEscapedRawUrlShouldNotBeInterpretedAsALink() throws Exception {
            var para = (Block) blockFromString((str(BACKSLASH) + "http://google.com"));
            assertEquals("http://google.com", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a comma separated list of links should not include commas in links")
        void aCommaSeparatedListOfLinksShouldNotIncludeCommasInLinks() throws Exception {
            var para = (Block) blockFromString("http://foo.com, http://bar.com, http://example.org");
            assertEquals("<a href=\"http://foo.com\" class=\"bare\">http://foo.com</a>, <a href=\"http://bar.com\" class=\"bare\">http://bar.com</a>, <a href=\"http://example.org\" class=\"bare\">http://example.org</a>", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a single-line image macro should be interpreted as an image")
        void aSingleLineImageMacroShouldBeInterpretedAsAnImage() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[]");
            assertEquals("<span class=\"image\"><img src=\"tiger.png\" alt=\"tiger\"></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should use the imagesdir attribute defined on image macro when resolving image path")
        void shouldUseTheImagesdirAttributeDefinedOnImageMacroWhenResolvingImagePath() throws Exception {
            var input = """
                    :imagesdir: images

                    Great job! image:rainbow.png[imagesdir=stickers]
                    '''
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "src=\"stickers/rainbow.png\"");
        }

        @Test
        @DisplayName("should replace underscore and hyphen with space in generated alt text for an inline image")
        void shouldReplaceUnderscoreAndHyphenWithSpaceInGeneratedAltTextForAnInlineImage() throws Exception {
            var para = (Block) blockFromString("image:tiger-with-family_1.png[]");
            assertEquals("<span class=\"image\"><img src=\"tiger-with-family_1.png\" alt=\"tiger with family 1\"></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("a single-line image macro with text should be interpreted as an image with alt text")
        void aSingleLineImageMacroWithTextShouldBeInterpretedAsAnImageWithAltText() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger]");
            assertEquals("<span class=\"image\"><img src=\"tiger.png\" alt=\"Tiger\"></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should encode special characters in alt text of inline image")
        void shouldEncodeSpecialCharactersInAltTextOfInlineImage() throws Exception {
            var input = "A tiger's \"roar\" is < a bear's \"growl\"";
            var expected = "A tiger&#8217;s &quot;roar&quot; is &lt; a bear&#8217;s &quot;growl&quot;";
            var output = gsub(convertInlineString(("image:tiger-roar.png[" + str(input) + "]")), rx(">\\s+<"), "><");
            assertEquals(("<span class=\"image\"><img src=\"tiger-roar.png\" alt=\"" + str(expected) + "\"></span>"), output);
        }

        @Test
        @DisplayName("an image macro with SVG image and text should be interpreted as an image with alt text")
        void anImageMacroWithSVGImageAndTextShouldBeInterpretedAsAnImageWithAltText() throws Exception {
            var para = (Block) blockFromString("image:tiger.svg[Tiger]");
            assertEquals("<span class=\"image\"><img src=\"tiger.svg\" alt=\"Tiger\"></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an image macro with an interactive SVG image and alt text should be converted to an object element")
        void anImageMacroWithAnInteractiveSVGImageAndAltTextShouldBeConvertedToAnObjectElement() throws Exception {
            var para = (Block) blockFromString("image:tiger.svg[Tiger,opts=interactive]", Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("imagesdir", "images")));
            assertEquals("<span class=\"image\"><object type=\"image/svg+xml\" data=\"images/tiger.svg\"><span class=\"alt\">Tiger</span></object></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an image macro with an interactive SVG image, fallback and alt text should be converted to an object element")
        void anImageMacroWithAnInteractiveSVGImageFallbackAndAltTextShouldBeConvertedToAnObjectElement() throws Exception {
            var para = (Block) blockFromString("image:tiger.svg[Tiger,fallback=tiger.png,opts=interactive]", Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("imagesdir", "images")));
            assertEquals("<span class=\"image\"><object type=\"image/svg+xml\" data=\"images/tiger.svg\"><img src=\"images/tiger.png\" alt=\"Tiger\"></object></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an image macro with an inline SVG image should be converted to an svg element")
        void anImageMacroWithAnInlineSVGImageShouldBeConvertedToAnSvgElement() throws Exception {
            var para = (Block) blockFromString("image:circle.svg[Tiger,100,opts=inline]", Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("imagesdir", "fixtures", "docdir", testdir())));
            var result = gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><");
            assertMatch(rx("<svg\\s[^>]*width=\"100\"[^>]*>"), result);
            refuteMatch(rx("<svg\\s[^>]*width=\"500\"[^>]*>"), result);
            refuteMatch(rx("<svg\\s[^>]*height=\"500\"[^>]*>"), result);
            refuteMatch(rx("<svg\\s[^>]*style=\"[^>]*>"), result);
        }

        @Test
        @DisplayName("should ignore link attribute if value is self and image target is inline SVG")
        void shouldIgnoreLinkAttributeIfValueIsSelfAndImageTargetIsInlineSVG() throws Exception {
            var para = (Block) blockFromString("image:circle.svg[Tiger,100,opts=inline,link=self]", Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("imagesdir", "fixtures", "docdir", testdir())));
            var result = gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><");
            assertMatch(rx("<svg\\s[^>]*width=\"100\"[^>]*>"), result);
            refuteMatch(rx("<a href="), result);
        }

        @Test
        @DisplayName("an image macro with an inline SVG image should be converted to an svg element even when data-uri is set")
        void anImageMacroWithAnInlineSVGImageShouldBeConvertedToAnSvgElementEvenWhenDataUriIsSet() throws Exception {
            var para = (Block) blockFromString("image:circle.svg[Tiger,100,opts=inline]", Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("data-uri", "", "imagesdir", "fixtures", "docdir", testdir())));
            assertMatch(rx("<svg\\s[^>]*width=\"100\">"), gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an image macro with an SVG image should not use an object element when safe mode is secure")
        void anImageMacroWithAnSVGImageShouldNotUseAnObjectElementWhenSafeModeIsSecure() throws Exception {
            var para = (Block) blockFromString("image:tiger.svg[Tiger,opts=interactive]", Options.of("attributes", Options.of("imagesdir", "images")));
            assertEquals("<span class=\"image\"><img src=\"images/tiger.svg\" alt=\"Tiger\"></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("a single-line image macro with text containing escaped square bracket should be interpreted as an image with alt text")
        void aSingleLineImageMacroWithTextContainingEscapedSquareBracketShouldBeInterpretedAsAnImageWithAltText() throws Exception {
            var para = (Block) blockFromString(("image:tiger.png[[Another" + str(BACKSLASH) + "] Tiger]"));
            assertEquals("<span class=\"image\"><img src=\"tiger.png\" alt=\"[Another] Tiger\"></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("a single-line image macro with text and dimensions should be interpreted as an image with alt text and dimensions")
        void aSingleLineImageMacroWithTextAndDimensionsShouldBeInterpretedAsAnImageWithAltTextAndDimensions() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger, 200, 100]");
            assertEquals("<span class=\"image\"><img src=\"tiger.png\" alt=\"Tiger\" width=\"200\" height=\"100\"></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("a single-line image macro with text and dimensions should be interpreted as an image with alt text and dimensions in docbook")
        void aSingleLineImageMacroWithTextAndDimensionsShouldBeInterpretedAsAnImageWithAltTextAndDimensionsInDocbook() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger, 200, 100]", Options.of("backend", "docbook"));
            assertEquals("<inlinemediaobject><imageobject><imagedata fileref=\"tiger.png\" contentwidth=\"200\" contentdepth=\"100\"/></imageobject><textobject><phrase>Tiger</phrase></textobject></inlinemediaobject>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("a single-line image macro with scaledwidth attribute should be supported in docbook")
        void aSingleLineImageMacroWithScaledwidthAttributeShouldBeSupportedInDocbook() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger,scaledwidth=25%]", Options.of("backend", "docbook"));
            assertEquals("<inlinemediaobject><imageobject><imagedata fileref=\"tiger.png\" width=\"25%\"/></imageobject><textobject><phrase>Tiger</phrase></textobject></inlinemediaobject>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("a single-line image macro with scaled attribute should be supported in docbook")
        void aSingleLineImageMacroWithScaledAttributeShouldBeSupportedInDocbook() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger,scale=200]", Options.of("backend", "docbook"));
            assertEquals("<inlinemediaobject><imageobject><imagedata fileref=\"tiger.png\" scale=\"200\"/></imageobject><textobject><phrase>Tiger</phrase></textobject></inlinemediaobject>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should pass through role on image macro to DocBook output")
        void shouldPassThroughRoleOnImageMacroToDocBookOutput() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger,200,role=animal]", Options.of("backend", "docbook"));
            var result = para.subMacros(para.getSource());
            assertIncludes(result, "<inlinemediaobject role=\"animal\">");
        }

        @Test
        @DisplayName("a single-line image macro with text and link should be interpreted as a linked image with alt text")
        void aSingleLineImageMacroWithTextAndLinkShouldBeInterpretedAsALinkedImageWithAltText() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger, link=\"http://en.wikipedia.org/wiki/Tiger\"]");
            assertEquals("<span class=\"image\"><a class=\"image\" href=\"http://en.wikipedia.org/wiki/Tiger\"><img src=\"tiger.png\" alt=\"Tiger\"></a></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an inline image macro with link should be interpreted as a linked image in docbook")
        void anInlineImageMacroWithLinkShouldBeInterpretedAsALinkedImageInDocbook() throws Exception {
            var para = (Block) blockFromString("image:apache license 2_0.png[Apache License 2.0,link=http://www.apache.org/licenses/LICENSE-2.0]", Options.of("backend", "docbook"));
            assertEquals("<link xl:href=\"http://www.apache.org/licenses/LICENSE-2.0\"><inlinemediaobject><imageobject><imagedata fileref=\"apache%20license%202_0.png\"/></imageobject><textobject><phrase>Apache License 2.0</phrase></textobject></inlinemediaobject></link>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("a single-line image macro with text and link to self should be interpreted as a self-referencing image with alt text")
        void aSingleLineImageMacroWithTextAndLinkToSelfShouldBeInterpretedAsASelfReferencingImageWithAltText() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger, link=self]", Options.of("attributes", Options.of("imagesdir", "img")));
            assertEquals("<span class=\"image\"><a class=\"image\" href=\"img/tiger.png\"><img src=\"img/tiger.png\" alt=\"Tiger\"></a></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an inline image macro with text and link to self should be interpreted as a self-referencing image in docbook")
        void anInlineImageMacroWithTextAndLinkToSelfShouldBeInterpretedAsASelfReferencingImageInDocbook() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger,link=self]", Options.of("attributes", Options.of("imagesdir", "img"), "backend", "docbook"));
            assertEquals("<link xl:href=\"img/tiger.png\"><inlinemediaobject><imageobject><imagedata fileref=\"img/tiger.png\"/></imageobject><textobject><phrase>Tiger</phrase></textobject></inlinemediaobject></link>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should link to data URI if value of link attribute is self and inline image is embedded")
        void shouldLinkToDataURIIfValueOfLinkAttributeIsSelfAndInlineImageIsEmbedded() throws Exception {
            var para = (Block) blockFromString("image:circle.svg[Tiger,100,link=self]", Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("data-uri", "", "imagesdir", "fixtures", "docdir", testdir())));
            var output = gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><");
            assertXpath("//a[starts-with(@href,\"data:image/svg+xml;base64,\")]", output, 1);
            assertXpath("//img[starts-with(@src,\"data:image/svg+xml;base64,\")]", output, 1);
        }

        @Test
        @DisplayName("rel=noopener should be added to an image with a link that targets the _blank window")
        void relNoopenerShouldBeAddedToAnImageWithALinkThatTargetsThe_blankWindow() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger,link=http://en.wikipedia.org/wiki/Tiger,window=_blank]");
            assertEquals("<span class=\"image\"><a class=\"image\" href=\"http://en.wikipedia.org/wiki/Tiger\" target=\"_blank\" rel=\"noopener\"><img src=\"tiger.png\" alt=\"Tiger\"></a></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("rel=noopener should be added to an image with a link that targets a named window when the noopener option is set")
        void relNoopenerShouldBeAddedToAnImageWithALinkThatTargetsANamedWindowWhenTheNoopenerOptionIsSet() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger,link=http://en.wikipedia.org/wiki/Tiger,window=name,opts=noopener]");
            assertEquals("<span class=\"image\"><a class=\"image\" href=\"http://en.wikipedia.org/wiki/Tiger\" target=\"name\" rel=\"noopener\"><img src=\"tiger.png\" alt=\"Tiger\"></a></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("rel=nofollow should be added to an image with a link when the nofollow option is set")
        void relNofollowShouldBeAddedToAnImageWithALinkWhenTheNofollowOptionIsSet() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Tiger,link=http://en.wikipedia.org/wiki/Tiger,opts=nofollow]");
            assertEquals("<span class=\"image\"><a class=\"image\" href=\"http://en.wikipedia.org/wiki/Tiger\" rel=\"nofollow\"><img src=\"tiger.png\" alt=\"Tiger\"></a></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("a multi-line image macro with text and dimensions should be interpreted as an image with alt text and dimensions")
        void aMultiLineImageMacroWithTextAndDimensionsShouldBeInterpretedAsAnImageWithAltTextAndDimensions() throws Exception {
            var para = (Block) blockFromString("image:tiger.png[Another\nAwesome\nTiger, 200,\n100]");
            assertEquals("<span class=\"image\"><img src=\"tiger.png\" alt=\"Another Awesome Tiger\" width=\"200\" height=\"100\"></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an inline image macro with a url target should be interpreted as an image")
        void anInlineImageMacroWithAUrlTargetShouldBeInterpretedAsAnImage() throws Exception {
            var para = (Block) blockFromString("Beware of the image:http://example.com/images/tiger.png[tiger].");
            assertEquals("Beware of the <span class=\"image\"><img src=\"http://example.com/images/tiger.png\" alt=\"tiger\"></span>.", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an inline image macro with a float attribute should be interpreted as a floating image")
        void anInlineImageMacroWithAFloatAttributeShouldBeInterpretedAsAFloatingImage() throws Exception {
            var para = (Block) blockFromString("image:http://example.com/images/tiger.png[tiger, float=\"right\"] Beware of the tigers!");
            assertEquals("<span class=\"image right\"><img src=\"http://example.com/images/tiger.png\" alt=\"tiger\"></span> Beware of the tigers!", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should propagate id attribute on inline image")
        void shouldPropagateIdAttributeOnInlineImage() throws Exception {
            var para = (Block) blockFromString("image:ruby.png[Ruby logo,id=ruby-logo] is the Ruby logo");
            assertEquals("<span id=\"ruby-logo\" class=\"image\"><img src=\"ruby.png\" alt=\"Ruby logo\"></span> is the Ruby logo", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should propagate id attribute on inline image and use alt text as reftext when converting to DocBook")
        void shouldPropagateIdAttributeOnInlineImageAndUseAltTextAsReftextWhenConvertingToDocBook() throws Exception {
            var para = (Block) blockFromString("image:ruby.png[Ruby logo,id=ruby-logo] is the Ruby logo", Options.of("backend", "docbook"));
            assertEquals("<inlinemediaobject xml:id=\"ruby-logo\"><imageobject><imagedata fileref=\"ruby.png\"/></imageobject><textobject><phrase>Ruby logo</phrase></textobject></inlinemediaobject> is the Ruby logo", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should prepend value of imagesdir attribute to inline image target if target is relative path")
        void shouldPrependValueOfImagesdirAttributeToInlineImageTargetIfTargetIsRelativePath() throws Exception {
            var para = (Block) blockFromString("Beware of the image:tiger.png[tiger].", Options.of("attributes", Options.of("imagesdir", "./images")));
            assertEquals("Beware of the <span class=\"image\"><img src=\"./images/tiger.png\" alt=\"tiger\"></span>.", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should not prepend value of imagesdir attribute to inline image target if target is absolute path")
        void shouldNotPrependValueOfImagesdirAttributeToInlineImageTargetIfTargetIsAbsolutePath() throws Exception {
            var para = (Block) blockFromString("Beware of the image:/tiger.png[tiger].", Options.of("attributes", Options.of("imagesdir", "./images")));
            assertEquals("Beware of the <span class=\"image\"><img src=\"/tiger.png\" alt=\"tiger\"></span>.", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should not prepend value of imagesdir attribute to inline image target if target is url")
        void shouldNotPrependValueOfImagesdirAttributeToInlineImageTargetIfTargetIsUrl() throws Exception {
            var para = (Block) blockFromString("Beware of the image:http://example.com/images/tiger.png[tiger].", Options.of("attributes", Options.of("imagesdir", "./images")));
            assertEquals("Beware of the <span class=\"image\"><img src=\"http://example.com/images/tiger.png\" alt=\"tiger\"></span>.", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should match an inline image macro if target contains a space character")
        void shouldMatchAnInlineImageMacroIfTargetContainsASpaceCharacter() throws Exception {
            var para = (Block) blockFromString("Beware of the image:big cats.png[] around here.");
            assertEquals("Beware of the <span class=\"image\"><img src=\"big%20cats.png\" alt=\"big cats\"></span> around here.", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should not match an inline image macro if target contains a newline character")
        void shouldNotMatchAnInlineImageMacroIfTargetContainsANewlineCharacter() throws Exception {
            var para = (Block) blockFromString("Fear not. There are no image:big\ncats.png[] around here.");
            var result = para.subMacros(para.getSource());
            refuteIncludes(result, "<img ");
            assertIncludes(result, "image:big\ncats.png[]");
        }

        @Test
        @DisplayName("should not match an inline image macro if target begins or ends with space character")
        void shouldNotMatchAnInlineImageMacroIfTargetBeginsOrEndsWithSpaceCharacter() throws Exception {
            for (var input : list("image: big cats.png[]", "image:big cats.png []")) {
                var para = (Block) blockFromString(("Fear not. There are no " + str(input) + " around here."));
                var result = para.subMacros(para.getSource());
                refuteIncludes(result, "<img ");
                assertIncludes(result, input);
            }
        }

        @Test
        @DisplayName("should not detect a block image macro found inline")
        void shouldNotDetectABlockImageMacroFoundInline() throws Exception {
            var para = (Block) blockFromString("Not an inline image macro image::tiger.png[].");
            var result = para.subMacros(para.getSource());
            refuteIncludes(result, "<img ");
            assertIncludes(result, "image::tiger.png[]");
        }

        @Test
        @DisplayName("should substitute attributes in target of inline image in section title")
        void shouldSubstituteAttributesInTargetOfInlineImageInSectionTitle() throws Exception {
            var input = "== image:{iconsdir}/dot.gif[dot] Title";
            try (var logger = usingMemoryLogger()) {
                var sect = blockFromString(input, Options.of("attributes", Options.of("data-uri", "", "iconsdir", "fixtures", "docdir", testdir()), "safe", "server", "catalog_assets", true));
                assertEquals(1, size(sect.getDocument().getCatalog().getImages()));
                assertEquals("fixtures/dot.gif", str(sect.getDocument().getCatalog().getImages().get(0)));
                assertNull(sect.getDocument().getCatalog().getImages().get(0).getImagesdir());
                assertEmpty(logger);
            }
        }

        @Test
        @DisplayName("an icon macro should be interpreted as an icon if icons are enabled")
        void anIconMacroShouldBeInterpretedAsAnIconIfIconsAreEnabled() throws Exception {
            var para = (Block) blockFromString("icon:github[]", Options.of("attributes", Options.of("icons", "")));
            assertEquals("<span class=\"icon\"><img src=\"./images/icons/github.png\" alt=\"github\"></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an icon macro should be interpreted as alt text if icons are disabled")
        void anIconMacroShouldBeInterpretedAsAltTextIfIconsAreDisabled() throws Exception {
            var para = (Block) blockFromString("icon:github[]");
            assertEquals("<span class=\"icon\">[github&#93;</span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should not mangle icon with link if icons are disabled")
        void shouldNotMangleIconWithLinkIfIconsAreDisabled() throws Exception {
            var para = (Block) blockFromString("icon:github[link=https://github.com]");
            assertEquals("<span class=\"icon\"><a class=\"image\" href=\"https://github.com\">[github&#93;</a></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should not mangle icon inside link if icons are disabled")
        void shouldNotMangleIconInsideLinkIfIconsAreDisabled() throws Exception {
            var para = (Block) blockFromString("https://github.com[icon:github[] GitHub]");
            assertEquals("<a href=\"https://github.com\"><span class=\"icon\">[github&#93;</span> GitHub</a>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an icon macro should output alt text if icons are disabled and alt is given")
        void anIconMacroShouldOutputAltTextIfIconsAreDisabledAndAltIsGiven() throws Exception {
            var para = (Block) blockFromString("icon:github[alt=\"GitHub\"]");
            assertEquals("<span class=\"icon\">[GitHub&#93;</span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an icon macro should be interpreted as a font-based icon when icons=font")
        void anIconMacroShouldBeInterpretedAsAFontBasedIconWhenIconsFont() throws Exception {
            var para = (Block) blockFromString("icon:github[]", Options.of("attributes", Options.of("icons", "font")));
            assertEquals("<span class=\"icon\"><i class=\"fa fa-github\"></i></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an icon macro with a size should be interpreted as a font-based icon with a size when icons=font")
        void anIconMacroWithASizeShouldBeInterpretedAsAFontBasedIconWithASizeWhenIconsFont() throws Exception {
            var para = (Block) blockFromString("icon:github[4x]", Options.of("attributes", Options.of("icons", "font")));
            assertEquals("<span class=\"icon\"><i class=\"fa fa-github fa-4x\"></i></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an icon macro with flip should be interpreted as a flipped font-based icon when icons=font")
        void anIconMacroWithFlipShouldBeInterpretedAsAFlippedFontBasedIconWhenIconsFont() throws Exception {
            var para = (Block) blockFromString("icon:shield[fw,flip=horizontal]", Options.of("attributes", Options.of("icons", "font")));
            assertEquals("<span class=\"icon\"><i class=\"fa fa-shield fa-fw fa-flip-horizontal\"></i></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an icon macro with rotate should be interpreted as a rotated font-based icon when icons=font")
        void anIconMacroWithRotateShouldBeInterpretedAsARotatedFontBasedIconWhenIconsFont() throws Exception {
            var para = (Block) blockFromString("icon:shield[fw,rotate=90]", Options.of("attributes", Options.of("icons", "font")));
            assertEquals("<span class=\"icon\"><i class=\"fa fa-shield fa-fw fa-rotate-90\"></i></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("an icon macro with a role and title should be interpreted as a font-based icon with a class and title when icons=font")
        void anIconMacroWithARoleAndTitleShouldBeInterpretedAsAFontBasedIconWithAClassAndTitleWhenIconsFont() throws Exception {
            var para = (Block) blockFromString("icon:heart[role=\"red\", title=\"Heart me\"]", Options.of("attributes", Options.of("icons", "font")));
            assertEquals("<span class=\"icon red\"><i class=\"fa fa-heart\" title=\"Heart me\"></i></span>", gsub(para.subMacros(para.getSource()), rx(">\\s+<"), "><"));
        }

        @Test
        @DisplayName("should use the imagesdir attribute on the node when resolving the icon path")
        void shouldUseTheImagesdirAttributeOnTheNodeWhenResolvingTheIconPath() throws Exception {
            var doc = emptyDocument(Options.of("attributes", Options.of("iconsdir", "assets/icons", "icons", "image")));
            var icon = new Inline(doc, "image", null, Options.of("type", "icon", "attributes", Options.of("iconsdir", "chapter-1/icons")));
            var iconUri = icon.iconUri("wave");
            assertEquals("chapter-1/icons/wave.png", iconUri);
        }

        @Test
        @DisplayName("a single-line footnote macro should be registered and output as a footnote")
        void aSingleLineFootnoteMacroShouldBeRegisteredAndOutputAsAFootnote() throws Exception {
            var para = (Block) blockFromString("Sentence text footnote:[An example footnote.].");
            assertEquals("Sentence text <sup class=\"footnote\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>.", para.subMacros(para.getSource()));
            assertEquals(1, size(para.getDocument().getCatalog().getFootnotes()));
            var footnote = first(para.getDocument().getCatalog().getFootnotes());
            assertEquals(1, footnote.getIndex());
            assertNull(footnote.getId());
            assertEquals("An example footnote.", footnote.getText());
        }

        @Test
        @DisplayName("a multi-line footnote macro should be registered and output as a footnote without newline")
        void aMultiLineFootnoteMacroShouldBeRegisteredAndOutputAsAFootnoteWithoutNewline() throws Exception {
            var para = (Block) blockFromString("Sentence text footnote:[An example footnote\nwith wrapped text.].");
            assertEquals("Sentence text <sup class=\"footnote\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>.", para.subMacros(para.getSource()));
            assertEquals(1, size(para.getDocument().getCatalog().getFootnotes()));
            var footnote = first(para.getDocument().getCatalog().getFootnotes());
            assertEquals(1, footnote.getIndex());
            assertNull(footnote.getId());
            assertEquals("An example footnote with wrapped text.", footnote.getText());
        }

        @Test
        @DisplayName("an escaped closing square bracket in a footnote should be unescaped when converted")
        void anEscapedClosingSquareBracketInAFootnoteShouldBeUnescapedWhenConverted() throws Exception {
            var para = (Block) blockFromString(("footnote:[a " + str(BACKSLASH) + "] b]."));
            assertEquals("<sup class=\"footnote\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>.", para.subMacros(para.getSource()));
            assertEquals(1, size(para.getDocument().getCatalog().getFootnotes()));
            var footnote = first(para.getDocument().getCatalog().getFootnotes());
            assertEquals("a ] b", footnote.getText());
        }

        @Test
        @DisplayName("a footnote macro can be directly adjacent to preceding word")
        void aFootnoteMacroCanBeDirectlyAdjacentToPrecedingWord() throws Exception {
            var para = (Block) blockFromString("Sentence textfootnote:[An example footnote.].");
            assertEquals("Sentence text<sup class=\"footnote\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>.", para.subMacros(para.getSource()));
        }

        @Test
        @DisplayName("a footnote macro may contain an escaped backslash")
        void aFootnoteMacroMayContainAnEscapedBackslash() throws Exception {
            var para = (Block) blockFromString("footnote:[\\]]\nfootnote:[a \\] b]\nfootnote:[a \\]\\] b]");
            para.subMacros(para.getSource());
            assertEquals(3, size(para.getDocument().getCatalog().getFootnotes()));
            var footnote1 = para.getDocument().getCatalog().getFootnotes().get(0);
            assertEquals("]", footnote1.getText());
            var footnote2 = para.getDocument().getCatalog().getFootnotes().get(1);
            assertEquals("a ] b", footnote2.getText());
            var footnote3 = para.getDocument().getCatalog().getFootnotes().get(2);
            assertEquals("a ]] b", footnote3.getText());
        }

        @Test
        @DisplayName("a footnote macro may contain a link macro")
        void aFootnoteMacroMayContainALinkMacro() throws Exception {
            var para = (Block) blockFromString("Share your code. footnote:[https://github.com[GitHub]]");
            assertEquals("Share your code. <sup class=\"footnote\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>", para.subMacros(para.getSource()));
            assertEquals(1, size(para.getDocument().getCatalog().getFootnotes()));
            var footnote1 = para.getDocument().getCatalog().getFootnotes().get(0);
            assertEquals("<a href=\"https://github.com\">GitHub</a>", footnote1.getText());
        }

        @Test
        @DisplayName("a footnote macro may contain a plain URL")
        void aFootnoteMacroMayContainAPlainURL() throws Exception {
            var para = (Block) blockFromString("the JLine footnote:[https://github.com/jline/jline2]\nlibrary.");
            var result = para.subMacros(para.getSource());
            assertEquals("the JLine <sup class=\"footnote\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>\nlibrary.", result);
            assertEquals(1, size(para.getDocument().getCatalog().getFootnotes()));
            var fn1 = first(para.getDocument().getCatalog().getFootnotes());
            assertEquals("<a href=\"https://github.com/jline/jline2\" class=\"bare\">https://github.com/jline/jline2</a>", fn1.getText());
        }

        @Test
        @DisplayName("a footnote macro followed by a semi-colon may contain a plain URL")
        void aFootnoteMacroFollowedByASemiColonMayContainAPlainURL() throws Exception {
            var para = (Block) blockFromString("the JLine footnote:[https://github.com/jline/jline2];\nlibrary.");
            var result = para.subMacros(para.getSource());
            assertEquals("the JLine <sup class=\"footnote\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>;\nlibrary.", result);
            assertEquals(1, size(para.getDocument().getCatalog().getFootnotes()));
            var fn1 = first(para.getDocument().getCatalog().getFootnotes());
            assertEquals("<a href=\"https://github.com/jline/jline2\" class=\"bare\">https://github.com/jline/jline2</a>", fn1.getText());
        }

        @Test
        @DisplayName("a footnote macro may contain text formatting")
        void aFootnoteMacroMayContainTextFormatting() throws Exception {
            var para = (Block) blockFromString("You can download patches from the product page.footnote:[Only available with an _active_ subscription.]");
            para.convert();
            var footnotes = para.getDocument().getCatalog().getFootnotes();
            assertEquals(1, size(footnotes));
            assertEquals("Only available with an <em>active</em> subscription.", footnotes.get(0).getText());
        }

        @Test
        @DisplayName("an externalized footnote macro may contain text formatting")
        void anExternalizedFootnoteMacroMayContainTextFormatting() throws Exception {
            var input = """
                    :fn-disclaimer: pass:q[footnote:[Only available with an _active_ subscription.]]

                    You can download patches from the production page.{fn-disclaimer}
                    """;
            var doc = documentFromString(input);
            doc.convert();
            var footnotes = doc.getCatalog().getFootnotes();
            assertEquals(1, size(footnotes));
            assertEquals("Only available with an <em>active</em> subscription.", footnotes.get(0).getText());
        }

        @Test
        @DisplayName("a footnote macro may contain a shorthand xref")
        void aFootnoteMacroMayContainAShorthandXref() throws Exception {
            var para = (Block) blockFromString("text footnote:[&lt;&lt;_install,install&gt;&gt;]");
            var doc = para.getDocument();
            doc.register("refs", list("_install", new Inline(doc, "anchor", "Install", Options.of("type", "ref", "target", "_install")), "Install"));
            var catalog = doc.getCatalog();
            assertEquals("text <sup class=\"footnote\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>", para.subMacros(para.getSource()));
            assertEquals(1, size(catalog.getFootnotes()));
            var footnote1 = catalog.getFootnotes().get(0);
            assertEquals("<a href=\"#_install\">install</a>", footnote1.getText());
        }

        @Test
        @DisplayName("a footnote macro may contain an xref macro")
        void aFootnoteMacroMayContainAnXrefMacro() throws Exception {
            var para = (Block) blockFromString("text footnote:[xref:_install[install]]");
            var doc = para.getDocument();
            doc.register("refs", list("_install", new Inline(doc, "anchor", "Install", Options.of("type", "ref", "target", "_install")), "Install"));
            var catalog = doc.getCatalog();
            assertEquals("text <sup class=\"footnote\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>", para.subMacros(para.getSource()));
            assertEquals(1, size(catalog.getFootnotes()));
            var footnote1 = catalog.getFootnotes().get(0);
            assertEquals("<a href=\"#_install\">install</a>", footnote1.getText());
        }

        @Test
        @DisplayName("a footnote macro may contain an anchor macro")
        void aFootnoteMacroMayContainAnAnchorMacro() throws Exception {
            var para = (Block) blockFromString("text footnote:[a [[b]] [[c\\]\\] d]");
            assertEquals("text <sup class=\"footnote\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>", para.subMacros(para.getSource()));
            assertEquals(1, size(para.getDocument().getCatalog().getFootnotes()));
            var footnote1 = para.getDocument().getCatalog().getFootnotes().get(0);
            assertEquals("a <a id=\"b\"></a> [[c]] d", footnote1.getText());
        }

        @Test
        @DisplayName("subsequent footnote macros with escaped URLs should be restored in DocBook")
        void subsequentFootnoteMacrosWithEscapedURLsShouldBeRestoredInDocBook() throws Exception {
            var input = "foofootnote:[+http://example.com+]barfootnote:[+http://acme.com+]baz";
            var result = convertStringToEmbedded(input, Options.of("doctype", "inline", "backend", "docbook"));
            assertEquals("foo<footnote><simpara>http://example.com</simpara></footnote>bar<footnote><simpara>http://acme.com</simpara></footnote>baz", result);
        }

        @Test
        @DisplayName("should increment index of subsequent footnote macros")
        void shouldIncrementIndexOfSubsequentFootnoteMacros() throws Exception {
            var para = (Block) blockFromString("Sentence text footnote:[An example footnote.]. Sentence text footnote:[Another footnote.].");
            assertEquals("Sentence text <sup class=\"footnote\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>. Sentence text <sup class=\"footnote\">[<a id=\"_footnoteref_2\" class=\"footnote\" href=\"#_footnotedef_2\" title=\"View footnote.\">2</a>]</sup>.", para.subMacros(para.getSource()));
            assertEquals(2, size(para.getDocument().getCatalog().getFootnotes()));
            var footnote1 = para.getDocument().getCatalog().getFootnotes().get(0);
            assertEquals(1, footnote1.getIndex());
            assertNull(footnote1.getId());
            assertEquals("An example footnote.", footnote1.getText());
            var footnote2 = para.getDocument().getCatalog().getFootnotes().get(1);
            assertEquals(2, footnote2.getIndex());
            assertNull(footnote2.getId());
            assertEquals("Another footnote.", footnote2.getText());
        }

        @Test
        @DisplayName("a footnoteref macro with id and single-line text should be registered and output as a footnote")
        void aFootnoterefMacroWithIdAndSingleLineTextShouldBeRegisteredAndOutputAsAFootnote() throws Exception {
            var para = (Block) blockFromString("Sentence text footnoteref:[ex1, An example footnote.].", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("Sentence text <sup class=\"footnote\" id=\"_footnote_ex1\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>.", para.subMacros(para.getSource()));
            assertEquals(1, size(para.getDocument().getCatalog().getFootnotes()));
            var footnote = first(para.getDocument().getCatalog().getFootnotes());
            assertEquals(1, footnote.getIndex());
            assertEquals("ex1", footnote.getId());
            assertEquals("An example footnote.", footnote.getText());
        }

        @Test
        @DisplayName("a footnoteref macro with id and multi-line text should be registered and output as a footnote without newlines")
        void aFootnoterefMacroWithIdAndMultiLineTextShouldBeRegisteredAndOutputAsAFootnoteWithoutNewlines() throws Exception {
            var para = (Block) blockFromString("Sentence text footnoteref:[ex1, An example footnote\nwith wrapped text.].", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("Sentence text <sup class=\"footnote\" id=\"_footnote_ex1\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>.", para.subMacros(para.getSource()));
            assertEquals(1, size(para.getDocument().getCatalog().getFootnotes()));
            var footnote = first(para.getDocument().getCatalog().getFootnotes());
            assertEquals(1, footnote.getIndex());
            assertEquals("ex1", footnote.getId());
            assertEquals("An example footnote with wrapped text.", footnote.getText());
        }

        @Test
        @DisplayName("a footnoteref macro with id should refer to footnoteref with same id")
        void aFootnoterefMacroWithIdShouldReferToFootnoterefWithSameId() throws Exception {
            var para = (Block) blockFromString("Sentence text footnoteref:[ex1, An example footnote.]. Sentence text footnoteref:[ex1].", Options.of("attributes", Options.of("compat-mode", "")));
            assertEquals("Sentence text <sup class=\"footnote\" id=\"_footnote_ex1\">[<a id=\"_footnoteref_1\" class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>. Sentence text <sup class=\"footnoteref\">[<a class=\"footnote\" href=\"#_footnotedef_1\" title=\"View footnote.\">1</a>]</sup>.", para.subMacros(para.getSource()));
            assertEquals(1, size(para.getDocument().getCatalog().getFootnotes()));
            var footnote = first(para.getDocument().getCatalog().getFootnotes());
            assertEquals(1, footnote.getIndex());
            assertEquals("ex1", footnote.getId());
            assertEquals("An example footnote.", footnote.getText());
        }

        @Test
        @DisplayName("an unresolved footnote reference should produce a warning message and output fallback text in red")
        void anUnresolvedFootnoteReferenceShouldProduceAWarningMessageAndOutputFallbackTextInRed() throws Exception {
            var input = "Sentence text.footnote:ex1[]";
            try (var logger = usingMemoryLogger()) {
                var para = (Block) blockFromString(input);
                var output = para.subMacros(para.getSource());
                assertEquals("Sentence text.<sup class=\"footnoteref red\" title=\"Unresolved footnote reference.\">[ex1]</sup>", output);
                assertMessage(logger, "WARN", "invalid footnote reference: ex1");
            }
        }

        @Test
        @DisplayName("using a footnoteref macro should generate a warning when compat mode is not enabled")
        void usingAFootnoterefMacroShouldGenerateAWarningWhenCompatModeIsNotEnabled() throws Exception {
            var input = "Sentence text.footnoteref:[fn1,Commentary on this sentence.]";
            try (var logger = usingMemoryLogger()) {
                var para = (Block) blockFromString(input);
                para.subMacros(para.getSource());
                assertMessage(logger, "WARN", "found deprecated footnoteref macro: footnoteref:[fn1,Commentary on this sentence.]; use footnote macro with target instead");
            }
        }

        @Test
        @DisplayName("inline footnote macro can be used to define and reference a footnote reference")
        void inlineFootnoteMacroCanBeUsedToDefineAndReferenceAFootnoteReference() throws Exception {
            var input = """
                    You can download the software from the product page.footnote:sub[Option only available if you have an active subscription.]

                    You can also file a support request.footnote:sub[]

                    If all else fails, you can give us a call.footnoteref:[sub]
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("compat-mode", "")));
                assertCss("#_footnotedef_1", output, 1);
                assertCss("p a[href=\"#_footnotedef_1\"]", output, 3);
                assertCss("#footnotes .footnote", output, 1);
                assertEmpty(logger);
            }
        }

        @Test
        @DisplayName("should parse multiple footnote references in a single line")
        void shouldParseMultipleFootnoteReferencesInASingleLine() throws Exception {
            var input = "notable text.footnote:id[about this [text\\]], footnote:id[], footnote:id[]";
            var output = convertStringToEmbedded(input);
            assertXpath("(//p)[1]/sup[starts-with(@class,\"footnote\")]", output, 3);
            assertXpath("(//p)[1]/sup[@class=\"footnote\"]", output, 1);
            assertXpath("(//p)[1]/sup[@class=\"footnoteref\"]", output, 2);
            assertXpath("(//p)[1]/sup[starts-with(@class,\"footnote\")]/a[@class=\"footnote\"][text()=\"1\"]", output, 3);
            assertCss("#footnotes .footnote", output, 1);
        }

        @Test
        @DisplayName("should not register footnote with id and text if id already registered")
        void shouldNotRegisterFootnoteWithIdAndTextIfIdAlreadyRegistered() throws Exception {
            var input = """
                    :fn-notable-text: footnote:id[about this text]

                    notable text.{fn-notable-text}

                    more notable text.{fn-notable-text}
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("(//p)[1]/sup[@class=\"footnote\"]", output, 1);
            assertXpath("(//p)[2]/sup[@class=\"footnoteref\"]", output, 1);
            assertCss("#footnotes .footnote", output, 1);
        }

        @Test
        @DisplayName("should not resolve an inline footnote macro missing both id and text")
        void shouldNotResolveAnInlineFootnoteMacroMissingBothIdAndText() throws Exception {
            var input = """
                    The footnote:[] macro can be used for defining and referencing footnotes.

                    The footnoteref:[] macro is now deprecated.
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "The footnote:[] macro");
            assertIncludes(output, "The footnoteref:[] macro");
        }

        @Test
        @DisplayName("inline footnote macro can define a numeric id without conflicting with auto-generated ID")
        void inlineFootnoteMacroCanDefineANumericIdWithoutConflictingWithAutoGeneratedID() throws Exception {
            var input = "You can download the software from the product page.footnote:1[Option only available if you have an active subscription.]";
            var output = convertStringToEmbedded(input);
            assertCss("#_footnote_1", output, 1);
            assertCss("p sup#_footnote_1", output, 1);
            assertCss("p a#_footnoteref_1", output, 1);
            assertCss("p a[href=\"#_footnotedef_1\"]", output, 1);
            assertCss("#footnotes #_footnotedef_1", output, 1);
        }

        @Test
        @DisplayName("inline footnote macro can define an id that uses any word characters in Unicode")
        void inlineFootnoteMacroCanDefineAnIdThatUsesAnyWordCharactersInUnicode() throws Exception {
            var input = """
                    L'origine du mot forêt{blank}footnote:forêt[un massif forestier] est complexe.

                    Qu'est-ce qu'une forêt ?{blank}footnote:forêt[]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("#_footnote_forêt", output, 1);
            assertCss("#_footnotedef_1", output, 1);
            assertXpath("//a[@class=\"footnote\"][text()=\"1\"]", output, 2);
        }

        @Test
        @DisplayName("should be able to reference a bibliography entry in a footnote")
        void shouldBeAbleToReferenceABibliographyEntryInAFootnote() throws Exception {
            var input = """
                    Choose a design pattern.footnote:[See <<gof>> to find a collection of design patterns.]

                    [bibliography]
                    == Bibliography

                    * [[[gof]]] Erich Gamma, et al. _Design Patterns: Elements of Reusable Object-Oriented Software._ Addison-Wesley. 1994.
                    """;
            var result = convertStringToEmbedded(input);
            assertInclude("<a href=\"#_footnoteref_1\">1</a>. See <a href=\"#gof\">[gof]</a> to find a collection of design patterns.", result);
        }

        @Test
        @DisplayName("footnotes in headings are expected to be numbered out of sequence")
        void footnotesInHeadingsAreExpectedToBeNumberedOutOfSequence() throws Exception {
            var input = """
                    == Section 1

                    para.footnote:[first footnote]

                    == Section 2footnote:[second footnote]

                    para.footnote:[third footnote]
                    """;
            var result = convertStringToEmbedded(input);
            var footnoteRefs = xmlnodesAtCss("a.footnote", result);
            var footnoteDefs = xmlnodesAtCss("div.footnote", result);
            assertEquals(3, size(footnoteRefs));
            assertEquals(list("1", "1", "2"), map(footnoteRefs, it -> send(it, "text")));
            assertEquals(3, size(footnoteDefs));
            assertEquals(list("1. second footnote", "1. first footnote", "2. third footnote"), map(map(footnoteDefs, it -> send(it, "text")), (String it) -> Strings.strip(it)));
        }

        @Test
        @DisplayName("a single-line index term macro with a primary term should be registered as an index reference")
        void aSingleLineIndexTermMacroWithAPrimaryTermShouldBeRegisteredAsAnIndexReference() throws Exception {
            var sentence = "The tiger (Panthera tigris) is the largest cat species.\n";
            var macros = list("indexterm:[Tigers]", "(((Tigers)))");
            for (var macro : macros) {
                var para = (Block) blockFromString((str(sentence) + str(macro)));
                var output = para.subMacros(para.getSource());
                assertEquals(sentence, output);
            }
        }

        @Test
        @DisplayName("a single-line index term macro with primary and secondary terms should be registered as an index reference")
        void aSingleLineIndexTermMacroWithPrimaryAndSecondaryTermsShouldBeRegisteredAsAnIndexReference() throws Exception {
            var sentence = "The tiger (Panthera tigris) is the largest cat species.\n";
            var macros = list("indexterm:[Big cats, Tigers]", "(((Big cats, Tigers)))");
            for (var macro : macros) {
                var para = (Block) blockFromString((str(sentence) + str(macro)));
                var output = para.subMacros(para.getSource());
                assertEquals(sentence, output);
            }
        }

        @Test
        @DisplayName("a single-line index term macro with primary, secondary and tertiary terms should be registered as an index reference")
        void aSingleLineIndexTermMacroWithPrimarySecondaryAndTertiaryTermsShouldBeRegisteredAsAnIndexReference() throws Exception {
            var sentence = "The tiger (Panthera tigris) is the largest cat species.\n";
            var macros = list("indexterm:[Big cats,Tigers , Panthera tigris]", "(((Big cats,Tigers , Panthera tigris)))");
            for (var macro : macros) {
                var para = (Block) blockFromString((str(sentence) + str(macro)));
                var output = para.subMacros(para.getSource());
                assertEquals(sentence, output);
            }
        }

        @Test
        @DisplayName("a multi-line index term macro should be compacted and registered as an index reference")
        void aMultiLineIndexTermMacroShouldBeCompactedAndRegisteredAsAnIndexReference() throws Exception {
            var sentence = "The tiger (Panthera tigris) is the largest cat species.\n";
            var macros = list("indexterm:[Panthera\ntigris]", "(((Panthera\ntigris)))");
            for (var macro : macros) {
                var para = (Block) blockFromString((str(sentence) + str(macro)));
                var output = para.subMacros(para.getSource());
                assertEquals(sentence, output);
            }
        }

        @Test
        @DisplayName("should escape concealed index term if second bracket is preceded by a backslash")
        void shouldEscapeConcealedIndexTermIfSecondBracketIsPrecededByABackslash() throws Exception {
            var input = ("National Institute of Science and Technology (" + str(BACKSLASH) + "((NIST)))");
            var doc = documentFromString(input, Options.of("standalone", false));
            var output = doc.convert();
            assertXpath("//p[text()=\"National Institute of Science and Technology (((NIST)))\"]", output, 1);
        }

        @Test
        @DisplayName("should only escape enclosing brackets if concealed index term is preceded by a backslash")
        void shouldOnlyEscapeEnclosingBracketsIfConcealedIndexTermIsPrecededByABackslash() throws Exception {
            var input = ("National Institute of Science and Technology " + str(BACKSLASH) + "(((NIST)))");
            var doc = documentFromString(input, Options.of("standalone", false));
            var output = doc.convert();
            assertXpath("//p[text()=\"National Institute of Science and Technology (NIST)\"]", output, 1);
        }

        @Test
        @DisplayName("should not split index terms on commas inside of quoted terms")
        void shouldNotSplitIndexTermsOnCommasInsideOfQuotedTerms() throws Exception {
            var inputs = new ArrayList<String>();
            inputs.add("""
                    Tigers are big, scary cats.
                    indexterm:[Tigers, "[Big\\],
                    scary cats"]
                    """);
            inputs.add("""
                    Tigers are big, scary cats.
                    (((Tigers, "[Big],
                    scary cats")))
                    """);
            for (var input : inputs) {
                var para = (Block) blockFromString(input);
                var output = para.subMacros(para.getSource());
                assertEquals(first(lines(input)), output);
            }
        }

        @Test
        @DisplayName("normal substitutions are performed on an index term macro")
        void normalSubstitutionsArePerformedOnAnIndexTermMacro() throws Exception {
            var sentence = "The tiger (Panthera tigris) is the largest cat species.\n";
            var macros = list("indexterm:[*Tigers*]", "(((*Tigers*)))");
            for (var macro : macros) {
                var para = (Block) blockFromString((str(sentence) + str(macro)));
                var output = para.applySubs(para.getSource());
                assertEquals(sentence, output);
            }
        }

        @Test
        @DisplayName("registers multiple index term macros")
        void registersMultipleIndexTermMacros() throws Exception {
            var sentence = "The tiger (Panthera tigris) is the largest cat species.";
            var macros = "(((Tigers)))\n(((Animals,Cats)))";
            var para = (Block) blockFromString((str(sentence) + "\n" + str(macros)));
            var output = para.subMacros(para.getSource());
            assertEquals(sentence, Strings.rstrip(output));
        }

        @Test
        @DisplayName("an index term macro with round bracket syntax may contain round brackets in term")
        void anIndexTermMacroWithRoundBracketSyntaxMayContainRoundBracketsInTerm() throws Exception {
            var sentence = "The tiger (Panthera tigris) is the largest cat species.\n";
            var macro = "(((Tiger (Panthera tigris))))";
            var para = (Block) blockFromString((str(sentence) + str(macro)));
            var output = para.subMacros(para.getSource());
            assertEquals(sentence, output);
        }

        @Test
        @DisplayName("visible shorthand index term macro should not consume trailing round bracket")
        void visibleShorthandIndexTermMacroShouldNotConsumeTrailingRoundBracket() throws Exception {
            var input = "(text with ((index term)))";
            var expected = Strings.chop("""
                    (text with <indexterm>
                    <primary>index term</primary>
                    </indexterm>index term)
                    """);
            var para = (Block) blockFromString(input, Options.of("backend", "docbook"));
            var output = para.subMacros(para.getSource());
            assertEquals(expected, output);
        }

        @Test
        @DisplayName("visible shorthand index term macro should not consume leading round bracket")
        void visibleShorthandIndexTermMacroShouldNotConsumeLeadingRoundBracket() throws Exception {
            var input = "(((index term)) for text)";
            var expected = Strings.chop("""
                    (<indexterm>
                    <primary>index term</primary>
                    </indexterm>index term for text)
                    """);
            var para = (Block) blockFromString(input, Options.of("backend", "docbook"));
            var output = para.subMacros(para.getSource());
            assertEquals(expected, output);
        }

        @Test
        @DisplayName("an index term macro with square bracket syntax may contain square brackets in term")
        void anIndexTermMacroWithSquareBracketSyntaxMayContainSquareBracketsInTerm() throws Exception {
            var sentence = "The tiger (Panthera tigris) is the largest cat species.\n";
            var macro = "indexterm:[Tiger [Panthera tigris\\]]";
            var para = (Block) blockFromString((str(sentence) + str(macro)));
            var output = para.subMacros(para.getSource());
            assertEquals(sentence, output);
        }

        @Test
        @DisplayName("a single-line index term 2 macro should be registered as an index reference and retain term inline")
        void aSingleLineIndexTerm2MacroShouldBeRegisteredAsAnIndexReferenceAndRetainTermInline() throws Exception {
            var sentence = "The tiger (Panthera tigris) is the largest cat species.";
            var macros = list("The indexterm2:[tiger] (Panthera tigris) is the largest cat species.", "The ((tiger)) (Panthera tigris) is the largest cat species.");
            for (var macro : macros) {
                var para = (Block) blockFromString(macro);
                var output = para.subMacros(para.getSource());
                assertEquals(sentence, output);
            }
        }

        @Test
        @DisplayName("a multi-line index term 2 macro should be compacted and registered as an index reference and retain term inline")
        void aMultiLineIndexTerm2MacroShouldBeCompactedAndRegisteredAsAnIndexReferenceAndRetainTermInline() throws Exception {
            var sentence = "The panthera tigris is the largest cat species.";
            var macros = list("The indexterm2:[ panthera\ntigris ] is the largest cat species.", "The (( panthera\ntigris )) is the largest cat species.");
            for (var macro : macros) {
                var para = (Block) blockFromString(macro);
                var output = para.subMacros(para.getSource());
                assertEquals(sentence, output);
            }
        }

        @Test
        @DisplayName("registers multiple index term 2 macros")
        void registersMultipleIndexTerm2Macros() throws Exception {
            var sentence = "The ((tiger)) (Panthera tigris) is the largest ((cat)) species.";
            var para = (Block) blockFromString(sentence);
            var output = para.subMacros(para.getSource());
            assertEquals("The tiger (Panthera tigris) is the largest cat species.", output);
        }

        @Test
        @DisplayName("should escape visible index term if preceded by a backslash")
        void shouldEscapeVisibleIndexTermIfPrecededByABackslash() throws Exception {
            var sentence = ("The " + str(BACKSLASH) + "((tiger)) (Panthera tigris) is the largest " + str(BACKSLASH) + "((cat)) species.");
            var para = (Block) blockFromString(sentence);
            var output = para.subMacros(para.getSource());
            assertEquals("The ((tiger)) (Panthera tigris) is the largest ((cat)) species.", output);
        }

        @Test
        @DisplayName("normal substitutions are performed on an index term 2 macro")
        void normalSubstitutionsArePerformedOnAnIndexTerm2Macro() throws Exception {
            var sentence = "The ((*tiger*)) (Panthera tigris) is the largest cat species.";
            var para = (Block) blockFromString(sentence);
            var output = para.applySubs(para.getSource());
            assertEquals("The <strong>tiger</strong> (Panthera tigris) is the largest cat species.", output);
        }

        @Test
        @DisplayName("index term 2 macro with round bracket syntax should not interfere with index term macro with round bracket syntax")
        void indexTerm2MacroWithRoundBracketSyntaxShouldNotInterfereWithIndexTermMacroWithRoundBracketSyntax() throws Exception {
            var sentence = "The ((panthera tigris)) is the largest cat species.\n(((Big cats,Tigers)))";
            var para = (Block) blockFromString(sentence);
            var output = para.subMacros(para.getSource());
            assertEquals("The panthera tigris is the largest cat species.\n", output);
        }

        @Test
        @DisplayName("should parse visible shorthand index term with see and seealso")
        void shouldParseVisibleShorthandIndexTermWithSeeAndSeealso() throws Exception {
            var sentence = "((Flash >> HTML 5)) has been supplanted by ((HTML 5 &> CSS 3 &> SVG)).";
            var output = convertStringToEmbedded(sentence, Options.of("backend", "docbook"));
            var indextermFlash = Strings.chop("""
                    <indexterm>
                    <primary>Flash</primary>
                    <see>HTML 5</see>
                    </indexterm>
                    """);
            var indextermHtml5 = Strings.chop("""
                    <indexterm>
                    <primary>HTML 5</primary>
                    <seealso>CSS 3</seealso>
                    <seealso>SVG</seealso>
                    </indexterm>
                    """);
            assertIncludes(output, indextermFlash);
            assertIncludes(output, indextermHtml5);
        }

        @Test
        @DisplayName("should parse concealed shorthand index term with see and seealso")
        void shouldParseConcealedShorthandIndexTermWithSeeAndSeealso() throws Exception {
            var sentence = "Flash(((Flash >> HTML 5))) has been supplanted by HTML 5(((HTML 5 &> CSS 3 &> SVG))).";
            var output = convertStringToEmbedded(sentence, Options.of("backend", "docbook"));
            var indextermFlash = Strings.chop("""
                    <indexterm>
                    <primary>Flash</primary>
                    <see>HTML 5</see>
                    </indexterm>
                    """);
            var indextermHtml5 = Strings.chop("""
                    <indexterm>
                    <primary>HTML 5</primary>
                    <seealso>CSS 3</seealso>
                    <seealso>SVG</seealso>
                    </indexterm>
                    """);
            assertIncludes(output, indextermFlash);
            assertIncludes(output, indextermHtml5);
        }

        @Test
        @DisplayName("should parse visible index term macro with see and seealso")
        void shouldParseVisibleIndexTermMacroWithSeeAndSeealso() throws Exception {
            var sentence = "indexterm2:[Flash,see=HTML 5] has been supplanted by indexterm2:[HTML 5,see-also=\"CSS 3, SVG\"].";
            var output = convertStringToEmbedded(sentence, Options.of("backend", "docbook"));
            var indextermFlash = Strings.chop("""
                    <indexterm>
                    <primary>Flash</primary>
                    <see>HTML 5</see>
                    </indexterm>
                    """);
            var indextermHtml5 = Strings.chop("""
                    <indexterm>
                    <primary>HTML 5</primary>
                    <seealso>CSS 3</seealso>
                    <seealso>SVG</seealso>
                    </indexterm>
                    """);
            assertIncludes(output, indextermFlash);
            assertIncludes(output, indextermHtml5);
        }

        @Test
        @DisplayName("should parse concealed index term macro with see and seealso")
        void shouldParseConcealedIndexTermMacroWithSeeAndSeealso() throws Exception {
            var sentence = "Flashindexterm:[Flash,see=HTML 5] has been supplanted by HTML 5indexterm:[HTML 5,see-also=\"CSS 3, SVG\"].";
            var output = convertStringToEmbedded(sentence, Options.of("backend", "docbook"));
            var indextermFlash = Strings.chop("""
                    <indexterm>
                    <primary>Flash</primary>
                    <see>HTML 5</see>
                    </indexterm>
                    """);
            var indextermHtml5 = Strings.chop("""
                    <indexterm>
                    <primary>HTML 5</primary>
                    <seealso>CSS 3</seealso>
                    <seealso>SVG</seealso>
                    </indexterm>
                    """);
            assertIncludes(output, indextermFlash);
            assertIncludes(output, indextermHtml5);
        }

        @Test
        @DisplayName("should honor secondary and tertiary index terms when primary index term is quoted and contains equals sign")
        void shouldHonorSecondaryAndTertiaryIndexTermsWhenPrimaryIndexTermIsQuotedAndContainsEqualsSign() throws Exception {
            var sentence = "Assigning variables.";
            var expected = (str(sentence) + "<indexterm><primary>name=value</primary><secondary>variable</secondary><tertiary>assignment</tertiary></indexterm>");
            var macros = list("indexterm:[\"name=value\",variable,assignment]", "(((name=value,variable,assignment)))");
            for (var macro : macros) {
                var para = (Block) blockFromString((str(sentence) + str(macro)), Options.of("backend", "docbook"));
                var output = Strings.tr(para.subMacros(para.getSource()), "\n", "");
                assertEquals(expected, output);
            }
        }

        @Nested
        @DisplayName("Button macro")
        class ButtonMacro {

            @Test
            @DisplayName("btn macro")
            void btnMacro() throws Exception {
                var para = (Block) blockFromString("btn:[Save]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<b class=\"button\">Save</b>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("btn macro that spans multiple lines")
            void btnMacroThatSpansMultipleLines() throws Exception {
                var para = (Block) blockFromString("btn:[Rebase and\nmerge]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<b class=\"button\">Rebase and merge</b>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("btn macro for docbook backend")
            void btnMacroForDocbookBackend() throws Exception {
                var para = (Block) blockFromString("btn:[Save]", Options.of("backend", "docbook", "attributes", Options.of("experimental", "")));
                assertEquals("<guibutton>Save</guibutton>", para.subMacros(para.getSource()));
            }
        }

        @Nested
        @DisplayName("Keyboard macro")
        class KeyboardMacro {

            @Test
            @DisplayName("kbd macro with single key")
            void kbdMacroWithSingleKey() throws Exception {
                var para = (Block) blockFromString("kbd:[F3]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<kbd>F3</kbd>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with single backslash key")
            void kbdMacroWithSingleBackslashKey() throws Exception {
                var para = (Block) blockFromString(("kbd:[" + str(BACKSLASH) + " ]"), Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<kbd>\\</kbd>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with single key, docbook backend")
            void kbdMacroWithSingleKeyDocbookBackend() throws Exception {
                var para = (Block) blockFromString("kbd:[F3]", Options.of("backend", "docbook", "attributes", Options.of("experimental", "")));
                assertEquals("<keycap>F3</keycap>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination")
            void kbdMacroWithKeyCombination() throws Exception {
                var para = (Block) blockFromString("kbd:[Ctrl+Shift+T]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"keyseq\"><kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>T</kbd></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination that spans multiple lines")
            void kbdMacroWithKeyCombinationThatSpansMultipleLines() throws Exception {
                var para = (Block) blockFromString("kbd:[Ctrl +\nT]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"keyseq\"><kbd>Ctrl</kbd>+<kbd>T</kbd></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination, docbook backend")
            void kbdMacroWithKeyCombinationDocbookBackend() throws Exception {
                var para = (Block) blockFromString("kbd:[Ctrl+Shift+T]", Options.of("backend", "docbook", "attributes", Options.of("experimental", "")));
                assertEquals("<keycombo><keycap>Ctrl</keycap><keycap>Shift</keycap><keycap>T</keycap></keycombo>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination delimited by pluses with spaces")
            void kbdMacroWithKeyCombinationDelimitedByPlusesWithSpaces() throws Exception {
                var para = (Block) blockFromString("kbd:[Ctrl + Shift + T]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"keyseq\"><kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>T</kbd></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination delimited by commas")
            void kbdMacroWithKeyCombinationDelimitedByCommas() throws Exception {
                var para = (Block) blockFromString("kbd:[Ctrl,Shift,T]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"keyseq\"><kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>T</kbd></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination delimited by commas with spaces")
            void kbdMacroWithKeyCombinationDelimitedByCommasWithSpaces() throws Exception {
                var para = (Block) blockFromString("kbd:[Ctrl, Shift, T]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"keyseq\"><kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>T</kbd></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination delimited by plus containing a comma key")
            void kbdMacroWithKeyCombinationDelimitedByPlusContainingACommaKey() throws Exception {
                var para = (Block) blockFromString("kbd:[Ctrl+,]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"keyseq\"><kbd>Ctrl</kbd>+<kbd>,</kbd></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination delimited by commas containing a plus key")
            void kbdMacroWithKeyCombinationDelimitedByCommasContainingAPlusKey() throws Exception {
                var para = (Block) blockFromString("kbd:[Ctrl, +, Shift]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"keyseq\"><kbd>Ctrl</kbd>+<kbd>+</kbd>+<kbd>Shift</kbd></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination where last key matches plus delimiter")
            void kbdMacroWithKeyCombinationWhereLastKeyMatchesPlusDelimiter() throws Exception {
                var para = (Block) blockFromString("kbd:[Ctrl + +]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"keyseq\"><kbd>Ctrl</kbd>+<kbd>+</kbd></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination where last key matches comma delimiter")
            void kbdMacroWithKeyCombinationWhereLastKeyMatchesCommaDelimiter() throws Exception {
                var para = (Block) blockFromString("kbd:[Ctrl, ,]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"keyseq\"><kbd>Ctrl</kbd>+<kbd>,</kbd></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination containing escaped bracket")
            void kbdMacroWithKeyCombinationContainingEscapedBracket() throws Exception {
                var para = (Block) blockFromString("kbd:[Ctrl + \\]]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"keyseq\"><kbd>Ctrl</kbd>+<kbd>]</kbd></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro with key combination ending in backslash")
            void kbdMacroWithKeyCombinationEndingInBackslash() throws Exception {
                var para = (Block) blockFromString(("kbd:[Ctrl + " + str(BACKSLASH) + " ]"), Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"keyseq\"><kbd>Ctrl</kbd>+<kbd>\\</kbd></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro looks for delimiter beyond first character")
            void kbdMacroLooksForDelimiterBeyondFirstCharacter() throws Exception {
                var para = (Block) blockFromString("kbd:[,te]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<kbd>,te</kbd>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("kbd macro restores trailing delimiter as key value")
            void kbdMacroRestoresTrailingDelimiterAsKeyValue() throws Exception {
                var para = (Block) blockFromString("kbd:[te,]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<kbd>te,</kbd>", para.subMacros(para.getSource()));
            }
        }

        @Nested
        @DisplayName("Menu macro")
        class MenuMacro {

            @Test
            @DisplayName("should process menu using macro sytnax")
            void shouldProcessMenuUsingMacroSytnax() throws Exception {
                var para = (Block) blockFromString("menu:File[]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<b class=\"menuref\">File</b>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process menu for docbook backend")
            void shouldProcessMenuForDocbookBackend() throws Exception {
                var para = (Block) blockFromString("menu:File[]", Options.of("backend", "docbook", "attributes", Options.of("experimental", "")));
                assertEquals("<guimenu>File</guimenu>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process multiple menu macros in same line")
            void shouldProcessMultipleMenuMacrosInSameLine() throws Exception {
                var para = (Block) blockFromString("menu:File[] and menu:Edit[]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<b class=\"menuref\">File</b> and <b class=\"menuref\">Edit</b>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process menu with menu item using macro syntax")
            void shouldProcessMenuWithMenuItemUsingMacroSyntax() throws Exception {
                var para = (Block) blockFromString("menu:File[Save As&#8230;]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">File</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Save As&#8230;</b></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process menu macro that spans multiple lines")
            void shouldProcessMenuMacroThatSpansMultipleLines() throws Exception {
                var input = "menu:Preferences[Compile\non\nSave]";
                var para = (Block) blockFromString(input, Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">Preferences</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Compile\non\nSave</b></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should unescape escaped closing bracket in menu macro")
            void shouldUnescapeEscapedClosingBracketInMenuMacro() throws Exception {
                var input = "menu:Preferences[Compile [on\\] Save]";
                var para = (Block) blockFromString(input, Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">Preferences</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Compile [on] Save</b></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process menu with menu item using macro syntax when fonts icons are enabled")
            void shouldProcessMenuWithMenuItemUsingMacroSyntaxWhenFontsIconsAreEnabled() throws Exception {
                var para = (Block) blockFromString("menu:Tools[More Tools &gt; Extensions]", Options.of("attributes", Options.of("experimental", "", "icons", "font")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">Tools</b>&#160;<i class=\"fa fa-angle-right caret\"></i> <b class=\"submenu\">More Tools</b>&#160;<i class=\"fa fa-angle-right caret\"></i> <b class=\"menuitem\">Extensions</b></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process menu with menu item for docbook backend")
            void shouldProcessMenuWithMenuItemForDocbookBackend() throws Exception {
                var para = (Block) blockFromString("menu:File[Save As&#8230;]", Options.of("backend", "docbook", "attributes", Options.of("experimental", "")));
                assertEquals("<menuchoice><guimenu>File</guimenu> <guimenuitem>Save As&#8230;</guimenuitem></menuchoice>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process menu with menu item in submenu using macro syntax")
            void shouldProcessMenuWithMenuItemInSubmenuUsingMacroSyntax() throws Exception {
                var para = (Block) blockFromString("menu:Tools[Project &gt; Build]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">Tools</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"submenu\">Project</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Build</b></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process menu with menu item in submenu for docbook backend")
            void shouldProcessMenuWithMenuItemInSubmenuForDocbookBackend() throws Exception {
                var para = (Block) blockFromString("menu:Tools[Project &gt; Build]", Options.of("backend", "docbook", "attributes", Options.of("experimental", "")));
                assertEquals("<menuchoice><guimenu>Tools</guimenu> <guisubmenu>Project</guisubmenu> <guimenuitem>Build</guimenuitem></menuchoice>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process menu with menu item in submenu using macro syntax and comma delimiter")
            void shouldProcessMenuWithMenuItemInSubmenuUsingMacroSyntaxAndCommaDelimiter() throws Exception {
                var para = (Block) blockFromString("menu:Tools[Project, Build]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">Tools</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"submenu\">Project</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Build</b></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process menu with menu item using inline syntax")
            void shouldProcessMenuWithMenuItemUsingInlineSyntax() throws Exception {
                var para = (Block) blockFromString("\"File &gt; Save As&#8230;\"", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">File</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Save As&#8230;</b></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process menu with menu item in submenu using inline syntax")
            void shouldProcessMenuWithMenuItemInSubmenuUsingInlineSyntax() throws Exception {
                var para = (Block) blockFromString("\"Tools &gt; Project &gt; Build\"", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">Tools</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"submenu\">Project</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Build</b></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("inline menu syntax should not match closing quote of XML attribute")
            void inlineMenuSyntaxShouldNotMatchClosingQuoteOfXMLAttribute() throws Exception {
                var para = (Block) blockFromString("<span class=\"xmltag\">&lt;node&gt;</span><span class=\"classname\">r</span>", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"xmltag\">&lt;node&gt;</span><span class=\"classname\">r</span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process menu macro with items containing multibyte characters")
            void shouldProcessMenuMacroWithItemsContainingMultibyteCharacters() throws Exception {
                var para = (Block) blockFromString("menu:视图[放大, 重置]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">视图</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"submenu\">放大</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">重置</b></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process inline menu with items containing multibyte characters")
            void shouldProcessInlineMenuWithItemsContainingMultibyteCharacters() throws Exception {
                var para = (Block) blockFromString("\"视图 &gt; 放大 &gt; 重置\"", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">视图</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"submenu\">放大</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">重置</b></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should process a menu macro with a target that begins with a character reference")
            void shouldProcessAMenuMacroWithATargetThatBeginsWithACharacterReference() throws Exception {
                var para = (Block) blockFromString("menu:&#8942;[More Tools, Extensions]", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">&#8942;</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"submenu\">More Tools</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Extensions</b></span>", para.subMacros(para.getSource()));
            }

            @Test
            @DisplayName("should not process a menu macro with a target that ends with a space")
            void shouldNotProcessAMenuMacroWithATargetThatEndsWithASpace() throws Exception {
                var input = "menu:foo [bar] menu:File[Save]";
                var para = (Block) blockFromString(input, Options.of("attributes", Options.of("experimental", "")));
                var result = para.subMacros(para.getSource());
                assertXpath("/span[@class=\"menuseq\"]", result, 1);
                assertXpath("//b[@class=\"menu\"][text()=\"File\"]", result, 1);
            }

            @Test
            @DisplayName("should process an inline menu that begins with a character reference")
            void shouldProcessAnInlineMenuThatBeginsWithACharacterReference() throws Exception {
                var para = (Block) blockFromString("\"&#8942; &gt; More Tools &gt; Extensions\"", Options.of("attributes", Options.of("experimental", "")));
                assertEquals("<span class=\"menuseq\"><b class=\"menu\">&#8942;</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"submenu\">More Tools</b>&#160;<b class=\"caret\">&#8250;</b> <b class=\"menuitem\">Extensions</b></span>", para.subMacros(para.getSource()));
            }
        }
    }

    @Nested
    @DisplayName("Passthroughs")
    class Passthroughs {

        @Test
        @DisplayName("collect inline triple plus passthroughs")
        void collectInlineTriplePlusPassthroughs() throws Exception {
            var para = (Block) blockFromString("+++<code>inline code</code>+++");
            var result = para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(((Substitutors.PASS_START + "0") + Substitutors.PASS_END), result);
            assertEquals(1, size(passthroughs));
            assertEquals("<code>inline code</code>", get(passthroughs.get(0), "text"));
            assertEmpty(get(passthroughs.get(0), "subs"));
        }

        @Test
        @DisplayName("collect multi-line inline triple plus passthroughs")
        void collectMultiLineInlineTriplePlusPassthroughs() throws Exception {
            var para = (Block) blockFromString("+++<code>inline\ncode</code>+++");
            var result = para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(((Substitutors.PASS_START + "0") + Substitutors.PASS_END), result);
            assertEquals(1, size(passthroughs));
            assertEquals("<code>inline\ncode</code>", get(passthroughs.get(0), "text"));
            assertEmpty(get(passthroughs.get(0), "subs"));
        }

        @Test
        @DisplayName("collect inline double dollar passthroughs")
        void collectInlineDoubleDollarPassthroughs() throws Exception {
            var para = (Block) blockFromString("$$<code>{code}</code>$$");
            var result = para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(((Substitutors.PASS_START + "0") + Substitutors.PASS_END), result);
            assertEquals(1, size(passthroughs));
            assertEquals("<code>{code}</code>", get(passthroughs.get(0), "text"));
            assertEquals(list(Substitution.SPECIAL_CHARACTERS), get(passthroughs.get(0), "subs"));
        }

        @Test
        @DisplayName("collect inline double plus passthroughs")
        void collectInlineDoublePlusPassthroughs() throws Exception {
            var para = (Block) blockFromString("++<code>{code}</code>++");
            var result = para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(((Substitutors.PASS_START + "0") + Substitutors.PASS_END), result);
            assertEquals(1, size(passthroughs));
            assertEquals("<code>{code}</code>", get(passthroughs.get(0), "text"));
            assertEquals(list(Substitution.SPECIAL_CHARACTERS), get(passthroughs.get(0), "subs"));
        }

        @Test
        @DisplayName("should not crash if role on passthrough is enclosed in quotes")
        void shouldNotCrashIfRoleOnPassthroughIsEnclosedInQuotes() throws Exception {
            for (var input : list(("['role']" + str(BACKSLASH) + "++This++++++++++++"), ("['role']" + str(BACKSLASH) + "+++++++++This++++++++++++"))) {
                var para = (Block) blockFromString(input);
                assertIncludes(para.getContent(), "<span class=\"'role'\">");
            }
        }

        @Test
        @DisplayName("should allow inline double plus passthrough to be escaped using backslash")
        void shouldAllowInlineDoublePlusPassthroughToBeEscapedUsingBackslash() throws Exception {
            var para = (Block) blockFromString(("you need to replace `int a = n" + str(BACKSLASH) + "++;` with `int a = ++n;`!"));
            var result = para.applySubs(para.getSource());
            assertEquals("you need to replace <code>int a = n++;</code> with <code>int a = ++n;</code>!", result);
        }

        @Test
        @DisplayName("should allow inline double plus passthrough with attributes to be escaped using backslash")
        void shouldAllowInlineDoublePlusPassthroughWithAttributesToBeEscapedUsingBackslash() throws Exception {
            var para = (Block) blockFromString(("=[attrs]" + str(BACKSLASH) + str(BACKSLASH) + "++text++"));
            var result = para.applySubs(para.getSource());
            assertEquals("=[attrs]++text++", result);
        }

        @Test
        @DisplayName("collect multi-line inline double dollar passthroughs")
        void collectMultiLineInlineDoubleDollarPassthroughs() throws Exception {
            var para = (Block) blockFromString("$$<code>\n{code}\n</code>$$");
            var result = para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(((Substitutors.PASS_START + "0") + Substitutors.PASS_END), result);
            assertEquals(1, size(passthroughs));
            assertEquals("<code>\n{code}\n</code>", get(passthroughs.get(0), "text"));
            assertEquals(list(Substitution.SPECIAL_CHARACTERS), get(passthroughs.get(0), "subs"));
        }

        @Test
        @DisplayName("collect multi-line inline double plus passthroughs")
        void collectMultiLineInlineDoublePlusPassthroughs() throws Exception {
            var para = (Block) blockFromString("++<code>\n{code}\n</code>++");
            var result = para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(((Substitutors.PASS_START + "0") + Substitutors.PASS_END), result);
            assertEquals(1, size(passthroughs));
            assertEquals("<code>\n{code}\n</code>", get(passthroughs.get(0), "text"));
            assertEquals(list(Substitution.SPECIAL_CHARACTERS), get(passthroughs.get(0), "subs"));
        }

        @Test
        @DisplayName("collect passthroughs from inline pass macro")
        void collectPassthroughsFromInlinePassMacro() throws Exception {
            var para = (Block) blockFromString("pass:specialcharacters,quotes[<code>['code'\\]</code>]");
            var result = para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(((Substitutors.PASS_START + "0") + Substitutors.PASS_END), result);
            assertEquals(1, size(passthroughs));
            assertEquals("<code>['code']</code>", get(passthroughs.get(0), "text"));
            assertEquals(list(Substitution.SPECIAL_CHARACTERS, Substitution.QUOTES), get(passthroughs.get(0), "subs"));
        }

        @Test
        @DisplayName("collect multi-line passthroughs from inline pass macro")
        void collectMultiLinePassthroughsFromInlinePassMacro() throws Exception {
            var para = (Block) blockFromString("pass:specialcharacters,quotes[<code>['more\ncode'\\]</code>]");
            var result = para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(((Substitutors.PASS_START + "0") + Substitutors.PASS_END), result);
            assertEquals(1, size(passthroughs));
            assertEquals("<code>['more\ncode']</code>", get(passthroughs.get(0), "text"));
            assertEquals(list(Substitution.SPECIAL_CHARACTERS, Substitution.QUOTES), get(passthroughs.get(0), "subs"));
        }

        @Test
        @DisplayName("should find and replace placeholder duplicated by substitution")
        void shouldFindAndReplacePlaceholderDuplicatedBySubstitution() throws Exception {
            var input = "+first passthrough+ followed by link:$$http://example.com/__u_no_format_me__$$[] with passthrough";
            var result = convertInlineString(input);
            assertEquals("first passthrough followed by <a href=\"http://example.com/__u_no_format_me__\" class=\"bare\">http://example.com/__u_no_format_me__</a> with passthrough", result);
        }

        @Test
        @DisplayName("resolves sub shorthands on inline pass macro")
        void resolvesSubShorthandsOnInlinePassMacro() throws Exception {
            var para = (Block) blockFromString("pass:q,a[*<{backend}>*]");
            var result = para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(1, size(passthroughs));
            assertEquals(list(Substitution.QUOTES, Substitution.ATTRIBUTES), get(passthroughs.get(0), "subs"));
            result = para.restorePassthroughs(result);
            assertEquals("<strong><html5></strong>", result);
        }

        @Test
        @DisplayName("inline pass macro supports incremental subs")
        void inlinePassMacroSupportsIncrementalSubs() throws Exception {
            var para = (Block) blockFromString("pass:n,-a[<{backend}>]");
            var result = para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(1, size(passthroughs));
            result = para.restorePassthroughs(result);
            assertEquals("&lt;{backend}&gt;", result);
        }

        @Test
        @DisplayName("should not recognize pass macro with invalid substitution list")
        void shouldNotRecognizePassMacroWithInvalidSubstitutionList() throws Exception {
            for (var subs : list(",", "42", "a,")) {
                var para = (Block) blockFromString(("pass:" + str(subs) + "[foobar]"));
                var result = para.extractPassthroughs(para.getSource());
                assertEquals(("pass:" + str(subs) + "[foobar]"), result);
            }
        }

        @Test
        @DisplayName("should warn if substitutions on pass macro are invalid")
        void shouldWarnIfSubstitutionsOnPassMacroAreInvalid() throws Exception {
            var subs = "bogus";
            var input = ("pass:" + str(subs) + "[++]");
            try (var logger = usingMemoryLogger()) {
                var para = (Block) blockFromString(input, Options.of("attributes", Options.of("stem", "asciimath")));
                assertEquals("++", para.getContent());
                assertMessage(logger, "WARN", ("invalid substitution type for passthrough macro: " + str(subs)));
            }
        }

        @Test
        @DisplayName("should allow content of inline pass macro to be empty")
        void shouldAllowContentOfInlinePassMacroToBeEmpty() throws Exception {
            var para = (Block) blockFromString("pass:[]");
            var result = para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(1, size(passthroughs));
            assertEquals("", para.restorePassthroughs(result));
        }

        @Test
        @DisplayName("restore inline passthroughs without subs")
        void restoreInlinePassthroughsWithoutSubs() throws Exception {
            var para = (Block) blockFromString(((("some " + str(Substitutors.PASS_START)) + "0") + (str(Substitutors.PASS_END) + " to study")));
            para.extractPassthroughs("");
            var passthroughs = para.passthroughs;
            passthroughs.add(new Substitutors.Passthrough("<code>inline code</code>", new ArrayList<Substitution>(), null, null));
            var result = para.restorePassthroughs(para.getSource());
            assertEquals("some <code>inline code</code> to study", result);
        }

        @Test
        @DisplayName("restore inline passthroughs with subs")
        void restoreInlinePassthroughsWithSubs() throws Exception {
            var para = (Block) blockFromString(((((("some " + str(Substitutors.PASS_START)) + "0") + (str(Substitutors.PASS_END) + " to study in the " + str(Substitutors.PASS_START))) + "1") + (str(Substitutors.PASS_END) + " programming language")));
            para.extractPassthroughs("");
            var passthroughs = para.passthroughs;
            passthroughs.add(new Substitutors.Passthrough("<code>{code}</code>", list(Substitution.SPECIAL_CHARACTERS), null, null));
            passthroughs.add(new Substitutors.Passthrough("{language}", list(Substitution.SPECIAL_CHARACTERS), null, null));
            var result = para.restorePassthroughs(para.getSource());
            assertEquals("some &lt;code&gt;{code}&lt;/code&gt; to study in the {language} programming language", result);
        }

        @Test
        @DisplayName("should restore nested passthroughs")
        void shouldRestoreNestedPassthroughs() throws Exception {
            var result = convertInlineString("+Sometimes you feel pass:q[`mono`].+ Sometimes you +$$don't$$+.");
            assertEquals("Sometimes you feel <code>mono</code>. Sometimes you don't.", result);
        }

        @Test
        @DisplayName("should not fail to restore remaining passthroughs after processing inline passthrough with macro substitution")
        void shouldNotFailToRestoreRemainingPassthroughsAfterProcessingInlinePassthroughWithMacroSubstitution() throws Exception {
            var input = "pass:m[.] pass:[.]";
            assertEquals(". .", convertInlineString(input));
        }

        @Test
        @DisplayName("should honor role on double plus passthrough")
        void shouldHonorRoleOnDoublePlusPassthrough() throws Exception {
            var result = convertInlineString("Print the version using [var]++{asciidoctor-version}++.");
            assertEquals("Print the version using <span class=\"var\">{asciidoctor-version}</span>.", result);
        }

        @Test
        @DisplayName("complex inline passthrough macro")
        void complexInlinePassthroughMacro() throws Exception {
            var textToEscape = "[(] <'basic form'> <'logical operator'> <'basic form'> [)]";
            var para = (Block) blockFromString(("$$" + str(textToEscape) + "$$"));
            para.extractPassthroughs(para.getSource());
            var passthroughs = para.passthroughs;
            assertEquals(1, size(passthroughs));
            assertEquals(textToEscape, get(passthroughs.get(0), "text"));
            var textToEscapeEscaped = "[(\\] <'basic form'> <'logical operator'> <'basic form'> [)\\]";
            para = (Block) blockFromString(("pass:specialcharacters[" + str(textToEscapeEscaped) + "]"));
            para.extractPassthroughs(para.getSource());
            passthroughs = para.passthroughs;
            assertEquals(1, size(passthroughs));
            assertEquals(textToEscape, get(passthroughs.get(0), "text"));
        }

        @Test
        @DisplayName("inline pass macro with a composite sub")
        void inlinePassMacroWithACompositeSub() throws Exception {
            var para = (Block) blockFromString("pass:verbatim[<{backend}>]");
            assertEquals("&lt;{backend}&gt;", para.getContent());
        }

        @Test
        @DisplayName("should support constrained passthrough in middle of monospace span")
        void shouldSupportConstrainedPassthroughInMiddleOfMonospaceSpan() throws Exception {
            var input = "a `foo +bar+ baz` kind of thing";
            var para = (Block) blockFromString(input);
            assertEquals("a <code>foo bar baz</code> kind of thing", para.getContent());
        }

        @Test
        @DisplayName("should support constrained passthrough in monospace span preceded by escaped boxed attrlist with transitional role")
        void shouldSupportConstrainedPassthroughInMonospaceSpanPrecededByEscapedBoxedAttrlistWithTransitionalRole() throws Exception {
            var input = (str(BACKSLASH) + "[x-]`foo +bar+ baz`");
            var para = (Block) blockFromString(input);
            assertEquals("[x-]<code>foo bar baz</code>", para.getContent());
        }

        @Test
        @DisplayName("should treat monospace phrase with escaped boxed attrlist with transitional role as monospace")
        void shouldTreatMonospacePhraseWithEscapedBoxedAttrlistWithTransitionalRoleAsMonospace() throws Exception {
            var input = (str(BACKSLASH) + "[x-]`*foo* +bar+ baz`");
            var para = (Block) blockFromString(input);
            assertEquals("[x-]<code><strong>foo</strong> bar baz</code>", para.getContent());
        }

        @Test
        @DisplayName("should ignore escaped attrlist with transitional role on monospace phrase if not proceeded by [")
        void shouldIgnoreEscapedAttrlistWithTransitionalRoleOnMonospacePhraseIfNotProceededBy() throws Exception {
            var input = (str(BACKSLASH) + "x-]`*foo* +bar+ baz`");
            var para = (Block) blockFromString(input);
            assertEquals((str(BACKSLASH) + "x-]<code><strong>foo</strong> bar baz</code>"), para.getContent());
        }

        @Test
        @DisplayName("should not process passthrough inside transitional literal monospace span")
        void shouldNotProcessPassthroughInsideTransitionalLiteralMonospaceSpan() throws Exception {
            var input = "a [x-]`foo +bar+ baz` kind of thing";
            var para = (Block) blockFromString(input);
            assertEquals("a <code>foo +bar+ baz</code> kind of thing", para.getContent());
        }

        @Test
        @DisplayName("should support constrained passthrough in monospace phrase with attrlist")
        void shouldSupportConstrainedPassthroughInMonospacePhraseWithAttrlist() throws Exception {
            var input = "[.role]`foo +bar+ baz`";
            var para = (Block) blockFromString(input);
            assertEquals("<code class=\"role\">foo bar baz</code>", para.getContent());
        }

        @Test
        @DisplayName("should support attrlist on a literal monospace phrase")
        void shouldSupportAttrlistOnALiteralMonospacePhrase() throws Exception {
            var input = "[.baz]`+foo--bar+`";
            var para = (Block) blockFromString(input);
            assertEquals("<code class=\"baz\">foo--bar</code>", para.getContent());
        }

        @Test
        @DisplayName("should not process an escaped passthrough macro inside a monospaced phrase")
        void shouldNotProcessAnEscapedPassthroughMacroInsideAMonospacedPhrase() throws Exception {
            var input = "use the `\\pass:c[]` macro";
            var para = (Block) blockFromString(input);
            assertEquals("use the <code>pass:c[]</code> macro", para.getContent());
        }

        @Test
        @DisplayName("should not process an escaped passthrough macro inside a monospaced phrase with attributes")
        void shouldNotProcessAnEscapedPassthroughMacroInsideAMonospacedPhraseWithAttributes() throws Exception {
            var input = "use the [syntax]`\\pass:c[]` macro";
            var para = (Block) blockFromString(input);
            assertEquals("use the <code class=\"syntax\">pass:c[]</code> macro", para.getContent());
        }

        @Test
        @DisplayName("should honor an escaped single plus passthrough inside a monospaced phrase")
        void shouldHonorAnEscapedSinglePlusPassthroughInsideAMonospacedPhrase() throws Exception {
            var input = "use `\\+{author}+` to show an attribute reference";
            var para = (Block) blockFromString(input, Options.of("attributes", Options.of("author", "Dan")));
            assertEquals("use <code>+Dan+</code> to show an attribute reference", para.getContent());
        }

        @Nested
        @DisplayName("Math macros")
        class MathMacros {

            @Test
            @DisplayName("should passthrough text in asciimath macro and surround with AsciiMath delimiters")
            void shouldPassthroughTextInAsciimathMacroAndSurroundWithAsciiMathDelimiters() throws Exception {
                try (var logger = usingMemoryLogger()) {
                    var input = "asciimath:[x/x={(1,if x!=0),(text{undefined},if x=0):}]";
                    var para = (Block) blockFromString(input, Options.of("attributes", Options.of("attribute-missing", "warn")));
                    assertEquals("\\$x/x={(1,if x!=0),(text{undefined},if x=0):}\\$", para.getContent());
                    assertEmpty(logger);
                }
            }

            @Test
            @DisplayName("should not recognize asciimath macro with no content")
            void shouldNotRecognizeAsciimathMacroWithNoContent() throws Exception {
                var input = "asciimath:[]";
                var para = (Block) blockFromString(input);
                assertEquals("asciimath:[]", para.getContent());
            }

            @Test
            @DisplayName("should perform specialcharacters subs on asciimath macro content in html backend by default")
            void shouldPerformSpecialcharactersSubsOnAsciimathMacroContentInHtmlBackendByDefault() throws Exception {
                var input = "asciimath:[a < b]";
                var para = (Block) blockFromString(input);
                assertEquals("\\$a &lt; b\\$", para.getContent());
            }

            @Test
            @DisplayName("should convert contents of asciimath macro to MathML in DocBook output if asciimath gem is available")
            void shouldConvertContentsOfAsciimathMacroToMathMLInDocBookOutputIfAsciimathGemIsAvailable() throws Exception {
                var asciimathAvailable = false;
                var input = "asciimath:[a < b]";
                var expected = "<inlineequation><mml:math xmlns:mml=\"http://www.w3.org/1998/Math/MathML\"><mml:mi>a</mml:mi><mml:mo>&lt;</mml:mo><mml:mi>b</mml:mi></mml:math></inlineequation>";
                try (var logger = usingMemoryLogger()) {
                    var para = (Block) blockFromString(input, Options.of("backend", "docbook"));
                    var actual = para.getContent();
                    if (isTruthy(asciimathAvailable)) {
                        assertEquals(expected, actual);
                    } else {
                        assertMessage(logger, "WARN", "optional gem 'asciimath' is not available. Functionality disabled.");
                    }
                }
            }

            @Test
            @DisplayName("should not perform specialcharacters subs on asciimath macro content in Docbook output if asciimath gem not available")
            void shouldNotPerformSpecialcharactersSubsOnAsciimathMacroContentInDocbookOutputIfAsciimathGemNotAvailable() throws Exception {
                var asciimathAvailable = false;
                var input = "asciimath:[a < b]";
                var para = (Block) blockFromString(input, Options.of("backend", "docbook"));
                if (isTruthy(asciimathAvailable)) {
                }
                assertEquals("<inlineequation><mathphrase><![CDATA[a < b]]></mathphrase></inlineequation>", para.getContent());
                if (isTruthy(asciimathAvailable)) {
                }
            }

            @Test
            @DisplayName("should honor explicit subslist on asciimath macro")
            void shouldHonorExplicitSubslistOnAsciimathMacro() throws Exception {
                var input = "asciimath:attributes[{expr}]";
                var para = (Block) blockFromString(input, Options.of("attributes", Options.of("expr", "x != 0")));
                assertEquals("\\$x != 0\\$", para.getContent());
            }

            @Test
            @DisplayName("should passthrough text in latexmath macro and surround with LaTeX math delimiters")
            void shouldPassthroughTextInLatexmathMacroAndSurroundWithLaTeXMathDelimiters() throws Exception {
                var input = "latexmath:[C = \\alpha + \\beta Y^{\\gamma} + \\epsilon]";
                var para = (Block) blockFromString(input);
                assertEquals("\\(C = \\alpha + \\beta Y^{\\gamma} + \\epsilon\\)", para.getContent());
            }

            @Test
            @DisplayName("should strip legacy LaTeX math delimiters around latexmath content if present")
            void shouldStripLegacyLaTeXMathDelimitersAroundLatexmathContentIfPresent() throws Exception {
                var input = "latexmath:[$C = \\alpha + \\beta Y^{\\gamma} + \\epsilon$]";
                var para = (Block) blockFromString(input);
                assertEquals("\\(C = \\alpha + \\beta Y^{\\gamma} + \\epsilon\\)", para.getContent());
            }

            @Test
            @DisplayName("should not recognize latexmath macro with no content")
            void shouldNotRecognizeLatexmathMacroWithNoContent() throws Exception {
                var input = "latexmath:[]";
                var para = (Block) blockFromString(input);
                assertEquals("latexmath:[]", para.getContent());
            }

            @Test
            @DisplayName("should unescape escaped square bracket in equation")
            void shouldUnescapeEscapedSquareBracketInEquation() throws Exception {
                var input = "latexmath:[\\sqrt[3\\]{x}]";
                var para = (Block) blockFromString(input);
                assertEquals("\\(\\sqrt[3]{x}\\)", para.getContent());
            }

            @Test
            @DisplayName("should perform specialcharacters subs on latexmath macro in html backend by default")
            void shouldPerformSpecialcharactersSubsOnLatexmathMacroInHtmlBackendByDefault() throws Exception {
                var input = "latexmath:[a < b]";
                var para = (Block) blockFromString(input);
                assertEquals("\\(a &lt; b\\)", para.getContent());
            }

            @Test
            @DisplayName("should not perform specialcharacters subs on latexmath macro content in docbook backend by default")
            void shouldNotPerformSpecialcharactersSubsOnLatexmathMacroContentInDocbookBackendByDefault() throws Exception {
                var input = "latexmath:[a < b]";
                var para = (Block) blockFromString(input, Options.of("backend", "docbook"));
                assertEquals("<inlineequation><alt><![CDATA[a < b]]></alt><mathphrase><![CDATA[a < b]]></mathphrase></inlineequation>", para.getContent());
            }

            @Test
            @DisplayName("should honor explicit subslist on latexmath macro")
            void shouldHonorExplicitSubslistOnLatexmathMacro() throws Exception {
                var input = "latexmath:attributes[{expr}]";
                var para = (Block) blockFromString(input, Options.of("attributes", Options.of("expr", "\\sqrt{4} = 2")));
                assertEquals("\\(\\sqrt{4} = 2\\)", para.getContent());
            }

            @Test
            @DisplayName("should passthrough math macro inside another passthrough")
            void shouldPassthroughMathMacroInsideAnotherPassthrough() throws Exception {
                var input = "the text `asciimath:[x = y]` should be passed through as +literal+ text";
                var para = (Block) blockFromString(input, Options.of("attributes", Options.of("compat-mode", "")));
                assertEquals("the text <code>asciimath:[x = y]</code> should be passed through as <code>literal</code> text", para.getContent());
                input = "the text [x-]`asciimath:[x = y]` should be passed through as `literal` text";
                para = (Block) blockFromString(input);
                assertEquals("the text <code>asciimath:[x = y]</code> should be passed through as <code>literal</code> text", para.getContent());
                input = "the text `+asciimath:[x = y]+` should be passed through as `literal` text";
                para = (Block) blockFromString(input);
                assertEquals("the text <code>asciimath:[x = y]</code> should be passed through as <code>literal</code> text", para.getContent());
            }

            @Test
            @DisplayName("should not recognize stem macro with no content")
            void shouldNotRecognizeStemMacroWithNoContent() throws Exception {
                var input = "stem:[]";
                var para = (Block) blockFromString(input);
                assertEquals(input, para.getContent());
            }

            @Test
            @DisplayName("should passthrough text in stem macro and surround with AsciiMath delimiters if stem attribute is asciimath, empty, or not set")
            void shouldPassthroughTextInStemMacroAndSurroundWithAsciiMathDelimitersIfStemAttributeIsAsciimathEmptyOrNotSet() throws Exception {
                for (var attributes : list(Options.of(), Options.of("stem", ""), Options.of("stem", "asciimath"), Options.of("stem", "bogus"))) {
                    try (var logger = usingMemoryLogger()) {
                        var input = "stem:[x/x={(1,if x!=0),(text{undefined},if x=0):}]";
                        var para = (Block) blockFromString(input, Options.of("attributes", merge(attributes, Options.of("attribute-missing", "warn"))));
                        assertEquals("\\$x/x={(1,if x!=0),(text{undefined},if x=0):}\\$", para.getContent());
                        assertEmpty(logger);
                    }
                }
            }

            @Test
            @DisplayName("should passthrough text in stem macro and surround with LaTeX math delimiters if stem attribute is latexmath, latex, or tex")
            void shouldPassthroughTextInStemMacroAndSurroundWithLaTeXMathDelimitersIfStemAttributeIsLatexmathLatexOrTex() throws Exception {
                for (var attributes : list(Options.of("stem", "latexmath"), Options.of("stem", "latex"), Options.of("stem", "tex"))) {
                    var input = "stem:[C = \\alpha + \\beta Y^{\\gamma} + \\epsilon]";
                    var para = (Block) blockFromString(input, Options.of("attributes", attributes));
                    assertEquals("\\(C = \\alpha + \\beta Y^{\\gamma} + \\epsilon\\)", para.getContent());
                }
            }

            @Test
            @DisplayName("should apply substitutions specified on stem macro")
            void shouldApplySubstitutionsSpecifiedOnStemMacro() throws Exception {
                for (var input : list("stem:c,a[sqrt(x) <=> {solve-for-x}]", "stem:n,-r[sqrt(x) <=> {solve-for-x}]")) {
                    var para = (Block) blockFromString(input, Options.of("attributes", Options.of("stem", "asciimath", "solve-for-x", "13")));
                    assertEquals("\\$sqrt(x) &lt;=&gt; 13\\$", para.getContent());
                }
            }

            @Test
            @DisplayName("should replace passthroughs inside stem expression")
            void shouldReplacePassthroughsInsideStemExpression() throws Exception {
                for (var entry : entries(list(list("stem:[+1+]", "\\$1\\$"), list("stem:[+\\infty-(+\\infty)]", "\\$\\infty-(\\infty)\\$"), list("stem:[+++\\infty-(+\\infty)++]", "\\$+\\infty-(+\\infty)\\$")))) {
                    var input = entry.getKey();
                    var expected = entry.getValue();
                    var para = (Block) blockFromString(input, Options.of("attributes", Options.of("stem", "")));
                    assertEquals(expected, para.getContent());
                }
            }

            @Test
            @DisplayName("should allow passthrough inside stem expression to be escaped")
            void shouldAllowPassthroughInsideStemExpressionToBeEscaped() throws Exception {
                for (var entry : entries(list(list("stem:[\\+] and stem:[+]", "\\$+\\$ and \\$+\\$"), list("stem:[\\+1+]", "\\$+1+\\$")))) {
                    var input = entry.getKey();
                    var expected = entry.getValue();
                    var para = (Block) blockFromString(input, Options.of("attributes", Options.of("stem", "")));
                    assertEquals(expected, para.getContent());
                }
            }

            @Test
            @DisplayName("should not recognize stem macro with invalid substitution list")
            void shouldNotRecognizeStemMacroWithInvalidSubstitutionList() throws Exception {
                for (var subs : list(",", "42", "a,")) {
                    var input = ("stem:" + str(subs) + "[x^2]");
                    var para = (Block) blockFromString(input, Options.of("attributes", Options.of("stem", "asciimath")));
                    assertEquals(("stem:" + str(subs) + "[x^2]"), para.getContent());
                }
            }

            @Test
            @DisplayName("should warn if substitutions on stem macro are invalid")
            void shouldWarnIfSubstitutionsOnStemMacroAreInvalid() throws Exception {
                var subs = "bogus";
                var input = ("stem:" + str(subs) + "[x^2]");
                try (var logger = usingMemoryLogger()) {
                    var para = (Block) blockFromString(input, Options.of("attributes", Options.of("stem", "asciimath")));
                    assertEquals("\\$x^2\\$", para.getContent());
                    assertMessage(logger, "WARN", ("invalid substitution type for stem macro: " + str(subs)));
                }
            }
        }
    }

    @Nested
    @DisplayName("Replacements")
    class Replacements {

        @Test
        @DisplayName("unescapes XML entities")
        void unescapesXMLEntities() throws Exception {
            var para = (Block) blockFromString("< &quot; &there4; &#34; &#x22; >");
            assertEquals("&lt; &quot; &there4; &#34; &#x22; &gt;", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("replaces arrows")
        void replacesArrows() throws Exception {
            var para = (Block) blockFromString("<- -> <= => \\<- \\-> \\<= \\=>");
            assertEquals("&#8592; &#8594; &#8656; &#8658; &lt;- -&gt; &lt;= =&gt;", para.applySubs(para.getSource()));
        }

        @Test
        @DisplayName("replaces dashes")
        void replacesDashes() throws Exception {
            var input = """
                    -- foo foo--bar foo\\--bar foo -- bar foo \\-- bar
                    stuff in between
                    -- foo
                    stuff in between
                    foo --
                    stuff in between
                    foo --
                    """;
            var expected = Strings.chop("""
                    &#8201;&#8212;&#8201;foo foo&#8212;&#8203;bar foo--bar foo&#8201;&#8212;&#8201;bar foo -- bar
                    stuff in between&#8201;&#8212;&#8201;foo
                    stuff in between
                    foo&#8201;&#8212;&#8201;stuff in between
                    foo&#8201;&#8212;&#8201;
                    """);
            var para = (Block) blockFromString(input);
            assertEquals(expected, para.subReplacements(para.getSource()));
        }

        @Test
        @DisplayName("replaces dashes between multibyte word characters")
        void replacesDashesBetweenMultibyteWordCharacters() throws Exception {
            var para = (Block) blockFromString("富--巴");
            var expected = "富&#8212;&#8203;巴";
            assertEquals(expected, para.subReplacements(para.getSource()));
        }

        @Test
        @DisplayName("replaces marks")
        void replacesMarks() throws Exception {
            var para = (Block) blockFromString("(C) (R) (TM) \\(C) \\(R) \\(TM)");
            assertEquals("&#169; &#174; &#8482; (C) (R) (TM)", para.subReplacements(para.getSource()));
        }

        @Test
        @DisplayName("preserves entity references")
        void preservesEntityReferences() throws Exception {
            var input = "&amp; &#169; &#10004; &#128512; &#x2022; &#x1f600;";
            var result = convertInlineString(input);
            assertEquals(input, result);
        }

        @Test
        @DisplayName("only preserves named entities with two or more letters")
        void onlyPreservesNamedEntitiesWithTwoOrMoreLetters() throws Exception {
            var input = "&amp; &a; &gt;";
            var result = convertInlineString(input);
            assertEquals("&amp; &amp;a; &gt;", result);
        }

        @Test
        @DisplayName("replaces punctuation")
        void replacesPunctuation() throws Exception {
            var para = (Block) blockFromString("John's Hideout is the Whites`' place... foo\\'bar");
            assertEquals("John&#8217;s Hideout is the Whites&#8217; place&#8230;&#8203; foo'bar", para.subReplacements(para.getSource()));
        }

        @Test
        @DisplayName("should replace right single quote marks")
        void shouldReplaceRightSingleQuoteMarks() throws Exception {
            var given = list("`'Twas the night", "a `'57 Chevy!", "the whites`' place", "the whites`'.", "the whites`'--where the wild things are", "the whites`'\nhave", "It's Mary`'s little lamb.", "consecutive single quotes '' are not modified", "he is 6' tall", "\\`'");
            var expected = list("&#8217;Twas the night", "a &#8217;57 Chevy!", "the whites&#8217; place", "the whites&#8217;.", "the whites&#8217;--where the wild things are", "the whites&#8217;\nhave", "It&#8217;s Mary&#8217;s little lamb.", "consecutive single quotes '' are not modified", "he is 6' tall", "`'");
            for (int i = 0; i < size(given); i++) {
                var para = (Block) blockFromString(get(given, i));
                assertEquals(get(expected, i), para.subReplacements(para.getSource()));
            }
        }
    }

    @Nested
    @DisplayName("Post replacements")
    class PostReplacements {

        @Test
        @DisplayName("line break inserted after line with line break character")
        void lineBreakInsertedAfterLineWithLineBreakCharacter() throws Exception {
            var para = (Block) blockFromString("First line +\nSecond line");
            var result = para.applySubs(lines(para), para.expandSubs("post_replacements"));
            assertEquals("First line<br>", first(result));
        }

        @Test
        @DisplayName("line break inserted after line wrap with hardbreaks enabled")
        void lineBreakInsertedAfterLineWrapWithHardbreaksEnabled() throws Exception {
            var para = (Block) blockFromString("First line\nSecond line", Options.of("attributes", Options.of("hardbreaks", "")));
            var result = para.applySubs(lines(para), para.expandSubs("post_replacements"));
            assertEquals("First line<br>", first(result));
        }

        @Test
        @DisplayName("line break character stripped from end of line with hardbreaks enabled")
        void lineBreakCharacterStrippedFromEndOfLineWithHardbreaksEnabled() throws Exception {
            var para = (Block) blockFromString("First line +\nSecond line", Options.of("attributes", Options.of("hardbreaks", "")));
            var result = para.applySubs(lines(para), para.expandSubs("post_replacements"));
            assertEquals("First line<br>", first(result));
        }

        @Test
        @DisplayName("line break not inserted for single line with hardbreaks enabled")
        void lineBreakNotInsertedForSingleLineWithHardbreaksEnabled() throws Exception {
            var para = (Block) blockFromString("First line", Options.of("attributes", Options.of("hardbreaks", "")));
            var result = para.applySubs(lines(para), para.expandSubs("post_replacements"));
            assertEquals("First line", first(result));
        }
    }

    @Nested
    @DisplayName("Resolve subs")
    class ResolveSubs {

        @Test
        @DisplayName("should resolve subs for block")
        void shouldResolveSubsForBlock() throws Exception {
            var doc = emptyDocument(Options.of("parse", true));
            var block = new Block(doc, "paragraph");
            block.getAttributes().put("subs", "quotes,normal");
            block.commitSubs();
            assertEquals(list(Substitution.QUOTES, Substitution.SPECIAL_CHARACTERS, Substitution.ATTRIBUTES, Substitution.REPLACEMENTS, Substitution.MACROS, Substitution.POST_REPLACEMENTS), block.getSubs());
        }

        @Test
        @Disabled("the CodeRay and Pygments adapters are not ported (they require a gem)")
        @DisplayName("should resolve specialcharacters sub as highlight for source block when source highlighter is coderay")
        void shouldResolveSpecialcharactersSubAsHighlightForSourceBlockWhenSourceHighlighterIsCoderay() throws Exception {
        }

        @Test
        @Disabled("the CodeRay and Pygments adapters are not ported (they require a gem)")
        @DisplayName("should resolve specialcharacters sub as highlight for source block when source highlighter is pygments")
        void shouldResolveSpecialcharactersSubAsHighlightForSourceBlockWhenSourceHighlighterIsPygments() throws Exception {
        }

        @Test
        @DisplayName("should not replace specialcharacters sub with highlight for source block when source highlighter is not set")
        void shouldNotReplaceSpecialcharactersSubWithHighlightForSourceBlockWhenSourceHighlighterIsNotSet() throws Exception {
            var doc = emptyDocument(Options.of("parse", true));
            var block = new Block(doc, "listing", Options.of("content_model", "verbatim"));
            block.setStyle("source");
            block.getAttributes().put("subs", "specialcharacters");
            block.getAttributes().put("language", "ruby");
            block.commitSubs();
            assertEquals(list(Substitution.SPECIAL_CHARACTERS), block.getSubs());
        }

        @Test
        @DisplayName("should not use subs if subs option passed to block constructor is nil")
        void shouldNotUseSubsIfSubsOptionPassedToBlockConstructorIsNil() throws Exception {
            var doc = emptyDocument(Options.of("parse", true));
            var block = new Block(doc, "paragraph", Options.of("source", "*bold* _italic_", "subs", null, "attributes", Options.of("subs", "quotes")));
            assertEmpty(block.getSubs());
            block.commitSubs();
            assertEmpty(block.getSubs());
        }

        @Test
        @DisplayName("should not use subs if subs option passed to block constructor is empty array")
        void shouldNotUseSubsIfSubsOptionPassedToBlockConstructorIsEmptyArray() throws Exception {
            var doc = emptyDocument(Options.of("parse", true));
            var block = new Block(doc, "paragraph", Options.of("source", "*bold* _italic_", "subs", new ArrayList<String>(), "attributes", Options.of("subs", "quotes")));
            assertEmpty(block.getSubs());
            block.commitSubs();
            assertEmpty(block.getSubs());
        }

        @Test
        @DisplayName("should use subs from subs option passed to block constructor")
        void shouldUseSubsFromSubsOptionPassedToBlockConstructor() throws Exception {
            var doc = emptyDocument(Options.of("parse", true));
            var block = new Block(doc, "paragraph", Options.of("source", "*bold* _italic_", "subs", list("specialcharacters"), "attributes", Options.of("subs", "quotes")));
            assertEquals(list(Substitution.SPECIAL_CHARACTERS), block.getSubs());
            block.commitSubs();
            assertEquals(list(Substitution.SPECIAL_CHARACTERS), block.getSubs());
        }

        @Test
        @DisplayName("should use subs from subs attribute if subs option is not passed to block constructor")
        void shouldUseSubsFromSubsAttributeIfSubsOptionIsNotPassedToBlockConstructor() throws Exception {
            var doc = emptyDocument(Options.of("parse", true));
            var block = new Block(doc, "paragraph", Options.of("source", "*bold* _italic_", "attributes", Options.of("subs", "quotes")));
            assertEmpty(block.getSubs());
            block.commitSubs();
            assertEquals(list(Substitution.QUOTES), block.getSubs());
        }

        @Test
        @DisplayName("should use subs from subs attribute if subs option passed to block constructor is :default")
        void shouldUseSubsFromSubsAttributeIfSubsOptionPassedToBlockConstructorIsDefault() throws Exception {
            var doc = emptyDocument(Options.of("parse", true));
            var block = new Block(doc, "paragraph", Options.of("source", "*bold* _italic_", "subs", "default", "attributes", Options.of("subs", "quotes")));
            assertEquals(list(Substitution.QUOTES), block.getSubs());
            block.commitSubs();
            assertEquals(list(Substitution.QUOTES), block.getSubs());
        }

        @Test
        @DisplayName("should use built-in subs if subs option passed to block constructor is :default and subs attribute is absent")
        void shouldUseBuiltInSubsIfSubsOptionPassedToBlockConstructorIsDefaultAndSubsAttributeIsAbsent() throws Exception {
            var doc = emptyDocument(Options.of("parse", true));
            var block = new Block(doc, "paragraph", Options.of("source", "*bold* _italic_", "subs", "default"));
            assertEquals(list(Substitution.SPECIAL_CHARACTERS, Substitution.QUOTES, Substitution.ATTRIBUTES, Substitution.REPLACEMENTS, Substitution.MACROS, Substitution.POST_REPLACEMENTS), block.getSubs());
            block.commitSubs();
            assertEquals(list(Substitution.SPECIAL_CHARACTERS, Substitution.QUOTES, Substitution.ATTRIBUTES, Substitution.REPLACEMENTS, Substitution.MACROS, Substitution.POST_REPLACEMENTS), block.getSubs());
        }
    }
}
