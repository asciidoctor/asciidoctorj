package org.asciidoctor.core;

import static org.asciidoctor.core.Asciidoctor.ASCIIDOC_EXTENSIONS;
import static org.asciidoctor.core.Asciidoctor.ATTR_REF_HEAD;
import static org.asciidoctor.core.Asciidoctor.LF;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;

import org.asciidoctor.core.Substitutors.ParseOption;
import org.asciidoctor.core.extension.IncludeProcessor;
import org.asciidoctor.core.extension.ProcessorExtension;

/**
 * Methods for retrieving lines from AsciiDoc source files, evaluating preprocessor directives as each line is read off
 * the List of lines.
 */
public class PreprocessorReader extends Reader {
    private record IncludeContext(List<String> lines, String file, String dir, String path, int lineno,
            MaxDepth maxdepth, boolean processLines) {
        @Override
        public String toString() {
            return "[" + path + ", " + lineno + "]";
        }
    }

    private record MaxDepth(int abs, int curr, int rel) {
    }

    private record ConditionalContext(String name, String target, String expr, boolean skip, boolean skipping,
            Cursor sourceLocation) {
    }

    private record TagContext(String name, Boolean select, int lineno) {
    }

    private final Document document;
    private final boolean sourcemap;
    private MaxDepth maxdepth;
    private final List<IncludeContext> includeStack = new ArrayList<>();
    private final Map<String, Boolean> includes;
    private boolean skipping;
    private final List<ConditionalContext> conditionalStack = new ArrayList<>();
    private List<ProcessorExtension<IncludeProcessor>> includeProcessorExtensions;
    private boolean includeProcessorsResolved;
    private int savedIncludeStackSize = -1;

    /**
     * Initialize the PreprocessorReader object.
     *
     * @param document the Document being parsed
     * @param data the source data as a String or a List of String lines
     * @param cursor the starting cursor (either a {@link Cursor} or a String file path) or null
     * @param normalize whether to normalize the lines (Boolean.TRUE, "chomp" or null)
     */
    public PreprocessorReader(Document document, Object data, Object cursor, Object normalize) {
        super(cursor, Deferred.LINES);
        this.document = document;
        this.sourcemap = document.isSourcemap();
        initLines(prepareLines(data, normalize, false, null,
                document.getAttributes().get("skip-front-matter") != null));
        int defaultIncludeDepth = Strings.toInt(document.getAttributes().getOrDefault("max-include-depth", 64));
        if (defaultIncludeDepth > 0) {
            // track absolute max depth, current max depth for comparing to include stack size, and relative max depth
            // for reporting
            maxdepth = new MaxDepth(defaultIncludeDepth, defaultIncludeDepth, defaultIncludeDepth);
        } else {
            // if maxdepth is not set, built-in include functionality is disabled
            maxdepth = null;
        }
        includes = document.getCatalog().getIncludes();
        skipping = false;
    }

    public PreprocessorReader(Document document, Object data, Object cursor) {
        this(document, data, cursor, null);
    }

    public PreprocessorReader(Document document, Object data) {
        this(document, data, null, null);
    }

    public Document getDocument() {
        return document;
    }

    /**
     * @return the paths of the files on the include stack, from the outermost to the innermost
     */
    public List<String> getIncludeStack() {
        return includeStack.stream().map(IncludeContext::path).toList();
    }

    @Override
    public boolean hasMoreLines() {
        return peekLine() != null;
    }

    @Override
    public boolean isEmpty() {
        return peekLine() == null;
    }

    @Override
    public boolean isEof() {
        return isEmpty();
    }

    /**
     * Override the Reader#peekLine method to pop the include stack if the last line has been reached and there's at
     * least one include on the stack.
     *
     * @return the next line of the source data as a String if there are lines remaining in the current include context
     *         or a parent include context, otherwise null.
     */
    @Override
    public String peekLine(boolean direct) {
        String line = super.peekLine(direct);
        if (line != null) {
            return line;
        } else if (includeStack.isEmpty()) {
            Cursor endCursor = null;
            for (ConditionalContext conditional : conditionalStack) {
                Cursor sourceLocation = conditional.sourceLocation();
                if (sourceLocation == null) {
                    sourceLocation = endCursor == null ? (endCursor = cursorAtPrevLine()) : endCursor;
                }
                getLogger().error(Logging.messageWithContext("detected unterminated preprocessor conditional directive: "
                        + conditional.name() + "::" + Objects.toString(conditional.target(), "") + "["
                        + Objects.toString(conditional.expr(), "") + "]", sourceLocation));
            }
            conditionalStack.clear();
            return null;
        }
        popInclude();
        return peekLine(direct);
    }

    /**
     * Push source onto the front of the reader and switch the context based on the file, document-relative path and
     * line information given.
     *
     * <p>This method is typically used in an IncludeProcessor to add source read from the target specified.
     *
     * <pre>
     *   path = "partial.adoc";
     *   file = new File(path).getAbsolutePath();
     *   data = Files.readString(Path.of(file));
     *   reader.pushInclude(data, file, path, 1, Map.of());
     * </pre>
     *
     * @param data the String or List of lines to push
     * @param file the absolute path or URI of the file being included (may be null)
     * @param path the path of the include relative to the outermost document (may be null)
     * @param lineno the line number at which the included content starts
     * @param attributes the attributes parsed from the include directive
     * @return this Reader object.
     */
    public PreprocessorReader pushInclude(Object data, String file, String path, int lineno,
            Map<String, Object> attributes) {
        if (attributes == null) {
            attributes = Map.of();
        }
        includeStack.add(new IncludeContext(lines, this.file, dir, this.path, this.lineno, maxdepth, processLines));
        if ((this.file = file) != null) {
            if (Helpers.isUriish(file)) {
                URI uri = URI.create(file);
                String uriDir = Helpers.dirname(uri.getPath() == null ? "/" : uri.getPath());
                String base = file.substring(0, file.length() - (uri.getRawPath() == null ? 0
                        : uri.getRawPath().length() + (uri.getRawQuery() == null ? 0 : uri.getRawQuery().length() + 1)
                        + (uri.getRawFragment() == null ? 0 : uri.getRawFragment().length() + 1)));
                dir = base + ("/".equals(uriDir) ? "" : uriDir);
            } else {
                dir = Helpers.dirname(file);
            }
            if (path == null) {
                path = Helpers.basename(file);
            }
            this.path = path;
            // only process lines in AsciiDoc files
            processLines = Strings.endsWithAny(file, ASCIIDOC_EXTENSIONS.toArray(String[]::new));
            if (processLines) {
                // NOTE registering the include with a null value tracks it while not making it visible to
                // interdocument xrefs
                String key = path.substring(0, Math.max(path.lastIndexOf('.'), 0));
                if (includes.get(key) == null) {
                    includes.put(key, attributes.get("partial-option") != null ? null : true);
                }
            }
        } else {
            dir = ".";
            // we don't know what file type we have, so assume AsciiDoc
            processLines = true;
            if ((this.path = path) != null) {
                // NOTE registering the include with a null value tracks it while not making it visible to
                // interdocument xrefs
                String key = Helpers.rootname(path);
                if (includes.get(key) == null) {
                    includes.put(key, attributes.get("partial-option") != null ? null : true);
                }
            } else {
                this.path = "<stdin>";
            }
        }

        this.lineno = lineno;

        if (maxdepth != null && attributes.containsKey("depth")) {
            int relMaxdepth = Strings.toInt(attributes.get("depth"));
            if (relMaxdepth > 0) {
                int currMaxdepth = includeStack.size() + relMaxdepth;
                int absMaxdepth = maxdepth.abs();
                if (currMaxdepth > absMaxdepth) {
                    // if relative depth exceeds absolute max depth, effectively ignore relative depth request
                    currMaxdepth = relMaxdepth = absMaxdepth;
                }
                maxdepth = new MaxDepth(absMaxdepth, currMaxdepth, relMaxdepth);
            } else {
                maxdepth = new MaxDepth(maxdepth.abs(), includeStack.size(), 0);
            }
        }

        // effectively fill the buffer
        List<String> preparedLines = prepareLines(data, processLines ? Boolean.TRUE : "chomp", true,
                attributes.get("indent"), attributes.get("skip-front-matter-option") != null);
        if (preparedLines.isEmpty()) {
            lines = preparedLines;
            popInclude();
        } else {
            // FIXME we eventually want to handle leveloffset without affecting the lines
            if (attributes.containsKey("leveloffset")) {
                Object leveloffset = document.getAttribute("leveloffset");
                // NOTE the stack is stored in reverse order, so the last line is read first
                List<String> stack = new ArrayList<>();
                stack.add(leveloffset != null ? ":leveloffset: " + leveloffset : ":leveloffset!:");
                stack.add("");
                List<String> reversedLines = new ArrayList<>(preparedLines);
                Collections.reverse(reversedLines);
                stack.addAll(reversedLines);
                stack.add("");
                stack.add(":leveloffset: " + attributes.get("leveloffset"));
                lines = stack;
                // compensate for these extra lines at the top
                this.lineno -= 2;
            } else {
                lines = new ArrayList<>(preparedLines);
                Collections.reverse(lines);
            }

            // FIXME kind of a hack
            //Document::AttributeEntry.new('infile', @file).save_to_next_block @document
            //Document::AttributeEntry.new('indir', @dir).save_to_next_block @document
            lookAhead = 0;
        }
        return this;
    }

    public PreprocessorReader pushInclude(Object data, String file, String path) {
        return pushInclude(data, file, path, 1, Map.of());
    }

    public PreprocessorReader pushInclude(Object data, String file) {
        return pushInclude(data, file, null, 1, Map.of());
    }

    public PreprocessorReader pushInclude(Object data) {
        return pushInclude(data, null, null, 1, Map.of());
    }

    public int getIncludeDepth() {
        return includeStack.size();
    }

    /**
     * Reports whether pushing an include on the include stack exceeds the max include depth.
     *
     * @return null if no max depth is set and includes are disabled (max-include-depth=0), false if the current max
     *         depth will not be exceeded, and the relative max include depth if the current max depth will be exceed.
     */
    public Object exceedsMaxDepth() {
        if (maxdepth == null) {
            return null;
        }
        return includeStack.size() >= maxdepth.curr() ? (Object) maxdepth.rel() : Boolean.FALSE;
    }

    @Override
    public String shift() {
        if (unescapeNextLine) {
            unescapeNextLine = false;
            String line = super.shift();
            return line == null ? null : line.substring(1);
        }
        return super.shift();
    }

    public boolean hasIncludeProcessors() {
        if (!includeProcessorsResolved) {
            includeProcessorsResolved = true;
            if (document.hasExtensions()) {
                List<ProcessorExtension<IncludeProcessor>> exts = document.getExtensions().getIncludeProcessors();
                includeProcessorExtensions = exts == null || exts.isEmpty() ? null : exts;
            }
        }
        return includeProcessorExtensions != null;
    }

    public Cursor createIncludeCursor(String file, String path, int lineno) {
        String dir;
        if (Helpers.isUriish(file)) {
            URI uri = URI.create(file);
            dir = Helpers.dirname(uri.getPath() == null ? "" : uri.getPath());
            if (dir.isEmpty()) {
                dir = "/";
            }
        } else {
            dir = Helpers.dirname(file);
        }
        return new Cursor(file, dir, path, lineno);
    }

    @Override
    public String toString() {
        return "#<" + getClass().getName() + "@" + System.identityHashCode(this) + " {path: " + Strings.inspect(path)
                + ", line: " + lineno + ", include depth: " + includeStack.size() + ", include stack: ["
                + String.join(", ", includeStack.stream().map(IncludeContext::toString).toList()) + "]}>";
    }

    @Override
    protected void saveExtraState() {
        savedIncludeStackSize = includeStack.size();
    }

    @Override
    protected void restoreExtraState() {
        while (savedIncludeStackSize >= 0 && includeStack.size() > savedIncludeStackSize) {
            includeStack.remove(includeStack.size() - 1);
        }
        savedIncludeStackSize = -1;
    }

    @Override
    protected List<String> prepareLines(Object data, Object normalize, boolean include, Object indent,
            boolean skipFrontMatter) {
        List<String> result = super.prepareLines(data, normalize, include, indent, skipFrontMatter);

        if (skipFrontMatter) {
            List<String> frontMatter = skipFrontMatter(result, true);
            if (frontMatter != null && !include) {
                document.getAttributes().put("front-matter", String.join(LF, frontMatter));
            }
        }

        if (include) {
            if (indent != null) {
                Parser.adjustIndentation(result, Strings.toInt(indent),
                        Strings.toInt(document.getAttribute("tabsize")));
            }
        } else {
            while (!result.isEmpty() && result.get(result.size() - 1).isEmpty()) {
                result.remove(result.size() - 1);
            }
        }

        return result;
    }

    @Override
    protected String processLine(String line) {
        if (!processLines) {
            return line;
        }

        if (line.isEmpty()) {
            if (skipping) {
                shift();
                return null;
            }
            lookAhead++;
            return line;
        }

        // NOTE highly optimized
        if (line.endsWith("]") && !line.startsWith("[") && line.contains("::")) {
            Matcher m;
            if (line.contains("if") && (m = Rx.match(Rx.CONDITIONAL_DIRECTIVE_RX, line)) != null) {
                // if escaped, mark as processed and return line unescaped
                if ("\\".equals(m.group(1))) {
                    unescapeNextLine = true;
                    lookAhead++;
                    return line.substring(1);
                } else if (preprocessConditionalDirective(m.group(2), m.group(3), m.group(4), m.group(5))) {
                    // move the pointer past the conditional line
                    shift();
                    // treat next line as uncharted territory
                    return null;
                } else {
                    // the line was not a valid conditional line
                    // mark it as visited and return it
                    lookAhead++;
                    return line;
                }
            } else if (skipping) {
                shift();
                return null;
            } else if ((line.startsWith("inc") || line.startsWith("\\inc"))
                    && (m = Rx.match(Rx.INCLUDE_DIRECTIVE_RX, line)) != null) {
                // if escaped, mark as processed and return line unescaped
                if ("\\".equals(m.group(1))) {
                    unescapeNextLine = true;
                    lookAhead++;
                    return line.substring(1);
                // QUESTION should we strip whitespace from raw attributes in Substitutors#parse_attributes? (check perf)
                } else if (Boolean.TRUE.equals(preprocessIncludeDirective(m.group(2), m.group(3)))) {
                    // peek again since the content has changed
                    return null;
                } else {
                    // the line was not a valid include line and is unchanged
                    // mark it as visited and return it
                    lookAhead++;
                    return line;
                }
            } else {
                // NOTE optimization to inline super
                lookAhead++;
                return line;
            }
        } else if (skipping) {
            shift();
            return null;
        } else {
            // NOTE optimization to inline super
            lookAhead++;
            return line;
        }
    }

    /**
     * Internal: Preprocess the directive to conditionally include or exclude content.
     *
     * <p>Preprocess the conditional directive (ifdef, ifndef, ifeval, endif) under the cursor. If Reader is currently
     * skipping content, then simply track the open and close delimiters of any nested conditional blocks. If Reader is
     * not skipping, mark whether the condition is satisfied and continue preprocessing recursively until the next line
     * of available content is found.
     *
     * @param name The name of the conditional inclusion directive (ifdef, ifndef, ifeval, endif)
     * @param target The target, which is the name of one or more attributes that are used in the condition (blank in
     *            the case of the ifeval directive)
     * @param delimiter The conditional delimiter for multiple attributes ('+' means all attributes must be defined or
     *            undefined, ',' means any of the attributes can be defined or undefined.
     * @param text The text associated with this directive (occurring between the square brackets) Used for a
     *            single-line conditional block in the case of the ifdef or ifndef directives, and for the conditional
     *            expression for the ifeval directive.
     * @return a Boolean indicating whether the cursor should be advanced
     */
    private boolean preprocessConditionalDirective(String name, String target, String delimiter, String text) {
        // attributes are case insensitive
        boolean noTarget = target.isEmpty();
        if (!noTarget) {
            target = target.toLowerCase();
        }
        Map<String, Object> docAttrs = document.getAttributes();

        boolean skip;
        if (name.equals("endif")) {
            if (text != null) {
                getLogger().error(Logging.messageWithContext("malformed preprocessor directive - text not permitted: endif::"
                        + target + "[" + text + "]", cursor()));
            } else if (conditionalStack.isEmpty()) {
                getLogger().error(Logging.messageWithContext("unmatched preprocessor directive: endif::" + target + "[]",
                        cursor()));
            } else {
                if (noTarget || target.equals(conditionalStack.get(conditionalStack.size() - 1).target())) {
                    conditionalStack.remove(conditionalStack.size() - 1);
                    skipping = !conditionalStack.isEmpty() && conditionalStack.get(conditionalStack.size() - 1).skipping();
                } else {
                    getLogger().error(Logging.messageWithContext("mismatched preprocessor directive: endif::" + target
                            + "[], expected endif::" + Objects.toString(conditionalStack.get(conditionalStack.size() - 1).target(), "") + "[]",
                            cursor()));
                }
            }
            return true;
        } else if (skipping) {
            if (name.equals("ifeval")) {
                if (!(noTarget && text != null && Rx.match(Rx.EVAL_EXPRESSION_RX, Strings.strip(text)) != null)) {
                    return true;
                }
            } else if (noTarget) {
                return true;
            }
            skip = false;
        } else {
            // QUESTION any way to wrap ifdef & ifndef logic up together?
            switch (name) {
                case "ifdef" -> {
                    if (noTarget) {
                        getLogger().error(Logging.messageWithContext(
                                "malformed preprocessor directive - missing target: ifdef::[" + Objects.toString(text, "") + "]", cursor()));
                        return true;
                    }
                    if (",".equals(delimiter)) {
                        // skip if no attribute is defined
                        skip = Strings.split(target, ",", -1).stream().noneMatch(docAttrs::containsKey);
                    } else if ("+".equals(delimiter)) {
                        // skip if any attribute is undefined
                        skip = Strings.split(target, "+", -1).stream().anyMatch(attrName -> !docAttrs.containsKey(attrName));
                    } else {
                        // if the attribute is undefined, then skip
                        skip = !docAttrs.containsKey(target);
                    }
                }
                case "ifndef" -> {
                    if (noTarget) {
                        getLogger().error(Logging.messageWithContext(
                                "malformed preprocessor directive - missing target: ifndef::[" + Objects.toString(text, "") + "]", cursor()));
                        return true;
                    }
                    if (",".equals(delimiter)) {
                        // skip if any attribute is defined
                        skip = Strings.split(target, ",", -1).stream().anyMatch(docAttrs::containsKey);
                    } else if ("+".equals(delimiter)) {
                        // skip if all attributes are defined
                        skip = Strings.split(target, "+", -1).stream().allMatch(docAttrs::containsKey);
                    } else {
                        // if the attribute is defined, then skip
                        skip = docAttrs.containsKey(target);
                    }
                }
                default -> { // ifeval
                    if (noTarget) {
                        // the text in brackets must match a conditional expression
                        Matcher m;
                        if (text != null && (m = Rx.match(Rx.EVAL_EXPRESSION_RX, Strings.strip(text))) != null) {
                            String lhs = m.group(1);
                            // regex enforces a restricted set of math-related operations (==, !=, <=, >=, <, >)
                            String op = m.group(2);
                            String rhs = m.group(3);
                            try {
                                skip = !evaluate(resolveExprVal(lhs), op, resolveExprVal(rhs));
                            } catch (RuntimeException e) {
                                skip = true;
                            }
                        } else {
                            getLogger().error(Logging.messageWithContext("malformed preprocessor directive - "
                                    + (text != null ? "invalid expression" : "missing expression") + ": ifeval::["
                                    + (text == null ? "" : text) + "]", cursor()));
                            return true;
                        }
                    } else {
                        getLogger().error(Logging.messageWithContext(
                                "malformed preprocessor directive - target not permitted: ifeval::" + target + "["
                                        + (text == null ? "" : text) + "]",
                                cursor()));
                        return true;
                    }
                }
            }
        }

        // conditional inclusion block
        if (name.equals("ifeval")) {
            if (skip) {
                skipping = true;
            }
            conditionalStack.add(new ConditionalContext(name, null, text, skip, skipping, sourcemap ? cursor() : null));
        // single line conditional inclusion
        } else if (text != null) {
            if (!(skipping || skip)) {
                replaceNextLine(Strings.rstrip(text));
                // HACK push dummy line to stand in for the opening conditional directive that's subsequently dropped
                unshift("");
                // NOTE force line to be processed again if it looks like an include directive
                // QUESTION should we just call preprocess_include_directive here?
                if (text.startsWith("include::")) {
                    lookAhead--;
                }
            }
        // conditional inclusion block
        } else {
            if (skip) {
                skipping = true;
            }
            conditionalStack.add(new ConditionalContext(name, target, null, skip, skipping, sourcemap ? cursor() : null));
        }

        return true;
    }

    private static String includeLinkAttrlist(Document doc, String attrlist) {
        return doc.hasAttribute("compat-mode") ? Objects.toString(attrlist, "")
                : "role=include" + (attrlist != null ? "," + attrlist : "");
    }

    /**
     * Evaluates the comparison expression using the semantics of Ruby.
     */
    private static boolean evaluate(Object lhs, String op, Object rhs) {
        return switch (op) {
            case "==" -> valueEquals(lhs, rhs);
            case "!=" -> !valueEquals(lhs, rhs);
            default -> {
                int cmp = compareValues(lhs, rhs);
                yield switch (op) {
                    case "<" -> cmp < 0;
                    case ">" -> cmp > 0;
                    case "<=" -> cmp <= 0;
                    default -> cmp >= 0; // >=
                };
            }
        };
    }

    private static boolean valueEquals(Object lhs, Object rhs) {
        if (lhs instanceof Number l && rhs instanceof Number r) {
            return l.doubleValue() == r.doubleValue();
        }
        return Objects.equals(lhs, rhs);
    }

    private static int compareValues(Object lhs, Object rhs) {
        if (lhs instanceof Number l && rhs instanceof Number r) {
            return Double.compare(l.doubleValue(), r.doubleValue());
        } else if (lhs instanceof String l && rhs instanceof String r) {
            return l.compareTo(r);
        }
        throw new IllegalArgumentException("comparison of " + lhs + " with " + rhs + " failed");
    }

    /**
     * Internal: Preprocess the directive to include lines from another document.
     *
     * <p>Preprocess the directive to include the target document. The scenarios are as follows:
     *
     * <p>If SafeMode is SECURE or greater, the directive is ignore and the include directive line is emitted verbatim.
     *
     * <p>Otherwise, if an include processor is specified pass the target and attributes to that processor and expect a
     * List of String lines in return.
     *
     * <p>Otherwise, if the max depth is greater than 0, and is not exceeded by the stack size, normalize the target
     * path and read the lines onto the beginning of the List of source data.
     *
     * <p>If none of the above apply, emit the include directive line verbatim.
     *
     * @param target The unsubstituted String name of the target document to include as specified in the target slot
     *            of the include directive.
     * @param attrlist An attribute list String, which is the text between the square brackets of the include
     *            directive.
     * @return a Boolean indicating whether the line under the cursor was changed. To skip over the directive, call
     *         shift and return true.
     */
    private Boolean preprocessIncludeDirective(String target, String attrlist) {
        Document doc = document;
        String expandedTarget = target;
        String attrMissing = null;
        if (target.contains(ATTR_REF_HEAD)) {
            attrMissing = Strings.valueOf(doc.getAttributes().get("attribute-missing"));
            if (attrMissing == null) {
                attrMissing = Compliance.getAttributeMissing();
            }
            expandedTarget = doc.subAttributes(target, "warn".equals(attrMissing) ? "drop-line" : attrMissing, null);
        }
        String attrlistText = attrlist == null ? "" : attrlist;
        if (target.contains(ATTR_REF_HEAD) && expandedTarget.isEmpty()) {
            if ("drop-line".equals(attrMissing)
                    && doc.subAttributes(target + " ", "drop-line", "ignore").isEmpty()) {
                getLogger().info(() -> Logging.messageWithContext("include dropped due to missing attribute: include::"
                        + target + "[" + attrlistText + "]", cursor()));
                shift();
                return true;
            } else if (doc.parseAttributes(attrlist, List.of(), EnumSet.of(ParseOption.SUB_INPUT), null)
                    .get("optional-option") != null) {
                String reason = "warn".equals(attrMissing)
                        && doc.subAttributes(target + " ", "drop-line", "ignore").isEmpty() ? "due to missing attribute"
                                : "because resolved target is blank";
                getLogger().info(() -> Logging.messageWithContext("optional include dropped " + reason + ": include::"
                        + target + "[" + attrlistText + "]", cursor()));
                shift();
                return true;
            } else {
                String reason = "warn".equals(attrMissing)
                        && doc.subAttributes(target + " ", "drop-line", "ignore").isEmpty() ? "due to missing attribute"
                                : "because resolved target is blank";
                getLogger().warn(Logging.messageWithContext("include dropped " + reason + ": include::" + target + "["
                        + attrlistText + "]", cursor()));
                // QUESTION should this line include target or expanded_target (or escaped target?)
                return replaceNextLine("Unresolved directive in " + path + " - include::" + target + "["
                        + attrlistText + "]");
            }
        }
        ProcessorExtension<IncludeProcessor> ext;
        if (hasIncludeProcessors() && (ext = findIncludeProcessor(expandedTarget)) != null) {
            shift();
            // FIXME parse attributes only if requested by extension
            ext.getInstance().process(doc, this, expandedTarget,
                    doc.parseAttributes(attrlist, List.of(), EnumSet.of(ParseOption.SUB_INPUT), null));
            return true;
        // if running in SafeMode::SECURE or greater, don't process this directive
        // however, be friendly and at least make it a link to the source document
        } else if (doc.getSafe() >= SafeMode.SECURE.getLevel()) {
            // FIXME we don't want to use a passthrough or link macro if we're in a verbatim context
            if (expandedTarget.contains(" ")) {
                expandedTarget = "pass:c[" + expandedTarget + "]";
            }
            return replaceNextLine("link:" + expandedTarget + "[" + includeLinkAttrlist(doc, attrlist) + "]");
        } else if (maxdepth != null) {
            if (includeStack.size() >= maxdepth.curr()) {
                getLogger().error(Logging.messageWithContext("maximum include depth of " + maxdepth.rel() + " exceeded",
                        cursor()));
                return null;
            }

            Map<String, Object> parsedAttrs = doc.parseAttributes(attrlist, List.of(),
                    EnumSet.of(ParseOption.SUB_INPUT), null);
            IncludeTarget resolved = resolveIncludePath(expandedTarget, attrlist, parsedAttrs);
            if (resolved.halt() != null) {
                // NOTE if target_type is not set, inc_path is a boolean to skip over (false) or reevaluate (true) the
                // current line
                return resolved.halt();
            }
            String incPath = resolved.path();
            String targetType = resolved.type();
            String relpath = resolved.relpath();

            // NOTE a null charset means the content is expected to be UTF-8 (or to begin with a BOM)
            Charset charset = null;
            Object enc = parsedAttrs.get("encoding");
            if (enc != null) {
                try {
                    charset = Charset.forName(enc.toString());
                } catch (RuntimeException e) {
                    // ignore unknown encoding
                }
            }

            List<Long> incLinenos = null;
            Map<String, Boolean> incTags = null;
            // NOTE attrlist is null if missing from include directive
            if (attrlist != null) {
                if (parsedAttrs.containsKey("lines")) {
                    List<Long> linenos = new ArrayList<>();
                    for (String linedef : splitDelimitedValue(String.valueOf(parsedAttrs.get("lines")))) {
                        if (linedef.contains("..")) {
                            String[] parts = Strings.partition(linedef, "..");
                            long from = Strings.toLong(parts[0]);
                            long to;
                            if (parts[2].isEmpty() || (to = Strings.toLong(parts[2])) < 0) {
                                linenos.add(from);
                                linenos.add(Long.MAX_VALUE);
                            } else {
                                for (long i = from; i <= to; i++) {
                                    linenos.add(i);
                                }
                            }
                        } else {
                            linenos.add(Strings.toLong(linedef));
                        }
                    }
                    incLinenos = linenos.isEmpty() ? null
                            : new ArrayList<>(new java.util.TreeSet<>(linenos));
                } else if (parsedAttrs.containsKey("tag")) {
                    String tag = String.valueOf(parsedAttrs.get("tag"));
                    if (!(tag.isEmpty() || tag.equals("!"))) {
                        incTags = new LinkedHashMap<>();
                        if (tag.startsWith("!")) {
                            incTags.put(tag.substring(1), false);
                        } else {
                            incTags.put(tag, true);
                        }
                    }
                } else if (parsedAttrs.containsKey("tags")) {
                    incTags = new LinkedHashMap<>();
                    for (String tagdef : splitDelimitedValue(String.valueOf(parsedAttrs.get("tags")))) {
                        if (!(tagdef.isEmpty() || tagdef.equals("!"))) {
                            if (tagdef.startsWith("!")) {
                                incTags.put(tagdef.substring(1), false);
                            } else {
                                incTags.put(tagdef, true);
                            }
                        }
                    }
                    if (incTags.isEmpty()) {
                        incTags = null;
                    }
                }
            }

            if (incLinenos != null) {
                List<String> incLines = new ArrayList<>();
                Integer incOffset = null;
                int incLineno = 0;
                try {
                    boolean selectRemaining = false;
                    for (String l : readIncludeLines(incPath, targetType, charset)) {
                        incLineno++;
                        Long select = incLinenos.get(0);
                        if (selectRemaining || (selectRemaining = select == Long.MAX_VALUE)) {
                            // NOTE record line where we started selecting
                            if (incOffset == null) {
                                incOffset = incLineno;
                            }
                            incLines.add(l);
                        } else {
                            if (select == incLineno) {
                                // NOTE record line where we started selecting
                                if (incOffset == null) {
                                    incOffset = incLineno;
                                }
                                incLines.add(l);
                                incLinenos.remove(0);
                            }
                            if (incLinenos.isEmpty()) {
                                break;
                            }
                        }
                    }
                } catch (InvalidByteSequenceException e) {
                    throw e;
                } catch (IOException | RuntimeException e) {
                    getLogger().error(Logging.messageWithContext("include " + targetType + " not readable: " + incPath,
                            cursor()));
                    return replaceNextLine("Unresolved directive in " + path + " - include::" + expandedTarget + "["
                            + attrlistText + "]");
                }
                shift();
                // FIXME not accounting for skipped lines in reader line numbering
                if (incOffset != null) {
                    parsedAttrs.put("partial-option", "");
                    pushInclude(incLines, incPath, relpath, incOffset, parsedAttrs);
                }
            } else if (incTags != null) {
                List<String> incLines = new ArrayList<>();
                Integer incOffset = null;
                int incLineno = 0;
                List<TagContext> tagStack = new ArrayList<>();
                Set<String> tagsSelected = new LinkedHashSet<>();
                String activeTag = null;
                Boolean select;
                Boolean baseSelect;
                Boolean wildcard = null;
                if (incTags.containsKey("**")) {
                    select = baseSelect = incTags.remove("**");
                    if (incTags.containsKey("*")) {
                        wildcard = incTags.remove("*");
                    } else if (!select && !incTags.isEmpty()
                            && Boolean.FALSE.equals(incTags.values().iterator().next())) {
                        wildcard = true;
                    }
                } else if (incTags.containsKey("*")) {
                    if ("*".equals(incTags.keySet().iterator().next())) {
                        wildcard = incTags.remove("*");
                        select = baseSelect = !wildcard;
                    } else {
                        select = baseSelect = false;
                        wildcard = incTags.remove("*");
                    }
                } else {
                    select = baseSelect = !incTags.containsValue(true);
                }
                try {
                    for (String l : readIncludeLines(incPath, targetType, charset)) {
                        incLineno++;
                        Matcher m;
                        if (l.contains("::") && l.contains("[]") && (m = Rx.match(Rx.TAG_DIRECTIVE_RX, l)) != null) {
                            String thisTag = m.group(2);
                            if (m.group(1) != null) { // end tag
                                if (thisTag.equals(activeTag)) {
                                    tagStack.remove(tagStack.size() - 1);
                                    if (tagStack.isEmpty()) {
                                        activeTag = null;
                                        select = baseSelect;
                                    } else {
                                        activeTag = tagStack.get(tagStack.size() - 1).name();
                                        select = tagStack.get(tagStack.size() - 1).select();
                                    }
                                } else if (incTags.containsKey(thisTag)) {
                                    Cursor includeCursor = createIncludeCursor(incPath, expandedTarget, incLineno);
                                    int idx = -1;
                                    for (int i = tagStack.size() - 1; i >= 0; i--) {
                                        if (tagStack.get(i).name().equals(thisTag)) {
                                            idx = i;
                                            break;
                                        }
                                    }
                                    if (idx >= 0) {
                                        tagStack.remove(idx);
                                        getLogger().warn(Logging.messageWithContext("mismatched end tag (expected '"
                                                + activeTag + "' but found '" + thisTag + "') at line " + incLineno
                                                + " of include " + targetType + ": " + incPath, cursor(), includeCursor));
                                    } else {
                                        getLogger().warn(Logging.messageWithContext("unexpected end tag '" + thisTag
                                                + "' at line " + incLineno + " of include " + targetType + ": " + incPath,
                                                cursor(), includeCursor));
                                    }
                                }
                            } else if (incTags.containsKey(thisTag)) {
                                if ((select = incTags.get(thisTag))) {
                                    tagsSelected.add(thisTag);
                                }
                                // QUESTION should we prevent tag from being selected when enclosing tag is excluded?
                                tagStack.add(new TagContext(activeTag = thisTag, select, incLineno));
                            } else if (wildcard != null) {
                                select = activeTag != null && !select ? false : wildcard;
                                tagStack.add(new TagContext(activeTag = thisTag, select, incLineno));
                            }
                        } else if (select) {
                            // NOTE record the line where we started selecting
                            if (incOffset == null) {
                                incOffset = incLineno;
                            }
                            incLines.add(l);
                        }
                    }
                } catch (InvalidByteSequenceException e) {
                    throw e;
                } catch (IOException | RuntimeException e) {
                    getLogger().error(Logging.messageWithContext("include " + targetType + " not readable: " + incPath,
                            cursor()));
                    return replaceNextLine("Unresolved directive in " + path + " - include::" + expandedTarget + "["
                            + attrlistText + "]");
                }
                for (TagContext tag : tagStack) {
                    getLogger().warn(Logging.messageWithContext("detected unclosed tag '" + tag.name()
                            + "' starting at line " + tag.lineno() + " of include " + targetType + ": " + incPath,
                            cursor(), createIncludeCursor(incPath, expandedTarget, tag.lineno())));
                }
                incTags.values().removeIf(v -> !v);
                List<String> missingTags = new ArrayList<>(incTags.keySet());
                missingTags.removeAll(tagsSelected);
                if (!missingTags.isEmpty()) {
                    getLogger().warn(Logging.messageWithContext("tag" + (missingTags.size() > 1 ? "s" : "") + " '"
                            + String.join(", ", missingTags) + "' not found in include " + targetType + ": " + incPath,
                            cursor()));
                }
                shift();
                if (incOffset != null) {
                    if (!(baseSelect && !Boolean.FALSE.equals(wildcard) && incTags.isEmpty())) {
                        parsedAttrs.put("partial-option", "");
                    }
                    // FIXME not accounting for skipped lines in reader line numbering
                    pushInclude(incLines, incPath, relpath, incOffset, parsedAttrs);
                }
            } else {
                String incContent;
                try {
                    // NOTE read content before shift so cursor is only advanced if IO operation succeeds
                    incContent = readIncludeContent(incPath, targetType, charset);
                    shift();
                } catch (InvalidByteSequenceException e) {
                    throw e;
                } catch (IOException | RuntimeException e) {
                    getLogger().error(Logging.messageWithContext("include " + targetType + " not readable: " + incPath,
                            cursor()));
                    return replaceNextLine("Unresolved directive in " + path + " - include::" + expandedTarget + "["
                            + attrlistText + "]");
                }
                pushInclude(incContent, incPath, relpath, 1, parsedAttrs);
            }
            return true;
        }
        return null;
    }

    private ProcessorExtension<IncludeProcessor> findIncludeProcessor(String target) {
        for (ProcessorExtension<IncludeProcessor> candidate : includeProcessorExtensions) {
            if (candidate.getInstance().handles(document, target)) {
                return candidate;
            }
        }
        return null;
    }

    private static byte[] readIncludeBytes(String incPath, String targetType) throws IOException {
        if ("uri".equals(targetType)) {
            try (InputStream in = URI.create(incPath).toURL().openStream()) {
                return in.readAllBytes();
            }
        }
        return Files.readAllBytes(Path.of(incPath));
    }

    private static String readIncludeContent(String incPath, String targetType, Charset charset) throws IOException {
        byte[] bytes = readIncludeBytes(incPath, targetType);
        String content = Helpers.decode(bytes, charset);
        if (charset == null && content.indexOf('\uFFFD') >= 0) {
            try {
                Helpers.decodeStrict(bytes);
            } catch (CharacterCodingException e) {
                throw new InvalidByteSequenceException(incPath, e);
            }
        }
        return content;
    }

    /**
     * Thrown when the content of an include file is not valid UTF-8 and no encoding is specified.
     */
    static final class InvalidByteSequenceException extends AsciidoctorException {
        InvalidByteSequenceException(String path, Throwable cause) {
            super("invalid byte sequence in UTF-8: " + path, cause);
        }
    }

    private static List<String> readIncludeLines(String incPath, String targetType, Charset charset)
            throws IOException {
        return Helpers.eachLine(readIncludeContent(incPath, targetType, charset));
    }

    /**
     * The resolved target of an include directive. Either the halt value is set (to indicate whether the cursor should
     * be advanced beyond the current line (true) or the line should be reprocessed (false)) or the path, type and
     * relative path are set.
     */
    public record IncludeTarget(String path, String type, String relpath, Boolean halt) {
        static IncludeTarget halt(boolean value) {
            return new IncludeTarget(null, null, null, value);
        }
    }

    /**
     * Internal: Resolve the target of an include directive.
     *
     * <p>An internal method to resolve the target of an include directive. This method must return an IncludeTarget
     * containing the resolved (absolute) path of the target, the target type (file or uri), and the path of the target
     * relative to the outermost document. Alternately, the method may return a halt value to halt processing of the
     * include directive line and to indicate whether the cursor should be advanced beyond this line (true) or the line
     * should be reprocessed (false).
     *
     * @param target A String containing the unresolved include target. (Attribute references in target value have
     *            already been resolved).
     * @param attrlist An attribute list String (i.e., the text between the square brackets).
     * @param attributes A Map of attributes parsed from attrlist.
     * @return the resolved include target
     */
    protected IncludeTarget resolveIncludePath(String target, String attrlist, Map<String, Object> attributes) {
        Document doc = document;
        boolean uri = Helpers.isUriish(target);
        if (!uri && Helpers.isUriish(dir)) {
            target = dir + "/" + target;
            uri = true;
        }
        if (uri) {
            if (!doc.hasAttribute("allow-uri-read")) {
                getLogger().warn(Logging.messageWithContext("cannot include contents of URI: " + target
                        + " (allow-uri-read attribute not enabled)", cursor()));
                // FIXME we don't want to use a passthrough or link macro if we're in a verbatim context
                if (target.contains(" ")) {
                    target = "pass:c[" + target + "]";
                }
                return IncludeTarget.halt(replaceNextLine("link:" + target + "[" + includeLinkAttrlist(doc, attrlist)
                        + "]"));
            }
            return new IncludeTarget(target, "uri", target, null);
        }
        // include file is resolved relative to dir of current include, or base_dir if within original docfile
        String incPath = doc.normalizeSystemPath(target, dir, null, Map.of("target_name", "include file"));
        if (!Helpers.isFile(incPath)) {
            if (attributes.get("optional-option") != null) {
                getLogger().info(() -> Logging.messageWithContext(
                        "optional include dropped because include file not found: " + incPath, cursor()));
                shift();
                return IncludeTarget.halt(true);
            }
            getLogger().error(Logging.messageWithContext("include file not found: " + incPath, cursor()));
            return IncludeTarget.halt(replaceNextLine("Unresolved directive in " + path + " - include::" + target + "["
                    + (attrlist == null ? "" : attrlist) + "]"));
        }
        // NOTE relpath is the path relative to the root document (or base_dir, if set)
        // QUESTION should we move relative_path method to Document
        String relpath = doc.getPathResolver().relativePath(incPath, doc.getBaseDir());
        return new IncludeTarget(incPath, "file", relpath, null);
    }

    private void popInclude() {
        if (includeStack.isEmpty()) {
            return;
        }
        IncludeContext context = includeStack.remove(includeStack.size() - 1);
        lines = context.lines();
        file = context.file();
        dir = context.dir();
        path = context.path();
        lineno = context.lineno();
        maxdepth = context.maxdepth();
        processLines = context.processLines();
        // FIXME kind of a hack
        //Document::AttributeEntry.new('infile', @file).save_to_next_block @document
        //Document::AttributeEntry.new('indir', ::File.dirname(@file)).save_to_next_block @document
        lookAhead = 0;
    }

    /**
     * Private: Split delimited value on comma (if found), otherwise semi-colon.
     */
    private static List<String> splitDelimitedValue(String val) {
        return val.contains(",") ? Strings.split(val, ",") : Strings.split(val, ";");
    }

    /**
     * Private: Ignore front-matter, commonly used in static site generators.
     */
    private List<String> skipFrontMatter(List<String> data, boolean incrementLinenos) {
        if (data.isEmpty()) {
            return null;
        }
        String delim = data.get(0);
        if (!(delim.equals("---") || delim.equals("+++"))) {
            return null;
        }
        List<String> originalData = new ArrayList<>(data);
        data.remove(0);
        List<String> frontMatter = new ArrayList<>();
        if (incrementLinenos) {
            lineno++;
        }
        boolean eof;
        while (!(eof = data.isEmpty()) && !data.get(0).equals(delim)) {
            frontMatter.add(data.remove(0));
            if (incrementLinenos) {
                lineno++;
            }
        }
        if (eof) {
            data.clear();
            data.addAll(originalData);
            if (incrementLinenos) {
                lineno -= originalData.size();
            }
            return null;
        }
        data.remove(0);
        if (incrementLinenos) {
            lineno++;
        }
        return frontMatter;
    }

    /**
     * Private: Resolve the value of one side of the expression.
     *
     * <pre>
     *   resolveExprVal("\"value\"")  // => "value\""
     *   resolveExprVal("{undefined}") // => null
     *   resolveExprVal("2")           // => 2
     * </pre>
     *
     * @return The value of the expression, coerced to the appropriate type
     */
    private Object resolveExprVal(String val) {
        boolean quoted;
        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
            quoted = true;
            val = val.substring(1);
        } else {
            quoted = false;
        }

        // QUESTION should we substitute first?
        // QUESTION should we also require string to be single quoted (like block attribute values?)
        if (val.contains(ATTR_REF_HEAD)) {
            val = document.subAttributes(val, "drop", null);
        }

        if (quoted) {
            return val;
        } else if (val.isEmpty()) {
            return null;
        } else if (val.equals("true")) {
            return true;
        } else if (val.equals("false")) {
            return false;
        } else if (Strings.rstrip(val).isEmpty()) {
            return " ";
        } else if (val.contains(".")) {
            return Strings.toDouble(val);
        } else {
            // fallback to coercing to integer, since we
            // require string values to be explicitly quoted
            return Strings.toLong(val);
        }
    }
}
