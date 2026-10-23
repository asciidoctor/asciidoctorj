package org.asciidoctor.core;

import static org.asciidoctor.core.Asciidoctor.CC_WORD;
import static org.asciidoctor.core.Asciidoctor.CG_WORD;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles parsing AsciiDoc attribute lists into a Map of key/value pairs. By default, attributes must each be separated
 * by a comma and quotes may be used around the value. If a key is not detected, the value is assigned to a 1-based
 * positional key (e.g., "1"). The positional attributes can be "rekeyed" when given a positional attribute names list
 * either during parsing or after the fact.
 *
 * <pre>
 *   attrlist = new AttributeList("astyle");
 *
 *   attrlist.parse();
 *   // => { "1" => "astyle" }
 *
 *   attrlist.rekey(List.of("style"));
 *   // => { "1" => "astyle", "style" => "astyle" }
 *
 *   attrlist = new AttributeList("quote, Famous Person, Famous Book (2001)");
 *
 *   attrlist.parse(List.of("style", "attribution", "citetitle"));
 *   // => { "style" => "quote", "attribution" => "Famous Person", "citetitle" => "Famous Book (2001)", ... }
 * </pre>
 */
public class AttributeList {
    private static final char APOS = '\'';
    private static final char BACKSLASH = '\\';
    private static final char QUOT = '"';

    /** Regular expressions for detecting the boundary of a value. */
    private static final Map<String, Pattern> BOUNDARY_RX = Map.of(
            "\"", Rx.compile(".*?[^\\\\](?=\")"),
            "'", Rx.compile(".*?[^\\\\](?=')"),
            ",", Rx.compile(".*?(?=[ \\t]*(,|$))"));

    /** Regular expressions for unescaping quoted characters. */
    private static final Map<Character, String> ESCAPED_QUOTES = Map.of(
            QUOT, "\\\"",
            APOS, "\\'");

    /** A regular expression for an attribute name (matches syntax of a document attribute). */
    private static final Pattern NAME_RX = Rx.compile(CG_WORD + "[" + CC_WORD + "-]*");

    private static final Pattern BLANK_RX = Rx.compile("[ \\t]+");

    /** Regular expressions for skipping delimiters. */
    private static final Map<String, Pattern> SKIP_RX = Map.of(",", Rx.compile("[ \\t]*(,|$)"));

    private final Scanner scanner;
    private final AbstractNode block;
    private final String delimiter;
    private final Pattern delimiterSkipPattern;
    private final Pattern delimiterBoundaryPattern;
    private Map<String, Object> attributes;

    public AttributeList(String source) {
        this(source, null, ",");
    }

    public AttributeList(String source, AbstractNode block) {
        this(source, block, ",");
    }

    public AttributeList(String source, AbstractNode block, String delimiter) {
        this.scanner = new Scanner(source);
        this.block = block;
        this.delimiter = delimiter;
        this.delimiterSkipPattern = SKIP_RX.getOrDefault(delimiter,
                Rx.compile("[ \\t]*(" + Rx.escape(delimiter) + "|$)"));
        this.delimiterBoundaryPattern = BOUNDARY_RX.getOrDefault(delimiter,
                Rx.compile(".*?(?=[ \\t]*(" + Rx.escape(delimiter) + "|$))"));
    }

    public Map<String, Object> parseInto(Map<String, Object> attributes, List<String> positionalAttrs) {
        attributes.putAll(parse(positionalAttrs));
        return attributes;
    }

    public Map<String, Object> parseInto(Map<String, Object> attributes) {
        return parseInto(attributes, List.of());
    }

    public Map<String, Object> parse() {
        return parse(List.of());
    }

    public Map<String, Object> parse(List<String> positionalAttrs) {
        // return if already parsed
        if (attributes != null) {
            return attributes;
        }

        attributes = new LinkedHashMap<>();
        int index = 0;

        while (parseAttribute(index, positionalAttrs)) {
            if (scanner.isEos()) {
                break;
            }
            skipDelimiter();
            index++;
        }

        return attributes;
    }

    public Map<String, Object> rekey(List<String> positionalAttrs) {
        return rekey(attributes, positionalAttrs);
    }

    public static Map<String, Object> rekey(Map<String, Object> attributes, List<String> positionalAttrs) {
        for (int index = 0; index < positionalAttrs.size(); index++) {
            String key = positionalAttrs.get(index);
            Object val;
            if (key != null && (val = attributes.get(String.valueOf(index + 1))) != null) {
                // QUESTION should we delete the positional key?
                attributes.put(key, val);
            }
        }
        return attributes;
    }

    private boolean parseAttribute(int index, List<String> positionalAttrs) {
        boolean continueParsing = true;
        boolean singleQuoted = false;
        String name;
        String value = null;
        skipBlank();
        String next = scanner.peek(1);
        // example: "quote" || "foo
        if (next.equals("\"")) {
            name = parseAttributeValue(scanner.getChar());
        // example: 'quote' || 'foo
        } else if (next.equals("'")) {
            name = parseAttributeValue(scanner.getChar());
            if (!name.startsWith("'")) {
                singleQuoted = true;
            }
        } else {
            name = scanName();
            int skipped = 0;
            if (name != null) {
                skipped = skipBlank();
            }
            Character c;
            if (scanner.isEos()) {
                if (name == null && !Strings.rstrip(scanner.getString()).endsWith(delimiter)) {
                    return false;
                }
                // example: quote (at eos)
                continueParsing = false;
            // example: quote,
            } else if (String.valueOf(c = scanner.getChar()).equals(delimiter)) {
                scanner.unscan();
            } else if (name != null) {
                // example: foo=...
                if (c == '=') {
                    skipBlank();
                    c = scanner.getChar();
                    // example: foo="bar" || foo="ba\"zaar" || foo="bar
                    if (c != null && c == QUOT) {
                        value = parseAttributeValue(c);
                    // example: foo='bar' || foo='ba\'zaar' || foo='ba"zaar' || foo='bar
                    } else if (c != null && c == APOS) {
                        value = parseAttributeValue(c);
                        if (!value.startsWith("'")) {
                            singleQuoted = true;
                        }
                    // example: foo=,
                    } else if (c != null && String.valueOf(c).equals(delimiter)) {
                        value = "";
                        scanner.unscan();
                    // example: foo= (at eos)
                    } else if (c == null) {
                        value = "";
                    // example: foo=bar || foo=None
                    } else {
                        value = c + nullToEmpty(scanToDelimiter());
                        if (value.equals("None")) {
                            return true;
                        }
                    }
                // example: foo bar
                } else {
                    name = name + " ".repeat(skipped) + c + nullToEmpty(scanToDelimiter());
                }
            // example: =foo= || !foo
            } else {
                name = c + nullToEmpty(scanToDelimiter());
            }
        }

        if (value != null) {
            // example: options="opt1,opt2,opt3" || opts="opts1,opt2,opt3"
            switch (name) {
                case "options", "opts" -> {
                    if (value.contains(",")) {
                        if (value.contains(" ")) {
                            value = value.replace(" ", "");
                        }
                        for (String opt : Strings.split(value, ",")) {
                            if (!opt.isEmpty()) {
                                attributes.put(opt + "-option", "");
                            }
                        }
                    } else if (!value.isEmpty()) {
                        attributes.put(value + "-option", "");
                    }
                }
                default -> {
                    if (singleQuoted && block != null) {
                        switch (name) {
                            case "title", "reftext" -> attributes.put(name, value);
                            default -> attributes.put(name, block.applySubs(value));
                        }
                    } else {
                        attributes.put(name, value);
                    }
                }
            }
        } else {
            if (singleQuoted && block != null) {
                name = block.applySubs(name);
            }
            String positionalAttrName;
            if (index < positionalAttrs.size() && (positionalAttrName = positionalAttrs.get(index)) != null
                    && name != null) {
                attributes.put(positionalAttrName, name);
            }
            // QUESTION should we assign the positional key even when it's claimed by a positional attribute?
            attributes.put(String.valueOf(index + 1), name);
        }

        return continueParsing;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String parseAttributeValue(char quote) {
        String quoteStr = String.valueOf(quote);
        // empty quoted value
        if (scanner.peek(1).equals(quoteStr)) {
            scanner.getChar();
            return "";
        }
        String value = scanToQuote(quoteStr);
        if (value != null) {
            scanner.getChar();
            return value.indexOf(BACKSLASH) >= 0 ? value.replace(ESCAPED_QUOTES.get(quote), quoteStr) : value;
        }
        // leading quote only
        return quote + nullToEmpty(scanToDelimiter());
    }

    private int skipBlank() {
        Integer skipped = scanner.skip(BLANK_RX);
        return skipped == null ? 0 : skipped;
    }

    private void skipDelimiter() {
        scanner.skip(delimiterSkipPattern);
    }

    private String scanName() {
        return scanner.scan(NAME_RX);
    }

    private String scanToDelimiter() {
        return scanner.scan(delimiterBoundaryPattern);
    }

    private String scanToQuote(String quote) {
        return scanner.scan(BOUNDARY_RX.get(quote));
    }

    /**
     * A minimal string scanner that mirrors the semantics of Ruby's StringScanner.
     */
    static final class Scanner {
        private final String string;
        private int pos;
        private int prevPos = -1;

        Scanner(String string) {
            this.string = string;
        }

        String getString() {
            return string;
        }

        boolean isEos() {
            return pos >= string.length();
        }

        String peek(int len) {
            return string.substring(pos, Math.min(pos + len, string.length()));
        }

        Character getChar() {
            if (isEos()) {
                return null;
            }
            prevPos = pos;
            return string.charAt(pos++);
        }

        void unscan() {
            if (prevPos >= 0) {
                pos = prevPos;
                prevPos = -1;
            }
        }

        String scan(Pattern pattern) {
            Matcher matcher = pattern.matcher(string);
            matcher.region(pos, string.length());
            matcher.useTransparentBounds(true);
            matcher.useAnchoringBounds(false);
            if (matcher.lookingAt()) {
                prevPos = pos;
                pos = matcher.end();
                return matcher.group();
            }
            return null;
        }

        Integer skip(Pattern pattern) {
            String matched = scan(pattern);
            return matched == null ? null : matched.length();
        }
    }
}
