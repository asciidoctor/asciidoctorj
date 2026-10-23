package org.asciidoctor.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a CSS selector to an XPath expression, following the conventions of Nokogiri.
 *
 * <p>Supports type, universal, class, id and attribute selectors, the descendant, child, adjacent sibling and general
 * sibling combinators, selector groups, and the :root, :first-child, :last-child, :nth-child(n), :first-of-type,
 * :last-of-type, :nth-of-type(n), :empty, :not(...) and :contains(...) pseudo-classes.
 */
final class CssToXpath {
    private final String css;
    private final String elementPrefix;
    private int pos;

    private CssToXpath(String css, String elementPrefix) {
        this.css = css;
        this.elementPrefix = elementPrefix;
    }

    /**
     * Converts the CSS selector to an XPath expression relative to the context node (i.e., using the prefix .//).
     */
    static String convert(String css) {
        return convert(css, null);
    }

    /**
     * Converts the CSS selector to an XPath expression relative to the context node (i.e., using the prefix .//).
     *
     * @param namespacePrefix the namespace prefix to apply to element names (used when the document has a default
     * namespace), or null
     */
    static String convert(String css, String namespacePrefix) {
        return new CssToXpath(css.trim(), namespacePrefix == null ? "" : namespacePrefix + ":").parseGroup(".//");
    }

    private String parseGroup(String prefix) {
        List<String> alternatives = new ArrayList<>();
        alternatives.add(parseSelector(prefix));
        while (pos < css.length()) {
            skipWhitespace();
            if (pos < css.length() && css.charAt(pos) == ',') {
                pos++;
                skipWhitespace();
                alternatives.add(parseSelector(prefix));
            } else {
                break;
            }
        }
        return String.join(" | ", alternatives);
    }

    private String parseSelector(String prefix) {
        StringBuilder xpath = new StringBuilder(prefix);
        xpath.append(parseCompound());
        while (pos < css.length()) {
            int save = pos;
            boolean whitespace = skipWhitespace();
            if (pos >= css.length() || css.charAt(pos) == ',' || css.charAt(pos) == ')') {
                pos = pos >= css.length() || css.charAt(pos) == ',' || css.charAt(pos) == ')' ? pos : save;
                break;
            }
            char c = css.charAt(pos);
            if (c == '>') {
                pos++;
                skipWhitespace();
                xpath.append('/').append(parseCompound());
            } else if (c == '+') {
                pos++;
                skipWhitespace();
                String compound = parseCompound();
                xpath.append("/following-sibling::*[1]/self::").append(compound);
            } else if (c == '~') {
                pos++;
                skipWhitespace();
                xpath.append("/following-sibling::").append(parseCompound());
            } else if (whitespace) {
                xpath.append("//").append(parseCompound());
            } else {
                throw new IllegalArgumentException("Unexpected character in CSS selector at " + pos + ": " + css);
            }
        }
        return xpath.toString();
    }

    private boolean skipWhitespace() {
        boolean skipped = false;
        while (pos < css.length() && Character.isWhitespace(css.charAt(pos))) {
            pos++;
            skipped = true;
        }
        return skipped;
    }

    private String parseCompound() {
        String tag = "*";
        List<String> predicates = new ArrayList<>();
        if (pos < css.length()) {
            char c = css.charAt(pos);
            if (c == '*') {
                pos++;
            } else if (isIdentChar(c)) {
                tag = elementPrefix + parseIdent();
            }
        }
        while (pos < css.length()) {
            char c = css.charAt(pos);
            if (c == '.') {
                pos++;
                String cls = parseIdent();
                predicates.add("contains(concat(' ', normalize-space(@class), ' '), ' " + cls + " ')");
            } else if (c == '#') {
                pos++;
                predicates.add("@id = '" + parseIdent() + "'");
            } else if (c == '[') {
                predicates.add(parseAttribute());
            } else if (c == ':') {
                pos++;
                predicates.add(parsePseudo());
            } else {
                break;
            }
        }
        StringBuilder result = new StringBuilder(tag);
        for (String predicate : predicates) {
            result.append('[').append(predicate).append(']');
        }
        return result.toString();
    }

    private static boolean isIdentChar(char c) {
        return Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == '|';
    }

    private String parseIdent() {
        int start = pos;
        while (pos < css.length() && isIdentChar(css.charAt(pos))) {
            pos++;
        }
        return css.substring(start, pos);
    }

    private String parseAttribute() {
        pos++; // [
        skipWhitespace();
        String name = parseIdent();
        String attr;
        if (css.startsWith("()", pos)) {
            // a function, such as text(), as supported by Nokogiri
            pos += 2;
            attr = name + "()";
        } else {
            attr = name.contains("|") ? "@*[name()='" + name.replace('|', ':') + "']" : "@" + name;
        }
        skipWhitespace();
        if (css.charAt(pos) == ']') {
            pos++;
            return attr;
        }
        String op;
        if (css.charAt(pos) == '=') {
            op = "=";
            pos++;
        } else {
            op = css.substring(pos, pos + 2);
            pos += 2;
        }
        skipWhitespace();
        String value;
        char quote = css.charAt(pos);
        if (quote == '"' || quote == '\'') {
            int end = css.indexOf(quote, pos + 1);
            value = css.substring(pos + 1, end);
            pos = end + 1;
        } else {
            int start = pos;
            while (pos < css.length() && css.charAt(pos) != ']') {
                pos++;
            }
            value = css.substring(start, pos).trim();
        }
        skipWhitespace();
        pos++; // ]
        String literal = literal(value);
        return switch (op) {
            case "=" -> attr + " = " + literal;
            case "^=" -> "starts-with(" + attr + ", " + literal + ")";
            case "$=" -> "substring(" + attr + ", string-length(" + attr + ") - string-length(" + literal
                    + ") + 1, string-length(" + literal + ")) = " + literal;
            case "*=" -> "contains(" + attr + ", " + literal + ")";
            case "~=" -> "contains(concat(' ', normalize-space(" + attr + "), ' '), concat(' ', " + literal
                    + ", ' '))";
            case "|=" -> attr + " = " + literal + " or starts-with(" + attr + ", concat(" + literal + ", '-'))";
            case "!=" -> "not(" + attr + " = " + literal + ")";
            default -> throw new IllegalArgumentException("Unsupported attribute operator: " + op);
        };
    }

    private static String literal(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        } else if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        return "concat('" + value.replace("'", "', \"'\", '") + "')";
    }

    private String parsePseudo() {
        String name = parseIdent();
        String arg = null;
        if (pos < css.length() && css.charAt(pos) == '(') {
            int depth = 1;
            int start = ++pos;
            while (pos < css.length() && depth > 0) {
                char c = css.charAt(pos);
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                }
                pos++;
            }
            arg = css.substring(start, pos - 1).trim();
        }
        return switch (name) {
            case "root" -> "not(parent::*)";
            case "first-child" -> "count(preceding-sibling::*) = 0";
            case "last-child" -> "count(following-sibling::*) = 0";
            case "only-child" -> "count(preceding-sibling::*) = 0 and count(following-sibling::*) = 0";
            case "nth-child" -> "count(preceding-sibling::*) = " + (Integer.parseInt(arg) - 1);
            case "nth-last-child" -> "count(following-sibling::*) = " + (Integer.parseInt(arg) - 1);
            case "first-of-type", "first" -> "position() = 1";
            case "last-of-type", "last" -> "position() = last()";
            case "nth-of-type" -> "position() = " + Integer.parseInt(arg);
            case "empty" -> "not(node())";
            case "contains" -> "contains(., " + literal(stripQuotes(arg)) + ")";
            case "not" -> {
                CssToXpath inner = new CssToXpath(arg, elementPrefix);
                String compound = inner.parseCompound();
                // drop the tag (or *) and convert the predicates
                int bracket = compound.indexOf('[');
                String tag = bracket < 0 ? compound : compound.substring(0, bracket);
                List<String> parts = new ArrayList<>();
                if (!tag.equals("*")) {
                    parts.add("self::" + tag);
                }
                if (bracket >= 0) {
                    String preds = compound.substring(bracket);
                    for (String pred : splitPredicates(preds)) {
                        parts.add(pred);
                    }
                }
                yield "not(" + String.join(" and ", parts) + ")";
            }
            default -> throw new IllegalArgumentException("Unsupported pseudo-class: " + name);
        };
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
                || value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static List<String> splitPredicates(String preds) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = -1;
        char quote = 0;
        for (int i = 0; i < preds.length(); i++) {
            char c = preds.charAt(i);
            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                }
                continue;
            }
            if (c == '\'' || c == '"') {
                quote = c;
            } else if (c == '[') {
                if (depth == 0) {
                    start = i + 1;
                }
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    result.add(preds.substring(start, i));
                }
            }
        }
        return result;
    }
}
