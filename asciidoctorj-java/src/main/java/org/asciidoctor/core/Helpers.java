package org.asciidoctor.core;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.ByteBuffer;

/**
 * Internal: Except where noted, a module that contains internal helper functions.
 */
public final class Helpers {
    private static final boolean WINDOWS = File.separatorChar == '\\';
    private static final Pattern UNC_ROOT_RX = Pattern.compile("^[\\\\/]{2}[^\\\\/]+[\\\\/]+[^\\\\/]+");
    private static final Pattern DRIVE_RX = Pattern.compile("[a-zA-Z]:");
    private static final Map<String, Integer> ROMAN_NUMERALS_WITH_REDUCERS = new LinkedHashMap<>();

    static {
        ROMAN_NUMERALS_WITH_REDUCERS.put("M", 1000);
        ROMAN_NUMERALS_WITH_REDUCERS.put("CM", 900);
        ROMAN_NUMERALS_WITH_REDUCERS.put("D", 500);
        ROMAN_NUMERALS_WITH_REDUCERS.put("CD", 400);
        ROMAN_NUMERALS_WITH_REDUCERS.put("C", 100);
        ROMAN_NUMERALS_WITH_REDUCERS.put("XC", 90);
        ROMAN_NUMERALS_WITH_REDUCERS.put("L", 50);
        ROMAN_NUMERALS_WITH_REDUCERS.put("XL", 40);
        ROMAN_NUMERALS_WITH_REDUCERS.put("X", 10);
        ROMAN_NUMERALS_WITH_REDUCERS.put("IX", 9);
        ROMAN_NUMERALS_WITH_REDUCERS.put("V", 5);
        ROMAN_NUMERALS_WITH_REDUCERS.put("IV", 4);
        ROMAN_NUMERALS_WITH_REDUCERS.put("I", 1);
    }

    private Helpers() {
    }

    /**
     * Creates an unmodifiable Set that contains the values. Unlike {@link Set#of}, the Set can be queried for null
     * (like a Ruby Array or Hash).
     */
    @SafeVarargs
    public static <T> Set<T> setOf(T... values) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values)));
    }

    /**
     * Internal: Prepare the source data List for parsing.
     *
     * <p>Removes any trailing whitespace from every line. If a BOM is found at the beginning of the data, it is removed.
     *
     * @param data the source data List to prepare (no null entries allowed)
     * @param trimEnd whether to trim whitespace from the end of each line; (true cleans all whitespace; false only
     *            removes trailing newline) (default: true)
     * @return a String List of prepared lines
     */
    public static List<String> prepareSourceArray(List<String> data, boolean trimEnd) {
        List<String> result = new ArrayList<>(data.size());
        if (data.isEmpty()) {
            return result;
        }
        boolean first = true;
        for (String line : data) {
            if (first) {
                line = stripBom(line);
                first = false;
            }
            result.add(trimEnd ? Strings.rstrip(line) : Strings.chomp(line));
        }
        return result;
    }

    public static List<String> prepareSourceArray(List<String> data) {
        return prepareSourceArray(data, true);
    }

    /**
     * Internal: Prepare the source data String for parsing.
     *
     * <p>Splits the data into a List and removes any trailing whitespace from every line. If a BOM is found at the
     * beginning of the data, it is removed.
     *
     * @param data the source data String to prepare
     * @param trimEnd whether to trim whitespace from the end of each line; (true cleans all whitespace; false only
     *            removes trailing newline) (default: true)
     * @return a String List of prepared lines
     */
    public static List<String> prepareSourceString(String data, boolean trimEnd) {
        List<String> lines = new ArrayList<>();
        if (Strings.isNullOrEmpty(data)) {
            return lines;
        }
        data = stripBom(data);
        for (String line : eachLine(data)) {
            lines.add(trimEnd ? Strings.rstrip(line) : Strings.chomp(line));
        }
        return lines;
    }

    public static List<String> prepareSourceString(String data) {
        return prepareSourceString(data, true);
    }

    /**
     * Splits the string into lines, retaining the line terminator on each line (mirrors String#each_line).
     */
    public static List<String> eachLine(String data) {
        List<String> lines = new ArrayList<>();
        int start = 0;
        int len = data.length();
        int idx;
        while (start < len && (idx = data.indexOf('\n', start)) >= 0) {
            lines.add(data.substring(start, idx + 1));
            start = idx + 1;
        }
        if (start < len) {
            lines.add(data.substring(start));
        }
        return lines;
    }

    private static String stripBom(String data) {
        return !data.isEmpty() && data.charAt(0) == '﻿' ? data.substring(1) : data;
    }

    /**
     * Decodes the bytes into a String, detecting a UTF-16 or UTF-8 BOM. Falls back to UTF-8.
     *
     * @param bytes the raw bytes
     * @param charset the charset to use if no BOM is detected (default: UTF-8)
     * @return the decoded String
     */
    public static String decode(byte[] bytes, Charset charset) {
        if (bytes.length >= 2) {
            if ((bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xfe) {
                return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
            } else if ((bytes[0] & 0xff) == 0xfe && (bytes[1] & 0xff) == 0xff) {
                return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
            } else if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xef && (bytes[1] & 0xff) == 0xbb
                    && (bytes[2] & 0xff) == 0xbf) {
                return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
            }
        }
        return new String(bytes, charset == null ? StandardCharsets.UTF_8 : charset);
    }

    /**
     * Decodes the bytes as UTF-8, raising an exception if the bytes are not valid UTF-8 (i.e., binary data).
     */
    public static String decodeStrict(byte[] bytes) throws CharacterCodingException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        return decoder.decode(ByteBuffer.wrap(bytes)).toString();
    }

    /**
     * Internal: Efficiently checks whether the specified String resembles a URI.
     *
     * <p>Uses the {@link Rx#URI_SNIFF_RX} regex to check whether the String begins with a URI prefix (e.g., http://).
     * No validation of the URI is performed.
     *
     * @param str the String to check
     * @return true if the String is a URI, false if it is not
     */
    public static boolean isUriish(String str) {
        return str != null && str.contains(":") && Rx.URI_SNIFF_RX.matcher(str).lookingAt();
    }

    /**
     * Internal: Encode a URI component String for safe inclusion in a URI.
     *
     * @param str the URI component String to encode
     * @return the String with all reserved URI characters encoded (e.g., /, &amp;, =, space, etc).
     */
    public static String encodeUriComponent(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8).replace("%7E", "~").replace("*", "%2A").replace("+", "%20");
    }

    /**
     * Internal: Apply URI path encoding to spaces in the specified string (i.e., convert spaces to %20).
     *
     * @param str the String to encode
     * @return the specified String with all spaces replaced with %20.
     */
    public static String encodeSpacesInUri(String str) {
        return str.contains(" ") ? str.replace(" ", "%20") : str;
    }

    /**
     * Public: Removes the file extension from filename and returns the result.
     *
     * <pre>
     *   Helpers.rootname("part1/chapter1.adoc")
     *   // => "part1/chapter1"
     * </pre>
     *
     * @param filename The String file name to process; expected to be a posix path
     * @return the String filename with the file extension removed
     */
    public static String rootname(String filename) {
        int lastDotIdx = filename.lastIndexOf('.');
        if (lastDotIdx >= 0) {
            return filename.indexOf('/', lastDotIdx) >= 0 ? filename : filename.substring(0, lastDotIdx);
        }
        return filename;
    }

    /**
     * Public: Retrieves the basename of the filename.
     *
     * @param filename The String file name to process.
     * @return the String filename with leading directories removed
     */
    public static String basename(String filename) {
        String path = Strings.chomp(filename.replace('\\', '/'), "/");
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        int idx = path.lastIndexOf('/');
        return idx >= 0 && path.length() > 1 ? path.substring(idx + 1) : path;
    }

    /**
     * Public: Retrieves the basename of the filename, removing the extension, if present.
     *
     * <pre>
     *   Helpers.basename("images/tiger.png", true)
     *   // => "tiger"
     * </pre>
     *
     * @param filename The String file name to process.
     * @param dropExt A Boolean flag indicating whether to drop the extension
     * @return the String filename with leading directories removed and, if specified, the extension removed
     */
    public static String basename(String filename, boolean dropExt) {
        return dropExt ? basename(filename, extname(basename(filename))) : basename(filename);
    }

    /**
     * Public: Retrieves the basename of the filename, removing the specified extension, if present.
     *
     * <pre>
     *   Helpers.basename("images/tiger.png", ".png")
     *   // => "tiger"
     * </pre>
     */
    public static String basename(String filename, String dropExt) {
        String base = basename(filename);
        if (dropExt == null || dropExt.isEmpty()) {
            return base;
        }
        if (dropExt.equals(".*")) {
            int dotIdx = base.lastIndexOf('.');
            return dotIdx > 0 ? base.substring(0, dotIdx) : base;
        }
        return base.endsWith(dropExt) && base.length() > dropExt.length()
                ? base.substring(0, base.length() - dropExt.length())
                : base;
    }

    /**
     * Public: Returns whether this path has a file extension.
     *
     * @param path The path String to check; expects a posix path
     * @return true if the path has a file extension, false otherwise
     */
    public static boolean hasExtname(String path) {
        int lastDotIdx = path.lastIndexOf('.');
        return lastDotIdx >= 0 && path.indexOf('/', lastDotIdx) < 0;
    }

    /**
     * Public: Retrieves the file extension of the specified path. The file extension is the portion of the path in the
     * last path segment starting from the last period.
     *
     * @param path The path String in which to look for a file extension
     * @param fallback The fallback String to return if no file extension is present (optional, default: '')
     * @return the String file extension (with the leading dot included) or the fallback value if the path has no file
     *         extension.
     */
    public static String extname(String path, String fallback) {
        int lastDotIdx = path.lastIndexOf('.');
        if (lastDotIdx >= 0) {
            return path.indexOf('/', lastDotIdx) >= 0 ? fallback : path.substring(lastDotIdx);
        }
        return fallback;
    }

    public static String extname(String path) {
        return extname(path, "");
    }

    /**
     * Returns the directory portion of the specified path (mirrors File.dirname).
     *
     * <p>On Windows, both the forward slash and the backslash are path separators, and the drive letter and UNC root
     * are preserved (e.g., {@code C:/}, {@code //server/share}).
     */
    public static String dirname(String path) {
        if (!WINDOWS) {
            return dirname(path, "/");
        }
        Matcher uncRoot = UNC_ROOT_RX.matcher(path);
        if (uncRoot.find()) {
            String root = uncRoot.group();
            String dir = dirname(path.substring(root.length()), "/\\");
            return dir.equals(".") || dir.equals("/") || dir.equals("\\") ? root : root + dir;
        }
        if (DRIVE_RX.matcher(path).lookingAt()) {
            return path.substring(0, 2) + dirname(path.substring(2), "/\\");
        }
        return dirname(path, "/\\");
    }

    private static String dirname(String path, String separators) {
        int end = path.length();
        while (end > 1 && separators.indexOf(path.charAt(end - 1)) >= 0) {
            end--;
        }
        int idx = end - 1;
        while (idx >= 0 && separators.indexOf(path.charAt(idx)) < 0) {
            idx--;
        }
        if (idx < 0) {
            return ".";
        } else if (idx == 0) {
            return path.substring(0, 1);
        }
        while (idx > 1 && separators.indexOf(path.charAt(idx - 1)) >= 0) {
            idx--;
        }
        return path.substring(0, idx);
    }

    /**
     * Joins the directory and path segment using a forward slash (mirrors File.join).
     */
    public static String joinPath(String dir, String path) {
        if (dir == null || dir.isEmpty()) {
            return path;
        }
        return dir.endsWith("/") ? dir + (path.startsWith("/") ? path.substring(1) : path)
                : dir + (path.startsWith("/") ? path : "/" + path);
    }

    /**
     * Expands the path to an absolute path, resolving it relative to the base directory, if specified, otherwise the
     * working directory (mirrors File.expand_path).
     */
    public static String expandPath(String path, String base) {
        Path resolved;
        if (path.startsWith("~")) {
            path = Asciidoctor.USER_HOME + path.substring(1);
        }
        Path candidate = Path.of(path);
        if (candidate.isAbsolute()) {
            resolved = candidate;
        } else if (base != null) {
            resolved = Path.of(expandPath(base, null)).resolve(path);
        } else {
            resolved = Path.of(workingDirectory()).resolve(path);
        }
        return posixify(resolved.normalize().toString());
    }

    /**
     * Returns the absolute path of the present working directory (mirrors Dir.pwd).
     */
    public static String workingDirectory() {
        return posixify(Path.of("").toAbsolutePath().toString());
    }

    /**
     * Converts the backslashes in the path to forward slashes (Ruby always uses forward slashes in paths).
     */
    public static String posixify(String path) {
        return path.replace('\\', '/');
    }

    /**
     * Returns the absolute, normalized path as a posix path (mirrors File.expand_path).
     */
    public static String absolutePath(Path path) {
        return posixify(path.toAbsolutePath().normalize().toString());
    }

    /**
     * Converts the String to a {@link Path}, or returns null if the String is not a valid path on this system (e.g.,
     * a path that contains a colon or a wildcard on Windows).
     */
    public static Path toPath(String path) {
        try {
            return Path.of(path);
        } catch (InvalidPathException e) {
            return null;
        }
    }

    /**
     * Returns whether the path is a regular file (mirrors File.file?); false if the path is not valid on this system.
     */
    public static boolean isFile(String path) {
        Path file = toPath(path);
        return file != null && Files.isRegularFile(file);
    }

    /**
     * Returns whether the path is a directory (mirrors File.directory?); false if the path is not valid on this system.
     */
    public static boolean isDirectory(String path) {
        Path dir = toPath(path);
        return dir != null && Files.isDirectory(dir);
    }

    /**
     * Internal: Make a directory, ensuring all parent directories exist.
     */
    public static void mkdirP(String dir) {
        try {
            Files.createDirectories(Path.of(dir));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Internal: Write the content to the file using the UTF-8 encoding.
     */
    public static void writeFile(String file, String content) {
        try {
            Files.writeString(Path.of(file), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Internal: Converts an integer to a Roman numeral.
     *
     * @param val the int value to convert
     * @return the String roman numeral for this integer
     */
    public static String intToRoman(int val) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Integer> entry : ROMAN_NUMERALS_WITH_REDUCERS.entrySet()) {
            int repeat = val / entry.getValue();
            val = val % entry.getValue();
            result.append(entry.getKey().repeat(repeat));
        }
        return result.toString();
    }

    private static final Map<Character, Integer> ROMAN_NUMERALS = Map.of('I', 1, 'V', 5, 'X', 10, 'L', 50, 'C', 100,
            'D', 500, 'M', 1000);

    /**
     * Internal: Converts an uppercase Roman numeral to an integer.
     *
     * @param val the String Roman numeral in uppercase to convert
     * @return the int value for this Roman numeral
     */
    public static int romanToInt(String val) {
        int result = 0;
        int len = val.length();
        for (int idx = 0; idx < len; idx++) {
            int v = ROMAN_NUMERALS.get(val.charAt(idx));
            if (idx + 1 < len && ROMAN_NUMERALS.get(val.charAt(idx + 1)) > v) {
                result -= v;
            } else {
                result += v;
            }
        }
        return result;
    }

    /**
     * Internal: Get the next value in the sequence.
     *
     * <p>Handles both integer and character sequences.
     *
     * @param current the value to increment as a String or Integer
     * @return the next value in the sequence according to the current value's type
     */
    public static Object nextval(Object current) {
        if (current instanceof Integer intval) {
            return intval + 1;
        } else if (current instanceof Long longval) {
            return longval + 1;
        }
        String str = String.valueOf(current);
        long intval = Strings.toLong(str);
        if (String.valueOf(intval).equals(str)) {
            return (int) (intval + 1);
        }
        return Strings.succ(str);
    }

    /**
     * Internal: Resolves a Class object for the qualified name.
     *
     * @param qualifiedName the fully-qualified class name
     * @return the Class
     * @throws IllegalArgumentException if the class cannot be resolved or the name resolves to an interface
     */
    public static Class<?> classForName(String qualifiedName) {
        Class<?> clazz;
        try {
            clazz = Class.forName(qualifiedName);
        } catch (ClassNotFoundException | LinkageError e) {
            throw new IllegalArgumentException("Could not resolve class for name: " + qualifiedName, e);
        }
        // an interface is the equivalent of a module, which cannot be instantiated
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("Could not resolve class for name: " + qualifiedName);
        }
        return clazz;
    }

    /**
     * Internal: Resolve the specified object as a Class.
     *
     * @param object The Object to resolve as a Class
     * @return a Class if the specified object is a Class or a String that resolves to a Class; otherwise, null
     */
    public static Class<?> resolveClass(Object object) {
        if (object instanceof Class<?> clazz) {
            return clazz;
        } else if (object instanceof String name) {
            return classForName(name);
        }
        return null;
    }
}
