package org.asciidoctor.nativeimage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A small JSON reader and writer. Objects are {@link Map}s that keep the order of the keys, arrays are
 * {@link List}s.
 */
final class Json {

    private final String text;
    private int position;

    private Json(String text) {
        this.text = text;
    }

    static Object parse(String text) {
        final Json json = new Json(text);
        final Object value = json.readValue();
        json.skipWhitespace();
        if (json.position != text.length()) {
            throw json.error("Unexpected content");
        }
        return value;
    }

    static String write(Object value) {
        final StringBuilder out = new StringBuilder();
        write(value, out, 0);
        return out.append('\n').toString();
    }

    private static void write(Object value, StringBuilder out, int level) {
        if (value instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                out.append("{}");
                return;
            }
            out.append("{\n");
            int index = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                indent(out, level + 1);
                writeString(entry.getKey().toString(), out);
                out.append(": ");
                write(entry.getValue(), out, level + 1);
                out.append(++index < map.size() ? ",\n" : "\n");
            }
            indent(out, level);
            out.append('}');
        } else if (value instanceof List<?> list) {
            if (list.isEmpty()) {
                out.append("[]");
                return;
            }
            out.append("[\n");
            for (int index = 0; index < list.size(); index++) {
                indent(out, level + 1);
                write(list.get(index), out, level + 1);
                out.append(index + 1 < list.size() ? ",\n" : "\n");
            }
            indent(out, level);
            out.append(']');
        } else if (value instanceof String string) {
            writeString(string, out);
        } else {
            out.append(value);
        }
    }

    private static void indent(StringBuilder out, int level) {
        out.append("  ".repeat(level));
    }

    private static void writeString(String string, StringBuilder out) {
        out.append('"');
        for (char c : string.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }

    private Object readValue() {
        skipWhitespace();
        if (position >= text.length()) {
            throw error("Unexpected end");
        }
        final char c = text.charAt(position);
        if (c == '{') {
            return readObject();
        } else if (c == '[') {
            return readArray();
        } else if (c == '"') {
            return readString();
        } else if (text.startsWith("true", position)) {
            position += 4;
            return Boolean.TRUE;
        } else if (text.startsWith("false", position)) {
            position += 5;
            return Boolean.FALSE;
        } else if (text.startsWith("null", position)) {
            position += 4;
            return null;
        }
        return readNumber();
    }

    private Map<String, Object> readObject() {
        final Map<String, Object> map = new LinkedHashMap<>();
        position++;
        skipWhitespace();
        if (peek() == '}') {
            position++;
            return map;
        }
        while (true) {
            skipWhitespace();
            final String key = readString();
            skipWhitespace();
            expect(':');
            map.put(key, readValue());
            skipWhitespace();
            if (peek() == ',') {
                position++;
            } else {
                expect('}');
                return map;
            }
        }
    }

    private List<Object> readArray() {
        final List<Object> list = new ArrayList<>();
        position++;
        skipWhitespace();
        if (peek() == ']') {
            position++;
            return list;
        }
        while (true) {
            list.add(readValue());
            skipWhitespace();
            if (peek() == ',') {
                position++;
            } else {
                expect(']');
                return list;
            }
        }
    }

    private String readString() {
        expect('"');
        final StringBuilder out = new StringBuilder();
        while (true) {
            if (position >= text.length()) {
                throw error("Unterminated string");
            }
            final char c = text.charAt(position++);
            if (c == '"') {
                return out.toString();
            }
            if (c != '\\') {
                out.append(c);
                continue;
            }
            final char escaped = text.charAt(position++);
            switch (escaped) {
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'u' -> {
                    out.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                    position += 4;
                }
                default -> out.append(escaped);
            }
        }
    }

    private Object readNumber() {
        final int start = position;
        while (position < text.length() && "+-0123456789.eE".indexOf(text.charAt(position)) >= 0) {
            position++;
        }
        if (start == position) {
            throw error("Unexpected character");
        }
        final String number = text.substring(start, position);
        return number.contains(".") || number.contains("e") || number.contains("E")
                ? (Object) Double.valueOf(number) : (Object) Long.valueOf(number);
    }

    private char peek() {
        if (position >= text.length()) {
            throw error("Unexpected end");
        }
        return text.charAt(position);
    }

    private void expect(char expected) {
        if (peek() != expected) {
            throw error("Expected '" + expected + "'");
        }
        position++;
    }

    private void skipWhitespace() {
        while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
            position++;
        }
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at position " + position);
    }
}
