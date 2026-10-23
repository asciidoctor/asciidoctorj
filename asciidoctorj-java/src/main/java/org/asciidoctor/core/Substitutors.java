package org.asciidoctor.core;

import static org.asciidoctor.core.Asciidoctor.ASCIIDOC_EXTENSIONS;
import static org.asciidoctor.core.Asciidoctor.ATTR_REF_HEAD;
import static org.asciidoctor.core.Asciidoctor.HARD_LINE_BREAK;
import static org.asciidoctor.core.Asciidoctor.INTRINSIC_ATTRIBUTES;
import static org.asciidoctor.core.Asciidoctor.LF;
import static org.asciidoctor.core.Substitution.ATTRIBUTES;
import static org.asciidoctor.core.Substitution.CALLOUTS;
import static org.asciidoctor.core.Substitution.HIGHLIGHT;
import static org.asciidoctor.core.Substitution.MACROS;
import static org.asciidoctor.core.Substitution.POST_REPLACEMENTS;
import static org.asciidoctor.core.Substitution.QUOTES;
import static org.asciidoctor.core.Substitution.REPLACEMENTS;
import static org.asciidoctor.core.Substitution.SPECIAL_CHARACTERS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.core.Asciidoctor.QuoteScope;
import org.asciidoctor.core.Asciidoctor.QuoteSub;
import org.asciidoctor.core.Asciidoctor.Replacement;
import org.asciidoctor.core.Document.Footnote;
import org.asciidoctor.core.extension.InlineMacroProcessor;
import org.asciidoctor.core.extension.ProcessorExtension;
import org.asciidoctor.core.syntaxhighlighter.SyntaxHighlighter;

/**
 * Methods to perform substitutions on lines of AsciiDoc text. This class is the base of {@link AbstractNode} so that
 * the substitution operations are available on all nodes (mirrors the Substitutors module mixed into nodes in Ruby).
 */
public abstract class Substitutors implements Logging {
    private static final Pattern SPECIAL_CHARS_RX = Rx.compile("[<&>]");

    /** Detects if text is a possible candidate for the quotes substitution. */
    private static final Map<Boolean, Pattern> QUOTED_TEXT_SNIFF_RX = Map.of(
            false, Rx.compile("[*_`#^~]"),
            true, Rx.compile("[*'_+#^~]"));

    public static final List<Substitution> BASIC_SUBS = List.of(SPECIAL_CHARACTERS);
    public static final List<Substitution> HEADER_SUBS = List.of(SPECIAL_CHARACTERS, ATTRIBUTES);
    public static final List<Substitution> NO_SUBS = List.of();
    public static final List<Substitution> NORMAL_SUBS = List.of(SPECIAL_CHARACTERS, QUOTES, ATTRIBUTES, REPLACEMENTS,
            MACROS, POST_REPLACEMENTS);
    public static final List<Substitution> REFTEXT_SUBS = List.of(SPECIAL_CHARACTERS, QUOTES, REPLACEMENTS);
    public static final List<Substitution> VERBATIM_SUBS = List.of(SPECIAL_CHARACTERS, CALLOUTS);

    public static final Map<String, List<Substitution>> SUB_GROUPS = Map.of(
            "none", NO_SUBS,
            "normal", NORMAL_SUBS,
            "verbatim", VERBATIM_SUBS,
            "specialchars", BASIC_SUBS);

    public static final Map<String, String> SUB_HINTS = Map.of(
            "a", "attributes",
            "m", "macros",
            "n", "normal",
            "p", "post_replacements",
            "q", "quotes",
            "r", "replacements",
            "c", "specialcharacters",
            "v", "verbatim");

    /** The type of substitutions being resolved. */
    public enum SubsType {
        BLOCK, INLINE
    }

    private static final Map<SubsType, Set<Substitution>> SUB_OPTIONS = Map.of(
            SubsType.BLOCK, EnumSet.of(SPECIAL_CHARACTERS, QUOTES, ATTRIBUTES, REPLACEMENTS, MACROS, POST_REPLACEMENTS,
                    CALLOUTS),
            SubsType.INLINE, EnumSet.of(SPECIAL_CHARACTERS, QUOTES, ATTRIBUTES, REPLACEMENTS, MACROS,
                    POST_REPLACEMENTS));

    public static final String CAN = "\u0018";
    public static final String DEL = "\u007f";

    // Delimiters and matchers for the passthrough placeholder
    // See http://www.aivosto.com/vbtips/control-characters.html#listabout for characters to use

    /** SPA, start of guarded protected area (\u0096). */
    public static final String PASS_START = "\u0096";

    /** EPA, end of guarded protected area (\u0097). */
    public static final String PASS_END = "\u0097";

    /** Match passthrough slot. */
    public static final Pattern PASS_SLOT_RX = Rx.compile(PASS_START + "(\\d+)" + PASS_END);

    /** Fix passthrough slot after syntax highlighting. */
    public static final Pattern HIGHLIGHTED_PASS_SLOT_RX = Rx.compile("<span\\b[^>]*>" + PASS_START
            + "</span>[^\\d]*(\\d+)[^\\d]*<span\\b[^>]*>" + PASS_END + "</span>");

    public static final String RS = "\\";

    public static final String R_SB = "]";

    public static final String ESC_R_SB = "\\]";

    public static final String PLUS = "+";

    /** Options that control how attributes are parsed by {@link #parseAttributes}. */
    public enum ParseOption {
        /** Substitute attributes prior to parsing. */
        SUB_INPUT,
        /** Apply substitutions to single-quoted attribute values. */
        SUB_RESULT,
        /** Unescape square brackets prior to parsing. */
        UNESCAPE_INPUT
    }

    /**
     * A passthrough region extracted from the text.
     *
     * @param text the text of the passthrough
     * @param subs the substitutions to apply to the text when it's restored (may be null)
     * @param type the type of the quoted node to wrap the text in (may be null)
     * @param attributes the attributes of the quoted node (may be null)
     */
    public record Passthrough(String text, List<Substitution> subs, String type, Map<String, Object> attributes) {
    }

    /**
     * A callout mark extracted from source code prior to syntax highlighting.
     *
     * @param guard the line comment guard (a String, a List of the opening and closing guards, or null)
     * @param numeral the numeral of the callout
     */
    public record CalloutMark(Object guard, String numeral) {
    }

    protected final List<Passthrough> passthroughs = new ArrayList<>();
    private boolean passthroughsLocked;

    // --- hooks implemented by AbstractNode ---

    public abstract Document getDocument();

    public abstract Map<String, Object> getAttributes();

    public abstract String getContext();

    public abstract AbstractNode getParent();

    public abstract Object getAttribute(String name);

    public abstract boolean hasAttribute(String name);

    public abstract boolean isOption(String name);

    private AbstractNode self() {
        return (AbstractNode) this;
    }

    /**
     * Apply the specified substitutions to the text.
     *
     * @param text The String text to process; must not be null.
     * @param subs The substitutions to perform; must be a Substitution List or null (default: NORMAL_SUBS).
     * @return a String with substitutions applied.
     */
    public String applySubs(String text, List<Substitution> subs) {
        if (text.isEmpty() || subs == null) {
            return text;
        }

        List<Passthrough> passthrus = null;
        boolean clearPassthrus = false;
        if (subs.contains(MACROS)) {
            text = extractPassthroughs(text);
            if (!passthroughs.isEmpty()) {
                passthrus = passthroughs;
                // NOTE placeholders can move around, so we can only clear in the outermost substitution call
                if (!passthroughsLocked) {
                    passthroughsLocked = clearPassthrus = true;
                }
            }
        }

        for (Substitution type : subs) {
            switch (type) {
                case SPECIAL_CHARACTERS -> text = subSpecialchars(text);
                case QUOTES -> text = subQuotes(text);
                case ATTRIBUTES -> {
                    if (text.contains(ATTR_REF_HEAD)) {
                        text = subAttributes(text);
                    }
                }
                case REPLACEMENTS -> text = subReplacements(text);
                case MACROS -> text = subMacros(text);
                case HIGHLIGHT -> text = highlightSource(text, subs.contains(CALLOUTS));
                case CALLOUTS -> {
                    if (!subs.contains(HIGHLIGHT)) {
                        text = subCallouts(text);
                    }
                }
                case POST_REPLACEMENTS -> text = subPostReplacements(text);
            }
        }

        if (passthrus != null) {
            text = restorePassthroughs(text);
            if (clearPassthrus) {
                passthrus.clear();
                passthroughsLocked = false;
            }
        }

        return text;
    }

    public String applySubs(String text) {
        return applySubs(text, NORMAL_SUBS);
    }

    public List<String> applySubs(List<String> lines) {
        return applySubs(lines, NORMAL_SUBS);
    }

    /**
     * Apply the specified substitutions to the lines of text.
     *
     * @param lines The String List of text to process; must not be null.
     * @param subs The substitutions to perform; must be a Substitution List or null.
     * @return a String List with substitutions applied.
     */
    public List<String> applySubs(List<String> lines, List<Substitution> subs) {
        if (lines.isEmpty() || subs == null) {
            return lines;
        }
        String text = lines.size() > 1 ? String.join(LF, lines) : lines.get(0);
        return Strings.split(applySubs(text, subs), LF, -1);
    }

    /**
     * Apply normal substitutions.
     *
     * @param text The String text to which to apply normal substitutions
     * @return the String with normal substitutions applied.
     */
    public String applyNormalSubs(String text) {
        return applySubs(text, NORMAL_SUBS);
    }

    /**
     * Apply substitutions for header metadata and attribute assignments.
     *
     * @param text String containing the text process
     * @return A String with header substitutions performed
     */
    public String applyHeaderSubs(String text) {
        return applySubs(text, HEADER_SUBS);
    }

    /**
     * Apply substitutions for titles.
     *
     * @param title The String title to process
     * @return A String with title substitutions performed
     */
    public String applyTitleSubs(String title) {
        return applySubs(title, NORMAL_SUBS);
    }

    /**
     * Apply substitutions for reftext.
     *
     * @param text The String to process
     * @return a String with all substitutions from the reftext substitution group applied
     */
    public String applyReftextSubs(String text) {
        return applySubs(text, REFTEXT_SUBS);
    }

    /**
     * Substitute special characters (i.e., encode XML).
     *
     * <p>The special characters &lt;, &amp;, and &gt; get replaced with &amp;lt;, &amp;amp;, and &amp;gt;,
     * respectively.
     *
     * @param text The String text to process.
     * @return The String text with special characters replaced.
     */
    public String subSpecialchars(String text) {
        if (text.indexOf('>') < 0 && text.indexOf('&') < 0 && text.indexOf('<') < 0) {
            return text;
        }
        StringBuilder result = new StringBuilder(text.length() + 16);
        for (int i = 0, len = text.length(); i < len; i++) {
            char c = text.charAt(i);
            switch (c) {
                case '>' -> result.append("&gt;");
                case '<' -> result.append("&lt;");
                case '&' -> result.append("&amp;");
                default -> result.append(c);
            }
        }
        return result.toString();
    }

    public String subSpecialcharacters(String text) {
        return subSpecialchars(text);
    }

    /**
     * Substitute quoted text (includes emphasis, strong, monospaced, etc.).
     *
     * @param text The String text to process
     * @return The converted String text
     */
    public String subQuotes(String text) {
        boolean compat = getDocument().isCompatMode();
        if (QUOTED_TEXT_SNIFF_RX.get(compat).matcher(text).find()) {
            for (QuoteSub quoteSub : Asciidoctor.QUOTE_SUBS.get(compat)) {
                text = Rx.gsub(quoteSub.pattern(), text, m -> convertQuotedText(m, quoteSub.type(), quoteSub.scope()));
            }
        }
        return text;
    }

    /**
     * Substitutes attribute references in the specified text.
     *
     * <p>Attribute references are in the format {name}.
     *
     * <p>If an attribute referenced in the line is missing or undefined, the line may be dropped based on the
     * attribute-missing or attribute-undefined setting, respectively.
     *
     * @param text The String text to process
     * @return the String text with the attribute references replaced with resolved values
     */
    public String subAttributes(String text) {
        return subAttributes(text, null, null);
    }

    /**
     * Substitutes attribute references in the specified text.
     *
     * @param text The String text to process
     * @param attributeMissingOpt controls how to handle a missing attribute (see Compliance.attribute_missing for
     *            values) (optional)
     * @param dropLineSeverityOpt the severity level at which to log a dropped line ("info" or "ignore") (optional)
     * @return the String text with the attribute references replaced with resolved values
     */
    public String subAttributes(String text, String attributeMissingOpt, String dropLineSeverityOpt) {
        Document document = getDocument();
        Map<String, Object> docAttrs = document.getAttributes();
        var state = new Object() {
            String drop;
            String dropLine;
            String dropEmptyLine;
            String attributeUndefined;
            String attributeMissing;
            String dropLineSeverity;
        };
        text = Rx.gsub(Rx.ATTRIBUTE_REFERENCE_RX, text, m -> {
            // escaped attribute, return unescaped
            if (RS.equals(m.group(1)) || RS.equals(m.group(4))) {
                return "{" + m.group(2) + "}";
            } else if (m.group(3) != null) {
                List<String> args = Strings.split(m.group(2), ":", 3);
                String directive = args.remove(0);
                switch (directive) {
                    case "set" -> {
                        Parser.AttributeEntryResult result = Parser.storeAttribute(args.get(0),
                                args.size() > 1 ? args.get(1) : "", document, null);
                        if (state.attributeUndefined == null) {
                            Object undefined = docAttrs.get("attribute-undefined");
                            state.attributeUndefined = undefined != null ? undefined.toString()
                                    : Compliance.getAttributeUndefined();
                        }
                        // NOTE since this is an assignment, only drop-line applies here (skip and drop imply the same
                        // result)
                        if (result.value() != null || !"drop-line".equals(state.attributeUndefined)) {
                            state.drop = state.dropEmptyLine = DEL;
                        } else {
                            state.drop = state.dropLine = CAN;
                        }
                        return state.drop;
                    }
                    case "counter2" -> {
                        document.counter(args.get(0), args.size() > 1 ? args.get(1) : null);
                        state.drop = state.dropEmptyLine = DEL;
                        return state.drop;
                    }
                    default -> { // 'counter'
                        return String.valueOf(document.counter(args.get(0), args.size() > 1 ? args.get(1) : null));
                    }
                }
            }
            String key = m.group(2).toLowerCase();
            String value;
            if (docAttrs.containsKey(key)) {
                return attributeValueToString(docAttrs.get(key));
            } else if ((value = INTRINSIC_ATTRIBUTES.get(key)) != null) {
                return value;
            }
            if (state.attributeMissing == null) {
                Object missing = docAttrs.get("attribute-missing");
                state.attributeMissing = attributeMissingOpt != null ? attributeMissingOpt
                        : missing != null ? missing.toString() : Compliance.getAttributeMissing();
            }
            switch (state.attributeMissing) {
                case "drop" -> {
                    state.drop = state.dropEmptyLine = DEL;
                    return state.drop;
                }
                case "drop-line" -> {
                    if (state.dropLineSeverity == null) {
                        state.dropLineSeverity = dropLineSeverityOpt != null ? dropLineSeverityOpt : "info";
                    }
                    if ("info".equals(state.dropLineSeverity)) {
                        getLogger().info(() -> "dropping line containing reference to missing attribute: " + key);
                    }
                    state.drop = state.dropLine = CAN;
                    return state.drop;
                }
                case "warn" -> {
                    getLogger().warn("skipping reference to missing attribute: " + key);
                    return m.group();
                }
                default -> {
                    return m.group();
                }
            }
        });

        if (state.drop != null) {
            // drop lines from text
            if (state.dropEmptyLine != null) {
                List<String> lines = Strings.split(Strings.squeeze(text, DEL.charAt(0)), LF, -1);
                List<String> kept = new ArrayList<>();
                for (String line : lines) {
                    if (line.equals(DEL)) {
                        continue;
                    }
                    if (state.dropLine != null && line.contains(CAN)) {
                        continue;
                    }
                    kept.add(line);
                }
                return String.join(LF, kept).replace(DEL, "");
            } else if (text.contains(LF)) {
                List<String> kept = new ArrayList<>();
                for (String line : Strings.split(text, LF, -1)) {
                    if (!line.contains(CAN)) {
                        kept.add(line);
                    }
                }
                return String.join(LF, kept);
            } else {
                return "";
            }
        }
        return text;
    }

    private static String attributeValueToString(Object value) {
        if (value == null) {
            return "";
        }
        return value instanceof List<?> list ? Strings.inspect(list) : value.toString();
    }

    /**
     * Substitute replacement characters (e.g., copyright, trademark, etc.).
     *
     * @param text The String text to process
     * @return The String text with the replacement characters substituted
     */
    public String subReplacements(String text) {
        if (Rx.REPLACEABLE_TEXT_RX.matcher(text).find()) {
            for (Replacement replacement : Asciidoctor.REPLACEMENTS) {
                text = Rx.gsub(replacement.pattern(), text,
                        m -> doReplacement(m, replacement.replacement(), replacement.restore()));
            }
        }
        return text;
    }

    /**
     * Substitute inline macros (e.g., links, images, etc).
     *
     * <p>Replace inline macros, which may span multiple lines, in the provided text.
     *
     * @param text The String text to process
     * @return The converted String text
     */
    public String subMacros(String text) {
        // some look ahead assertions to cut unnecessary regex calls
        boolean foundSquareBracket = text.contains("[");
        boolean foundColon = text.contains(":");
        boolean foundMacroish = foundSquareBracket && foundColon;
        boolean foundMacroishShort = foundMacroish && text.contains(":[");
        Document doc = getDocument();
        Map<String, Object> docAttrs = doc.getAttributes();

        // TODO allow position of substitution to be controlled (before or after other macros)
        // TODO this handling needs some cleanup
        if (doc.getExtensions() != null && doc.getExtensions().hasInlineMacros()) {
            for (ProcessorExtension<InlineMacroProcessor> extension : doc.getExtensions().getInlineMacros()) {
                text = Rx.gsub(extension.getInstance().getRegexp(), text, m -> processInlineMacro(extension, m));
            }
        }

        if (docAttrs.containsKey("experimental")) {
            if (foundMacroishShort && (text.contains("kbd:") || text.contains("btn:"))) {
                text = Rx.gsub(Rx.INLINE_KBD_BTN_MACRO_RX, text, m -> {
                    // honor the escape
                    if (m.group(1) != null) {
                        return m.group().substring(1);
                    } else if (m.group(2).equals("kbd")) {
                        String keys = Strings.strip(m.group(3));
                        if (keys.contains(R_SB)) {
                            keys = keys.replace(ESC_R_SB, R_SB);
                        }
                        List<String> keyList;
                        int delimIdx = -1;
                        if (keys.length() > 1) {
                            int commaIdx = keys.indexOf(',', 1);
                            int plusIdx = keys.indexOf('+', 1);
                            if (commaIdx >= 0) {
                                delimIdx = plusIdx >= 0 ? Math.min(commaIdx, plusIdx) : commaIdx;
                            } else {
                                delimIdx = plusIdx;
                            }
                        }
                        if (delimIdx >= 0) {
                            String delim = keys.substring(delimIdx, delimIdx + 1);
                            // NOTE handle special case where keys ends with delimiter (e.g., Ctrl++ or Ctrl,,)
                            if (keys.endsWith(delim)) {
                                keyList = new ArrayList<>();
                                for (String key : Strings.split(Strings.chop(keys), delim, -1)) {
                                    keyList.add(Strings.strip(key));
                                }
                                keyList.set(keyList.size() - 1, keyList.get(keyList.size() - 1) + delim);
                            } else {
                                keyList = new ArrayList<>();
                                for (String key : Strings.split(keys, delim)) {
                                    keyList.add(Strings.strip(key));
                                }
                            }
                        } else {
                            keyList = List.of(keys);
                        }
                        Map<String, Object> attrs = new LinkedHashMap<>();
                        attrs.put("keys", keyList);
                        return new Inline(self(), "kbd", null, null, null, null, attrs).convert();
                    } else { // btn
                        return new Inline(self(), "button", normalizeText(m.group(3), true, true)).convert();
                    }
                });
            }

            if (foundMacroish && text.contains("menu:")) {
                text = Rx.gsub(Rx.INLINE_MENU_MACRO_RX, text, m -> {
                    // honor the escape
                    if (m.group().startsWith(RS)) {
                        return m.group().substring(1);
                    }
                    String menu = m.group(1);
                    List<String> submenus;
                    String menuitem;
                    String items = m.group(2);
                    if (items != null) {
                        if (items.contains(R_SB)) {
                            items = items.replace(ESC_R_SB, R_SB);
                        }
                        String delim = items.contains("&gt;") ? "&gt;" : (items.contains(",") ? "," : null);
                        if (delim != null) {
                            submenus = new ArrayList<>();
                            for (String it : Strings.split(items, delim)) {
                                submenus.add(Strings.strip(it));
                            }
                            menuitem = submenus.isEmpty() ? null : submenus.remove(submenus.size() - 1);
                        } else {
                            submenus = new ArrayList<>();
                            menuitem = Strings.rstrip(items);
                        }
                    } else {
                        submenus = new ArrayList<>();
                        menuitem = null;
                    }
                    Map<String, Object> attrs = new LinkedHashMap<>();
                    attrs.put("menu", menu);
                    attrs.put("submenus", submenus);
                    attrs.put("menuitem", menuitem);
                    return new Inline(self(), "menu", null, null, null, null, attrs).convert();
                });
            }

            if (text.contains("\"") && text.contains("&gt;")) {
                text = Rx.gsub(Rx.INLINE_MENU_RX, text, m -> {
                    // honor the escape
                    if (m.group().startsWith(RS)) {
                        return m.group().substring(1);
                    }
                    List<String> parts = new ArrayList<>();
                    for (String it : Strings.split(m.group(1), "&gt;")) {
                        parts.add(Strings.strip(it));
                    }
                    String menu = parts.remove(0);
                    String menuitem = parts.isEmpty() ? null : parts.remove(parts.size() - 1);
                    Map<String, Object> attrs = new LinkedHashMap<>();
                    attrs.put("menu", menu);
                    attrs.put("submenus", parts);
                    attrs.put("menuitem", menuitem);
                    return new Inline(self(), "menu", null, null, null, null, attrs).convert();
                });
            }
        }

        if (foundMacroish && (text.contains("image:") || text.contains("icon:"))) {
            // image:filename.png[Alt Text]
            text = Rx.gsub(Rx.INLINE_IMAGE_MACRO_RX, text, m -> {
                String type;
                List<String> posattrs;
                // honor the escape
                if (m.group().startsWith(RS)) {
                    return m.group().substring(1);
                } else if (m.group().startsWith("icon:")) {
                    type = "icon";
                    posattrs = List.of("size");
                } else {
                    type = "image";
                    posattrs = List.of("alt", "width", "height");
                }
                String target = m.group(1);
                Map<String, Object> attrs = parseAttributes(m.group(2), posattrs, EnumSet.of(ParseOption.UNESCAPE_INPUT),
                        null);
                String id = null;
                if (!type.equals("icon")) {
                    id = Strings.valueOf(attrs.get("id"));
                    doc.register("images", target);
                    if (attrs.get("imagesdir") == null) {
                        attrs.put("imagesdir", docAttrs.get("imagesdir"));
                    }
                }
                if (attrs.get("alt") == null) {
                    String defaultAlt = Strings.tr(Helpers.basename(target, true), "_-", "  ");
                    attrs.put("default-alt", defaultAlt);
                    attrs.put("alt", defaultAlt);
                }
                return new Inline(self(), "image", null, type, id, target, attrs).convert();
            });
        }

        if ((text.contains("((") && text.contains("))")) || (foundMacroishShort && text.contains("dexterm"))) {
            // (((Tigers,Big cats)))
            // indexterm:[Tigers,Big cats]
            // ((Tigers))
            // indexterm2:[Tigers]
            text = Rx.gsub(Rx.INLINE_INDEXTERM_MACRO_RX, text, this::convertIndexterm);
        }

        if (foundColon && text.contains("://")) {
            // inline urls, target[text] (optionally prefixed with link: or enclosed in <>)
            text = Rx.gsub(Rx.INLINE_LINK_RX, text, m -> convertInlineLink(m, doc, docAttrs));
        }

        if (foundMacroish && (text.contains("link:") || text.contains("ilto:"))) {
            // inline link macros, link:target[text]
            text = Rx.gsub(Rx.INLINE_LINK_MACRO_RX, text, m -> convertInlineLinkMacro(m, doc, docAttrs));
        }

        if (text.contains("@")) {
            text = Rx.gsub(Rx.INLINE_EMAIL_RX, text, m -> {
                // honor the escape
                if (m.group(1) != null) {
                    return RS.equals(m.group(1)) ? m.group().substring(1) : m.group();
                }

                String address = m.group();
                String target = "mailto:" + address;
                // QUESTION should this be registered as an e-mail address?
                doc.register("links", target);

                return new Inline(self(), "anchor", address, "link", null, target, null).convert();
            });
        }

        if (foundSquareBracket && "list_item".equals(getContext()) && getParent() instanceof AbstractBlock parent
                && "bibliography".equals(parent.getStyle())) {
            text = Rx.sub(Rx.INLINE_BIBLIO_ANCHOR_RX, text,
                    m -> new Inline(self(), "anchor", m.group(2), "bibref", m.group(1), null, null).convert());
        }

        if ((foundSquareBracket && text.contains("[[")) || (foundMacroish && text.contains("or:"))) {
            text = Rx.gsub(Rx.INLINE_ANCHOR_RX, text, m -> {
                // honor the escape
                if (m.group(1) != null) {
                    return m.group().substring(1);
                }

                // NOTE reftext is only relevant for DocBook output; used as value of xreflabel attribute
                String id = m.group(2);
                String reftext;
                if (id != null) {
                    reftext = m.group(3);
                } else {
                    id = m.group(4);
                    if ((reftext = m.group(5)) != null && reftext.contains(R_SB)) {
                        reftext = reftext.replace(ESC_R_SB, R_SB);
                    }
                }
                return new Inline(self(), "anchor", reftext, "ref", id, null, null).convert();
            });
        }

        if ((text.contains("&") && text.contains(";&l")) || (foundMacroish && text.contains("xref:"))) {
            text = Rx.gsub(Rx.INLINE_XREF_MACRO_RX, text, m -> convertInlineXref(m, doc));
        }

        if (foundMacroish && text.contains("tnote")) {
            text = Rx.gsub(Rx.INLINE_FOOTNOTE_MACRO_RX, text, m -> convertInlineFootnote(m, doc));
        }

        return text;
    }

    private String processInlineMacro(ProcessorExtension<InlineMacroProcessor> extension, Matcher m) {
        String match = m.group();
        // honor the escape
        if (match.startsWith(RS)) {
            return match.substring(1);
        }
        String target;
        String content;
        if (!Rx.hasNamedGroups(m.pattern())) {
            target = m.groupCount() >= 1 ? m.group(1) : null;
            content = m.groupCount() >= 2 ? m.group(2) : null;
        } else {
            target = Rx.namedGroup(m, "target");
            content = Rx.namedGroup(m, "content");
        }
        Map<String, Object> extConfig = extension.getConfig();
        Map<String, Object> attributes = new LinkedHashMap<>();
        if (extConfig.get("default_attrs") instanceof Map<?, ?> defaultAttrs) {
            defaultAttrs.forEach((key, value) -> attributes.put(String.valueOf(key), value));
        }
        ContentModel contentModel = ContentModel.of(extConfig.get("content_model"));
        if (content != null) {
            if (content.isEmpty()) {
                if (contentModel != ContentModel.ATTRIBUTES) {
                    attributes.put("text", content);
                }
            } else {
                content = normalizeText(content, true, true);
                // QUESTION should we store the unparsed attrlist in the attrlist key?
                if (contentModel == ContentModel.ATTRIBUTES) {
                    parseAttributes(content, positionalAttrs(extConfig), EnumSet.noneOf(ParseOption.class), attributes);
                } else {
                    attributes.put("text", content);
                }
            }
            // NOTE for convenience, map content (unparsed attrlist) to target when format is short
            if (target == null && "short".equals(String.valueOf(extConfig.get("format")))) {
                target = content;
            }
        }
        Object replacement = extension.getInstance().process(self(), target, attributes);
        if (replacement instanceof Inline inline) {
            Object inlineSubs = inline.getAttributes().remove("subs");
            List<Substitution> expanded;
            if (inlineSubs != null && (expanded = expandSubs(inlineSubs, "custom inline macro")) != null) {
                inline.setText(applySubs(inline.getText(), expanded));
            }
            return inline.convert();
        } else if (replacement != null) {
            getLogger().info(() -> "expected substitution value for custom inline macro to be of type Inline; got "
                    + replacement.getClass().getSimpleName() + ": " + match);
            return replacement.toString();
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    static List<String> positionalAttrs(Map<String, Object> extConfig) {
        Object positional = extConfig.get("positional_attrs");
        if (positional == null) {
            positional = extConfig.get("pos_attrs");
        }
        return positional == null ? List.of() : (List<String>) positional;
    }

    private String convertIndexterm(Matcher m) {
        String match = m.group();
        String kind = m.group(1);
        if ("indexterm".equals(kind)) {
            // honor the escape
            if (match.startsWith(RS)) {
                return match.substring(1);
            }

            // indexterm:[Tigers,Big cats]
            String attrlist = normalizeText(m.group(2), true, true);
            Map<String, Object> attrs;
            if (attrlist.contains("=")) {
                attrs = new AttributeList(attrlist, self()).parse();
                Object primary = attrs.get("1");
                if (primary != null) {
                    List<Object> terms = new ArrayList<>();
                    terms.add(primary);
                    Object secondary = attrs.get("2");
                    if (secondary != null) {
                        terms.add(secondary);
                        Object tertiary = attrs.get("3");
                        if (tertiary != null) {
                            terms.add(tertiary);
                        }
                    }
                    attrs.put("terms", terms);
                    Object seeAlso = attrs.get("see-also");
                    if (seeAlso != null) {
                        attrs.put("see-also", splitSeeAlso(seeAlso.toString()));
                    }
                } else {
                    attrs = new LinkedHashMap<>();
                    attrs.put("terms", attrlist);
                }
            } else {
                attrs = new LinkedHashMap<>();
                attrs.put("terms", splitSimpleCsv(attrlist));
            }
            return new Inline(self(), "indexterm", null, null, null, null, attrs).convert();
        } else if ("indexterm2".equals(kind)) {
            // honor the escape
            if (match.startsWith(RS)) {
                return match.substring(1);
            }

            // indexterm2:[Tigers]
            String term = normalizeText(m.group(2), true, true);
            Map<String, Object> attrs = null;
            if (term.contains("=")) {
                attrs = new AttributeList(term, self()).parse();
                Object primary = attrs.get("1");
                if (primary != null) {
                    term = primary.toString();
                    Object seeAlso = attrs.get("see-also");
                    if (seeAlso != null) {
                        attrs.put("see-also", splitSeeAlso(seeAlso.toString()));
                    }
                } else {
                    attrs = null;
                }
            }
            return new Inline(self(), "indexterm", term, "visible", null, null, attrs).convert();
        }

        String enclText = m.group(3);
        boolean visible;
        String before = null;
        String after = null;
        // honor the escape
        if (match.startsWith(RS)) {
            // escape concealed index term, but process nested flow index term
            if (enclText.startsWith("(") && enclText.endsWith(")")) {
                enclText = enclText.substring(1, enclText.length() - 1);
                visible = true;
                before = "(";
                after = ")";
            } else {
                return match.substring(1);
            }
        } else {
            visible = true;
            if (enclText.startsWith("(")) {
                if (enclText.endsWith(")")) {
                    enclText = enclText.substring(1, enclText.length() - 1);
                    visible = false;
                } else {
                    enclText = enclText.substring(1);
                    before = "(";
                    after = "";
                }
            } else if (enclText.endsWith(")")) {
                enclText = Strings.chop(enclText);
                before = "";
                after = ")";
            }
        }
        String subbedTerm;
        if (visible) {
            // ((Tigers))
            String term = normalizeText(enclText, true, false);
            Map<String, Object> attrs = null;
            if (term.contains(";&")) {
                if (term.contains(" &gt;&gt; ")) {
                    String[] parts = Strings.partition(term, " &gt;&gt; ");
                    term = parts[0];
                    attrs = new LinkedHashMap<>();
                    attrs.put("see", parts[2]);
                } else if (term.contains(" &amp;&gt; ")) {
                    List<String> parts = Strings.split(term, " &amp;&gt; ");
                    term = parts.remove(0);
                    attrs = new LinkedHashMap<>();
                    attrs.put("see-also", parts);
                }
            }
            subbedTerm = new Inline(self(), "indexterm", term, "visible", null, null, attrs).convert();
        } else {
            // (((Tigers,Big cats)))
            Map<String, Object> attrs = new LinkedHashMap<>();
            String terms = normalizeText(enclText, true, false);
            if (terms.contains(";&")) {
                if (terms.contains(" &gt;&gt; ")) {
                    String[] parts = Strings.partition(terms, " &gt;&gt; ");
                    terms = parts[0];
                    attrs.put("see", parts[2]);
                } else if (terms.contains(" &amp;&gt; ")) {
                    List<String> parts = Strings.split(terms, " &amp;&gt; ");
                    terms = parts.remove(0);
                    attrs.put("see-also", parts);
                }
            }
            attrs.put("terms", splitSimpleCsv(terms));
            subbedTerm = new Inline(self(), "indexterm", null, null, null, null, attrs).convert();
        }
        return before != null ? before + subbedTerm + after : subbedTerm;
    }

    private static List<String> splitSeeAlso(String seeAlso) {
        if (seeAlso.contains(",")) {
            List<String> result = new ArrayList<>();
            for (String it : Strings.split(seeAlso, ",")) {
                result.add(Strings.lstrip(it));
            }
            return result;
        }
        return new ArrayList<>(List.of(seeAlso));
    }

    private String convertInlineLink(Matcher m, Document doc, Map<String, Object> docAttrs) {
        String match = m.group();
        String g1 = m.group(1);
        String g3 = m.group(3);
        if (m.group(2) != null && m.group(5) == null) {
            // honor the escapes
            if (g1.startsWith(RS)) {
                return match.substring(1);
            }
            if (g3.startsWith(RS)) {
                return g1 + match.substring(g1.length() + 1);
            }
            if (m.group(6) == null) {
                return match;
            }
            String target = g3 + m.group(6);
            doc.register("links", target);
            String linkText = docAttrs.containsKey("hide-uri-scheme") ? Rx.sub(Rx.URI_SNIFF_RX, target, "") : target;
            Map<String, Object> attrs = new LinkedHashMap<>();
            attrs.put("role", "bare");
            return new Inline(self(), "anchor", linkText, "link", null, target, attrs).convert();
        }

        // honor the escape
        if (g3.startsWith(RS)) {
            return g1 + match.substring(g1.length() + 1);
        }
        String prefix = g1;
        String target = g3 + (m.group(4) != null ? m.group(4) : nullToEmpty(m.group(7)));
        String suffix = "";
        String linkText = null;
        Map<String, Object> attrs = null;
        // NOTE if group 5 is set (the attrlist), we're looking at a formal macro (e.g., https://example.org[])
        if (m.group(5) != null) {
            if (prefix.equals("link:")) {
                prefix = "";
            }
            linkText = m.group(5).isEmpty() ? null : m.group(5);
        } else {
            switch (prefix) {
                // invalid macro syntax (link: prefix w/o trailing square brackets or URL enclosed in quotes)
                // FIXME we probably shouldn't even get here when the link: prefix is present; the regex is doing too
                // much
                case "link:", "\"", "'" -> {
                    return match;
                }
                default -> {
                }
            }
            String lastChar = m.group(8);
            if (";".equals(lastChar)) {
                if ((target = Strings.chop(target)).endsWith(")")) {
                    // move trailing ); out of URL
                    target = Strings.chop(target);
                    suffix = ");";
                } else {
                    // move trailing ; out of URL
                    suffix = ";";
                }
                // NOTE handle case when modified target is a bare URI scheme (e.g., http://)
                if (target.equals(g3)) {
                    return match;
                }
            } else if (":".equals(lastChar)) {
                if ((target = Strings.chop(target)).endsWith(")")) {
                    // move trailing ): out of URL
                    target = Strings.chop(target);
                    suffix = "):";
                } else {
                    // move trailing : out of URL
                    suffix = ":";
                }
                // NOTE handle case when modified target is a bare URI scheme (e.g., http://)
                if (target.equals(g3)) {
                    return match;
                }
            }
        }

        String id = null;
        boolean bare = false;
        if (linkText != null) {
            String newLinkText;
            if (linkText.contains(R_SB)) {
                linkText = linkText.replace(ESC_R_SB, R_SB);
            }
            newLinkText = linkText;
            if (!doc.isCompatMode() && linkText.contains("=")) {
                // NOTE if an equals sign (=) is present, extract attributes from link text
                ExtractedAttributes extracted = extractAttributesFromText(linkText, "");
                linkText = extracted.text();
                attrs = extracted.attributes();
                newLinkText = linkText;
                id = Strings.valueOf(attrs.get("id"));
            }

            if (linkText.endsWith("^")) {
                newLinkText = linkText = Strings.chop(linkText);
                if (attrs != null) {
                    attrs.putIfAbsent("window", "_blank");
                    if (attrs.get("window") == null) {
                        attrs.put("window", "_blank");
                    }
                } else {
                    attrs = new LinkedHashMap<>();
                    attrs.put("window", "_blank");
                }
            }

            if (newLinkText != null && newLinkText.isEmpty()) {
                // NOTE the modified target will not be a bare URI scheme (e.g., http://) in this case
                linkText = docAttrs.containsKey("hide-uri-scheme") ? Rx.sub(Rx.URI_SNIFF_RX, target, "") : target;
                bare = true;
            }
        } else {
            // NOTE the modified target will not be a bare URI scheme (e.g., http://) in this case
            linkText = docAttrs.containsKey("hide-uri-scheme") ? Rx.sub(Rx.URI_SNIFF_RX, target, "") : target;
            bare = true;
        }

        if (bare) {
            if (attrs != null) {
                attrs.put("role", attrs.containsKey("role") ? "bare " + attrs.get("role") : "bare");
            } else {
                attrs = new LinkedHashMap<>();
                attrs.put("role", "bare");
            }
        }

        doc.register("links", target);
        return prefix + new Inline(self(), "anchor", linkText, "link", id, target, attrs).convert() + suffix;
    }

    private String convertInlineLinkMacro(Matcher m, Document doc, Map<String, Object> docAttrs) {
        String match = m.group();
        // honor the escape
        if (match.startsWith(RS)) {
            return match.substring(1);
        }
        String mailto = m.group(1);
        String mailtoText = null;
        String target;
        if (mailto != null) {
            mailtoText = m.group(2);
            target = "mailto:" + mailtoText;
        } else {
            target = m.group(2);
        }
        Map<String, Object> attrs = null;
        String id = null;
        String linkText = m.group(3);
        if (!linkText.isEmpty()) {
            if (linkText.contains(R_SB)) {
                linkText = linkText.replace(ESC_R_SB, R_SB);
            }
            if (mailto != null) {
                if (!doc.isCompatMode() && linkText.contains(",")) {
                    // NOTE if a comma (,) is present, extract attributes from link text
                    ExtractedAttributes extracted = extractAttributesFromText(linkText, "");
                    linkText = extracted.text();
                    attrs = extracted.attributes();
                    id = Strings.valueOf(attrs.get("id"));
                    if (attrs.containsKey("2")) {
                        if (attrs.containsKey("3")) {
                            target = target + "?subject=" + Helpers.encodeUriComponent(String.valueOf(attrs.get("2")))
                                    + "&amp;body=" + Helpers.encodeUriComponent(String.valueOf(attrs.get("3")));
                        } else {
                            target = target + "?subject=" + Helpers.encodeUriComponent(String.valueOf(attrs.get("2")));
                        }
                    }
                }
            } else if (!doc.isCompatMode() && linkText.contains("=")) {
                // NOTE if an equals sign (=) is present, extract attributes from link text
                ExtractedAttributes extracted = extractAttributesFromText(linkText, "");
                linkText = extracted.text();
                attrs = extracted.attributes();
                id = Strings.valueOf(attrs.get("id"));
            }

            if (linkText.endsWith("^")) {
                linkText = Strings.chop(linkText);
                if (attrs != null) {
                    if (attrs.get("window") == null) {
                        attrs.put("window", "_blank");
                    }
                } else {
                    attrs = new LinkedHashMap<>();
                    attrs.put("window", "_blank");
                }
            }
        }

        if (linkText.isEmpty()) {
            // mailto is a special case, already processed
            if (mailto != null) {
                linkText = mailtoText;
            } else {
                if (docAttrs.containsKey("hide-uri-scheme")) {
                    if ((linkText = Rx.sub(Rx.URI_SNIFF_RX, target, "")).isEmpty()) {
                        linkText = target;
                    }
                } else {
                    linkText = target;
                }
                if (attrs != null) {
                    attrs.put("role", attrs.containsKey("role") ? "bare " + attrs.get("role") : "bare");
                } else {
                    attrs = new LinkedHashMap<>();
                    attrs.put("role", "bare");
                }
            }
        }

        // QUESTION should a mailto be registered as an e-mail address?
        doc.register("links", target);
        return new Inline(self(), "anchor", linkText, "link", id, target, attrs).convert();
    }

    private String convertInlineXref(Matcher m, Document doc) {
        String match = m.group();
        // honor the escape
        if (match.startsWith(RS)) {
            return match.substring(1);
        }

        Map<String, Object> attrs = new LinkedHashMap<>();
        String refid = m.group(1);
        String linkText = null;
        boolean macro = false;
        if (refid != null) {
            if (refid.contains(",")) {
                String[] parts = Strings.partition(refid, ",");
                refid = parts[0];
                linkText = Strings.lstrip(parts[2]);
                if (linkText.isEmpty()) {
                    linkText = null;
                }
            }
        } else {
            macro = true;
            refid = m.group(2);
            if ((linkText = m.group(3)) != null) {
                if (linkText.contains(R_SB)) {
                    linkText = linkText.replace(ESC_R_SB, R_SB);
                }
                // NOTE if an equals sign (=) is present, extract attributes from link text
                if (!doc.isCompatMode() && linkText.contains("=")) {
                    ExtractedAttributes extracted = extractAttributesFromText(linkText, null);
                    linkText = extracted.text();
                    attrs = extracted.attributes();
                }
            }
        }

        String fragment = null;
        String path = null;
        String target = null;
        String src2src = null;
        int hashIdx;
        if (doc.isCompatMode()) {
            fragment = refid;
        } else if ((hashIdx = refid.indexOf('#')) >= 0
                && refid.charAt(hashIdx > 0 ? hashIdx - 1 : refid.length() - 1) != '&') {
            if (hashIdx > 0) {
                int fragmentLen = refid.length() - 1 - hashIdx;
                if (fragmentLen > 0) {
                    path = refid.substring(0, hashIdx);
                    fragment = refid.substring(hashIdx + 1);
                } else {
                    path = Strings.chop(refid);
                }
                if (macro) {
                    if (path.endsWith(".adoc")) {
                        src2src = path = path.substring(0, path.length() - 5);
                    } else if (!Helpers.hasExtname(path)) {
                        src2src = path;
                    }
                } else if (Strings.endsWithAny(path, ASCIIDOC_EXTENSIONS.toArray(String[]::new))) {
                    src2src = path = path.substring(0, path.lastIndexOf('.'));
                } else {
                    src2src = path;
                }
            } else {
                target = refid;
                fragment = refid.substring(1);
            }
        } else if (macro) {
            if (refid.endsWith(".adoc")) {
                src2src = path = refid.substring(0, refid.length() - 5);
            } else if (Helpers.hasExtname(refid)) {
                path = refid;
            } else {
                fragment = refid;
            }
        } else {
            fragment = refid;
        }

        Map<String, AbstractNode> refs = doc.getCatalog().getRefs();
        // handles: #id
        if (target != null) {
            refid = fragment;
            String possiblyInvalid = refid;
            if (getLogger().isInfo() && !refs.containsKey(refid)) {
                getLogger().info("possible invalid reference: " + possiblyInvalid);
            }
        } else if (path != null) {
            // handles: path#, path#id, path.adoc#, path.adoc#id, or path.adoc (xref macro only)
            // the referenced path is the current document, or its contents have been included in the current document
            if (src2src != null && (path.equals(doc.getAttributes().get("docname"))
                    || Boolean.TRUE.equals(doc.getCatalog().getIncludes().get(path)))) {
                if (fragment != null) {
                    refid = fragment;
                    path = null;
                    target = "#" + fragment;
                    String possiblyInvalid = refid;
                    if (getLogger().isInfo() && !refs.containsKey(refid)) {
                        getLogger().info("possible invalid reference: " + possiblyInvalid);
                    }
                } else {
                    refid = null;
                    path = null;
                    target = "#";
                }
            } else {
                Object relfileprefix = doc.getAttributes().get("relfileprefix");
                Object relfilesuffix = doc.getAttributes().containsKey("relfilesuffix")
                        ? doc.getAttributes().get("relfilesuffix") : doc.getOutfilesuffix();
                refid = path;
                path = (relfileprefix == null ? "" : relfileprefix.toString()) + path
                        + (src2src != null ? Strings.valueOf(relfilesuffix) : "");
                if (fragment != null) {
                    refid = refid + "#" + fragment;
                    target = path + "#" + fragment;
                } else {
                    target = path;
                }
            }
        // handles: id (in compat mode or when natural xrefs are disabled)
        } else if (doc.isCompatMode() || !Compliance.isNaturalXrefs()) {
            refid = fragment;
            target = "#" + fragment;
            String possiblyInvalid = refid;
            if (getLogger().isInfo() && !refs.containsKey(refid)) {
                getLogger().info("possible invalid reference: " + possiblyInvalid);
            }
        // handles: id
        } else if (refs.get(fragment) != null) {
            refid = fragment;
            target = "#" + fragment;
        // handles: Node Title or Reference Text
        // do reverse lookup on fragment if not a known ID and resembles reftext (contains a space or uppercase char)
        } else if ((fragment.contains(" ") || !fragment.toLowerCase().equals(fragment))
                && (refid = doc.resolveId(fragment)) != null) {
            fragment = refid;
            target = "#" + refid;
        } else {
            refid = fragment;
            target = "#" + fragment;
            String possiblyInvalid = refid;
            if (getLogger().isInfo()) {
                getLogger().info("possible invalid reference: " + possiblyInvalid);
            }
        }
        attrs.put("path", path);
        attrs.put("fragment", fragment);
        attrs.put("refid", refid);
        return new Inline(self(), "anchor", linkText, "xref", null, target, attrs).convert();
    }

    private String convertInlineFootnote(Matcher m, Document doc) {
        String match = m.group();
        // honor the escape
        if (match.startsWith(RS)) {
            return match.substring(1);
        }

        String id;
        String content;
        // footnoteref
        if (m.group(1) != null) {
            if (m.group(3) != null) {
                List<String> parts = Strings.split(m.group(3), ",", 2);
                id = parts.isEmpty() ? null : parts.get(0);
                content = parts.size() > 1 ? parts.get(1) : null;
                if (!doc.isCompatMode()) {
                    getLogger().warn("found deprecated footnoteref macro: " + match
                            + "; use footnote macro with target instead");
                }
            } else {
                return match;
            }
        // footnote
        } else {
            id = m.group(2);
            content = m.group(3);
        }

        Object index;
        String type;
        String target;
        if (id != null) {
            Footnote footnote = null;
            for (Footnote candidate : doc.getFootnotes()) {
                if (id.equals(candidate.id())) {
                    footnote = candidate;
                    break;
                }
            }
            if (footnote != null) {
                index = footnote.index();
                content = footnote.text();
                type = "xref";
                target = id;
                id = null;
            } else if (content != null) {
                content = restorePassthroughs(normalizeText(content, true, true));
                index = doc.counter("footnote-number");
                doc.register("footnotes", new Footnote(index instanceof Integer number ? number : Strings.toInt(index), id, content));
                type = "ref";
                target = null;
            } else {
                getLogger().warn("invalid footnote reference: " + id);
                type = "xref";
                target = id;
                content = id;
                id = null;
                index = null;
            }
        } else if (content != null) {
            content = restorePassthroughs(normalizeText(content, true, true));
            index = doc.counter("footnote-number");
            doc.register("footnotes", new Footnote(index instanceof Integer number ? number : Strings.toInt(index), id, content));
            type = null;
            target = null;
        } else {
            return match;
        }
        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("index", index);
        return new Inline(self(), "footnote", content, type, id, target, attrs).convert();
    }

    /**
     * Substitute post replacements.
     *
     * @param text The String text to process
     * @return the converted String text
     */
    public String subPostReplacements(String text) {
        if (getAttributes().get("hardbreaks-option") != null
                || getDocument().getAttributes().get("hardbreaks-option") != null) {
            List<String> lines = Strings.split(text, LF, -1);
            if (lines.size() < 2) {
                return text;
            }
            String last = lines.remove(lines.size() - 1);
            List<String> result = new ArrayList<>();
            for (String line : lines) {
                result.add(new Inline(self(), "break", line.endsWith(HARD_LINE_BREAK)
                        ? line.substring(0, line.length() - 2) : line, "line", null, null, null).convert());
            }
            result.add(last);
            return String.join(LF, result);
        } else if (text.contains(PLUS) && text.contains(HARD_LINE_BREAK)) {
            return Rx.gsub(Rx.HARD_LINE_BREAK_RX, text,
                    m -> new Inline(self(), "break", m.group(1), "line", null, null, null).convert());
        }
        return text;
    }

    /**
     * Apply verbatim substitutions on source (for use when highlighting is disabled).
     *
     * @param source the source code String on which to apply verbatim substitutions
     * @param processCallouts a Boolean flag indicating whether callout marks should be substituted
     * @return the substituted source
     */
    public String subSource(String source, boolean processCallouts) {
        return processCallouts ? subCallouts(subSpecialchars(source)) : subSpecialchars(source);
    }

    /**
     * Substitute callout source references.
     *
     * @param text The String text to process
     * @return the converted String text
     */
    public String subCallouts(String text) {
        Pattern calloutRx = getAttributes().containsKey("line-comment")
                ? Rx.calloutSourceRx(String.valueOf(getAttributes().get("line-comment")))
                : Rx.CALLOUT_SOURCE_RX;
        int[] autonum = { 0 };
        return Rx.gsub(calloutRx, text, m -> {
            // honor the escape
            if (m.group(2) != null) {
                // use sub since it might be behind a line comment
                return m.group().replaceFirst("\\\\", "");
            }
            Map<String, Object> attrs = new LinkedHashMap<>();
            attrs.put("guard", m.group(1) != null ? m.group(1) : ("--".equals(m.group(3)) ? List.of("<!--", "-->")
                    : null));
            String numeral = ".".equals(m.group(4)) ? String.valueOf(++autonum[0]) : m.group(4);
            return new Inline(self(), "callout", numeral, null, getDocument().getCallouts().readNextId(), null, attrs)
                    .convert();
        });
    }

    /**
     * Highlight (i.e., colorize) the source code during conversion using a syntax highlighter, if activated by the
     * source-highlighter document attribute. Otherwise return the text with verbatim substitutions applied.
     *
     * <p>If the processCallouts argument is true, this method will extract the callout marks from the source before
     * passing it to the syntax highlighter, then subsequently restore those callout marks to the highlighted source so
     * the callout marks don't confuse the syntax highlighter.
     *
     * @param source the source code String to syntax highlight
     * @param processCallouts a Boolean flag indicating whether callout marks should be located and substituted
     * @return the highlighted source code, if a syntax highlighter is defined on the document, otherwise the source
     *         with verbatim substitutions applied
     */
    public String highlightSource(String source, boolean processCallouts) {
        // NOTE the call to highlight? is a defensive check since, normally, we wouldn't arrive here unless it returns
        // true
        SyntaxHighlighter syntaxHl = getDocument().getSyntaxHighlighter();
        if (syntaxHl == null || !syntaxHl.isHighlight()) {
            return subSource(source, processCallouts);
        }

        Map<Integer, List<CalloutMark>> calloutMarks = null;
        if (processCallouts) {
            ExtractedCallouts extracted = extractCallouts(source);
            source = extracted.source();
            calloutMarks = extracted.calloutMarks();
        }

        Map<String, Object> docAttrs = getDocument().getAttributes();
        String syntaxHlName = syntaxHl.getName();
        String linenumsMode = null;
        Integer startLineNumber = null;
        if (isOption("linenums")) {
            Object mode = docAttrs.get(syntaxHlName + "-linenums-mode");
            linenumsMode = mode == null ? "table" : mode.toString();
            Object start = getAttributes().get("start");
            startLineNumber = start == null ? 1 : Strings.toInt(start);
            if (startLineNumber < 1) {
                startLineNumber = 1;
            }
        }
        List<Integer> highlightLines = null;
        if (getAttributes().containsKey("highlight")) {
            highlightLines = resolveLinesToHighlight(source, String.valueOf(getAttributes().get("highlight")),
                    startLineNumber);
        }

        Map<String, Object> opts = new LinkedHashMap<>();
        opts.put("callouts", calloutMarks);
        Object cssMode = docAttrs.get(syntaxHlName + "-css");
        opts.put("css_mode", cssMode == null ? "class" : cssMode.toString());
        opts.put("highlight_lines", highlightLines);
        opts.put("number_lines", linenumsMode);
        opts.put("start_line_number", startLineNumber);
        opts.put("style", docAttrs.get(syntaxHlName + "-style"));
        Object lang = getAttributes().get("language");
        SyntaxHighlighter.HighlightResult highlighted = syntaxHl.highlight((AbstractBlock) this, source,
                lang == null ? null : lang.toString(), opts);
        String highlightedText = highlighted.text();

        // fix passthrough placeholders that got caught up in syntax highlighting
        if (!passthroughs.isEmpty()) {
            highlightedText = Rx.gsub(HIGHLIGHTED_PASS_SLOT_RX, highlightedText,
                    m -> PASS_START + m.group(1) + PASS_END);
        }

        // NOTE highlight method may have depleted callouts
        return Strings.isNullOrEmpty(calloutMarks) ? highlightedText
                : restoreCallouts(highlightedText, calloutMarks, highlighted.sourceOffset());
    }

    /**
     * Resolve the line numbers in the specified source to highlight from the provided spec.
     *
     * <p>e.g., highlight="1-5, !2, 10" or highlight=1-5;!2,10
     *
     * @param source The String source.
     * @param spec The lines specifier (e.g., "1-5, !2, 10" or "1..5;!2;10")
     * @param start The line number of the first line (optional, default: null)
     * @return a List of unique, sorted line numbers.
     */
    public List<Integer> resolveLinesToHighlight(String source, String spec, Integer start) {
        List<Integer> lines = new ArrayList<>();
        if (spec.contains(" ")) {
            spec = spec.replace(" ", "");
        }
        for (String entry : spec.contains(",") ? Strings.split(spec, ",") : Strings.split(spec, ";")) {
            boolean negate = false;
            if (entry.startsWith("!")) {
                entry = entry.substring(1);
                negate = true;
            }
            String delim = entry.contains("..") ? ".." : (entry.contains("-") ? "-" : null);
            if (delim != null) {
                String[] parts = Strings.partition(entry, delim);
                int from = Strings.toInt(parts[0]);
                int to;
                if (parts[2].isEmpty() || (to = Strings.toInt(parts[2])) < 0) {
                    to = Strings.count(source, '\n') + 1;
                }
                if (negate) {
                    for (int i = from; i <= to; i++) {
                        lines.remove(Integer.valueOf(i));
                    }
                } else {
                    for (int i = from; i <= to; i++) {
                        if (!lines.contains(i)) {
                            lines.add(i);
                        }
                    }
                }
            } else if (negate) {
                lines.remove(Integer.valueOf(Strings.toInt(entry)));
            } else {
                int line = Strings.toInt(entry);
                if (!lines.contains(line)) {
                    lines.add(line);
                }
            }
        }
        // If the start attribute is defined, then the lines to highlight specified by the provided spec should be
        // relative to the start value.
        int shift = start != null ? start - 1 : 0;
        if (shift != 0) {
            lines.replaceAll(it -> it - shift);
        }
        lines.sort(null);
        return lines;
    }

    public List<Integer> resolveLinesToHighlight(String source, String spec) {
        return resolveLinesToHighlight(source, spec, null);
    }

    /**
     * Extract the passthrough text from the document for reinsertion after processing.
     *
     * @param text The String from which to extract passthrough fragments
     * @return the String text with passthrough regions substituted with placeholders
     */
    public String extractPassthroughs(String text) {
        boolean compatMode = getDocument().isCompatMode();
        List<Passthrough> passthrus = passthroughs;
        if (text.contains("++") || text.contains("$$") || text.contains("ss:")) {
            text = Rx.gsub(Rx.INLINE_PASS_MACRO_RX, text, m -> {
                String boundary = m.group(4);
                String preceding = null;
                int passthruKey;
                if (boundary != null) { // $$, ++, or +++
                    // skip ++ in compat mode, handled as normal quoted text
                    if (compatMode && boundary.equals("++")) {
                        return (m.group(2) != null ? nullToEmpty(m.group(1)) + "[" + m.group(2) + "]" + m.group(3)
                                : nullToEmpty(m.group(1)) + m.group(3)) + "++" + extractPassthroughs(m.group(5)) + "++";
                    }

                    String attrlist = m.group(2);
                    Map<String, Object> attributes = null;
                    boolean oldBehavior = false;
                    int escapeCount = m.group(3).length();
                    if (attrlist != null) {
                        if (escapeCount > 0) {
                            // NOTE we don't look for nested unconstrained pass macros
                            return nullToEmpty(m.group(1)) + "[" + attrlist + "]" + RS.repeat(escapeCount - 1) + boundary
                                    + m.group(5) + boundary;
                        } else if (RS.equals(m.group(1))) {
                            preceding = "[" + attrlist + "]";
                        } else if (boundary.equals("++")) {
                            if (attrlist.equals("x-")) {
                                oldBehavior = true;
                                attributes = new LinkedHashMap<>();
                            } else if (attrlist.endsWith(" x-")) {
                                oldBehavior = true;
                                attributes = parseQuotedTextAttributes(attrlist.substring(0, attrlist.length() - 3));
                            } else {
                                attributes = parseQuotedTextAttributes(attrlist);
                            }
                        } else {
                            attributes = parseQuotedTextAttributes(attrlist);
                        }
                    } else if (escapeCount > 0) {
                        // NOTE we don't look for nested unconstrained pass macros
                        return RS.repeat(escapeCount - 1) + boundary + m.group(5) + boundary;
                    }
                    List<Substitution> subs = boundary.equals("+++") ? NO_SUBS : BASIC_SUBS;

                    passthruKey = passthrus.size();
                    if (attributes != null) {
                        if (oldBehavior) {
                            passthrus.add(new Passthrough(m.group(5), NORMAL_SUBS, "monospaced", attributes));
                        } else {
                            passthrus.add(new Passthrough(m.group(5), subs, "unquoted", attributes));
                        }
                    } else {
                        passthrus.add(new Passthrough(m.group(5), subs, null, null));
                    }
                } else { // pass:[]
                    // NOTE we don't look for nested pass:[] macros
                    // honor the escape
                    if (RS.equals(m.group(6))) {
                        return m.group().substring(1);
                    }
                    passthruKey = passthrus.size();
                    String subs = m.group(7);
                    if (subs != null) {
                        passthrus.add(new Passthrough(normalizeText(m.group(8), false, true), resolvePassSubs(subs), null,
                                null));
                    } else {
                        passthrus.add(new Passthrough(normalizeText(m.group(8), false, true), null, null, null));
                    }
                }

                return (preceding == null ? "" : preceding) + PASS_START + passthruKey + PASS_END;
            });
        }

        Rx.InlinePass inlinePass = Rx.INLINE_PASS_RX.get(compatMode);
        if (text.contains(inlinePass.char1()) || (inlinePass.char2() != null && text.contains(inlinePass.char2()))) {
            text = Rx.gsub(inlinePass.pattern(), text, m -> {
                String preceding = m.group(1);
                String attrlist = m.group(4) != null ? m.group(4) : m.group(3);
                boolean escaped = m.group(5) != null;
                String quotedText = m.group(6);
                String formatMark = m.group(7);
                String content = m.group(8);

                boolean oldBehavior = false;
                boolean oldBehaviorForced = false;
                if (compatMode) {
                    oldBehavior = true;
                } else if (attrlist != null && (attrlist.equals("x-") || attrlist.endsWith(" x-"))) {
                    oldBehavior = oldBehaviorForced = true;
                }

                Map<String, Object> attributes = null;
                if (attrlist != null) {
                    if (escaped) {
                        // honor the escape of the formatting mark
                        return nullToEmpty(preceding) + "[" + attrlist + "]" + quotedText.substring(1);
                    } else if (RS.equals(preceding)) {
                        // honor the escape of the attributes
                        if (oldBehaviorForced && formatMark.equals("`")) {
                            return preceding + "[" + attrlist + "]" + quotedText;
                        }
                        preceding = "[" + attrlist + "]";
                    } else if (oldBehaviorForced) {
                        attributes = attrlist.equals("x-") ? new LinkedHashMap<>()
                                : parseQuotedTextAttributes(attrlist.substring(0, attrlist.length() - 3));
                    } else {
                        attributes = parseQuotedTextAttributes(attrlist);
                    }
                } else if (escaped) {
                    // honor the escape of the formatting mark
                    return nullToEmpty(preceding) + quotedText.substring(1);
                } else if (compatMode && RS.equals(preceding)) {
                    return quotedText;
                }

                int passthruKey = passthrus.size();
                if (compatMode) {
                    passthrus.add(new Passthrough(content, BASIC_SUBS, "monospaced", attributes));
                } else if (attributes != null) {
                    if (oldBehavior) {
                        List<Substitution> subs = formatMark.equals("`") ? BASIC_SUBS : NORMAL_SUBS;
                        passthrus.add(new Passthrough(content, subs, "monospaced", attributes));
                    } else {
                        passthrus.add(new Passthrough(content, BASIC_SUBS, "unquoted", attributes));
                    }
                } else {
                    passthrus.add(new Passthrough(content, BASIC_SUBS, null, null));
                }

                return nullToEmpty(preceding) + PASS_START + passthruKey + PASS_END;
            });
        }

        // NOTE we need to do the stem in a subsequent step to allow it to be escaped by the former
        if (text.contains(":") && (text.contains("stem:") || text.contains("math:"))) {
            text = Rx.gsub(Rx.INLINE_STEM_MACRO_RX, text, m -> {
                // honor the escape
                if (m.group().startsWith(RS)) {
                    return m.group().substring(1);
                }

                String type = m.group(1);
                if (type.equals("stem")) {
                    type = Asciidoctor.resolveStemType(getDocument().getAttributes().get("stem"));
                }
                String subsSpec = m.group(2);
                String content = normalizeText(m.group(3), false, true);
                // NOTE drop enclosing $ signs around latexmath for backwards compatibility with AsciiDoc.py
                if (type.equals("latexmath") && content.startsWith("$") && content.endsWith("$")) {
                    content = content.substring(1, content.length() - 1);
                }
                List<Substitution> subs = subsSpec != null ? resolvePassSubs(subsSpec, "stem macro")
                        : (getDocument().isBasebackend("html") ? BASIC_SUBS : null);
                int passthruKey = passthrus.size();
                passthrus.add(new Passthrough(content, subs, type, null));
                return PASS_START + passthruKey + PASS_END;
            });
        }

        return text;
    }

    /**
     * Restore the passthrough text by reinserting into the placeholder positions.
     *
     * @param text The String text into which to restore the passthrough text
     * @return the String text with the passthrough text restored
     */
    public String restorePassthroughs(String text) {
        List<Passthrough> passthrus = passthroughs;
        return Rx.gsub(PASS_SLOT_RX, text, m -> {
            int idx = Integer.parseInt(m.group(1));
            Passthrough pass = idx < passthrus.size() ? passthrus.get(idx) : null;
            if (pass != null) {
                String subbedText = applySubs(pass.text(), pass.subs());
                String type = pass.type();
                if (type != null) {
                    String id = null;
                    Map<String, Object> attributes = pass.attributes();
                    if (attributes != null) {
                        id = Strings.valueOf(attributes.get("id"));
                    }
                    subbedText = new Inline(self(), "quoted", subbedText, type, id, null, attributes).convert();
                }
                return subbedText.contains(PASS_START) ? restorePassthroughs(subbedText) : subbedText;
            }
            getLogger().error("unresolved passthrough detected: " + text);
            return "??pass??";
        });
    }

    /**
     * Resolve the list of comma-delimited subs against the possible options.
     *
     * @param subs The comma-delimited String of substitution names or aliases.
     * @param type A SubsType representing the context for which the subs are being resolved (default: BLOCK).
     * @param defaults A List of substitutions to start with when computing incremental substitutions (default: null).
     * @param subject The String to use in log messages to communicate the subject for which subs are being resolved
     *            (default: null)
     * @return A List of Substitutions representing the substitution operation or null if no subs are found.
     */
    public List<Substitution> resolveSubs(String subs, SubsType type, List<Substitution> defaults, String subject) {
        if (Strings.isNullOrEmpty(subs)) {
            return null;
        }
        if (type == null) {
            type = SubsType.BLOCK;
        }
        // QUESTION should we store candidates as a Set instead of a List?
        List<String> candidates = null;
        if (subs.contains(" ")) {
            subs = subs.replace(" ", "");
        }
        boolean modifiersPresent = Rx.SUB_MODIFIER_SNIFF_RX.matcher(subs).find();
        for (String key : Strings.split(subs, ",")) {
            String modifierOperation = null;
            if (modifiersPresent) {
                if (key.startsWith("+")) {
                    modifierOperation = "append";
                    key = key.substring(1);
                } else if (key.startsWith("-")) {
                    modifierOperation = "remove";
                    key = key.substring(1);
                } else if (key.endsWith("+")) {
                    modifierOperation = "prepend";
                    key = Strings.chop(key);
                }
            }
            List<String> resolvedKeys;
            // special case to disable callouts for inline subs
            if (type == SubsType.INLINE && (key.equals("verbatim") || key.equals("v"))) {
                resolvedKeys = keys(BASIC_SUBS);
            } else if (SUB_GROUPS.containsKey(key)) {
                resolvedKeys = keys(SUB_GROUPS.get(key));
            } else if (type == SubsType.INLINE && key.length() == 1 && SUB_HINTS.containsKey(key)) {
                String resolvedKey = SUB_HINTS.get(key);
                List<Substitution> candidate = SUB_GROUPS.get(resolvedKey);
                resolvedKeys = candidate != null ? keys(candidate) : List.of(resolvedKey);
            } else {
                resolvedKeys = List.of(key);
            }

            if (modifierOperation != null) {
                if (candidates == null) {
                    candidates = defaults != null ? keys(defaults) : new ArrayList<>();
                }
                switch (modifierOperation) {
                    case "append" -> candidates.addAll(resolvedKeys);
                    case "prepend" -> candidates.addAll(0, resolvedKeys);
                    default -> candidates.removeAll(resolvedKeys);
                }
            } else {
                if (candidates == null) {
                    candidates = new ArrayList<>();
                }
                candidates.addAll(resolvedKeys);
            }
        }
        if (candidates == null) {
            return null;
        }
        // weed out invalid options and remove duplicates (order is preserved; first occurrence wins)
        Set<Substitution> resolved = new LinkedHashSet<>();
        List<String> invalid = new ArrayList<>();
        Set<Substitution> options = SUB_OPTIONS.get(type);
        for (String candidate : candidates) {
            Substitution sub = Substitution.forKey(candidate);
            if (sub != null && options.contains(sub)) {
                resolved.add(sub);
            } else {
                invalid.add(candidate);
            }
        }
        if (!invalid.isEmpty()) {
            getLogger().warn("invalid substitution type" + (invalid.size() > 1 ? "s" : "") + (subject != null ? " for "
                    : "") + nullToEmpty(subject) + ": " + String.join(", ", invalid));
        }
        return new ArrayList<>(resolved);
    }

    private static List<String> keys(Collection<Substitution> subs) {
        List<String> result = new ArrayList<>();
        for (Substitution sub : subs) {
            result.add(sub.getKey());
        }
        return result;
    }

    /**
     * Call resolveSubs for the BLOCK type.
     */
    public List<Substitution> resolveBlockSubs(String subs, List<Substitution> defaults, String subject) {
        return resolveSubs(subs, SubsType.BLOCK, defaults, subject);
    }

    /**
     * Call resolveSubs for the INLINE type with the subject set as passthrough macro.
     */
    public List<Substitution> resolvePassSubs(String subs) {
        return resolvePassSubs(subs, "passthrough macro");
    }

    /**
     * Call resolveSubs for the INLINE type with the specified subject.
     */
    public List<Substitution> resolvePassSubs(String subs, String subject) {
        return resolveSubs(subs, SubsType.INLINE, null, subject);
    }

    /**
     * Expand all groups in the subs list and return. If no subs are resolved, return null.
     *
     * @param subs The substitutions to expand; can be a Substitution, a List of Substitutions or group names, or a
     *            String
     * @param subject The String to use in log messages to communicate the subject for which subs are being resolved
     *            (default: null)
     * @return a Substitution List to pass to applySubs or null if no substitutions were resolved.
     */
    public List<Substitution> expandSubs(Object subs, String subject) {
        if (subs instanceof Substitution sub) {
            return List.of(sub);
        } else if (subs instanceof List<?> list) {
            List<Substitution> expandedSubs = new ArrayList<>();
            for (Object key : list) {
                if (key instanceof Substitution sub) {
                    expandedSubs.add(sub);
                } else if (!"none".equals(String.valueOf(key))) {
                    List<Substitution> subGroup = SUB_GROUPS.get(String.valueOf(key));
                    if (subGroup != null) {
                        expandedSubs.addAll(subGroup);
                    } else {
                        Substitution sub = Substitution.forKey(String.valueOf(key));
                        if (sub != null) {
                            expandedSubs.add(sub);
                        }
                    }
                }
            }
            return expandedSubs.isEmpty() ? null : expandedSubs;
        }
        return resolveSubs(subs == null ? null : subs.toString(), SubsType.INLINE, null, subject);
    }

    public List<Substitution> expandSubs(Object subs) {
        return expandSubs(subs, null);
    }

    /**
     * Parse attributes in name or name=value format from a comma-separated String.
     *
     * @param attrlist A comma-separated String list of attributes in name or name=value format.
     * @param posattrs A List of positional attribute names (default: []).
     * @param options the options that control how the string is parsed
     * @param into The Map to parse the attributes into (optional, default: null).
     * @return an empty Map if attrlist is null or empty, otherwise a Map of parsed attributes.
     */
    public Map<String, Object> parseAttributes(String attrlist, List<String> posattrs, Set<ParseOption> options,
            Map<String, Object> into) {
        if (attrlist == null || attrlist.isEmpty()) {
            return into != null ? into : new LinkedHashMap<>();
        }
        if (options == null) {
            options = Set.of();
        }
        if (posattrs == null) {
            posattrs = List.of();
        }
        if (options.contains(ParseOption.UNESCAPE_INPUT)) {
            attrlist = normalizeText(attrlist, true, true);
        }
        if (options.contains(ParseOption.SUB_INPUT) && attrlist.contains(ATTR_REF_HEAD)) {
            attrlist = getDocument().subAttributes(attrlist);
        }
        // substitutions are only performed on attribute values if block is not null
        AbstractNode block = options.contains(ParseOption.SUB_RESULT) ? self() : null;
        if (into != null) {
            return new AttributeList(attrlist, block).parseInto(into, posattrs);
        }
        return new AttributeList(attrlist, block).parse(posattrs);
    }

    public Map<String, Object> parseAttributes(String attrlist, List<String> posattrs) {
        return parseAttributes(attrlist, posattrs, Set.of(), null);
    }

    public Map<String, Object> parseAttributes(String attrlist) {
        return parseAttributes(attrlist, List.of(), Set.of(), null);
    }

    /** The text and attributes extracted from text that may contain an attribute list. */
    protected record ExtractedAttributes(String text, Map<String, Object> attributes) {
    }

    /**
     * This method is used in cases when the attrlist can be mixed with the text of a macro. If no attributes are
     * detected aside from the first positional attribute, and the first positional attribute matches the attrlist,
     * then the original text is returned.
     */
    protected ExtractedAttributes extractAttributesFromText(String text, String defaultText) {
        String attrlist = text.contains(LF) ? text.replace('\n', ' ') : text;
        Map<String, Object> attrs = new AttributeList(attrlist, self()).parse();
        Object resolvedText = attrs.get("1");
        if (resolvedText != null) {
            // NOTE if resolved text remains unchanged, clear attributes and return unparsed text
            if (resolvedText.equals(attrlist)) {
                attrs.clear();
                return new ExtractedAttributes(text, attrs);
            }
            return new ExtractedAttributes(resolvedText.toString(), attrs);
        }
        return new ExtractedAttributes(defaultText, attrs);
    }

    private record ExtractedCallouts(String source, Map<Integer, List<CalloutMark>> calloutMarks) {
    }

    /**
     * Extract the callout numbers from the source to prepare it for syntax highlighting.
     */
    private ExtractedCallouts extractCallouts(String source) {
        Map<Integer, List<CalloutMark>> calloutMarks = new TreeMap<>();
        int[] autonum = { 0 };
        int lineno = 0;
        Integer lastLineno = null;
        Pattern calloutRx = getAttributes().containsKey("line-comment")
                ? Rx.calloutExtractRx(String.valueOf(getAttributes().get("line-comment")))
                : Rx.CALLOUT_EXTRACT_RX;
        // extract callout marks, indexed by line number
        List<String> lines = new ArrayList<>();
        for (String line : Strings.split(source, LF, -1)) {
            int currentLineno = ++lineno;
            boolean[] found = { false };
            lines.add(Rx.gsub(calloutRx, line, m -> {
                // honor the escape
                if (m.group(2) != null) {
                    // use sub since it might be behind a line comment
                    return m.group().replaceFirst("\\\\", "");
                }
                Object guard = m.group(1) != null ? m.group(1) : ("--".equals(m.group(3)) ? List.of("<!--", "-->")
                        : null);
                String numeral = ".".equals(m.group(4)) ? String.valueOf(++autonum[0]) : m.group(4);
                calloutMarks.computeIfAbsent(currentLineno, k -> new ArrayList<>()).add(new CalloutMark(guard, numeral));
                found[0] = true;
                return "";
            }));
            if (found[0]) {
                lastLineno = currentLineno;
            }
        }
        source = String.join(LF, lines);
        if (lastLineno != null) {
            if (lastLineno == lineno) {
                source = source + LF;
            }
            return new ExtractedCallouts(source, calloutMarks);
        }
        return new ExtractedCallouts(source, null);
    }

    /**
     * Restore the callout numbers to the highlighted source.
     */
    private String restoreCallouts(String source, Map<Integer, List<CalloutMark>> calloutMarks, Integer sourceOffset) {
        String preamble;
        if (sourceOffset != null) {
            preamble = source.substring(0, sourceOffset);
            source = source.substring(sourceOffset);
        } else {
            preamble = "";
        }
        int lineno = 0;
        List<String> result = new ArrayList<>();
        for (String line : Strings.split(source, LF, -1)) {
            List<CalloutMark> conums = calloutMarks.remove(++lineno);
            if (conums != null) {
                List<String> converted = new ArrayList<>();
                for (CalloutMark mark : conums) {
                    Map<String, Object> attrs = new LinkedHashMap<>();
                    attrs.put("guard", mark.guard());
                    converted.add(new Inline(self(), "callout", mark.numeral(), null,
                            getDocument().getCallouts().readNextId(), null, attrs).convert());
                }
                result.add(line + String.join(" ", converted));
            } else {
                result.add(line);
            }
        }
        return preamble + String.join(LF, result);
    }

    /**
     * Convert a quoted text region.
     *
     * @param match The match for the quoted text region
     * @param type The quoting type (single, double, strong, emphasis, monospaced, etc)
     * @param scope The scope of the quoting (constrained or unconstrained)
     * @return The converted String text for the quoted text region
     */
    private String convertQuotedText(Matcher match, String type, QuoteScope scope) {
        String unescapedAttrs = null;
        if (match.group().startsWith(RS)) {
            String attrs;
            if (scope == QuoteScope.CONSTRAINED && (attrs = match.group(2)) != null) {
                unescapedAttrs = "[" + attrs + "]";
            } else {
                return match.group().substring(1);
            }
        }

        if (scope == QuoteScope.CONSTRAINED) {
            if (unescapedAttrs != null) {
                return unescapedAttrs + new Inline(self(), "quoted", match.group(3), type, null, null, null).convert();
            }
            String attrlist = match.group(2);
            String id = null;
            Map<String, Object> attributes = null;
            if (attrlist != null) {
                attributes = parseQuotedTextAttributes(attrlist);
                id = Strings.valueOf(attributes.get("id"));
                if (type.equals("mark")) {
                    type = "unquoted";
                }
            }
            return match.group(1) + new Inline(self(), "quoted", match.group(3), type, id, null, attributes).convert();
        }
        String attrlist = match.group(1);
        String id = null;
        Map<String, Object> attributes = null;
        if (attrlist != null) {
            attributes = parseQuotedTextAttributes(attrlist);
            id = Strings.valueOf(attributes.get("id"));
            if (type.equals("mark")) {
                type = "unquoted";
            }
        }
        return new Inline(self(), "quoted", match.group(2), type, id, null, attributes).convert();
    }

    /**
     * Substitute replacement text for matched location.
     *
     * @return The String text with the replacement characters substituted
     */
    private String doReplacement(Matcher match, String replacement, Asciidoctor.ReplacementRestore restore) {
        String captured = match.group();
        if (captured.contains(RS)) {
            // we have to use sub since we aren't sure it's the first char
            return captured.replaceFirst("\\\\", "");
        }
        return switch (restore) {
            case NONE -> replacement;
            case BOUNDING -> match.group(1) + replacement + match.group(2);
            case LEADING -> match.group(1) + replacement;
        };
    }

    /**
     * Inserts text into a formatted text enclosure; used by xreftext.
     */
    protected String subPlaceholder(String format, String text) {
        int idx = format.indexOf("%s");
        return idx < 0 ? format : format.substring(0, idx) + nullToEmpty(text) + format.substring(idx + 2);
    }

    /**
     * Parse the attributes that are defined on quoted (aka formatted) text.
     *
     * @param str A non-null String of unprocessed attributes; space-separated roles (e.g., role1 role2) or the id/role
     *            shorthand syntax (e.g., #idname.role)
     * @return a Map of attributes (role and id only)
     */
    protected Map<String, Object> parseQuotedTextAttributes(String str) {
        // NOTE attributes are typically resolved after quoted text, so substitute eagerly
        if (str.contains(ATTR_REF_HEAD)) {
            str = subAttributes(str);
        }
        // for compliance, only consider first positional attribute (very unlikely)
        if (str.contains(",")) {
            str = str.substring(0, str.indexOf(','));
        }
        Map<String, Object> attrs = new LinkedHashMap<>();
        if ((str = Strings.strip(str)).isEmpty()) {
            return attrs;
        } else if ((str.startsWith(".") || str.startsWith("#")) && Compliance.isShorthandPropertySyntax()) {
            String[] parts = Strings.partition(str, "#");
            String before = parts[0];
            String after = parts[2];
            if (after.isEmpty()) {
                if (before.length() > 1) {
                    attrs.put("role", Strings.lstrip(before.replace('.', ' ')));
                }
            } else {
                String[] idParts = Strings.partition(after, ".");
                String id = idParts[0];
                String roles = idParts[2];
                if (!id.isEmpty()) {
                    attrs.put("id", id);
                }
                if (roles.isEmpty()) {
                    if (before.length() > 1) {
                        attrs.put("role", Strings.lstrip(before.replace('.', ' ')));
                    }
                } else if (before.length() > 1) {
                    attrs.put("role", Strings.lstrip((before + "." + roles).replace('.', ' ')));
                } else {
                    attrs.put("role", roles.replace('.', ' '));
                }
            }
            return attrs;
        }
        attrs.put("role", str);
        return attrs;
    }

    /**
     * Normalize text to prepare it for parsing.
     *
     * <p>If normalizeWhitespace is true, strip surrounding whitespace and fold newlines. If
     * unescapeClosingSquareBrackets is set, unescape any escaped closing square brackets.
     *
     * @return the normalized text String
     */
    protected String normalizeText(String text, boolean normalizeWhitespace, boolean unescapeClosingSquareBrackets) {
        if (!text.isEmpty()) {
            if (normalizeWhitespace) {
                text = Strings.strip(text).replace('\n', ' ');
            }
            if (unescapeClosingSquareBrackets && text.contains(R_SB)) {
                text = text.replace(ESC_R_SB, R_SB);
            }
        }
        return text;
    }

    /**
     * Split text formatted as CSV with support for double-quoted values (in which commas are ignored).
     */
    protected List<String> splitSimpleCsv(String str) {
        List<String> values = new ArrayList<>();
        if (str.isEmpty()) {
            return values;
        } else if (str.contains("\"")) {
            StringBuilder accum = new StringBuilder();
            boolean quoteOpen = false;
            for (int i = 0, len = str.length(); i < len; i++) {
                char c = str.charAt(i);
                switch (c) {
                    case ',' -> {
                        if (quoteOpen) {
                            accum.append(c);
                        } else {
                            values.add(Strings.strip(accum.toString()));
                            accum.setLength(0);
                        }
                    }
                    case '"' -> quoteOpen = !quoteOpen;
                    default -> accum.append(c);
                }
            }
            values.add(Strings.strip(accum.toString()));
            return values;
        }
        for (String it : Strings.split(str, ",")) {
            values.add(Strings.strip(it));
        }
        return values;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
