package org.asciidoctor.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Internal: String operations that mirror the semantics of the Ruby core String methods relied upon by the processor
 * (e.g., whitespace stripping limited to ASCII whitespace, splitting that drops trailing empty entries, etc).
 */
public final class Strings {
    private Strings() {
    }

    /**
     * Checks whether the value is null or empty. Works with strings, collections and maps (mirrors nil_or_empty?).
     */
    public static boolean isNullOrEmpty(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof CharSequence str) {
            return str.isEmpty();
        } else if (value instanceof Collection<?> coll) {
            return coll.isEmpty();
        } else if (value instanceof Map<?, ?> map) {
            return map.isEmpty();
        }
        return false;
    }

    /**
     * Returns whether the character is considered whitespace by Ruby's strip methods.
     */
    public static boolean isRubySpace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\u000b' || c == '\f' || c == '\r' || c == '\0';
    }

    public static String rstrip(String str) {
        int end = str.length();
        while (end > 0 && isRubySpace(str.charAt(end - 1))) {
            end--;
        }
        return end == str.length() ? str : str.substring(0, end);
    }

    public static String lstrip(String str) {
        int start = 0;
        int len = str.length();
        while (start < len && isRubySpace(str.charAt(start)) && str.charAt(start) != '\0') {
            start++;
        }
        return start == 0 ? str : str.substring(start);
    }

    public static String strip(String str) {
        return lstrip(rstrip(str));
    }

    /**
     * Removes the trailing line ending (\n, \r\n or \r) from the string, if present.
     */
    public static String chomp(String str) {
        if (str.endsWith("\r\n")) {
            return str.substring(0, str.length() - 2);
        } else if (str.endsWith("\n") || str.endsWith("\r")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * Removes the specified suffix from the string, if present.
     */
    public static String chomp(String str, String suffix) {
        return str.endsWith(suffix) ? str.substring(0, str.length() - suffix.length()) : str;
    }

    /**
     * Removes the last character from the string.
     */
    public static String chop(String str) {
        if (str.isEmpty()) {
            return str;
        }
        return str.endsWith("\r\n") ? str.substring(0, str.length() - 2) : str.substring(0, str.length() - 1);
    }

    /**
     * Collapses runs of the specified character into a single character.
     */
    public static String squeeze(String str, char c) {
        if (str.indexOf(c) < 0) {
            return str;
        }
        StringBuilder result = new StringBuilder(str.length());
        char prev = 0;
        boolean first = true;
        for (int i = 0, len = str.length(); i < len; i++) {
            char ch = str.charAt(i);
            if (!first && ch == c && prev == c) {
                continue;
            }
            result.append(ch);
            prev = ch;
            first = false;
        }
        return result.toString();
    }

    /**
     * Collapses runs of any repeating character into a single character.
     */
    public static String squeeze(String str) {
        StringBuilder result = new StringBuilder(str.length());
        for (int i = 0, len = str.length(); i < len; i++) {
            char ch = str.charAt(i);
            if (i > 0 && ch == str.charAt(i - 1)) {
                continue;
            }
            result.append(ch);
        }
        return result.toString();
    }

    /**
     * Replaces each occurrence of the characters in from with the corresponding character in to (mirrors String#tr for
     * simple character lists). If to is shorter than from, the last character in to is repeated.
     */
    public static String tr(String str, String from, String to) {
        StringBuilder result = null;
        for (int i = 0, len = str.length(); i < len; i++) {
            char ch = str.charAt(i);
            int idx = from.indexOf(ch);
            if (idx >= 0) {
                if (result == null) {
                    result = new StringBuilder(str.length());
                    result.append(str, 0, i);
                }
                if (!to.isEmpty()) {
                    result.append(to.charAt(Math.min(idx, to.length() - 1)));
                }
            } else if (result != null) {
                result.append(ch);
            }
        }
        return result == null ? str : result.toString();
    }

    /**
     * Translates the characters in from to the corresponding characters in to, then squeezes runs of translated
     * characters (mirrors String#tr_s).
     */
    public static String trS(String str, String from, String to) {
        StringBuilder result = new StringBuilder(str.length());
        int lastTranslated = -1;
        for (int i = 0, len = str.length(); i < len; i++) {
            char ch = str.charAt(i);
            int idx = from.indexOf(ch);
            if (idx >= 0) {
                char replacement = to.charAt(Math.min(idx, to.length() - 1));
                if (lastTranslated == replacement) {
                    continue;
                }
                result.append(replacement);
                lastTranslated = replacement;
            } else {
                result.append(ch);
                lastTranslated = -1;
            }
        }
        return result.toString();
    }

    /**
     * Removes all occurrences of the characters in chars from the string (mirrors String#delete).
     */
    public static String delete(String str, String chars) {
        StringBuilder result = new StringBuilder(str.length());
        for (int i = 0, len = str.length(); i < len; i++) {
            char ch = str.charAt(i);
            if (chars.indexOf(ch) < 0) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * Counts the occurrences of the specified character in the string.
     */
    public static int count(String str, char c) {
        int count = 0;
        for (int i = 0, len = str.length(); i < len; i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    /**
     * Splits the string on the literal delimiter, dropping trailing empty entries (mirrors String#split).
     */
    public static List<String> split(String str, String delimiter) {
        return split(str, delimiter, 0);
    }

    /**
     * Splits the string on the literal delimiter (mirrors String#split with a limit).
     *
     * <p>A limit of 0 drops trailing empty entries, a negative limit keeps them and a positive limit restricts the
     * number of entries.
     */
    public static List<String> split(String str, String delimiter, int limit) {
        List<String> result = new ArrayList<>();
        if (str.isEmpty()) {
            return result;
        }
        if (delimiter.equals(" ")) {
            return splitOnWhitespace(str, limit);
        }
        int start = 0;
        int idx;
        while ((limit <= 0 || result.size() < limit - 1) && (idx = str.indexOf(delimiter, start)) >= 0) {
            result.add(str.substring(start, idx));
            start = idx + delimiter.length();
        }
        result.add(str.substring(start));
        if (limit == 0) {
            int last = result.size();
            while (last > 0 && result.get(last - 1).isEmpty()) {
                result.remove(--last);
            }
        }
        return result;
    }

    /**
     * Splits the string on runs of whitespace, ignoring leading whitespace (mirrors String#split with no arguments).
     */
    public static List<String> splitOnWhitespace(String str) {
        return splitOnWhitespace(str, 0);
    }

    public static List<String> splitOnWhitespace(String str, int limit) {
        List<String> result = new ArrayList<>();
        int len = str.length();
        int i = 0;
        while (i < len) {
            while (i < len && isRubySpace(str.charAt(i)) && str.charAt(i) != '\0') {
                i++;
            }
            if (i >= len) {
                break;
            }
            if (limit > 0 && result.size() == limit - 1) {
                result.add(str.substring(i));
                return result;
            }
            int start = i;
            while (i < len && !(isRubySpace(str.charAt(i)) && str.charAt(i) != '\0')) {
                i++;
            }
            result.add(str.substring(start, i));
        }
        return result;
    }

    /**
     * Splits the string at the first occurrence of the separator (mirrors String#partition).
     *
     * @return an array containing the part before the separator, the separator (or empty string) and the part after
     */
    public static String[] partition(String str, String separator) {
        int idx = str.indexOf(separator);
        if (idx < 0) {
            return new String[] { str, "", "" };
        }
        return new String[] { str.substring(0, idx), separator, str.substring(idx + separator.length()) };
    }

    /**
     * Splits the string at the last occurrence of the separator (mirrors String#rpartition).
     */
    public static String[] rpartition(String str, String separator) {
        int idx = str.lastIndexOf(separator);
        if (idx < 0) {
            return new String[] { "", "", str };
        }
        return new String[] { str.substring(0, idx), separator, str.substring(idx + separator.length()) };
    }

    /**
     * Parses the leading integer of the string, returning 0 if there is none (mirrors String#to_i).
     */
    public static long toLong(String str) {
        if (str == null) {
            return 0;
        }
        int len = str.length();
        int i = 0;
        while (i < len && isRubySpace(str.charAt(i))) {
            i++;
        }
        boolean negative = false;
        if (i < len && (str.charAt(i) == '-' || str.charAt(i) == '+')) {
            negative = str.charAt(i) == '-';
            i++;
        }
        long result = 0;
        boolean digits = false;
        while (i < len) {
            char c = str.charAt(i);
            if (c >= '0' && c <= '9') {
                result = result * 10 + (c - '0');
                digits = true;
            } else if (c == '_' && digits && i + 1 < len && Character.isDigit(str.charAt(i + 1))) {
                // underscore separators are permitted between digits
            } else {
                break;
            }
            i++;
        }
        return negative ? -result : result;
    }

    /**
     * Converts the value to an integer using the semantics of Ruby's to_i.
     */
    public static int toInt(Object value) {
        if (value == null) {
            return 0;
        } else if (value instanceof Number number) {
            return number.intValue();
        } else if (value instanceof Boolean ignored) {
            return 0;
        }
        return (int) toLong(value.toString());
    }

    /**
     * Parses the leading floating point number of the string, returning 0.0 if there is none (mirrors String#to_f).
     */
    public static double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        } else if (value == null) {
            return 0.0;
        }
        String str = strip(value.toString());
        int len = str.length();
        int i = 0;
        if (i < len && (str.charAt(i) == '-' || str.charAt(i) == '+')) {
            i++;
        }
        while (i < len && Character.isDigit(str.charAt(i))) {
            i++;
        }
        if (i < len - 1 && str.charAt(i) == '.' && Character.isDigit(str.charAt(i + 1))) {
            i++;
            while (i < len && Character.isDigit(str.charAt(i))) {
                i++;
            }
        }
        if (i < len && (str.charAt(i) == 'e' || str.charAt(i) == 'E')) {
            int j = i + 1;
            if (j < len && (str.charAt(j) == '-' || str.charAt(j) == '+')) {
                j++;
            }
            if (j < len && Character.isDigit(str.charAt(j))) {
                while (j < len && Character.isDigit(str.charAt(j))) {
                    j++;
                }
                i = j;
            }
        }
        try {
            return Double.parseDouble(str.substring(0, i));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Returns the successor of the string (mirrors String#succ).
     */
    public static String succ(String str) {
        if (str.isEmpty()) {
            return "";
        }
        char[] chars = str.toCharArray();
        int lastAlnum = -1;
        for (int i = chars.length - 1; i >= 0; i--) {
            if (Character.isLetterOrDigit(chars[i]) && chars[i] < 128) {
                lastAlnum = i;
                break;
            }
        }
        if (lastAlnum < 0) {
            // no alphanumerics; increment the rightmost character
            int i = chars.length - 1;
            while (i >= 0) {
                if (chars[i] == Character.MAX_VALUE) {
                    chars[i] = 0;
                    i--;
                } else {
                    chars[i]++;
                    return new String(chars);
                }
            }
            return "\u0001" + new String(chars);
        }
        int i = lastAlnum;
        while (true) {
            char c = chars[i];
            char reset;
            char carry;
            if (c >= '0' && c <= '9') {
                if (c < '9') {
                    chars[i]++;
                    return new String(chars);
                }
                reset = '0';
                carry = '1';
            } else if (c >= 'a' && c <= 'z') {
                if (c < 'z') {
                    chars[i]++;
                    return new String(chars);
                }
                reset = 'a';
                carry = 'a';
            } else if (c >= 'A' && c <= 'Z') {
                if (c < 'Z') {
                    chars[i]++;
                    return new String(chars);
                }
                reset = 'A';
                carry = 'A';
            } else {
                chars[i]++;
                return new String(chars);
            }
            chars[i] = reset;
            // find the next alphanumeric to the left
            int j = i - 1;
            while (j >= 0 && !(Character.isLetterOrDigit(chars[j]) && chars[j] < 128)) {
                j--;
            }
            if (j < 0) {
                return new String(chars, 0, i) + carry + new String(chars, i, chars.length - i);
            }
            i = j;
        }
    }

    /**
     * Upcases the first character and downcases the remaining characters (mirrors String#capitalize).
     */
    public static String capitalize(String str) {
        if (str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Returns the first character of the string as a string, or an empty string if the string is empty.
     */
    public static String chr(String str) {
        return str.isEmpty() ? "" : str.substring(0, Character.charCount(str.codePointAt(0)));
    }

    /**
     * Repeats the string the specified number of times.
     */
    public static String repeat(String str, int times) {
        return times <= 0 ? "" : str.repeat(times);
    }

    /**
     * Checks whether the string starts with any of the specified prefixes.
     */
    public static boolean startsWithAny(String str, String... prefixes) {
        for (String prefix : prefixes) {
            if (str.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the string ends with any of the specified suffixes.
     */
    public static boolean endsWithAny(String str, String... suffixes) {
        for (String suffix : suffixes) {
            if (str.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts the object to a string, or returns null if the object is null.
     */
    public static String valueOf(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * Returns the inspect representation of the value, as Ruby would print it.
     */
    public static String inspect(Object value) {
        if (value == null) {
            return "nil";
        } else if (value instanceof String str) {
            return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
        } else if (value instanceof List<?> list) {
            List<String> items = new ArrayList<>();
            for (Object item : list) {
                items.add(inspect(item));
            }
            return "[" + String.join(", ", items) + "]";
        }
        return value.toString();
    }

    /**
     * Converts the snake_case (or kebab-case) name to camelCase.
     */
    public static String camelCase(String name) {
        StringBuilder result = new StringBuilder(name.length());
        boolean upcase = false;
        for (int i = 0, len = name.length(); i < len; i++) {
            char c = name.charAt(i);
            if (c == '_' || c == '-') {
                upcase = result.length() > 0;
            } else if (upcase) {
                result.append(Character.toUpperCase(c));
                upcase = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
