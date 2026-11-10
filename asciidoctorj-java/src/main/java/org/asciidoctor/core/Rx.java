package org.asciidoctor.core;

import static org.asciidoctor.core.Asciidoctor.ADMONITION_STYLES;
import static org.asciidoctor.core.Asciidoctor.CC_ALL;
import static org.asciidoctor.core.Asciidoctor.CC_ALNUM;
import static org.asciidoctor.core.Asciidoctor.CC_ALPHA;
import static org.asciidoctor.core.Asciidoctor.CC_ANY;
import static org.asciidoctor.core.Asciidoctor.CC_EOL;
import static org.asciidoctor.core.Asciidoctor.CC_WORD;
import static org.asciidoctor.core.Asciidoctor.CG_ALNUM;
import static org.asciidoctor.core.Asciidoctor.CG_ALPHA;
import static org.asciidoctor.core.Asciidoctor.CG_BLANK;
import static org.asciidoctor.core.Asciidoctor.CG_WORD;
import static org.asciidoctor.core.Asciidoctor.QUOTE_ATTRIBUTE_LIST_RXT;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of regular expression constants used by the parser, as well as helpers to work with regular
 * expressions using the semantics of Ruby (where ^ and $ always match at line boundaries).
 *
 * <p>NOTE The following pattern, which appears frequently, captures the contents between square brackets, ignoring
 * escaped closing brackets (closing brackets prefixed with a backslash '\' character)
 *
 * <pre>
 *   Pattern: \[(|#{CC_ALL}*?[^\\])\]
 *   Matches: [enclosed text] and [enclosed [text\]], not [enclosed text \\] or [\\] (as these require a trailing space)
 * </pre>
 */
public final class Rx {
    private static final int FLAGS = Pattern.MULTILINE | Pattern.UNIX_LINES;

    private Rx() {
    }

    /**
     * Compiles the regular expression using the line-oriented anchors of Ruby (i.e., ^ and $ match at line
     * boundaries). The dot does not match a newline.
     */
    public static Pattern compile(String regex) {
        return Pattern.compile(regex, FLAGS);
    }

    /**
     * Compiles the regular expression using the line-oriented anchors of Ruby, where the dot also matches a newline
     * (equivalent to the m flag in Ruby).
     */
    public static Pattern compileMultiline(String regex) {
        return Pattern.compile(regex, FLAGS | Pattern.DOTALL);
    }

    /**
     * Replaces all matches of the pattern in the text using the result of the replacement function. The value returned
     * by the function is used literally (i.e., group references are not expanded). A null value is treated as an empty
     * string.
     */
    public static String gsub(Pattern pattern, String text, Function<Matcher, String> replacement) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return text;
        }
        StringBuilder result = new StringBuilder(text.length() + 16);
        int last = 0;
        do {
            result.append(text, last, matcher.start());
            String replaced = replacement.apply(matcher);
            if (replaced != null) {
                result.append(replaced);
            }
            last = matcher.end();
        } while (matcher.find());
        result.append(text, last, text.length());
        return result.toString();
    }

    /**
     * Replaces all matches of the pattern in the text with the literal replacement.
     */
    public static String gsub(Pattern pattern, String text, String replacement) {
        return pattern.matcher(text).replaceAll(Matcher.quoteReplacement(replacement));
    }

    /**
     * Replaces the first match of the pattern in the text using the result of the replacement function.
     */
    public static String sub(Pattern pattern, String text, Function<Matcher, String> replacement) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return text;
        }
        String replaced = replacement.apply(matcher);
        return text.substring(0, matcher.start()) + (replaced == null ? "" : replaced) + text.substring(matcher.end());
    }

    /**
     * Replaces the first match of the pattern in the text with the literal replacement.
     */
    public static String sub(Pattern pattern, String text, String replacement) {
        return sub(pattern, text, m -> replacement);
    }

    /**
     * Matches the pattern against the text and returns the match result, or null if there is no match.
     */
    public static Matcher match(Pattern pattern, String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher : null;
    }

    /**
     * Checks whether the pattern matches any part of the text.
     */
    public static boolean matches(Pattern pattern, String text) {
        return text != null && pattern.matcher(text).find();
    }

    /**
     * Returns the value of the named group, or null if the pattern does not define that group.
     */
    public static String namedGroup(Matcher match, String name) {
        try {
            return match.group(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Matches the opening of a named group, i.e., (?<name>, but not an escaped parenthesis or a lookbehind
    private static final Pattern NAMED_GROUP_RX = Pattern.compile("(?<!\\\\)\\(\\?<[a-zA-Z]");

    /**
     * Returns whether the pattern defines any named groups.
     */
    public static boolean hasNamedGroups(Pattern pattern) {
        return NAMED_GROUP_RX.matcher(pattern.pattern()).find();
    }

    /**
     * Escapes the string so it's matched literally when used in a regular expression.
     */
    public static String escape(String str) {
        StringBuilder result = new StringBuilder(str.length() * 2);
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);
            if ("\\^$.|?*+()[]{}-#&~ ".indexOf(c) >= 0) {
                result.append('\\');
            }
            result.append(c);
        }
        return result.toString();
    }

    // ## Document header

    // Matches the author info line immediately following the document title.
    //
    // Examples
    //
    //   Doc Writer <doc@example.com>
    //   Mary_Sue Brontë
    //
    public static final Pattern AUTHOR_INFO_LINE_RX = compile("^(" + CG_WORD + "[" + CC_WORD + "\\-'.]*)(?: +("
            + CG_WORD + "[" + CC_WORD + "\\-'.]*))?(?: +(" + CG_WORD + "[" + CC_WORD + "\\-'.]*))?(?: +<([^>]+)>)?$");

    // Matches the delimiter that separates multiple authors.
    //
    // Examples
    //
    //   Doc Writer; Junior Writer
    //
    public static final Pattern AUTHOR_DELIMITER_RX = compile(";(?: |$)");

    // Matches the revision info line, which appears immediately following the author info line beneath the document
    // title.
    //
    // Examples
    //
    //   v1.0
    //   2013-01-01
    //   v1.0, 2013-01-01: Ring in the new year release
    //   1.0, Jan 01, 2013
    //
    public static final Pattern REVISION_INFO_LINE_RX = compile("^(?:[^\\d{]*(" + CC_ANY + "*?),)? *(?!:)(" + CC_ANY
            + "*?)(?: *(?!^),?: *(" + CC_ANY + "*))?$");

    // Matches the title and volnum in the manpage doctype.
    //
    // Examples
    //
    //   = asciidoctor(1)
    //   = asciidoctor ( 1 )
    //
    public static final Pattern MANPAGE_TITLE_VOLNUM_RX = compile("^(" + CC_ANY + "+?) *\\( *(" + CC_ANY
            + "+?) *\\)$");

    // Matches the name and purpose in the manpage doctype.
    //
    // Examples
    //
    //   asciidoctor - converts AsciiDoc source files to HTML, DocBook and other formats
    //
    public static final Pattern MANPAGE_NAME_PURPOSE_RX = compile("^(" + CC_ANY + "+?) +- +(" + CC_ANY + "+)$");

    // ## Preprocessor directives

    // Matches a conditional preprocessor directive (e.g., ifdef, ifndef, ifeval and endif).
    //
    // Examples
    //
    //   ifdef::basebackend-html[]
    //   ifndef::theme[]
    //   ifeval::["{asciidoctor-version}" >= "0.1.0"]
    //   ifdef::asciidoctor[Asciidoctor!]
    //   endif::theme[]
    //   endif::basebackend-html[]
    //   endif::[]
    //
    public static final Pattern CONDITIONAL_DIRECTIVE_RX = compile("^(\\\\)?(ifdef|ifndef|ifeval|endif)::(\\S*?(?:([,+])"
            + "\\S*?)?)\\[(" + CC_ANY + "+)?\\]$");

    // Matches a restricted (read as safe) eval expression.
    //
    // Examples
    //
    //   "{asciidoctor-version}" >= "0.1.0"
    //
    public static final Pattern EVAL_EXPRESSION_RX = compile("^(" + CC_ANY + "+?) *([=!><]=|[><]) *(" + CC_ANY
            + "+)$");

    // Matches an include preprocessor directive.
    //
    // Examples
    //
    //   include::chapter1.ad[]
    //   include::example.txt[lines=1;2;5..10]
    //
    public static final Pattern INCLUDE_DIRECTIVE_RX = compile("^(\\\\)?include::([^\\s\\[](?:[^\\[]*[^\\s\\[])?)\\[("
            + CC_ANY + "+)?\\]$");

    // Matches a trailing tag directive in an include file.
    //
    // Examples
    //
    //   // tag::try-catch[]
    //   try {
    //     someMethod();
    //   catch (Exception e) {
    //     log(e);
    //   }
    //   // end::try-catch[]
    // NOTE the regex checks for \r to account of include files that use Windows newlines
    public static final Pattern TAG_DIRECTIVE_RX = compileMultiline("\\b(?:tag|(e)nd)::(\\S+?)\\[\\](?=$|[ \\r])");

    // ## Attribute entries and references

    // Matches a document attribute entry.
    //
    // Examples
    //
    //   :foo: bar
    //   :First Name: Dan
    //   :sectnums!:
    //   :!toc:
    //   :long-entry: Attribute value lines ending in ' \' \
    //                are joined together as a single value, \
    //                collapsing the line breaks and indentation to \
    //                a single space.
    //
    public static final Pattern ATTRIBUTE_ENTRY_RX = compile("^:(!?" + CG_WORD + "[^:]*):(?:[ \\t]+(" + CC_ANY
            + "*))?$");

    // Matches invalid characters in an attribute name.
    public static final Pattern INVALID_ATTRIBUTE_NAME_CHARS_RX = compile("[^" + CC_WORD + "-]");

    // Matches a pass inline macro that surrounds the value of an attribute entry once it has been parsed.
    //
    // Examples
    //
    //   pass:[text]
    //   pass:a[{a} {b} {c}]
    //
    public static final Pattern ATTRIBUTE_ENTRY_PASS_MACRO_RX = compileMultiline(
            "\\Apass:([a-z]+(?:,[a-z-]+)*)?\\[(.*)\\]\\Z");

    // Matches an inline attribute reference.
    //
    // Examples
    //
    //   {foobar} or {app_name} or {product-version}
    //   {counter:sequence-name:1}
    //   {set:foo:bar}
    //   {set:name!}
    //
    public static final Pattern ATTRIBUTE_REFERENCE_RX = compile("(\\\\)?\\{(" + CG_WORD + "[" + CC_WORD
            + "-]*|(set|counter2?):" + CC_ANY + "+?)(\\\\)?\\}");

    // ## Paragraphs and delimited blocks

    // Matches an anchor (i.e., id + optional reference text) on a line above a block.
    //
    // Examples
    //
    //   [[idname]]
    //   [[idname,Reference Text]]
    //
    public static final Pattern BLOCK_ANCHOR_RX = compile("^\\[\\[(?:|([" + CC_ALPHA + "_:][" + CC_WORD
            + "\\-:.]*)(?:, *(" + CC_ANY + "+))?)\\]\\]$");

    // Matches an attribute list above a block element.
    //
    // Examples
    //
    //   # strictly positional
    //   [quote, Adam Smith, Wealth of Nations]
    //
    //   # name/value pairs
    //   [NOTE, caption="Good to know"]
    //
    //   # as attribute reference
    //   [{lead}]
    //
    public static final Pattern BLOCK_ATTRIBUTE_LIST_RX = compile("^\\[(|[" + CC_WORD + ".#%{,\"']" + CC_ANY
            + "*)\\]$");

    // A combined pattern that matches either a block anchor or a block attribute list.
    //
    // TODO this one gets hit a lot, should be optimized as much as possible
    public static final Pattern BLOCK_ATTRIBUTE_LINE_RX = compile("^\\[(?:|[" + CC_WORD + ".#%{,\"']" + CC_ANY
            + "*|\\[(?:|[" + CC_ALPHA + "_:][" + CC_WORD + "\\-:.]*(?:, *" + CC_ANY + "+)?)\\])\\]$");

    // Matches a title above a block.
    //
    // Examples
    //
    //   .Title goes here
    //
    public static final Pattern BLOCK_TITLE_RX = compile("^\\.(\\.?[^ \\t.]" + CC_ANY + "*)$");

    // Matches an admonition label at the start of a paragraph.
    //
    // Examples
    //
    //   NOTE: Just a little note.
    //   TIP: Don't forget!
    //
    public static final Pattern ADMONITION_PARAGRAPH_RX = compile("^(" + String.join("|", ADMONITION_STYLES)
            + "):[ \\t]+");

    // Matches a literal paragraph, which is a line of text preceded by at least one space.
    //
    // Examples
    //
    //   <SPACE>Foo
    //   <TAB>Foo
    public static final Pattern LITERAL_PARAGRAPH_RX = compile("^([ \\t]+" + CC_ANY + "*)$");

    // ## Section titles

    // Matches an Atx (single-line) section title.
    //
    // Examples
    //
    //   == Foo
    //   // ^ a level 1 (h2) section title
    //
    //   == Foo ==
    //   // ^ also a level 1 (h2) section title
    //
    public static final Pattern ATX_SECTION_TITLE_RX = compile("^(=={0,5})[ \\t]+(" + CC_ANY
            + "+?)(?:[ \\t]+\\1)?$");

    // Matches an extended Atx section title that includes support for the Markdown variant.
    public static final Pattern EXT_ATX_SECTION_TITLE_RX = compile("^(=={0,5}|##{0,5})[ \\t]+(" + CC_ANY
            + "+?)(?:[ \\t]+\\1)?$");

    // Matches the title only (first line) of an Setext (two-line) section title.
    // The title cannot begin with a dot and must have at least one alphanumeric character.
    public static final Pattern SETEXT_SECTION_TITLE_RX = compile("^((?!\\.)" + CC_ANY + "*?" + CG_ALNUM + CC_ANY
            + "*)$");

    // Matches an anchor (i.e., id + optional reference text) inside a section title.
    //
    // Examples
    //
    //   Section Title [[idname]]
    //   Section Title [[idname,Reference Text]]
    //
    public static final Pattern INLINE_SECTION_ANCHOR_RX = compile(" (\\\\)?\\[\\[([" + CC_ALPHA + "_:][" + CC_WORD
            + "\\-:.]*)(?:, *(" + CC_ANY + "+))?\\]\\]$");

    // Matches invalid ID characters in a section title.
    //
    // NOTE uppercase chars not included since expression is only run on a lowercase string
    public static final Pattern INVALID_SECTION_ID_CHARS_RX = compile(
            "<[^>]+>|&(?:[a-z][a-z]+\\d{0,2}|#\\d\\d\\d{0,4}|#x[\\da-f][\\da-f][\\da-f]{0,3});|[^ " + CC_WORD
                    + "\\-.]+?");

    // Matches an explicit section level style like sect1
    //
    public static final Pattern SECTION_LEVEL_STYLE_RX = compile("^sect\\d$");

    // ## Lists

    // Detects the start of any list item.
    //
    // NOTE we only have to check as far as the blank character because we know it means non-whitespace follows.
    // IMPORTANT if this regexp does not agree with the regexp for each list type, the parser will hang.
    public static final Pattern ANY_LIST_RX = compile("^(?:[ \\t]*(?:-|\\*\\**|\\.\\.*|\\u2022|\\d+\\.|[a-zA-Z]\\.|"
            + "[IVXivx]+\\))[ \\t]|(?!//[^/])[ \\t]*[^ \\t]" + CC_ANY + "*?(?::::{0,2}|;;)(?:$|[ \\t])|<(?:\\d+|\\.)>"
            + "[ \\t])");

    // Matches an unordered list item (one level for hyphens, up to 5 levels for asterisks).
    //
    // Examples
    //
    //   * Foo
    //   - Foo
    //
    // NOTE we know trailing (.*) will match at least one character because we strip trailing spaces
    public static final Pattern UNORDERED_LIST_RX = compile("^[ \\t]*(-|\\*\\**|\\u2022)[ \\t]+(" + CC_ANY + "*)$");

    // Matches an ordered list item (explicit numbering or up to 5 consecutive dots).
    //
    // Examples
    //
    //   . Foo
    //   .. Foo
    //   1. Foo (arabic, default)
    //   a. Foo (loweralpha)
    //   A. Foo (upperalpha)
    //   i. Foo (lowerroman)
    //   I. Foo (upperroman)
    //
    // NOTE leading space match is not always necessary, but is used for list reader
    // NOTE we know trailing (.*) will match at least one character because we strip trailing spaces
    public static final Pattern ORDERED_LIST_RX = compile("^[ \\t]*(\\.\\.*|\\d+\\.|[a-zA-Z]\\.|[IVXivx]+\\))[ \\t]+("
            + CC_ANY + "*)$");

    // Matches the ordinals for each type of ordered list.
    public static final Map<String, Pattern> ORDERED_LIST_MARKER_RX_MAP = Map.of(
            "arabic", compile("\\d+\\."),
            "loweralpha", compile("[a-z]\\."),
            "lowerroman", compile("[ivx]+\\)"),
            "upperalpha", compile("[A-Z]\\."),
            "upperroman", compile("[IVX]+\\)"));

    // Matches a description list entry.
    //
    // Examples
    //
    //   foo::
    //   bar:::
    //   baz::::
    //   blah;;
    //
    //   # the term may be followed by a description on the same line...
    //
    //   foo:: The metasyntactic variable that commonly accompanies 'bar' (see also, <<bar>>).
    //
    //   # ...or on a separate line, which may optionally be indented
    //
    //   foo::
    //     The metasyntactic variable that commonly accompanies 'bar' (see also, <<bar>>).
    //
    //   # attribute references may be used in both the term and the description
    //
    //   {foo-term}:: {foo-desc}
    //
    // NOTE we know trailing (.*) will match at least one character because we strip trailing spaces
    // NOTE must skip line comment when looking for next list item inside list
    public static final Pattern DESCRIPTION_LIST_RX = compile("^(?!//[^/])[ \\t]*([^ \\t]" + CC_ANY
            + "*?)(:::{0,2}|;;)(?:$|[ \\t]+(" + CC_ANY + "*)$)");

    // Matches a sibling description list item (excluding the delimiter specified by the key).
    // NOTE must skip line comment when looking for sibling list item
    public static final Map<String, Pattern> DESCRIPTION_LIST_SIBLING_RX = Map.of(
            "::", compile("^(?!//[^/])[ \\t]*([^ \\t]" + CC_ANY + "*?[^:]|[^ \\t:])(::)(?:$|[ \\t]+(" + CC_ANY
                    + "*)$)"),
            ":::", compile("^(?!//[^/])[ \\t]*([^ \\t]" + CC_ANY + "*?[^:]|[^ \\t:])(:::)(?:$|[ \\t]+(" + CC_ANY
                    + "*)$)"),
            "::::", compile("^(?!//[^/])[ \\t]*([^ \\t]" + CC_ANY + "*?[^:]|[^ \\t:])(::::)(?:$|[ \\t]+(" + CC_ANY
                    + "*)$)"),
            ";;", compile("^(?!//[^/])[ \\t]*([^ \\t]" + CC_ANY + "*?)(;;)(?:$|[ \\t]+(" + CC_ANY + "*)$)"));

    // Matches a callout list item.
    //
    // Examples
    //
    //   <1> Explanation
    //
    // or
    //
    //   <.> Explanation with automatic number
    //
    // NOTE we know trailing (.*) will match at least one character because we strip trailing spaces
    public static final Pattern CALLOUT_LIST_RX = compile("^<(\\d+|\\.)>[ \\t]+(" + CC_ANY + "*)$");

    // Matches a callout reference inside literal text.
    //
    // Examples
    //   <1> (optionally prefixed by //, #, -- or ;; line comment chars)
    //   <1> <2> (multiple callouts on one line)
    //   <!--1--> (for XML-based languages)
    //   <.> (auto-numbered)
    //
    // NOTE extract regexps are applied line-by-line, so we can use $ as end-of-line char
    public static final Pattern CALLOUT_EXTRACT_RX = compile(
            "((?://|#|--|;;) ?)?(\\\\)?<!?(|--)(\\d+|\\.)\\3>(?=(?: ?\\\\?<!?\\3(?:\\d+|\\.)\\3>)*$)");
    private static final String CALLOUT_EXTRACT_RXT = "(\\\\)?<()(\\d+|\\.)>(?=(?: ?\\\\?<(?:\\d+|\\.)>)*$)";
    private static final Map<String, Pattern> CALLOUT_EXTRACT_RX_MAP = new ConcurrentHashMap<>();

    public static Pattern calloutExtractRx(String lineComment) {
        return CALLOUT_EXTRACT_RX_MAP.computeIfAbsent(lineComment, key -> compile(
                "(" + (key.isEmpty() ? "" : escape(key) + " ?") + ")?" + CALLOUT_EXTRACT_RXT));
    }

    // NOTE special characters have not been replaced when scanning
    public static final Pattern CALLOUT_SCAN_RX = compile("\\\\?<!?(|--)(\\d+|\\.)\\1>(?=(?: ?\\\\?<!?\\1(?:\\d+|\\.)"
            + "\\1>)*" + CC_EOL + ")");
    // NOTE special characters have already been replaced when converting to an SGML format
    public static final Pattern CALLOUT_SOURCE_RX = compile("((?://|#|--|;;) ?)?(\\\\)?&lt;!?(|--)(\\d+|\\.)\\3&gt;"
            + "(?=(?: ?\\\\?&lt;!?\\3(?:\\d+|\\.)\\3&gt;)*" + CC_EOL + ")");
    private static final String CALLOUT_SOURCE_RXT = "(\\\\)?&lt;()(\\d+|\\.)&gt;(?=(?: ?\\\\?&lt;(?:\\d+|\\.)&gt;)*"
            + CC_EOL + ")";
    private static final Map<String, Pattern> CALLOUT_SOURCE_RX_MAP = new ConcurrentHashMap<>();

    public static Pattern calloutSourceRx(String lineComment) {
        return CALLOUT_SOURCE_RX_MAP.computeIfAbsent(lineComment, key -> compile(
                "(" + (key.isEmpty() ? "" : escape(key) + " ?") + ")?" + CALLOUT_SOURCE_RXT));
    }

    // A Map of regexps for lists used for dynamic access.
    public static final Map<String, Pattern> LIST_RX_MAP = Map.of(
            "ulist", UNORDERED_LIST_RX,
            "olist", ORDERED_LIST_RX,
            "dlist", DESCRIPTION_LIST_RX,
            "colist", CALLOUT_LIST_RX);

    // ## Tables

    // Parses the column spec (i.e., colspec) for a table.
    //
    // Examples
    //
    //   1*h,2*,^3e
    //
    public static final Pattern COLUMN_SPEC_RX = compile("^(?:(\\d+)\\*)?([<^>](?:\\.[<^>]?)?|(?:[<^>]?\\.)?[<^>])?"
            + "(\\d+%?|~)?([a-z])?$");

    // Parses the start and end of a cell spec (i.e., cellspec) for a table.
    //
    // Examples
    //
    //   2.3+<.>m
    //
    // FIXME use step-wise scan (or treetop) rather than this mega-regexp
    public static final Pattern CELL_SPEC_START_RX = compile("^[ \\t]*(?:(\\d+(?:\\.\\d*)?|(?:\\d*\\.)?\\d+)([*+]))?"
            + "([<^>](?:\\.[<^>]?)?|(?:[<^>]?\\.)?[<^>])?([a-z])?$");
    public static final Pattern CELL_SPEC_END_RX = compile("[ \\t]+(?:(\\d+(?:\\.\\d*)?|(?:\\d*\\.)?\\d+)([*+]))?"
            + "([<^>](?:\\.[<^>]?)?|(?:[<^>]?\\.)?[<^>])?([a-z])?$");

    // ## Block macros

    // Matches the custom block macro pattern.
    //
    // Examples
    //
    //   gist::123456[]
    //
    // NOTE we've relaxed the match for target to accommodate the short format (e.g., name::[attrlist])
    public static final Pattern CUSTOM_BLOCK_MACRO_RX = compile("^(" + CG_WORD + "[" + CC_WORD + "-]*)::(|\\S|\\S"
            + CC_ANY + "*?\\S)\\[(" + CC_ANY + "+)?\\]$");

    // Matches an image, video or audio block macro.
    //
    // Examples
    //
    //   image::filename.png[Caption]
    //   video::http://youtube.com/12345[Cats vs Dogs]
    //
    public static final Pattern BLOCK_MEDIA_MACRO_RX = compile("^(image|video|audio)::(\\S|\\S" + CC_ANY
            + "*?\\S)\\[(" + CC_ANY + "+)?\\]$");

    // Matches the TOC block macro.
    //
    // Examples
    //
    //   toc::[]
    //   toc::[levels=2]
    //
    public static final Pattern BLOCK_TOC_MACRO_RX = compile("^toc::\\[(" + CC_ANY + "+)?\\]$");

    // ## Inline macros

    // Matches an anchor (i.e., id + optional reference text) in the flow of text.
    //
    // Examples
    //
    //   [[idname]]
    //   [[idname,Reference Text]]
    //   anchor:idname[]
    //   anchor:idname[Reference Text]
    //
    public static final Pattern INLINE_ANCHOR_RX = compile("(\\\\)?(?:\\[\\[([" + CC_ALPHA + "_:][" + CC_WORD
            + "\\-:.]*)(?:, *(" + CC_ANY + "+?))? ?\\]\\]|anchor:([" + CC_ALPHA + "_:][" + CC_WORD
            + "\\-:.]*)\\[(?:\\]|(" + CC_ANY + "*?[^\\\\])\\]))");

    // Scans for a non-escaped anchor (i.e., id + optional reference text) in the flow of text.
    public static final Pattern INLINE_ANCHOR_SCAN_RX = compile("(?:^|[^\\\\\\[])\\[\\[([" + CC_ALPHA + "_:]["
            + CC_WORD + "\\-:.]*)(?:, *(" + CC_ANY + "+?))? ?\\]\\]|(?:^|[^\\\\])anchor:([" + CC_ALPHA + "_:]["
            + CC_WORD + "\\-:.]*)\\[(?:\\]|(" + CC_ANY + "*?[^\\\\])\\])");

    // Scans for a leading, non-escaped anchor (i.e., id + optional reference text).
    public static final Pattern LEADING_INLINE_ANCHOR_RX = compile("^\\[\\[([" + CC_ALPHA + "_:][" + CC_WORD
            + "\\-:.]*)(?:, *(" + CC_ANY + "+?))?\\]\\]");

    // Matches a bibliography anchor at the start of the list item text (in a bibliography list).
    //
    // Examples
    //
    //   [[[Fowler_1997]]] Fowler M. ...
    //
    public static final Pattern INLINE_BIBLIO_ANCHOR_RX = compile("^\\[\\[\\[([" + CC_ALPHA + "_:][" + CC_WORD
            + "\\-:.]*)(?:, *(" + CC_ANY + "+?))?\\]\\]\\]");

    // Matches an inline e-mail address.
    //
    //   doc.writer@example.com
    //
    public static final Pattern INLINE_EMAIL_RX = compile("([\\\\>:/])?" + CG_WORD + "(?:&amp;|[" + CC_WORD
            + "\\-.%+])*@" + CG_ALNUM + "[" + CC_ALNUM + "_\\-.]*\\.[a-zA-Z]{2,5}\\b");

    // Matches an inline footnote macro, which is allowed to span multiple lines.
    //
    // Examples
    //   footnote:[text] (not referenceable)
    //   footnote:id[text] (referenceable)
    //   footnote:id[] (reference)
    //   footnoteref:[id,text] (legacy)
    //   footnoteref:[id] (legacy)
    //
    public static final Pattern INLINE_FOOTNOTE_MACRO_RX = compileMultiline("\\\\?footnote(?:(ref):|:([" + CC_WORD
            + "-]+)?)\\[(?:|(" + CC_ALL + "*?[^\\\\]))\\](?!</a>)");

    // Matches an image or icon inline macro.
    //
    // Examples
    //
    //   image:filename.png[Alt Text]
    //   image:http://example.com/images/filename.png[Alt Text]
    //   image:filename.png[More [Alt\] Text] (alt text becomes "More [Alt] Text")
    //   icon:github[large]
    //
    // NOTE be as non-greedy as possible by not allowing newline or left square bracket in target
    public static final Pattern INLINE_IMAGE_MACRO_RX = compileMultiline("\\\\?i(?:mage|con):([^:\\s\\[](?:[^\\n\\[]*"
            + "[^\\s\\[])?)\\[(|" + CC_ALL + "*?[^\\\\])\\]");

    // Matches an indexterm inline macro, which may span multiple lines.
    //
    // Examples
    //
    //   indexterm:[Tigers,Big cats]
    //   (((Tigers,Big cats)))
    //   indexterm2:[Tigers]
    //   ((Tigers))
    //
    public static final Pattern INLINE_INDEXTERM_MACRO_RX = compileMultiline("\\\\?(?:(indexterm2?):\\[(" + CC_ALL
            + "*?[^\\\\])\\]|\\(\\((" + CC_ALL + "+?)\\)\\)(?!\\)))");

    // Matches either the kbd or btn inline macro.
    //
    // Examples
    //
    //   kbd:[F3]
    //   kbd:[Ctrl+Shift+T]
    //   kbd:[Ctrl+\]]
    //   kbd:[Ctrl,T]
    //   btn:[Save]
    //
    public static final Pattern INLINE_KBD_BTN_MACRO_RX = compileMultiline("(\\\\)?(kbd|btn):\\[(" + CC_ALL
            + "*?[^\\\\])\\]");

    // Matches an implicit link and some of the link inline macro.
    //
    // Examples
    //
    //   https://github.com
    //   https://github.com[GitHub]
    //   <https://github.com> <= angle brackets not included in autolink
    //   link:https://github.com[]
    //   "https://github.com[]"
    //   (https://github.com) <= parenthesis not included in autolink
    //
    public static final Pattern INLINE_LINK_RX = compileMultiline("(^|link:|" + CG_BLANK
            + "|\\\\?&lt;()|[>\\(\\)\\[\\];\"'])(\\\\?(?:https?|file|ftp|irc)://)(?:([^\\s\\[\\]]+)\\[(|" + CC_ALL
            + "*?[^\\\\])\\]|\\2([^\\s]+?)&gt;|([^\\s\\[\\]<]*([^\\s,.?!\\[\\]<\\)])))");

    // Match a link or e-mail inline macro.
    //
    // Examples
    //
    //   link:path[label]
    //   mailto:doc.writer@example.com[]
    //
    // NOTE be as non-greedy as possible by not allowing space or left square bracket in target
    public static final Pattern INLINE_LINK_MACRO_RX = compileMultiline("\\\\?(?:link|(mailto)):(|[^:\\s\\[][^\\s\\[]*)"
            + "\\[(|" + CC_ALL + "*?[^\\\\])\\]");

    // Matches the name of a macro.
    //
    public static final Pattern MACRO_NAME_RX = compile("^" + CG_WORD + "[" + CC_WORD + "-]*$");

    // Matches a stem (and alternatives, asciimath and latexmath) inline macro, which may span multiple lines.
    //
    // Examples
    //
    //   stem:[x != 0]
    //   asciimath:[x != 0]
    //   latexmath:[\sqrt{4} = 2]
    //
    public static final Pattern INLINE_STEM_MACRO_RX = compileMultiline("\\\\?(stem|(?:latex|ascii)math):([a-z]+(?:,"
            + "[a-z-]+)*)?\\[(" + CC_ALL + "*?[^\\\\])\\]");

    // Matches a menu inline macro.
    //
    // Examples
    //
    //   menu:File[Save As...]
    //   menu:Edit[]
    //   menu:View[Page Style > No Style]
    //   menu:View[Page Style, No Style]
    //
    public static final Pattern INLINE_MENU_MACRO_RX = compileMultiline("\\\\?menu:(" + CG_WORD + "|[" + CC_WORD
            + "&][^\\n\\[]*[^\\s\\[])\\[ *(?:|(" + CC_ALL + "*?[^\\\\]))\\]");

    // Matches an implicit menu inline macro.
    //
    // Examples
    //
    //   "File > New..."
    //
    public static final Pattern INLINE_MENU_RX = compile("\\\\?\"([" + CC_WORD + "&][^\"]*?[ \\n]+&gt;[ \\n]+[^\"]*)\"");

    /** Describes the inline passthrough syntax for a given compat mode. */
    public record InlinePass(String char1, String char2, Pattern pattern) {
    }

    // Matches an inline passthrough, which may span multiple lines.
    //
    // Examples
    //
    //   +text+
    //   [x-]+text+
    //   [x-]`text`
    //   `text` (compat only)
    //   [role]`text` (compat only)
    //
    // NOTE we always capture the attributes so we know when to use compatible (i.e., legacy) behavior
    public static final Map<Boolean, InlinePass> INLINE_PASS_RX = Map.of(
            false, new InlinePass("+", "-]", compileMultiline("((?:^|[^" + CC_WORD + ";:\\\\])(?=(\\[)|\\+)|\\\\(?=\\[)"
                    + "|(?=\\\\\\+))(?:\\2(x-|[^\\[\\]]+ x-)\\]|(?:" + QUOTE_ATTRIBUTE_LIST_RXT + ")?(?=(\\\\)?\\+))(\\5?"
                    + "(\\+|`)(\\S|\\S" + CC_ALL + "*?\\S)\\7)(?!" + CG_WORD + ")")),
            true, new InlinePass("`", null, compileMultiline("(^|[^`" + CC_WORD + "])(?:(\\Z)()|"
                    + QUOTE_ATTRIBUTE_LIST_RXT + "(?=(\\\\))?)?(\\5?(`)([^`\\s]|[^`\\s]" + CC_ALL + "*?\\S)\\7)(?![`"
                    + CC_WORD + "])")));

    // Matches several variants of the passthrough inline macro, which may span multiple lines.
    //
    // Examples
    //
    //   +++text+++
    //   $$text$$
    //   pass:quotes[text]
    //
    // NOTE we have to support an empty pass:[] for compatibility with AsciiDoc.py
    public static final Pattern INLINE_PASS_MACRO_RX = compileMultiline("(?:(?:(\\\\?)" + QUOTE_ATTRIBUTE_LIST_RXT
            + ")?(\\\\{0,2})(\\+\\+\\+?|\\$\\$)(" + CC_ALL + "*?)\\4|(\\\\?)pass:([a-z]+(?:,[a-z-]+)*)?\\[(|" + CC_ALL
            + "*?[^\\\\])\\])");

    // Matches an xref (i.e., cross-reference) inline macro, which may span multiple lines.
    //
    // Examples
    //
    //   <<id,reftext>>
    //   xref:id[reftext]
    //
    // NOTE special characters have already been escaped, hence the entity references
    // NOTE { is included in start characters to support target that begins with attribute reference in title content
    public static final Pattern INLINE_XREF_MACRO_RX = compileMultiline("\\\\?(?:&lt;&lt;([" + CC_WORD + "#/.:{]"
            + CC_ALL + "*?)&gt;&gt;|xref:([" + CC_WORD + "#/.:{]" + CC_ALL + "*?)\\[(?:\\]|(" + CC_ALL
            + "*?[^\\\\])\\]))");

    // ## Layout

    // Matches a trailing + preceded by at least one space character, which forces a hard line break (<br> tag in HTML
    // output).
    //
    // NOTE AsciiDoc.py allows + to be preceded by TAB; Asciidoctor does not
    //
    // Examples
    //
    //   Humpty Dumpty sat on a wall, +
    //   Humpty Dumpty had a great fall.
    //
    public static final Pattern HARD_LINE_BREAK_RX = compile("^(.*) \\+$");

    // Matches a Markdown horizontal rule.
    //
    // Examples
    //
    //   --- or - - -
    //   *** or * * *
    //   ___ or _ _ _
    //
    public static final Pattern MARKDOWN_THEMATIC_BREAK_RX = compile("^ {0,3}([-*_])( *)\\1\\2\\1$");

    // Matches an AsciiDoc or Markdown horizontal rule or AsciiDoc page break.
    //
    // Examples
    //
    //   ''' (horizontal rule)
    //   <<< (page break)
    //   --- or - - - (horizontal rule, Markdown)
    //   *** or * * * (horizontal rule, Markdown)
    //   ___ or _ _ _ (horizontal rule, Markdown)
    //
    public static final Pattern EXT_LAYOUT_BREAK_RX = compile("^(?:'{3,}|<{3,}|([-*_])( *)\\1\\2\\1)$");

    // ## General

    // Matches consecutive blank lines.
    //
    // Examples
    //
    //   one
    //
    //   two
    //
    public static final Pattern BLANK_LINE_RX = compile("\\n{2,}");

    // Matches whitespace (space, tab, newline) escaped by a backslash.
    //
    // Examples
    //
    //   three\ blind\ mice
    //
    public static final Pattern ESCAPED_SPACE_RX = compile("\\\\([ \\t\\n])");

    // Detects if text is a possible candidate for the replacements substitution.
    //
    public static final Pattern REPLACEABLE_TEXT_RX = compile("[&']|--|\\.\\.\\.|\\([CRT]M?\\)");

    // Matches a whitespace delimiter, a sequence of spaces, tabs, and/or newlines.
    // Matches the parsing rules of %w strings in Ruby.
    //
    // Examples
    //
    //   one two	 three   four
    //   five	six
    //
    public static final Pattern SPACE_DELIMITER_RX = compile("([^\\\\])[ \\t\\n]+");

    // Matches a + or - modifier in a subs list
    //
    public static final Pattern SUB_MODIFIER_SNIFF_RX = compile("[+-]");

    // Matches one or more consecutive digits at the end of a line.
    //
    // Examples
    //
    //   docbook5
    //   html5
    //
    public static final Pattern TRAILING_DIGITS_RX = compile("\\d+$");

    // Detects strings that resemble URIs.
    //
    // Examples
    //   http://domain
    //   https://domain
    //   file:///path
    //   data:info
    //
    //   not c:/sample.adoc or c:\sample.adoc
    //
    public static final Pattern URI_SNIFF_RX = compile("\\A" + CG_ALPHA + "[" + CC_ALNUM + ".+-]+:/{0,2}");

    // Detects XML tags
    public static final Pattern XML_SANITIZE_RX = compile("<[^>]+>");
}
