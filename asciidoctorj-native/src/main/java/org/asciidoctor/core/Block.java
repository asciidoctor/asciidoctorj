package org.asciidoctor.core;

import static org.asciidoctor.core.Asciidoctor.LF;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Methods for managing AsciiDoc content blocks.
 *
 * <pre>
 *   block = new Block(parent, "paragraph", Options.of("source", "_This_ is a &lt;test&gt;"));
 *   block.getContent();
 *   // => "&lt;em&gt;This&lt;/em&gt; is a &amp;lt;test&amp;gt;"
 * </pre>
 */
public class Block extends AbstractBlock {
    private static final Map<String, ContentModel> DEFAULT_CONTENT_MODEL = Map.of(
            // TODO should probably fill in all known blocks
            "audio", ContentModel.EMPTY,
            "image", ContentModel.EMPTY,
            "listing", ContentModel.VERBATIM,
            "literal", ContentModel.VERBATIM,
            "stem", ContentModel.RAW,
            "open", ContentModel.COMPOUND,
            "page_break", ContentModel.EMPTY,
            "pass", ContentModel.RAW,
            "thematic_break", ContentModel.EMPTY,
            "video", ContentModel.EMPTY);

    protected List<String> lines;

    /**
     * Initialize a Block object.
     *
     * <p>IMPORTANT: If you don't specify the subs option, you must explicitly call the {@link #commitSubs()} method to
     * resolve and assign the substitutions to this block (which are resolved from the subs attribute, if specified, or
     * the default substitutions based on this block's context). If you want to use the default subs for a block, pass
     * the option subs: "default". You can override the default subs using the default_subs option.
     *
     * @param parent The parent AbstractBlock with a compound content model to which this Block will be appended.
     * @param context The String context name for the type of content (e.g., paragraph).
     * @param opts a Map of options to customize block initialization: (default: {})
     *            <ul>
     *            <li>content_model indicates whether blocks can be nested in this Block (compound), otherwise how the
     *            lines should be processed (simple, verbatim, raw, empty). (default: simple)</li>
     *            <li>attributes a Map of attributes (key/value pairs) to assign to this Block. (default: {})</li>
     *            <li>source a String or List of raw source for this Block. (default: null)</li>
     *            <li>subs the substitutions ("default", a List of Substitutions, a String or null)</li>
     *            <li>default_subs the default substitutions (a List of Substitutions)</li>
     *            </ul>
     */
    @SuppressWarnings("unchecked")
    public Block(AbstractNode parent, String context, Map<String, Object> opts) {
        super(parent, context, opts == null ? null : (Map<String, Object>) opts.get("attributes"));
        if (opts == null) {
            opts = Map.of();
        }
        ContentModel model = ContentModel.of(opts.get("content_model"));
        contentModel = model != null ? model : DEFAULT_CONTENT_MODEL.getOrDefault(context, ContentModel.SIMPLE);
        if (opts.containsKey("subs")) {
            // FIXME feels funky; we have to be defensive to get commit_subs to honor override
            // FIXME does not resolve substitution groups inside List (e.g., [normal])
            Object subsOpt = opts.get("subs");
            if (subsOpt != null) {
                // e.g., subs: "default"
                // subs attribute is honored; falls back to opts[:default_subs], then built-in defaults based on context
                if ("default".equals(subsOpt.toString())) {
                    defaultSubs = (List<Substitution>) opts.get("default_subs");
                // e.g., subs: [quotes]
                // subs attribute is not honored
                } else if (subsOpt instanceof List<?> list) {
                    // NOTE the subs may be specified as Substitution constants or by name
                    defaultSubs = new ArrayList<>(list.stream()
                            .map(sub -> sub instanceof Substitution substitution ? substitution
                                    : Substitution.forKey(sub.toString()))
                            .toList());
                    attributes.remove("subs");
                // e.g., subs: "normal"
                // subs attribute is not honored
                } else {
                    defaultSubs = null;
                    attributes.put("subs", subsOpt.toString());
                }
                // resolve the subs eagerly only if subs option is specified
                // QUESTION should we skip subsequent calls to commit_subs?
                commitSubs();
            // e.g., subs: null
            } else {
                // NOTE subs is initialized as empty list by super constructor
                // prevent subs from being resolved
                defaultSubs = new ArrayList<>();
                attributes.remove("subs");
            }
        // defer subs resolution; subs attribute is honored
        } else {
            // NOTE subs is initialized as empty list by super constructor
            // QUESTION should we honor :default_subs option (i.e., @default_subs = opts[:default_subs])?
            defaultSubs = null;
        }
        Object rawSource = opts.get("source");
        if (Strings.isNullOrEmpty(rawSource)) {
            lines = new ArrayList<>();
        } else if (rawSource instanceof String str) {
            lines = Helpers.prepareSourceString(str);
        } else {
            lines = new ArrayList<>((List<String>) rawSource);
        }
    }

    public Block(AbstractNode parent, String context) {
        this(parent, context, null);
    }

    /**
     * Create alias for context to be consistent w/ AsciiDoc.
     */
    public String getBlockname() {
        return context;
    }

    /**
     * @return the original List content for this block, if applicable
     */
    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    /**
     * Get the converted result of the child blocks by converting the children appropriate to content model that this
     * block supports.
     *
     * <pre>
     *   doc = new Document();
     *   block = new Block(doc, "paragraph",
     *       Options.of("source", "_This_ is what happens when you &lt;meet&gt; a stranger in the &lt;alps&gt;!"));
     *   block.getContent();
     *   // => "&lt;em&gt;This&lt;/em&gt; is what happens when you &amp;lt;meet&amp;gt; a stranger in the ..."
     * </pre>
     */
    @Override
    public String getContent() {
        switch (contentModel) {
            case COMPOUND -> {
                return super.getContent();
            }
            case SIMPLE -> {
                return applySubs(String.join(LF, lines), subs);
            }
            case VERBATIM, RAW -> {
                // QUESTION could we use strip here instead of popping empty lines?
                // maybe apply_subs can know how to strip whitespace?
                List<String> result = new ArrayList<>(applySubs(lines, subs));
                if (result.size() < 2) {
                    return result.isEmpty() ? "" : result.get(0);
                }
                while (!result.isEmpty() && Strings.rstrip(result.get(0)).isEmpty()) {
                    result.remove(0);
                }
                while (!result.isEmpty() && Strings.rstrip(result.get(result.size() - 1)).isEmpty()) {
                    result.remove(result.size() - 1);
                }
                return String.join(LF, result);
            }
            default -> {
                if (contentModel != ContentModel.EMPTY) {
                    getLogger().warn("unknown content model '" + contentModel + "' for block: " + this);
                }
                return null;
            }
        }
    }

    /**
     * Returns the preprocessed source of this block.
     *
     * @return the a String containing the lines joined together or empty string if there are no lines
     */
    public String getSource() {
        return String.join(LF, lines);
    }

    @Override
    public String toString() {
        String contentSummary = contentModel == ContentModel.COMPOUND ? "blocks: " + blocks.size()
                : "lines: " + lines.size();
        return "#<" + getClass().getName() + "@" + System.identityHashCode(this) + " {context: " + context
                + ", content_model: " + contentModel + ", style: " + Strings.inspect(style) + ", " + contentSummary
                + "}>";
    }
}
