package org.asciidoctor.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A lenient HTML parser that builds a W3C DOM tree. It supports the subset of HTML produced by the converters (void
 * elements, raw text elements, comments, processing instructions, CDATA sections and character references).
 */
final class HtmlParser {
    private static final Set<String> VOID_ELEMENTS = Set.of("area", "base", "br", "col", "embed", "hr", "img",
            "input", "link", "meta", "param", "source", "track", "wbr");

    private static final Set<String> RAW_TEXT_ELEMENTS = Set.of("script", "style");

    private static final Map<String, String> ENTITIES = new HashMap<>();

    static {
        String[][] entities = {
            { "amp", "&" }, { "lt", "<" }, { "gt", ">" }, { "quot", "\"" }, { "apos", "'" }, { "nbsp", " " },
            { "copy", "©" }, { "reg", "®" }, { "trade", "™" }, { "mdash", "—" },
            { "ndash", "–" }, { "hellip", "…" }, { "lsquo", "‘" }, { "rsquo", "’" },
            { "ldquo", "“" }, { "rdquo", "”" }, { "deg", "°" }, { "zwsp", "​" },
            { "rarr", "→" }, { "larr", "←" }, { "rArr", "⇒" }, { "lArr", "⇐" },
            { "thinsp", " " }, { "middot", "·" }, { "bull", "•" }, { "laquo", "«" },
            { "raquo", "»" }, { "times", "×" }, { "brvbar", "¦" }, { "sect", "§" },
            { "para", "¶" }, { "euro", "€" }, { "pound", "£" }, { "yen", "¥" },
            { "cent", "¢" }, { "eacute", "é" }, { "egrave", "è" }, { "agrave", "à" },
            { "ccedil", "ç" }, { "uuml", "ü" }, { "ouml", "ö" }, { "auml", "ä" },
            { "szlig", "ß" }, { "iexcl", "¡" }, { "iquest", "¿" }, { "shy", "­" },
        };
        for (String[] entity : entities) {
            ENTITIES.put(entity[0], entity[1]);
        }
    }

    private final String input;
    private int pos;
    private final Document document;

    private HtmlParser(String input) {
        this.input = input;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            this.document = factory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Parses the HTML as a full document.
     */
    static Document parseDocument(String html) {
        HtmlParser parser = new HtmlParser(html);
        DocumentFragment fragment = parser.document.createDocumentFragment();
        parser.parseInto(fragment);
        Node root = null;
        for (Node child = fragment.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                root = child;
                break;
            }
        }
        if (root != null) {
            parser.document.appendChild(root);
        }
        return parser.document;
    }

    /**
     * Parses the HTML as a fragment.
     */
    static DocumentFragment parseFragment(String html) {
        HtmlParser parser = new HtmlParser(html);
        DocumentFragment fragment = parser.document.createDocumentFragment();
        parser.parseInto(fragment);
        return fragment;
    }

    private void parseInto(Node root) {
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);
        StringBuilder text = new StringBuilder();
        int len = input.length();
        while (pos < len) {
            char c = input.charAt(pos);
            if (c == '<') {
                if (input.startsWith("<!--", pos)) {
                    flushText(text, stack.peek());
                    int end = input.indexOf("-->", pos + 4);
                    end = end < 0 ? len : end;
                    stack.peek().appendChild(document.createComment(input.substring(pos + 4, end)));
                    pos = Math.min(len, end + 3);
                    continue;
                } else if (input.startsWith("<![CDATA[", pos)) {
                    int end = input.indexOf("]]>", pos + 9);
                    end = end < 0 ? len : end;
                    text.append(input, pos + 9, end);
                    pos = Math.min(len, end + 3);
                    continue;
                } else if (input.startsWith("<!", pos)) {
                    flushText(text, stack.peek());
                    int end = input.indexOf('>', pos);
                    pos = end < 0 ? len : end + 1;
                    continue;
                } else if (input.startsWith("<?", pos)) {
                    flushText(text, stack.peek());
                    int end = input.indexOf("?>", pos + 2);
                    end = end < 0 ? len : end;
                    String content = input.substring(pos + 2, end);
                    String[] parts = content.split("\\s+", 2);
                    if (!parts[0].isEmpty() && !parts[0].equalsIgnoreCase("xml")) {
                        try {
                            stack.peek().appendChild(document.createProcessingInstruction(parts[0],
                                    parts.length > 1 ? parts[1] : ""));
                        } catch (RuntimeException e) {
                            // ignore malformed processing instruction
                        }
                    }
                    pos = Math.min(len, end + 2);
                    continue;
                } else if (input.startsWith("</", pos)) {
                    int end = input.indexOf('>', pos);
                    if (end < 0) {
                        text.append(c);
                        pos++;
                        continue;
                    }
                    String name = input.substring(pos + 2, end).trim().toLowerCase(Locale.ROOT);
                    flushText(text, stack.peek());
                    // pop until matching element
                    boolean found = false;
                    for (Node node : stack) {
                        if (node instanceof Element element && element.getTagName().equals(name)) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        while (!stack.isEmpty()) {
                            Node node = stack.pop();
                            if (node instanceof Element element && element.getTagName().equals(name)) {
                                break;
                            }
                        }
                    }
                    pos = end + 1;
                    continue;
                } else if (pos + 1 < len && isNameStart(input.charAt(pos + 1))) {
                    flushText(text, stack.peek());
                    parseStartTag(stack);
                    continue;
                }
            } else if (c == '&') {
                pos = decodeEntity(text, pos);
                continue;
            }
            text.append(c);
            pos++;
        }
        flushText(text, stack.peek());
    }

    private static boolean isNameStart(char c) {
        return Character.isLetter(c) || c == '_' || c == ':';
    }

    private static boolean isNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '-' || c == '.';
    }

    private void parseStartTag(Deque<Node> stack) {
        int len = input.length();
        pos++; // skip <
        int start = pos;
        while (pos < len && isNameChar(input.charAt(pos))) {
            pos++;
        }
        String name = input.substring(start, pos).toLowerCase(Locale.ROOT);
        Element element = document.createElement(name);
        boolean selfClosing = false;
        while (pos < len) {
            char c = input.charAt(pos);
            if (Character.isWhitespace(c)) {
                pos++;
            } else if (c == '>') {
                pos++;
                break;
            } else if (c == '/' && pos + 1 < len && input.charAt(pos + 1) == '>') {
                selfClosing = true;
                pos += 2;
                break;
            } else {
                int attrStart = pos;
                while (pos < len && !Character.isWhitespace(input.charAt(pos)) && input.charAt(pos) != '='
                        && input.charAt(pos) != '>' && !(input.charAt(pos) == '/' && pos + 1 < len
                                && input.charAt(pos + 1) == '>')) {
                    pos++;
                }
                String attrName = input.substring(attrStart, pos).toLowerCase(Locale.ROOT);
                if (attrName.isEmpty()) {
                    pos++;
                    continue;
                }
                while (pos < len && Character.isWhitespace(input.charAt(pos))) {
                    pos++;
                }
                String value = "";
                if (pos < len && input.charAt(pos) == '=') {
                    pos++;
                    while (pos < len && Character.isWhitespace(input.charAt(pos))) {
                        pos++;
                    }
                    if (pos < len && (input.charAt(pos) == '"' || input.charAt(pos) == '\'')) {
                        char quote = input.charAt(pos);
                        int end = input.indexOf(quote, pos + 1);
                        end = end < 0 ? len : end;
                        value = decodeEntities(input.substring(pos + 1, end));
                        pos = Math.min(len, end + 1);
                    } else {
                        int valueStart = pos;
                        while (pos < len && !Character.isWhitespace(input.charAt(pos)) && input.charAt(pos) != '>') {
                            pos++;
                        }
                        value = decodeEntities(input.substring(valueStart, pos));
                    }
                }
                try {
                    element.setAttribute(attrName, value);
                } catch (RuntimeException e) {
                    // ignore invalid attribute names
                }
            }
        }
        stack.peek().appendChild(element);
        if (selfClosing || VOID_ELEMENTS.contains(name)) {
            return;
        }
        if (RAW_TEXT_ELEMENTS.contains(name)) {
            String endTag = "</" + name;
            int end = indexOfIgnoreCase(input, endTag, pos);
            end = end < 0 ? len : end;
            if (end > pos) {
                element.appendChild(document.createTextNode(input.substring(pos, end)));
            }
            int close = input.indexOf('>', end);
            pos = close < 0 ? len : close + 1;
            return;
        }
        stack.push(element);
    }

    private static int indexOfIgnoreCase(String str, String search, int from) {
        int max = str.length() - search.length();
        for (int i = from; i <= max; i++) {
            if (str.regionMatches(true, i, search, 0, search.length())) {
                return i;
            }
        }
        return -1;
    }

    private void flushText(StringBuilder text, Node parent) {
        if (text.length() > 0) {
            parent.appendChild(document.createTextNode(text.toString()));
            text.setLength(0);
        }
    }

    private String decodeEntities(String value) {
        if (value.indexOf('&') < 0) {
            return value;
        }
        StringBuilder result = new StringBuilder();
        HtmlParser sub = new HtmlParser(value);
        int i = 0;
        while (i < value.length()) {
            if (value.charAt(i) == '&') {
                i = sub.decodeEntity(result, i);
            } else {
                result.append(value.charAt(i++));
            }
        }
        return result.toString();
    }

    private int decodeEntity(StringBuilder out, int at) {
        int semi = input.indexOf(';', at);
        if (semi < 0 || semi - at > 12) {
            out.append('&');
            return at + 1;
        }
        String ref = input.substring(at + 1, semi);
        if (ref.startsWith("#x") || ref.startsWith("#X")) {
            try {
                out.appendCodePoint(Integer.parseInt(ref.substring(2), 16));
                return semi + 1;
            } catch (IllegalArgumentException e) {
                out.append('&');
                return at + 1;
            }
        } else if (ref.startsWith("#")) {
            try {
                out.appendCodePoint(Integer.parseInt(ref.substring(1)));
                return semi + 1;
            } catch (IllegalArgumentException e) {
                out.append('&');
                return at + 1;
            }
        }
        String decoded = ENTITIES.get(ref);
        if (decoded != null) {
            out.append(decoded);
            return semi + 1;
        }
        out.append('&');
        return at + 1;
    }

    /**
     * Serializes the node as HTML (void elements have no end tag, other elements always have one).
     */
    static void serialize(Node node, StringBuilder out) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE -> {
                String name = node.getNodeName();
                out.append('<').append(name);
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attribute = attributes.item(i);
                    out.append(' ').append(attribute.getNodeName()).append("=\"")
                            .append(escape(attribute.getNodeValue(), true)).append('"');
                }
                out.append('>');
                if (VOID_ELEMENTS.contains(name)) {
                    return;
                }
                serializeChildren(node, out);
                out.append("</").append(name).append('>');
            }
            case Node.TEXT_NODE -> {
                Node parent = node.getParentNode();
                boolean raw = parent != null && ("script".equals(parent.getNodeName())
                        || "style".equals(parent.getNodeName()));
                out.append(raw ? node.getNodeValue() : escape(node.getNodeValue(), false));
            }
            case Node.CDATA_SECTION_NODE -> out.append(node.getNodeValue());
            case Node.COMMENT_NODE -> out.append("<!--").append(node.getNodeValue()).append("-->");
            case Node.DOCUMENT_NODE, Node.DOCUMENT_FRAGMENT_NODE -> serializeChildren(node, out);
            default -> {
                // ignore other node types
            }
        }
    }

    private static void serializeChildren(Node node, StringBuilder out) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            serialize(children.item(i), out);
        }
    }

    private static String escape(String text, boolean attribute) {
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> result.append("&amp;");
                case '<' -> result.append(attribute ? "<" : "&lt;");
                case '>' -> result.append(attribute ? ">" : "&gt;");
                case '"' -> result.append(attribute ? "&quot;" : "\"");
                default -> result.append(c);
            }
        }
        return result.toString();
    }
}
