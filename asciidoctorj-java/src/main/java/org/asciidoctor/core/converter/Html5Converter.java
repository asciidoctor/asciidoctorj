package org.asciidoctor.core.converter;

import static org.asciidoctor.core.Asciidoctor.BLOCK_MATH_DELIMITERS;
import static org.asciidoctor.core.Asciidoctor.DEFAULT_STYLESHEET_KEYS;
import static org.asciidoctor.core.Asciidoctor.DEFAULT_STYLESHEET_NAME;
import static org.asciidoctor.core.Asciidoctor.FONT_AWESOME_VERSION;
import static org.asciidoctor.core.Asciidoctor.INLINE_MATH_DELIMITERS;
import static org.asciidoctor.core.Asciidoctor.LF;
import static org.asciidoctor.core.Asciidoctor.MATHJAX_VERSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.AbstractNode;
import org.asciidoctor.core.Asciidoctor;
import org.asciidoctor.core.DescriptionListEntry;
import org.asciidoctor.core.Document;
import org.asciidoctor.core.Document.Author;
import org.asciidoctor.core.Document.Footnote;
import org.asciidoctor.core.Helpers;
import org.asciidoctor.core.Inline;
import org.asciidoctor.core.ListItem;
import org.asciidoctor.core.Rx;
import org.asciidoctor.core.SafeMode;
import org.asciidoctor.core.Section;
import org.asciidoctor.core.Strings;
import org.asciidoctor.core.Stylesheets;
import org.asciidoctor.core.Table;
import org.asciidoctor.core.syntaxhighlighter.SyntaxHighlighter;

/**
 * A built-in {@link Converter} implementation that generates HTML 5 output consistent with the html5 backend from
 * AsciiDoc.py.
 */
public class Html5Converter extends AbstractConverter<String> {

    /** The open tag, close tag and whether the tag is an element (versus a quote character). */
    private record QuoteTag(String open, String close, boolean tag) {
    }

    private static final Map<String, QuoteTag> QUOTE_TAGS = Map.of(
            "monospaced", new QuoteTag("<code>", "</code>", true),
            "emphasis", new QuoteTag("<em>", "</em>", true),
            "strong", new QuoteTag("<strong>", "</strong>", true),
            "double", new QuoteTag("&#8220;", "&#8221;", false),
            "single", new QuoteTag("&#8216;", "&#8217;", false),
            "mark", new QuoteTag("<mark>", "</mark>", true),
            "superscript", new QuoteTag("<sup>", "</sup>", true),
            "subscript", new QuoteTag("<sub>", "</sub>", true),
            "asciimath", new QuoteTag("\\$", "\\$", false),
            "latexmath", new QuoteTag("\\(", "\\)", false));

    private static final QuoteTag DEFAULT_QUOTE_TAG = new QuoteTag("", "", false);

    public static final Pattern DROP_ANCHOR_RX = Rx.compile("<(?:a\\b[^>]*|/a)>");
    public static final Pattern LEADING_ANCHORS_RX = Rx.compile("^(?:<a id=\"[^\"]+\"></a>)+");
    private static final Pattern STEM_BREAK_RX = Rx.compile(" *\\\\\\n(?:\\\\?\\n)*|\\n\\n+");
    private static final Pattern SVG_PREAMBLE_RX = Rx.compileMultiline("\\A.*?(?=<svg[\\s>])");
    private static final Pattern SVG_START_TAG_RX = Rx.compile("\\A<svg(?:\\s[^>]*)?>");
    private static final Pattern DIMENSION_ATTRIBUTE_RX = Rx.compile("\\s(?:width|height|style)=([\"'])"
            + Asciidoctor.CC_ANY + "*?\\1");

    private final boolean xmlMode;
    private final String voidElementSlash;
    private Map<String, AbstractNode> refs;
    private boolean resolvingXref;

    public Html5Converter(String backend, Map<String, Object> opts) {
        super(backend, opts);
        String syntax;
        if (opts != null && "xml".equals(opts.get("htmlsyntax"))) {
            syntax = "xml";
            xmlMode = true;
            voidElementSlash = "/";
        } else {
            syntax = "html";
            xmlMode = false;
            voidElementSlash = "";
        }
        initBackendTraits("html", "html", syntax, ".html", true);
        registerHandlers();
    }

    public Html5Converter(String backend) {
        this(backend, Map.of());
    }

    private void registerHandlers() {
        handle("inline_quoted", Inline.class, this::convertInlineQuoted);
        handle("paragraph", AbstractBlock.class, this::convertParagraph);
        handle("inline_anchor", Inline.class, this::convertInlineAnchor);
        handle("section", Section.class, this::convertSection);
        handle("listing", AbstractBlock.class, this::convertListing);
        handle("literal", AbstractBlock.class, this::convertLiteral);
        handle("ulist", org.asciidoctor.core.List.class, this::convertUlist);
        handle("olist", org.asciidoctor.core.List.class, this::convertOlist);
        handle("dlist", org.asciidoctor.core.List.class, this::convertDlist);
        handle("admonition", AbstractBlock.class, this::convertAdmonition);
        handle("colist", org.asciidoctor.core.List.class, this::convertColist);
        handle("embedded", Document.class, this::convertEmbedded);
        handle("example", AbstractBlock.class, this::convertExample);
        handle("floating_title", AbstractBlock.class, this::convertFloatingTitle);
        handle("image", AbstractBlock.class, this::convertImage);
        handle("inline_break", Inline.class, this::convertInlineBreak);
        handle("inline_button", Inline.class, this::convertInlineButton);
        handle("inline_callout", Inline.class, this::convertInlineCallout);
        handle("inline_footnote", Inline.class, this::convertInlineFootnote);
        handle("inline_image", Inline.class, this::convertInlineImage);
        handle("inline_indexterm", Inline.class, this::convertInlineIndexterm);
        handle("inline_kbd", Inline.class, this::convertInlineKbd);
        handle("inline_menu", Inline.class, this::convertInlineMenu);
        handle("open", AbstractBlock.class, this::convertOpen);
        handle("page_break", AbstractBlock.class, this::convertPageBreak);
        handle("preamble", AbstractBlock.class, this::convertPreamble);
        handle("quote", AbstractBlock.class, this::convertQuote);
        handle("sidebar", AbstractBlock.class, this::convertSidebar);
        handle("stem", AbstractBlock.class, this::convertStem);
        handle("table", Table.class, this::convertTable);
        handle("thematic_break", AbstractBlock.class, this::convertThematicBreak);
        handle("verse", AbstractBlock.class, this::convertVerse);
        handle("video", AbstractBlock.class, this::convertVideo);
        handle("document", Document.class, this::convertDocument);
        handle("toc", AbstractBlock.class, this::convertToc);
        handle("pass", AbstractBlock.class, this::convertPass);
        handle("audio", AbstractBlock.class, this::convertAudio);
        handle("outline", (node, opts) -> convertOutline((AbstractBlock) node, opts));
    }

    private static String idAttribute(AbstractNode node) {
        return node.getId() != null ? " id=\"" + node.getId() + "\"" : "";
    }

    private static String roleSuffix(AbstractNode node) {
        String role = node.getRole();
        return role != null ? " " + role : "";
    }

    private static String joinNonNull(String separator, String... values) {
        return String.join(separator, Arrays.stream(values).filter(Objects::nonNull).toList());
    }

    public String convertDocument(Document node) {
        String slash = voidElementSlash;
        String br = "<br" + slash + ">";
        String assetUriScheme = node.getStringAttribute("asset-uri-scheme", "https");
        if (!assetUriScheme.isEmpty()) {
            assetUriScheme = assetUriScheme + ":";
        }
        String cdnBaseUrl = assetUriScheme + "//cdnjs.cloudflare.com/ajax/libs";
        boolean linkcss = node.hasAttribute("linkcss");
        String maxWidthAttr = node.hasAttribute("max-width") ? " style=\"max-width: " + node.getAttribute("max-width")
                + ";\"" : "";
        List<String> result = new ArrayList<>();
        result.add("<!DOCTYPE html>");
        String langAttribute = node.hasAttribute("nolang") ? "" : " lang=\"" + node.getAttribute("lang", "en") + "\"";
        result.add("<html" + (xmlMode ? " xmlns=\"http://www.w3.org/1999/xhtml\"" : "") + langAttribute + ">");
        result.add("<head>\n<meta charset=\"" + node.getAttribute("encoding", "UTF-8") + "\"" + slash + ">\n"
                + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"" + slash + ">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"" + slash + ">");
        boolean reproducible = node.hasAttribute("reproducible");
        if (!reproducible) {
            result.add("<meta name=\"generator\" content=\"AsciidoctorJ " + node.getAttribute("asciidoctor-version")
                    + "\"" + slash + ">");
        }
        if (node.hasAttribute("app-name")) {
            result.add("<meta name=\"application-name\" content=\"" + node.getAttribute("app-name") + "\"" + slash
                    + ">");
        }
        if (node.hasAttribute("description")) {
            result.add("<meta name=\"description\" content=\"" + node.getAttribute("description") + "\"" + slash
                    + ">");
        }
        if (node.hasAttribute("keywords")) {
            result.add("<meta name=\"keywords\" content=\"" + node.getAttribute("keywords") + "\"" + slash + ">");
        }
        if (node.hasAttribute("authors")) {
            String authors = node.subReplacements(node.getStringAttribute("authors"));
            result.add("<meta name=\"author\" content=\"" + (authors.contains("<")
                    ? Rx.gsub(Rx.XML_SANITIZE_RX, authors, "") : authors) + "\"" + slash + ">");
        }
        if (node.hasAttribute("copyright")) {
            result.add("<meta name=\"copyright\" content=\"" + node.getAttribute("copyright") + "\"" + slash + ">");
        }
        if (node.hasAttribute("favicon")) {
            String iconHref = node.getStringAttribute("favicon");
            String iconType;
            String iconExt;
            if (iconHref.isEmpty()) {
                iconHref = "favicon.ico";
                iconType = "image/x-icon";
            } else if ((iconExt = Helpers.extname(iconHref, null)) != null) {
                iconType = iconExt.equals(".ico") ? "image/x-icon" : "image/" + iconExt.substring(1);
            } else {
                iconType = "image/x-icon";
            }
            result.add("<link rel=\"icon\" type=\"" + iconType + "\" href=\"" + iconHref + "\"" + slash + ">");
        }
        result.add("<title>" + Strings.valueOf(node.getDoctitle(true, true)) + "</title>");

        if (DEFAULT_STYLESHEET_KEYS.contains(node.getStringAttribute("stylesheet"))) {
            String webfonts = node.getStringAttribute("webfonts");
            if (webfonts != null) {
                result.add("<link rel=\"stylesheet\" href=\"" + assetUriScheme + "//fonts.googleapis.com/css?family="
                        + (webfonts.isEmpty()
                                ? "Open+Sans:300,300italic,400,400italic,600,600italic%7CNoto+Serif:400,400italic,700,"
                                        + "700italic%7CNoto+Sans+Mono:400,700"
                                : webfonts)
                        + "\"" + slash + ">");
            }
            if (linkcss) {
                result.add("<link rel=\"stylesheet\" href=\"" + node.normalizeWebPath(DEFAULT_STYLESHEET_NAME,
                        node.getStringAttribute("stylesdir"), false) + "\"" + slash + ">");
            } else {
                result.add("<style>\n" + Stylesheets.getInstance().getPrimaryStylesheetData() + "\n</style>");
            }
        } else if (node.hasAttribute("stylesheet")) {
            if (linkcss) {
                result.add("<link rel=\"stylesheet\" href=\"" + node.normalizeWebPath(node.getStringAttribute(
                        "stylesheet"), node.getStringAttribute("stylesdir")) + "\"" + slash + ">");
            } else {
                Map<String, Object> readOpts = new LinkedHashMap<>();
                readOpts.put("start", node.getStringAttribute("stylesdir"));
                readOpts.put("warn_on_failure", true);
                readOpts.put("label", "stylesheet");
                result.add("<style>\n" + Strings.valueOf(node.readContents(node.getStringAttribute("stylesheet"),
                        readOpts)) + "\n</style>");
            }
        }

        if (node.isAttribute("icons", "font")) {
            if (node.hasAttribute("iconfont-remote")) {
                result.add("<link rel=\"stylesheet\" href=\"" + node.getAttribute("iconfont-cdn", cdnBaseUrl
                        + "/font-awesome/" + FONT_AWESOME_VERSION + "/css/font-awesome.min.css") + "\"" + slash + ">");
            } else {
                String iconfontStylesheet = node.getAttribute("iconfont-name", "font-awesome") + ".css";
                result.add("<link rel=\"stylesheet\" href=\"" + node.normalizeWebPath(iconfontStylesheet,
                        node.getStringAttribute("stylesdir"), false) + "\"" + slash + ">");
            }
        }

        SyntaxHighlighter syntaxHl = node.getSyntaxHighlighter();
        int syntaxHlDocinfoHeadIdx = -1;
        if (syntaxHl != null) {
            syntaxHlDocinfoHeadIdx = result.size();
            result.add(null);
        }

        String docinfoContent = node.getDocinfo();
        if (!docinfoContent.isEmpty()) {
            result.add(docinfoContent);
        }

        result.add("</head>");
        String idAttr = idAttribute(node);
        boolean sectioned = node.hasSections();
        List<String> classes = new ArrayList<>();
        classes.add(node.getDoctype());
        if (sectioned && node.hasAttribute("toc-class") && node.hasAttribute("toc")
                && node.isAttribute("toc-placement", "auto")) {
            classes.add(node.getStringAttribute("toc-class"));
            classes.add("toc-" + node.getAttribute("toc-position", "header"));
        }
        if (node.isRole()) {
            classes.add(node.getRole());
        }
        result.add("<body" + idAttr + " class=\"" + String.join(" ", classes) + "\">");

        if (!(docinfoContent = node.getDocinfo("header")).isEmpty()) {
            result.add(docinfoContent);
        }

        if (!node.isNoheader()) {
            result.add("<div id=\"header\"" + maxWidthAttr + ">");
            if ("manpage".equals(node.getDoctype())) {
                result.add("<h1>" + node.getDoctitle() + " Manual Page</h1>");
                if (sectioned && node.hasAttribute("toc") && node.isAttribute("toc-placement", "auto")) {
                    result.add("<div id=\"toc\" class=\"" + node.getAttribute("toc-class", "toc") + "\">\n"
                            + "<div id=\"toctitle\">" + node.getAttribute("toc-title") + "</div>\n"
                            + Converter.asString(node.getConverter().convert(node, "outline")) + "\n</div>");
                }
                if (node.hasAttribute("manpurpose")) {
                    result.add(generateMannameSection(node));
                }
            } else {
                if (node.hasHeader()) {
                    if (!node.isNotitle()) {
                        result.add("<h1>" + node.getHeader().getTitle() + "</h1>");
                    }
                    List<String> details = new ArrayList<>();
                    int idx = 1;
                    for (Author author : node.getAuthors()) {
                        details.add("<span id=\"author" + (idx > 1 ? idx : "") + "\" class=\"author\">"
                                + node.subReplacements(author.fullName()) + "</span>" + br);
                        if (author.email() != null) {
                            details.add("<span id=\"email" + (idx > 1 ? idx : "") + "\" class=\"email\">"
                                    + node.subMacros(author.email()) + "</span>" + br);
                        }
                        idx++;
                    }
                    if (node.hasAttribute("revnumber")) {
                        Object versionLabel = node.getAttribute("version-label");
                        details.add("<span id=\"revnumber\">" + (versionLabel == null ? ""
                                : versionLabel.toString().toLowerCase()) + " " + node.getAttribute("revnumber")
                                + (node.hasAttribute("revdate") ? "," : "") + "</span>");
                    }
                    if (node.hasAttribute("revdate")) {
                        details.add("<span id=\"revdate\">" + node.getAttribute("revdate") + "</span>");
                    }
                    if (node.hasAttribute("revremark")) {
                        details.add(br + "<span id=\"revremark\">" + node.getAttribute("revremark") + "</span>");
                    }
                    if (!details.isEmpty()) {
                        result.add("<div class=\"details\">");
                        result.addAll(details);
                        result.add("</div>");
                    }
                }

                if (sectioned && node.hasAttribute("toc") && node.isAttribute("toc-placement", "auto")) {
                    result.add("<div id=\"toc\" class=\"" + node.getAttribute("toc-class", "toc") + "\">\n"
                            + "<div id=\"toctitle\">" + node.getAttribute("toc-title") + "</div>\n"
                            + Converter.asString(node.getConverter().convert(node, "outline")) + "\n</div>");
                }
            }
            result.add("</div>");
        }

        result.add("<div id=\"content\"" + maxWidthAttr + ">\n" + node.getContent() + "\n</div>");

        if (node.hasFootnotes() && !node.hasAttribute("nofootnotes")) {
            result.add("<div id=\"footnotes\"" + maxWidthAttr + ">\n<hr" + slash + ">");
            for (Footnote footnote : node.getFootnotes()) {
                result.add("<div class=\"footnote\" id=\"_footnotedef_" + footnote.index() + "\">\n"
                        + "<a href=\"#_footnoteref_" + footnote.index() + "\">" + footnote.index() + "</a>. "
                        + footnote.text() + "\n</div>");
            }
            result.add("</div>");
        }

        if (!node.isNofooter()) {
            result.add("<div id=\"footer\"" + maxWidthAttr + ">");
            result.add("<div id=\"footer-text\">");
            if (node.hasAttribute("revnumber")) {
                result.add(Objects.toString(node.getAttribute("version-label"), "") + " " + node.getAttribute("revnumber") + br);
            }
            if (node.hasAttribute("last-update-label") && !reproducible) {
                result.add(node.getAttribute("last-update-label") + " " + node.getAttribute("docdatetime"));
            }
            result.add("</div>");
            result.add("</div>");
        }

        // JavaScript (and auxiliary stylesheets) loaded at the end of body for performance reasons
        // See http://www.html5rocks.com/en/tutorials/speed/script-loading/

        if (syntaxHl != null) {
            Map<String, Object> docinfoOpts = new LinkedHashMap<>();
            docinfoOpts.put("cdn_base_url", cdnBaseUrl);
            docinfoOpts.put("linkcss", linkcss);
            docinfoOpts.put("self_closing_tag_slash", slash);
            if (syntaxHl.hasDocinfo("head")) {
                result.set(syntaxHlDocinfoHeadIdx, syntaxHl.getDocinfo("head", node, docinfoOpts));
            } else {
                result.remove(syntaxHlDocinfoHeadIdx);
            }
            if (syntaxHl.hasDocinfo("footer")) {
                result.add(syntaxHl.getDocinfo("footer", node, docinfoOpts));
            }
        }

        if (node.hasAttribute("stem")) {
            String eqnumsVal = node.getStringAttribute("eqnums", "none");
            if (eqnumsVal.isEmpty()) {
                eqnumsVal = "AMS";
            }
            String eqnumsOpt = " equationNumbers: { autoNumber: \"" + eqnumsVal + "\" } ";
            // IMPORTANT inspect calls on delimiter arrays are intentional for JavaScript compat (emulates
            // JSON.stringify)
            result.add("<script type=\"text/x-mathjax-config\">\n"
                    + "MathJax.Hub.Config({\n"
                    + "  messageStyle: \"none\",\n"
                    + "  tex2jax: {\n"
                    + "    inlineMath: [" + inspect(INLINE_MATH_DELIMITERS.get("latexmath")) + "],\n"
                    + "    displayMath: [" + inspect(BLOCK_MATH_DELIMITERS.get("latexmath")) + "],\n"
                    + "    ignoreClass: \"nostem|nolatexmath\"\n"
                    + "  },\n"
                    + "  asciimath2jax: {\n"
                    + "    delimiters: [" + inspect(BLOCK_MATH_DELIMITERS.get("asciimath")) + "],\n"
                    + "    ignoreClass: \"nostem|noasciimath\"\n"
                    + "  },\n"
                    + "  TeX: {" + eqnumsOpt + "}\n"
                    + "})\n"
                    + "MathJax.Hub.Register.StartupHook(\"AsciiMath Jax Ready\", function () {\n"
                    + "  MathJax.InputJax.AsciiMath.postfilterHooks.Add(function (data, node) {\n"
                    + "    if ((node = data.script.parentNode) && (node = node.parentNode) && "
                    + "node.classList.contains(\"stemblock\")) {\n"
                    + "      data.math.root.display = \"block\"\n"
                    + "    }\n"
                    + "    return data\n"
                    + "  })\n"
                    + "})\n"
                    + "</script>\n"
                    + "<script src=\"" + cdnBaseUrl + "/mathjax/" + MATHJAX_VERSION
                    + "/MathJax.js?config=TeX-MML-AM_CHTML\"></script>");
        }

        if (!(docinfoContent = node.getDocinfo("footer")).isEmpty()) {
            result.add(docinfoContent);
        }

        result.add("</body>");
        result.add("</html>");
        return String.join(LF, result);
    }

    private static String inspect(List<String> values) {
        List<String> items = new ArrayList<>();
        for (String value : values) {
            items.add("\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
        }
        return "[" + String.join(", ", items) + "]";
    }

    public String convertEmbedded(Document node) {
        List<String> result = new ArrayList<>();
        if ("manpage".equals(node.getDoctype())) {
            // QUESTION should notitle control the manual page title?
            if (!node.isNotitle()) {
                result.add("<h1" + idAttribute(node) + ">" + node.getDoctitle() + " Manual Page</h1>");
            }
            if (node.hasAttribute("manpurpose")) {
                result.add(generateMannameSection(node));
            }
        } else if (node.hasHeader() && !node.isNotitle()) {
            result.add("<h1" + idAttribute(node) + ">" + node.getHeader().getTitle() + "</h1>");
        }

        String tocP;
        if (node.hasSections() && node.hasAttribute("toc")
                && !"macro".equals(tocP = node.getStringAttribute("toc-placement")) && !"preamble".equals(tocP)) {
            result.add("<div id=\"toc\" class=\"toc\">\n<div id=\"toctitle\">" + node.getAttribute("toc-title")
                    + "</div>\n" + Converter.asString(node.getConverter().convert(node, "outline")) + "\n</div>");
        }

        result.add(node.getContent());

        if (node.hasFootnotes() && !node.hasAttribute("nofootnotes")) {
            result.add("<div id=\"footnotes\">\n<hr" + voidElementSlash + ">");
            for (Footnote footnote : node.getFootnotes()) {
                result.add("<div class=\"footnote\" id=\"_footnotedef_" + footnote.index() + "\">\n"
                        + "<a href=\"#_footnoteref_" + footnote.index() + "\">" + footnote.index() + "</a>. "
                        + footnote.text() + "\n</div>");
            }
            result.add("</div>");
        }

        return String.join(LF, result);
    }

    public String convertOutline(AbstractBlock node, Map<String, Object> opts) {
        if (!node.hasSections()) {
            return null;
        }
        if (opts == null) {
            opts = Map.of();
        }
        Map<String, Object> docAttrs = node.getDocument().getAttributes();
        List<Section> sections = node.getSections();
        boolean parts = node instanceof Document document && document.isMultipart();
        int sectlevel = parts ? 0 : sections.get(0).getLevel();
        int sectnumlevels = opts.get("sectnumlevels") != null ? Strings.toInt(opts.get("sectnumlevels"))
                : Strings.toInt(docAttrs.getOrDefault("sectnumlevels", 3));
        int toclevels;
        if (opts.get("toclevels") != null) {
            toclevels = Strings.toInt(opts.get("toclevels"));
        } else if (docAttrs.get("toclevels") != null) {
            toclevels = Strings.toInt(docAttrs.get("toclevels"));
            if (toclevels < 1 && !parts) {
                toclevels = 1;
            }
        } else {
            toclevels = 2;
        }
        List<String> result = new ArrayList<>();
        result.add("<ul class=\"sectlevel" + sectlevel + "\">");
        for (Section section : sections) {
            int slevel = section.getLevel();
            int stoclevels = section.hasAttribute("toclevels") ? Strings.toInt(section.getAttribute("toclevels"))
                    : toclevels;
            if (slevel > stoclevels) {
                continue;
            }
            String stitle;
            if (section.getCaption() != null) {
                stitle = section.getCaptionedTitle();
            } else if (section.isNumbered() && slevel <= sectnumlevels) {
                if (slevel < 2 && "book".equals(node.getDocument().getDoctype())) {
                    switch (Strings.valueOf(section.getSectionName())) {
                        case "chapter" -> {
                            Object signifier = docAttrs.get("chapter-signifier");
                            stitle = (signifier != null ? signifier + " " : "") + section.getSectnum() + " "
                                    + section.getTitle();
                        }
                        case "part" -> {
                            Object signifier = docAttrs.get("part-signifier");
                            stitle = (signifier != null ? signifier + " " : "") + section.getSectnum(null, ":") + " "
                                    + section.getTitle();
                        }
                        default -> stitle = section.getSectnum() + " " + section.getTitle();
                    }
                } else {
                    stitle = section.getSectnum() + " " + section.getTitle();
                }
            } else {
                stitle = section.getTitle();
            }
            if (stitle.contains("<a")) {
                stitle = Rx.gsub(DROP_ANCHOR_RX, stitle, "");
            }
            String childTocLevel = null;
            if (slevel < stoclevels) {
                Map<String, Object> childOpts = new LinkedHashMap<>();
                childOpts.put("toclevels", stoclevels);
                childOpts.put("sectnumlevels", sectnumlevels);
                childTocLevel = convertOutline(section, childOpts);
            }
            String otag = slevel == sectlevel ? "<li>" : "<li class=\"sectlevel" + slevel + "\">";
            if (childTocLevel != null) {
                result.add(otag + "<a href=\"#" + Objects.toString(section.getId(), "") + "\">" + stitle + "</a>");
                result.add(childTocLevel);
                result.add("</li>");
            } else {
                result.add(otag + "<a href=\"#" + Objects.toString(section.getId(), "") + "\">" + stitle + "</a></li>");
            }
        }
        result.add("</ul>");
        return String.join(LF, result);
    }

    public String convertSection(Section node) {
        Map<String, Object> docAttrs = node.getDocument().getAttributes();
        int level = node.getLevel();
        String title;
        if (node.getCaption() != null) {
            title = node.getCaptionedTitle();
        } else if (node.isNumbered() && level <= Strings.toInt(docAttrs.getOrDefault("sectnumlevels", 3))) {
            if (level < 2 && "book".equals(node.getDocument().getDoctype())) {
                switch (Strings.valueOf(node.getSectionName())) {
                    case "chapter" -> {
                        Object signifier = docAttrs.get("chapter-signifier");
                        title = (signifier != null ? signifier + " " : "") + node.getSectnum() + " "
                                + node.getTitle();
                    }
                    case "part" -> {
                        Object signifier = docAttrs.get("part-signifier");
                        title = (signifier != null ? signifier + " " : "") + node.getSectnum(null, ":") + " "
                                + node.getTitle();
                    }
                    default -> title = node.getSectnum() + " " + node.getTitle();
                }
            } else {
                title = node.getSectnum() + " " + node.getTitle();
            }
        } else {
            title = node.getTitle();
        }
        String idAttr;
        String id = node.getId();
        if (id != null) {
            idAttr = " id=\"" + id + "\"";
            if (docAttrs.get("sectlinks") != null) {
                Matcher m;
                if (title.startsWith("<a ") && (m = Rx.match(LEADING_ANCHORS_RX, title)) != null) {
                    title = m.group() + "<a class=\"link\" href=\"#" + id + "\">" + title.substring(m.group().length())
                            + "</a>";
                } else {
                    title = "<a class=\"link\" href=\"#" + id + "\">" + title + "</a>";
                }
            }
            Object sectanchors = docAttrs.get("sectanchors");
            if (sectanchors != null) {
                // QUESTION should we add a font-based icon in anchor if icons=font?
                if ("after".equals(sectanchors)) {
                    title = title + "<a class=\"anchor\" href=\"#" + id + "\"></a>";
                } else {
                    title = "<a class=\"anchor\" href=\"#" + id + "\"></a>" + title;
                }
            }
        } else {
            idAttr = "";
        }
        if (level == 0) {
            return "<h1" + idAttr + " class=\"sect0" + roleSuffix(node) + "\">" + title + "</h1>\n"
                    + node.getContent();
        }
        return "<div class=\"sect" + level + roleSuffix(node) + "\">\n"
                + "<h" + (level + 1) + idAttr + ">" + title + "</h" + (level + 1) + ">\n"
                + (level == 1 ? "<div class=\"sectionbody\">\n" + node.getContent() + "\n</div>" : node.getContent())
                + "\n</div>";
    }

    public String convertAdmonition(AbstractBlock node) {
        String idAttr = idAttribute(node);
        Object name = node.getAttribute("name");
        String titleElement = node.hasTitle() ? "<div class=\"title\">" + node.getTitle() + "</div>\n" : "";
        String label;
        if (node.getDocument().hasAttribute("icons")) {
            if (node.getDocument().isAttribute("icons", "font") && !node.hasAttribute("icon")) {
                label = "<i class=\"fa icon-" + name + "\" title=\"" + node.getAttribute("textlabel") + "\"></i>";
            } else {
                label = "<img src=\"" + node.iconUri(String.valueOf(name)) + "\" alt=\""
                        + node.getAttribute("textlabel") + "\"" + voidElementSlash + ">";
            }
        } else {
            label = "<div class=\"title\">" + node.getAttribute("textlabel") + "</div>";
        }
        return "<div" + idAttr + " class=\"admonitionblock " + name + roleSuffix(node) + "\">\n"
                + "<table>\n"
                + "<tr>\n"
                + "<td class=\"icon\">\n"
                + label + "\n"
                + "</td>\n"
                + "<td class=\"content\">\n"
                + titleElement + node.getContent() + "\n"
                + "</td>\n"
                + "</tr>\n"
                + "</table>\n"
                + "</div>";
    }

    public String convertAudio(AbstractBlock node) {
        boolean xml = xmlMode;
        String idAttribute = idAttribute(node);
        String classAttribute = " class=\"" + joinNonNull(" ", "audioblock", node.getRole()) + "\"";
        String titleElement = node.hasTitle() ? "<div class=\"title\">" + node.getTitle() + "</div>\n" : "";
        Object startT = node.getAttribute("start");
        Object endT = node.getAttribute("end");
        String timeAnchor = startT != null || endT != null
                ? "#t=" + (startT == null ? "" : startT) + (endT != null ? "," + endT : "") : "";
        return "<div" + idAttribute + classAttribute + ">\n"
                + titleElement + "<div class=\"content\">\n"
                + "<audio src=\"" + node.mediaUri(node.getStringAttribute("target")) + timeAnchor + "\""
                + (node.isOption("autoplay") ? appendBooleanAttribute("autoplay", xml) : "")
                + (node.isOption("nocontrols") ? "" : appendBooleanAttribute("controls", xml))
                + (node.isOption("loop") ? appendBooleanAttribute("loop", xml) : "") + ">\n"
                + "Your browser does not support the audio tag.\n"
                + "</audio>\n"
                + "</div>\n"
                + "</div>";
    }

    public String convertColist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        String idAttribute = idAttribute(node);
        String classAttribute = " class=\"" + joinNonNull(" ", "colist", node.getStyle(), node.getRole()) + "\"";

        result.add("<div" + idAttribute + classAttribute + ">");
        if (node.hasTitle()) {
            result.add("<div class=\"title\">" + node.getTitle() + "</div>");
        }

        if (node.getDocument().hasAttribute("icons")) {
            result.add("<table>");
            boolean fontIcons = node.getDocument().isAttribute("icons", "font");
            int num = 0;
            for (AbstractBlock block : node.getItems()) {
                ListItem item = (ListItem) block;
                num++;
                String numLabel;
                if (fontIcons) {
                    numLabel = "<i class=\"conum\" data-value=\"" + num + "\"></i><b>" + num + "</b>";
                } else {
                    numLabel = "<img src=\"" + node.iconUri("callouts/" + num) + "\" alt=\"" + num + "\""
                            + voidElementSlash + ">";
                }
                result.add("<tr>\n<td>" + numLabel + "</td>\n<td>" + item.getText()
                        + (item.hasBlocks() ? LF + item.getContent() : "") + "</td>\n</tr>");
            }
            result.add("</table>");
        } else {
            result.add("<ol>");
            for (AbstractBlock block : node.getItems()) {
                ListItem item = (ListItem) block;
                result.add("<li>\n<p>" + item.getText() + "</p>" + (item.hasBlocks() ? LF + item.getContent() : "")
                        + "\n</li>");
            }
            result.add("</ol>");
        }

        result.add("</div>");
        return String.join(LF, result);
    }

    public String convertDlist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        String idAttribute = idAttribute(node);

        String style = node.getStyle();
        String classes = switch (style == null ? "" : style) {
            case "qanda" -> joinNonNull(" ", "qlist", "qanda", node.getRole());
            case "horizontal" -> joinNonNull(" ", "hdlist", node.getRole());
            default -> joinNonNull(" ", "dlist", style, node.getRole());
        };

        String classAttribute = " class=\"" + classes + "\"";

        result.add("<div" + idAttribute + classAttribute + ">");
        if (node.hasTitle()) {
            result.add("<div class=\"title\">" + node.getTitle() + "</div>");
        }
        switch (style == null ? "" : style) {
            case "qanda" -> {
                result.add("<ol>");
                for (AbstractBlock block : node.getItems()) {
                    DescriptionListEntry entry = (DescriptionListEntry) block;
                    result.add("<li>");
                    for (ListItem dt : entry.getTerms()) {
                        result.add("<p><em>" + dt.getText() + "</em></p>");
                    }
                    ListItem dd = entry.getDescription();
                    if (dd != null) {
                        if (dd.hasText()) {
                            result.add("<p>" + dd.getText() + "</p>");
                        }
                        if (dd.hasBlocks()) {
                            result.add(dd.getContent());
                        }
                    }
                    result.add("</li>");
                }
                result.add("</ol>");
            }
            case "horizontal" -> {
                String slash = voidElementSlash;
                result.add("<table>");
                if (node.hasAttribute("labelwidth") || node.hasAttribute("itemwidth")) {
                    result.add("<colgroup>");
                    String colStyleAttribute = node.hasAttribute("labelwidth") ? " width=\""
                            + Strings.chomp(node.getStringAttribute("labelwidth"), "%") + "%\"" : "";
                    result.add("<col" + colStyleAttribute + slash + ">");
                    colStyleAttribute = node.hasAttribute("itemwidth") ? " width=\""
                            + Strings.chomp(node.getStringAttribute("itemwidth"), "%") + "%\"" : "";
                    result.add("<col" + colStyleAttribute + slash + ">");
                    result.add("</colgroup>");
                }
                for (AbstractBlock block : node.getItems()) {
                    DescriptionListEntry entry = (DescriptionListEntry) block;
                    result.add("<tr>");
                    result.add("<td class=\"hdlist1" + (node.isOption("strong") ? " strong" : "") + "\">");
                    boolean firstTerm = true;
                    for (ListItem dt : entry.getTerms()) {
                        if (!firstTerm) {
                            result.add("<br" + slash + ">");
                        }
                        result.add(dt.getText());
                        firstTerm = false;
                    }
                    result.add("</td>");
                    result.add("<td class=\"hdlist2\">");
                    ListItem dd = entry.getDescription();
                    if (dd != null) {
                        if (dd.hasText()) {
                            result.add("<p>" + dd.getText() + "</p>");
                        }
                        if (dd.hasBlocks()) {
                            result.add(dd.getContent());
                        }
                    }
                    result.add("</td>");
                    result.add("</tr>");
                }
                result.add("</table>");
            }
            default -> {
                result.add("<dl>");
                String dtStyleAttribute = style != null ? "" : " class=\"hdlist1\"";
                for (AbstractBlock block : node.getItems()) {
                    DescriptionListEntry entry = (DescriptionListEntry) block;
                    for (ListItem dt : entry.getTerms()) {
                        result.add("<dt" + dtStyleAttribute + ">" + dt.getText() + "</dt>");
                    }
                    ListItem dd = entry.getDescription();
                    if (dd == null) {
                        continue;
                    }
                    result.add("<dd>");
                    if (dd.hasText()) {
                        result.add("<p>" + dd.getText() + "</p>");
                    }
                    if (dd.hasBlocks()) {
                        result.add(dd.getContent());
                    }
                    result.add("</dd>");
                }
                result.add("</dl>");
            }
        }

        result.add("</div>");
        return String.join(LF, result);
    }

    public String convertExample(AbstractBlock node) {
        String idAttribute = idAttribute(node);
        if (node.isOption("collapsible")) {
            String classAttribute = node.getRole() != null ? " class=\"" + node.getRole() + "\"" : "";
            String summaryElement = node.hasTitle() ? "<summary class=\"title\">" + node.getTitle() + "</summary>"
                    : "<summary class=\"title\">Details</summary>";
            return "<details" + idAttribute + classAttribute + (node.isOption("open") ? " open" : "") + ">\n"
                    + summaryElement + "\n"
                    + "<div class=\"content\">\n"
                    + node.getContent() + "\n"
                    + "</div>\n"
                    + "</details>";
        }
        String titleElement = node.hasTitle() ? "<div class=\"title\">" + node.getCaptionedTitle() + "</div>\n" : "";
        return "<div" + idAttribute + " class=\"exampleblock" + roleSuffix(node) + "\">\n"
                + titleElement + "<div class=\"content\">\n"
                + node.getContent() + "\n"
                + "</div>\n"
                + "</div>";
    }

    public String convertFloatingTitle(AbstractBlock node) {
        String tagName = "h" + (node.getLevel() + 1);
        String idAttribute = idAttribute(node);
        String classes = joinNonNull(" ", node.getStyle(), node.getRole());
        return "<" + tagName + idAttribute + " class=\"" + classes + "\">" + node.getTitle() + "</" + tagName + ">";
    }

    public String convertImage(AbstractBlock node) {
        String target = node.getStringAttribute("target");
        String widthAttr = node.hasAttribute("width") ? " width=\"" + node.getAttribute("width") + "\"" : "";
        String heightAttr = node.hasAttribute("height") ? " height=\"" + node.getAttribute("height") + "\"" : "";
        String img;
        String src = null;
        if ((node.isAttribute("format", "svg") || target.contains(".svg"))
                && node.getDocument().getSafe() < SafeMode.SECURE.getLevel()) {
            if (node.isOption("inline")) {
                img = readSvgContents(node, target);
                if (img == null) {
                    img = "<span class=\"alt\">" + node.getAlt() + "</span>";
                }
            } else if (node.isOption("interactive")) {
                String fallback = node.hasAttribute("fallback") ? "<img src=\"" + node.imageUri(node.getStringAttribute(
                        "fallback")) + "\" alt=\"" + encodeAttributeValue(node.getAlt()) + "\"" + widthAttr
                        + heightAttr + voidElementSlash + ">" : "<span class=\"alt\">" + node.getAlt() + "</span>";
                img = "<object type=\"image/svg+xml\" data=\"" + (src = node.imageUri(target)) + "\"" + widthAttr
                        + heightAttr + ">" + fallback + "</object>";
            } else {
                img = "<img src=\"" + (src = node.imageUri(target)) + "\" alt=\"" + encodeAttributeValue(node.getAlt())
                        + "\"" + widthAttr + heightAttr + voidElementSlash + ">";
            }
        } else {
            img = "<img src=\"" + (src = node.imageUri(target)) + "\" alt=\"" + encodeAttributeValue(node.getAlt())
                    + "\"" + widthAttr + heightAttr + voidElementSlash + ">";
        }
        String hrefAttrVal;
        if (node.hasAttribute("link") && (!"self".equals(hrefAttrVal = node.getStringAttribute("link"))
                || (hrefAttrVal = src) != null)) {
            img = "<a class=\"image\" href=\"" + hrefAttrVal + "\"" + String.join("",
                    appendLinkConstraintAttrs(node, new ArrayList<>())) + ">" + img + "</a>";
        }
        String idAttr = idAttribute(node);
        List<String> classes = new ArrayList<>();
        classes.add("imageblock");
        if (node.hasAttribute("float")) {
            classes.add(node.getStringAttribute("float"));
        }
        if (node.hasAttribute("align")) {
            classes.add("text-" + node.getAttribute("align"));
        }
        if (node.getRole() != null) {
            classes.add(node.getRole());
        }
        String classAttr = " class=\"" + String.join(" ", classes) + "\"";
        String titleEl = node.hasTitle() ? "\n<div class=\"title\">" + node.getCaptionedTitle() + "</div>" : "";
        return "<div" + idAttr + classAttr + ">\n"
                + "<div class=\"content\">\n"
                + img + "\n"
                + "</div>" + titleEl + "\n"
                + "</div>";
    }

    public String convertListing(AbstractBlock node) {
        boolean nowrap = node.isOption("nowrap") || !node.getDocument().hasAttribute("prewrap");
        String lang = null;
        SyntaxHighlighter syntaxHl = null;
        Map<String, Object> opts = null;
        String preOpen;
        String preClose;
        if ("source".equals(node.getStyle())) {
            lang = node.getStringAttribute("language");
            if ((syntaxHl = node.getDocument().getSyntaxHighlighter()) != null) {
                opts = new LinkedHashMap<>();
                if (syntaxHl.isHighlight()) {
                    Map<String, Object> docAttrs = node.getDocument().getAttributes();
                    Object cssMode = docAttrs.get(syntaxHl.getName() + "-css");
                    opts.put("css_mode", cssMode == null ? "class" : cssMode.toString());
                    opts.put("style", docAttrs.get(syntaxHl.getName() + "-style"));
                }
                opts.put("nowrap", nowrap);
                preOpen = preClose = null;
            } else {
                preOpen = "<pre class=\"highlight" + (nowrap ? " nowrap" : "") + "\"><code" + (lang != null
                        ? " class=\"language-" + lang + "\" data-lang=\"" + lang + "\"" : "") + ">";
                preClose = "</code></pre>";
            }
        } else {
            preOpen = "<pre" + (nowrap ? " class=\"nowrap\"" : "") + ">";
            preClose = "</pre>";
        }
        String idAttribute = idAttribute(node);
        String titleElement = node.hasTitle() ? "<div class=\"title\">" + node.getCaptionedTitle() + "</div>\n" : "";
        return "<div" + idAttribute + " class=\"listingblock" + roleSuffix(node) + "\">\n"
                + titleElement + "<div class=\"content\">\n"
                + (syntaxHl != null ? syntaxHl.format(node, lang, opts) : preOpen + node.getContent() + preClose) + "\n"
                + "</div>\n"
                + "</div>";
    }

    public String convertLiteral(AbstractBlock node) {
        String idAttribute = idAttribute(node);
        String titleElement = node.hasTitle() ? "<div class=\"title\">" + node.getTitle() + "</div>\n" : "";
        boolean nowrap = !node.getDocument().hasAttribute("prewrap") || node.isOption("nowrap");
        return "<div" + idAttribute + " class=\"literalblock" + roleSuffix(node) + "\">\n"
                + titleElement + "<div class=\"content\">\n"
                + "<pre" + (nowrap ? " class=\"nowrap\"" : "") + ">" + node.getContent() + "</pre>\n"
                + "</div>\n"
                + "</div>";
    }

    public String convertStem(AbstractBlock node) {
        String idAttribute = idAttribute(node);
        String titleElement = node.hasTitle() ? "<div class=\"title\">" + node.getTitle() + "</div>\n" : "";
        String style = node.getStyle();
        List<String> delimiters = BLOCK_MATH_DELIMITERS.get(style);
        String open = delimiters == null ? "" : delimiters.get(0);
        String close = delimiters == null ? "" : delimiters.get(1);
        String equation = node.getContent();
        if (equation != null) {
            if ("asciimath".equals(style) && equation.contains(LF)) {
                String br = LF + "<br" + voidElementSlash + ">";
                equation = Rx.gsub(STEM_BREAK_RX, equation, m -> close + br.repeat(Math.max(Strings.count(m.group(),
                        '\n') - 1, 0)) + LF + open);
            }
            if (!(equation.startsWith(open) && equation.endsWith(close))) {
                equation = open + equation + close;
            }
        } else {
            equation = "";
        }
        return "<div" + idAttribute + " class=\"stemblock" + roleSuffix(node) + "\">\n"
                + titleElement + "<div class=\"content\">\n"
                + equation + "\n"
                + "</div>\n"
                + "</div>";
    }

    public String convertOlist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        String idAttribute = idAttribute(node);
        String classAttribute = " class=\"" + joinNonNull(" ", "olist", node.getStyle(), node.getRole()) + "\"";

        result.add("<div" + idAttribute + classAttribute + ">");
        if (node.hasTitle()) {
            result.add("<div class=\"title\">" + node.getTitle() + "</div>");
        }

        String keyword = node.getListMarkerKeyword();
        String typeAttribute = keyword != null ? " type=\"" + keyword + "\"" : "";
        String startAttribute = node.hasAttribute("start") ? " start=\"" + node.getAttribute("start") + "\"" : "";
        String reversedAttribute = node.isOption("reversed") ? appendBooleanAttribute("reversed", xmlMode) : "";
        result.add("<ol class=\"" + Strings.valueOf(node.getStyle()) + "\"" + typeAttribute + startAttribute
                + reversedAttribute + ">");

        for (AbstractBlock block : node.getItems()) {
            ListItem item = (ListItem) block;
            if (item.getId() != null) {
                result.add("<li id=\"" + item.getId() + "\"" + (item.getRole() != null ? " class=\"" + item.getRole()
                        + "\"" : "") + ">");
            } else if (item.getRole() != null) {
                result.add("<li class=\"" + item.getRole() + "\">");
            } else {
                result.add("<li>");
            }
            result.add("<p>" + item.getText() + "</p>");
            if (item.hasBlocks()) {
                result.add(item.getContent());
            }
            result.add("</li>");
        }

        result.add("</ol>");
        result.add("</div>");
        return String.join(LF, result);
    }

    public String convertOpen(AbstractBlock node) {
        String style = node.getStyle();
        if ("abstract".equals(style)) {
            if (node.getParent() == node.getDocument() && "book".equals(node.getDocument().getDoctype())) {
                getLogger().warn("abstract block cannot be used in a document without a doctitle when doctype is "
                        + "book. Excluding block content.");
                return "";
            }
            String idAttr = idAttribute(node);
            String titleEl = node.hasTitle() ? "<div class=\"title\">" + node.getTitle() + "</div>\n" : "";
            return "<div" + idAttr + " class=\"quoteblock abstract" + roleSuffix(node) + "\">\n"
                    + titleEl + "<blockquote>\n"
                    + node.getContent() + "\n"
                    + "</blockquote>\n"
                    + "</div>";
        } else if ("partintro".equals(style) && (node.getLevel() > 0 || !"section".equals(node.getParent()
                .getContext()) || !"book".equals(node.getDocument().getDoctype()))) {
            getLogger().error("partintro block can only be used when doctype is book and must be a child of a book "
                    + "part. Excluding block content.");
            return "";
        }
        String idAttr = idAttribute(node);
        String titleEl = node.hasTitle() ? "<div class=\"title\">" + node.getTitle() + "</div>\n" : "";
        return "<div" + idAttr + " class=\"openblock" + (style != null && !style.equals("open") ? " " + style : "")
                + roleSuffix(node) + "\">\n"
                + titleEl + "<div class=\"content\">\n"
                + node.getContent() + "\n"
                + "</div>\n"
                + "</div>";
    }

    public String convertPageBreak(AbstractBlock node) {
        return "<div class=\"page-break\"></div>";
    }

    public String convertParagraph(AbstractBlock node) {
        String attributes;
        if (node.getRole() != null) {
            attributes = idAttribute(node) + " class=\"paragraph " + node.getRole() + "\"";
        } else if (node.getId() != null) {
            attributes = " id=\"" + node.getId() + "\" class=\"paragraph\"";
        } else {
            attributes = " class=\"paragraph\"";
        }
        if (node.hasTitle()) {
            return "<div" + attributes + ">\n"
                    + "<div class=\"title\">" + node.getTitle() + "</div>\n"
                    + "<p>" + node.getContent() + "</p>\n"
                    + "</div>";
        }
        return "<div" + attributes + ">\n"
                + "<p>" + node.getContent() + "</p>\n"
                + "</div>";
    }

    public String convertPass(AbstractBlock node) {
        return contentOnly(node);
    }

    public String convertPreamble(AbstractBlock node) {
        Document doc = node.getDocument();
        String toc;
        if (doc.isAttribute("toc-placement", "preamble") && doc.hasSections() && doc.hasAttribute("toc")) {
            toc = "\n<div id=\"toc\" class=\"" + doc.getAttribute("toc-class", "toc") + "\">\n"
                    + "<div id=\"toctitle\">" + doc.getAttribute("toc-title") + "</div>\n"
                    + Converter.asString(doc.getConverter().convert(doc, "outline")) + "\n"
                    + "</div>";
        } else {
            toc = "";
        }

        return "<div id=\"preamble\">\n"
                + "<div class=\"sectionbody\">\n"
                + node.getContent() + "\n"
                + "</div>" + toc + "\n"
                + "</div>";
    }

    public String convertQuote(AbstractBlock node) {
        String idAttribute = idAttribute(node);
        String classAttribute = " class=\"" + joinNonNull(" ", "quoteblock", node.getRole()) + "\"";
        String titleElement = node.hasTitle() ? "\n<div class=\"title\">" + node.getTitle() + "</div>" : "";
        Object attribution = node.hasAttribute("attribution") ? node.getAttribute("attribution") : null;
        Object citetitle = node.hasAttribute("citetitle") ? node.getAttribute("citetitle") : null;
        String attributionElement;
        if (attribution != null || citetitle != null) {
            String citeElement = citetitle != null ? "<cite>" + citetitle + "</cite>" : "";
            String attributionText = attribution != null ? "&#8212; " + attribution + (citetitle != null ? "<br"
                    + voidElementSlash + ">\n" : "") : "";
            attributionElement = "\n<div class=\"attribution\">\n" + attributionText + citeElement + "\n</div>";
        } else {
            attributionElement = "";
        }

        return "<div" + idAttribute + classAttribute + ">" + titleElement + "\n"
                + "<blockquote>\n"
                + node.getContent() + "\n"
                + "</blockquote>" + attributionElement + "\n"
                + "</div>";
    }

    public String convertThematicBreak(AbstractBlock node) {
        String classAttribute = node.getRole() != null ? " class=\"" + node.getRole() + "\"" : "";
        return "<hr" + classAttribute + voidElementSlash + ">";
    }

    public String convertSidebar(AbstractBlock node) {
        String idAttribute = idAttribute(node);
        String titleElement = node.hasTitle() ? "<div class=\"title\">" + node.getTitle() + "</div>\n" : "";
        return "<div" + idAttribute + " class=\"sidebarblock" + roleSuffix(node) + "\">\n"
                + "<div class=\"content\">\n"
                + titleElement + node.getContent() + "\n"
                + "</div>\n"
                + "</div>";
    }

    public String convertTable(Table node) {
        List<String> result = new ArrayList<>();
        String idAttribute = idAttribute(node);
        String frame = node.getStringAttribute("frame", "all", "table-frame");
        if ("topbot".equals(frame)) {
            frame = "ends";
        }
        List<String> classes = new ArrayList<>();
        classes.add("tableblock");
        classes.add("frame-" + frame);
        classes.add("grid-" + node.getAttribute("grid", "all", "table-grid"));
        Object stripes = node.getAttribute("stripes", null, "table-stripes");
        if (stripes != null) {
            classes.add("stripes-" + stripes);
        }
        String widthAttribute = "";
        boolean autowidth = node.isOption("autowidth");
        Object tablewidth;
        if (autowidth && !node.hasAttribute("width")) {
            classes.add("fit-content");
        } else if (Objects.equals(tablewidth = node.getAttribute("tablepcwidth"), 100)) {
            classes.add("stretch");
        } else {
            widthAttribute = " width=\"" + tablewidth + "%\"";
        }
        if (node.hasAttribute("float")) {
            classes.add(node.getStringAttribute("float"));
        }
        String role = node.getRole();
        if (role != null) {
            classes.add(role);
        }
        String classAttribute = " class=\"" + String.join(" ", classes) + "\"";

        result.add("<table" + idAttribute + classAttribute + widthAttribute + ">");
        if (node.hasTitle()) {
            result.add("<caption class=\"title\">" + node.getCaptionedTitle() + "</caption>");
        }
        if (Strings.toInt(node.getAttribute("rowcount")) > 0) {
            String slash = voidElementSlash;
            result.add("<colgroup>");
            if (autowidth) {
                for (int i = 0; i < node.getColumns().size(); i++) {
                    result.add("<col" + slash + ">");
                }
            } else {
                for (Table.Column col : node.getColumns()) {
                    result.add(col.isOption("autowidth") ? "<col" + slash + ">"
                            : "<col width=\"" + col.getAttribute("colpcwidth") + "%\"" + slash + ">");
                }
            }
            result.add("</colgroup>");
            for (Map.Entry<String, List<Table.Row>> entry : node.getRows().bySection().entrySet()) {
                String tsec = entry.getKey();
                List<Table.Row> rows = entry.getValue();
                if (rows.isEmpty()) {
                    continue;
                }
                result.add("<t" + tsec + ">");
                for (Table.Row row : rows) {
                    result.add("<tr>");
                    for (Table.Cell cell : row) {
                        String cellContent;
                        if (tsec.equals("head")) {
                            cellContent = cell.getText();
                        } else {
                            switch (Strings.valueOf(cell.getStyle()) == null ? "" : cell.getStyle()) {
                                case "asciidoc" -> cellContent = "<div class=\"content\">" + cell.getContent()
                                        + "</div>";
                                case "literal" -> cellContent = "<div class=\"literal\"><pre>" + cell.getText()
                                        + "</pre></div>";
                                default -> {
                                    List<String> paragraphs = cell.getParagraphs();
                                    cellContent = paragraphs.isEmpty() ? "" : "<p class=\"tableblock\">"
                                            + String.join("</p>\n<p class=\"tableblock\">", paragraphs) + "</p>";
                                }
                            }
                        }

                        String cellTagName = tsec.equals("head") || "header".equals(cell.getStyle()) ? "th" : "td";
                        String cellClassAttribute = " class=\"tableblock halign-" + cell.getAttribute("halign")
                                + " valign-" + cell.getAttribute("valign") + "\"";
                        String cellColspanAttribute = cell.getColspan() != null ? " colspan=\"" + cell.getColspan()
                                + "\"" : "";
                        String cellRowspanAttribute = cell.getRowspan() != null ? " rowspan=\"" + cell.getRowspan()
                                + "\"" : "";
                        String cellStyleAttribute = node.getDocument().hasAttribute("cellbgcolor")
                                ? " style=\"background-color: " + node.getDocument().getAttribute("cellbgcolor") + ";\""
                                : "";
                        result.add("<" + cellTagName + cellClassAttribute + cellColspanAttribute
                                + cellRowspanAttribute + cellStyleAttribute + ">" + cellContent + "</" + cellTagName
                                + ">");
                    }
                    result.add("</tr>");
                }
                result.add("</t" + tsec + ">");
            }
        }
        result.add("</table>");
        return String.join(LF, result);
    }

    public String convertToc(AbstractBlock node) {
        Document doc = node.getDocument();
        if (!(doc.isAttribute("toc-placement", "macro") && doc.hasSections() && doc.hasAttribute("toc"))) {
            return "<!-- toc disabled -->";
        }

        String idAttr;
        String titleIdAttr;
        if (node.getId() != null) {
            idAttr = " id=\"" + node.getId() + "\"";
            titleIdAttr = " id=\"" + node.getId() + "title\"";
        } else {
            idAttr = " id=\"toc\"";
            titleIdAttr = " id=\"toctitle\"";
        }
        Object title = node.hasTitle() ? node.getTitle() : doc.getAttribute("toc-title");
        Integer levels = node.hasAttribute("levels") ? Strings.toInt(node.getAttribute("levels")) : null;
        Object role = node.isRole() ? node.getRole() : doc.getAttribute("toc-class", "toc");

        Map<String, Object> outlineOpts = new LinkedHashMap<>();
        outlineOpts.put("toclevels", levels);
        return "<div" + idAttr + " class=\"" + role + "\">\n"
                + "<div" + titleIdAttr + " class=\"title\">" + title + "</div>\n"
                + Converter.asString(doc.getConverter().convert(doc, "outline", outlineOpts)) + "\n"
                + "</div>";
    }

    public String convertUlist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        String idAttribute = idAttribute(node);
        List<String> divClasses = new ArrayList<>();
        divClasses.add("ulist");
        if (node.getStyle() != null) {
            divClasses.add(node.getStyle());
        }
        if (node.getRole() != null) {
            divClasses.add(node.getRole());
        }
        String markerChecked = "";
        String markerUnchecked = "";
        String ulClassAttribute;
        boolean checklist = node.isOption("checklist");
        if (checklist) {
            divClasses.add(1, "checklist");
            ulClassAttribute = " class=\"checklist\"";
            if (node.isOption("interactive")) {
                if (xmlMode) {
                    markerChecked = "<input type=\"checkbox\" data-item-complete=\"1\" checked=\"checked\"/> ";
                    markerUnchecked = "<input type=\"checkbox\" data-item-complete=\"0\"/> ";
                } else {
                    markerChecked = "<input type=\"checkbox\" data-item-complete=\"1\" checked> ";
                    markerUnchecked = "<input type=\"checkbox\" data-item-complete=\"0\"> ";
                }
            } else if (node.getDocument().isAttribute("icons", "font")) {
                markerChecked = "<i class=\"fa fa-check-square-o\"></i> ";
                markerUnchecked = "<i class=\"fa fa-square-o\"></i> ";
            } else {
                markerChecked = "&#10003; ";
                markerUnchecked = "&#10063; ";
            }
        } else {
            ulClassAttribute = node.getStyle() != null ? " class=\"" + node.getStyle() + "\"" : "";
        }
        result.add("<div" + idAttribute + " class=\"" + String.join(" ", divClasses) + "\">");
        if (node.hasTitle()) {
            result.add("<div class=\"title\">" + node.getTitle() + "</div>");
        }
        result.add("<ul" + ulClassAttribute + ">");

        for (AbstractBlock block : node.getItems()) {
            ListItem item = (ListItem) block;
            if (item.getId() != null) {
                result.add("<li id=\"" + item.getId() + "\"" + (item.getRole() != null ? " class=\"" + item.getRole()
                        + "\"" : "") + ">");
            } else if (item.getRole() != null) {
                result.add("<li class=\"" + item.getRole() + "\">");
            } else {
                result.add("<li>");
            }
            if (checklist && item.hasAttribute("checkbox")) {
                result.add("<p>" + (item.hasAttribute("checked") ? markerChecked : markerUnchecked) + item.getText()
                        + "</p>");
            } else {
                result.add("<p>" + item.getText() + "</p>");
            }
            if (item.hasBlocks()) {
                result.add(item.getContent());
            }
            result.add("</li>");
        }

        result.add("</ul>");
        result.add("</div>");
        return String.join(LF, result);
    }

    public String convertVerse(AbstractBlock node) {
        String idAttribute = idAttribute(node);
        String classAttribute = " class=\"" + joinNonNull(" ", "verseblock", node.getRole()) + "\"";
        String titleElement = node.hasTitle() ? "\n<div class=\"title\">" + node.getTitle() + "</div>" : "";
        Object attribution = node.hasAttribute("attribution") ? node.getAttribute("attribution") : null;
        Object citetitle = node.hasAttribute("citetitle") ? node.getAttribute("citetitle") : null;
        String attributionElement;
        if (attribution != null || citetitle != null) {
            String citeElement = citetitle != null ? "<cite>" + citetitle + "</cite>" : "";
            String attributionText = attribution != null ? "&#8212; " + attribution + (citetitle != null ? "<br"
                    + voidElementSlash + ">\n" : "") : "";
            attributionElement = "\n<div class=\"attribution\">\n" + attributionText + citeElement + "\n</div>";
        } else {
            attributionElement = "";
        }

        return "<div" + idAttribute + classAttribute + ">" + titleElement + "\n"
                + "<pre class=\"content\">" + node.getContent() + "</pre>" + attributionElement + "\n"
                + "</div>";
    }

    public String convertVideo(AbstractBlock node) {
        boolean xml = xmlMode;
        String idAttribute = idAttribute(node);
        List<String> classes = new ArrayList<>();
        classes.add("videoblock");
        if (node.hasAttribute("float")) {
            classes.add(node.getStringAttribute("float"));
        }
        if (node.hasAttribute("align")) {
            classes.add("text-" + node.getAttribute("align"));
        }
        if (node.getRole() != null) {
            classes.add(node.getRole());
        }
        String classAttribute = " class=\"" + String.join(" ", classes) + "\"";
        String titleElement = node.hasTitle() ? "\n<div class=\"title\">" + node.getTitle() + "</div>" : "";
        String widthAttribute = node.hasAttribute("width") ? " width=\"" + node.getAttribute("width") + "\"" : "";
        String heightAttribute = node.hasAttribute("height") ? " height=\"" + node.getAttribute("height") + "\"" : "";
        String poster = node.getStringAttribute("poster");
        switch (poster == null ? "" : poster) {
            case "vimeo" -> {
                String assetUriScheme = node.getDocument().getStringAttribute("asset-uri-scheme", "https");
                if (!assetUriScheme.isEmpty()) {
                    assetUriScheme = assetUriScheme + ":";
                }
                String startAnchor = node.hasAttribute("start") ? "#at=" + node.getAttribute("start") : "";
                List<String> delimiter = new ArrayList<>(List.of("?"));
                List<String> parts = Strings.split(node.getStringAttribute("target"), "/", 2);
                String target = parts.isEmpty() ? "" : parts.get(0);
                String hash = parts.size() > 1 ? parts.get(1) : null;
                if (hash == null) {
                    hash = node.getStringAttribute("hash");
                }
                String hashParam = hash != null ? popDelimiter(delimiter) + "h=" + hash : "";
                String autoplayParam = node.isOption("autoplay") ? popDelimiter(delimiter) + "autoplay=1" : "";
                String loopParam = node.isOption("loop") ? popDelimiter(delimiter) + "loop=1" : "";
                String mutedParam = node.isOption("muted") ? popDelimiter(delimiter) + "muted=1" : "";
                return "<div" + idAttribute + classAttribute + ">" + titleElement + "\n"
                        + "<div class=\"content\">\n"
                        + "<iframe" + widthAttribute + heightAttribute + " src=\"" + assetUriScheme
                        + "//player.vimeo.com/video/" + target + hashParam + autoplayParam + loopParam + mutedParam
                        + startAnchor + "\" frameborder=\"0\"" + (node.isOption("nofullscreen") ? ""
                                : appendBooleanAttribute("allowfullscreen", xml)) + "></iframe>\n"
                        + "</div>\n"
                        + "</div>";
            }
            case "youtube" -> {
                String assetUriScheme = node.getDocument().getStringAttribute("asset-uri-scheme", "https");
                if (!assetUriScheme.isEmpty()) {
                    assetUriScheme = assetUriScheme + ":";
                }
                int relParamVal = node.isOption("related") ? 1 : 0;
                // NOTE start and end must be seconds (t parameter allows XmYs where X is minutes and Y is seconds)
                String startParam = node.hasAttribute("start") ? "&amp;start=" + node.getAttribute("start") : "";
                String endParam = node.hasAttribute("end") ? "&amp;end=" + node.getAttribute("end") : "";
                String autoplayParam = node.isOption("autoplay") ? "&amp;autoplay=1" : "";
                boolean hasLoopParam = node.isOption("loop");
                String loopParam = hasLoopParam ? "&amp;loop=1" : "";
                String muteParam = node.isOption("muted") ? "&amp;mute=1" : "";
                String controlsParam = node.isOption("nocontrols") ? "&amp;controls=0" : "";
                // cover both ways of controlling fullscreen option
                String fsParam;
                String fsAttribute;
                if (node.isOption("nofullscreen")) {
                    fsParam = "&amp;fs=0";
                    fsAttribute = "";
                } else {
                    fsParam = "";
                    fsAttribute = appendBooleanAttribute("allowfullscreen", xml);
                }
                String modestParam = node.isOption("modest") ? "&amp;modestbranding=1" : "";
                String themeParam = node.hasAttribute("theme") ? "&amp;theme=" + node.getAttribute("theme") : "";
                String hlParam = node.hasAttribute("lang") ? "&amp;hl=" + node.getAttribute("lang") : "";

                // parse video_id/list_id syntax where list_id (i.e., playlist) is optional
                List<String> parts = Strings.split(node.getStringAttribute("target"), "/", 2);
                String target = parts.isEmpty() ? "" : parts.get(0);
                String list = parts.size() > 1 ? parts.get(1) : null;
                if (list == null) {
                    list = node.getStringAttribute("list");
                }
                String listParam;
                if (list != null) {
                    listParam = "&amp;list=" + list;
                } else {
                    // parse dynamic playlist syntax: video_id1,video_id2,...
                    List<String> playlistParts = Strings.split(target, ",", 2);
                    target = playlistParts.isEmpty() ? "" : playlistParts.get(0);
                    String playlist = playlistParts.size() > 1 ? playlistParts.get(1) : null;
                    if (playlist == null) {
                        playlist = node.getStringAttribute("playlist");
                    }
                    if (playlist != null) {
                        // INFO playlist bar doesn't appear in Firefox unless showinfo=1 and modestbranding=1
                        listParam = "&amp;playlist=" + target + "," + playlist;
                    } else {
                        // NOTE for loop to work, playlist must be specified; use VIDEO_ID if there's no explicit
                        // playlist
                        listParam = hasLoopParam ? "&amp;playlist=" + target : "";
                    }
                }

                return "<div" + idAttribute + classAttribute + ">" + titleElement + "\n"
                        + "<div class=\"content\">\n"
                        + "<iframe" + widthAttribute + heightAttribute + " src=\"" + assetUriScheme
                        + "//www.youtube.com/embed/" + target + "?rel=" + relParamVal + startParam + endParam
                        + autoplayParam + loopParam + muteParam + controlsParam + listParam + fsParam + modestParam
                        + themeParam + hlParam + "\" frameborder=\"0\"" + fsAttribute + "></iframe>\n"
                        + "</div>\n"
                        + "</div>";
            }
            case "wistia" -> {
                String assetUriScheme = node.getDocument().getStringAttribute("asset-uri-scheme", "https");
                if (!assetUriScheme.isEmpty()) {
                    assetUriScheme = assetUriScheme + ":";
                }
                List<String> delimiter = new ArrayList<>(List.of("?"));
                String startAnchor = node.hasAttribute("start") ? popDelimiter(delimiter) + "time="
                        + node.getAttribute("start") : "";
                String endVideoBehaviorParam = node.isOption("loop") ? popDelimiter(delimiter)
                        + "endVideoBehavior=loop" : (node.isOption("reset") ? popDelimiter(delimiter)
                                + "endVideoBehavior=reset" : "");
                String target = node.getStringAttribute("target");
                String autoplayParam = node.isOption("autoplay") ? popDelimiter(delimiter) + "autoPlay=true" : "";
                String mutedParam = node.isOption("muted") ? popDelimiter(delimiter) + "muted=true" : "";
                return "<div" + idAttribute + classAttribute + ">" + titleElement + "\n"
                        + "<div class=\"content\">\n"
                        + "<iframe" + widthAttribute + heightAttribute + " src=\"" + assetUriScheme
                        + "//fast.wistia.com/embed/iframe/" + target + startAnchor + autoplayParam
                        + endVideoBehaviorParam + mutedParam + "\" frameborder=\"0\"" + (node.isOption("nofullscreen")
                                ? "" : appendBooleanAttribute("allowfullscreen", xml))
                        + " class=\"wistia_embed\" name=\"wistia_embed\"></iframe>\n"
                        + "</div>\n"
                        + "</div>";
            }
            default -> {
                String val = node.getStringAttribute("poster");
                String posterAttribute = Strings.isNullOrEmpty(val) ? "" : " poster=\"" + node.mediaUri(val) + "\"";
                val = node.getStringAttribute("preload");
                String preloadAttribute = Strings.isNullOrEmpty(val) ? "" : " preload=\"" + val + "\"";
                Object startT = node.getAttribute("start");
                Object endT = node.getAttribute("end");
                String timeAnchor = startT != null || endT != null
                        ? "#t=" + (startT == null ? "" : startT) + (endT != null ? "," + endT : "") : "";
                return "<div" + idAttribute + classAttribute + ">" + titleElement + "\n"
                        + "<div class=\"content\">\n"
                        + "<video src=\"" + node.mediaUri(node.getStringAttribute("target")) + timeAnchor + "\""
                        + widthAttribute + heightAttribute + posterAttribute
                        + (node.isOption("autoplay") ? appendBooleanAttribute("autoplay", xml) : "")
                        + (node.isOption("muted") ? appendBooleanAttribute("muted", xml) : "")
                        + (node.isOption("nocontrols") ? "" : appendBooleanAttribute("controls", xml))
                        + (node.isOption("loop") ? appendBooleanAttribute("loop", xml) : "") + preloadAttribute + ">\n"
                        + "Your browser does not support the video tag.\n"
                        + "</video>\n"
                        + "</div>\n"
                        + "</div>";
            }
        }
    }

    private static String popDelimiter(List<String> delimiter) {
        return delimiter.isEmpty() ? "&amp;" : delimiter.remove(delimiter.size() - 1);
    }

    public String convertInlineAnchor(Inline node) {
        switch (Strings.valueOf(node.getType()) == null ? "" : node.getType()) {
            case "xref" -> {
                String attrs;
                String text;
                Object path = node.getAttributes().get("path");
                if (path != null) {
                    List<String> attrList = new ArrayList<>();
                    if (node.getRole() != null) {
                        attrList.add(" class=\"" + node.getRole() + "\"");
                    }
                    attrs = String.join("", appendLinkConstraintAttrs(node, attrList));
                    text = node.getText() != null ? node.getText() : path.toString();
                } else {
                    attrs = node.getRole() != null ? " class=\"" + node.getRole() + "\"" : "";
                    text = node.getText();
                    if (text == null) {
                        if (refs == null) {
                            refs = node.getDocument().getCatalog().getRefs();
                        }
                        String refid = Strings.valueOf(node.getAttributes().get("refid"));
                        AbstractNode ref = refid == null ? null : refs.get(refid);
                        Document top = null;
                        if (ref == null && Strings.isNullOrEmpty(refid)) {
                            ref = top = getRootDocument(node);
                        }
                        if (ref != null) {
                            if (!resolvingXref) {
                                resolvingXref = true;
                                try {
                                    text = ref.getXreftext(node.getStringAttribute("xrefstyle", null, "xrefstyle"));
                                    if (text != null) {
                                        if (text.contains("<a")) {
                                            text = Rx.gsub(DROP_ANCHOR_RX, text, "");
                                        }
                                    } else {
                                        text = top != null ? "[^top]" : "[" + refid + "]";
                                    }
                                } finally {
                                    resolvingXref = false;
                                }
                            } else {
                                text = top != null ? "[^top]" : "[" + refid + "]";
                            }
                        } else {
                            text = "[" + Strings.valueOf(refid) + "]";
                        }
                    }
                }
                return "<a href=\"" + node.getTarget() + "\"" + attrs + ">" + text + "</a>";
            }
            case "ref" -> {
                return "<a id=\"" + node.getId() + "\"></a>";
            }
            case "link" -> {
                List<String> attrs = new ArrayList<>();
                if (node.getId() != null) {
                    attrs.add(" id=\"" + node.getId() + "\"");
                }
                if (node.getRole() != null) {
                    attrs.add(" class=\"" + node.getRole() + "\"");
                }
                if (node.hasAttribute("title")) {
                    attrs.add(" title=\"" + node.getAttribute("title") + "\"");
                }
                return "<a href=\"" + node.getTarget() + "\"" + String.join("", appendLinkConstraintAttrs(node, attrs))
                        + ">" + node.getText() + "</a>";
            }
            case "bibref" -> {
                String reftext = node.getReftext();
                return "<a id=\"" + node.getId() + "\"></a>[" + (reftext != null ? reftext : node.getId()) + "]";
            }
            default -> {
                getLogger().warn("unknown anchor type: " + Strings.inspect(node.getType()));
                return null;
            }
        }
    }

    public String convertInlineBreak(Inline node) {
        return node.getText() + "<br" + voidElementSlash + ">";
    }

    public String convertInlineButton(Inline node) {
        return "<b class=\"button\">" + node.getText() + "</b>";
    }

    public String convertInlineCallout(Inline node) {
        if (node.getDocument().isAttribute("icons", "font")) {
            return "<i class=\"conum\" data-value=\"" + node.getText() + "\"></i><b>(" + node.getText() + ")</b>";
        } else if (node.getDocument().hasAttribute("icons")) {
            String src = node.iconUri("callouts/" + node.getText());
            return "<img src=\"" + src + "\" alt=\"" + node.getText() + "\"" + voidElementSlash + ">";
        }
        Object guard = node.getAttributes().get("guard");
        if (guard instanceof List<?>) {
            return "&lt;!--<b class=\"conum\">(" + node.getText() + ")</b>--&gt;";
        }
        return (guard == null ? "" : guard) + "<b class=\"conum\">(" + node.getText() + ")</b>";
    }

    public String convertInlineFootnote(Inline node) {
        Object index = node.getAttribute("index");
        if (index != null) {
            if ("xref".equals(node.getType())) {
                return "<sup class=\"footnoteref\">[<a class=\"footnote\" href=\"#_footnotedef_" + index
                        + "\" title=\"View footnote.\">" + index + "</a>]</sup>";
            }
            String idAttr = node.getId() != null ? " id=\"_footnote_" + node.getId() + "\"" : "";
            return "<sup class=\"footnote\"" + idAttr + ">[<a id=\"_footnoteref_" + index
                    + "\" class=\"footnote\" href=\"#_footnotedef_" + index + "\" title=\"View footnote.\">" + index
                    + "</a>]</sup>";
        } else if ("xref".equals(node.getType())) {
            return "<sup class=\"footnoteref red\" title=\"Unresolved footnote reference.\">[" + node.getText()
                    + "]</sup>";
        }
        return null;
    }

    public String convertInlineImage(Inline node) {
        String target = node.getTarget();
        String type = node.getType() != null ? node.getType() : "image";
        String img;
        String src = null;
        if (type.equals("icon")) {
            Object icons = node.getDocument().getAttribute("icons");
            if ("font".equals(icons)) {
                String iClassAttrVal = "fa fa-" + target;
                if (node.hasAttribute("size")) {
                    iClassAttrVal = iClassAttrVal + " fa-" + node.getAttribute("size");
                }
                if (node.hasAttribute("flip")) {
                    iClassAttrVal = iClassAttrVal + " fa-flip-" + node.getAttribute("flip");
                } else if (node.hasAttribute("rotate")) {
                    iClassAttrVal = iClassAttrVal + " fa-rotate-" + node.getAttribute("rotate");
                }
                String attrs = node.hasAttribute("title") ? " title=\"" + node.getAttribute("title") + "\"" : "";
                img = "<i class=\"" + iClassAttrVal + "\"" + attrs + "></i>";
            } else if (icons != null) {
                String attrs = node.hasAttribute("width") ? " width=\"" + node.getAttribute("width") + "\"" : "";
                if (node.hasAttribute("height")) {
                    attrs = attrs + " height=\"" + node.getAttribute("height") + "\"";
                }
                if (node.hasAttribute("title")) {
                    attrs = attrs + " title=\"" + node.getAttribute("title") + "\"";
                }
                img = "<img src=\"" + (src = node.iconUri(target)) + "\" alt=\"" + encodeAttributeValue(node.getAlt())
                        + "\"" + attrs + voidElementSlash + ">";
            } else {
                img = "[" + node.getAlt() + "&#93;";
            }
        } else {
            String attrs = node.hasAttribute("width") ? " width=\"" + node.getAttribute("width") + "\"" : "";
            if (node.hasAttribute("height")) {
                attrs = attrs + " height=\"" + node.getAttribute("height") + "\"";
            }
            if (node.hasAttribute("title")) {
                attrs = attrs + " title=\"" + node.getAttribute("title") + "\"";
            }
            if ((node.isAttribute("format", "svg") || target.contains(".svg"))
                    && node.getDocument().getSafe() < SafeMode.SECURE.getLevel()) {
                if (node.isOption("inline")) {
                    img = readSvgContents(node, target);
                    if (img == null) {
                        img = "<span class=\"alt\">" + node.getAlt() + "</span>";
                    }
                } else if (node.isOption("interactive")) {
                    String fallback = node.hasAttribute("fallback") ? "<img src=\"" + node.imageUri(
                            node.getStringAttribute("fallback")) + "\" alt=\"" + encodeAttributeValue(node.getAlt())
                            + "\"" + attrs + voidElementSlash + ">" : "<span class=\"alt\">" + node.getAlt()
                                    + "</span>";
                    img = "<object type=\"image/svg+xml\" data=\"" + (src = node.imageUri(target)) + "\"" + attrs
                            + ">" + fallback + "</object>";
                } else {
                    img = "<img src=\"" + (src = node.imageUri(target)) + "\" alt=\""
                            + encodeAttributeValue(node.getAlt()) + "\"" + attrs + voidElementSlash + ">";
                }
            } else {
                img = "<img src=\"" + (src = node.imageUri(target)) + "\" alt=\"" + encodeAttributeValue(node.getAlt())
                        + "\"" + attrs + voidElementSlash + ">";
            }
        }
        String hrefAttrVal;
        if (node.hasAttribute("link") && (!"self".equals(hrefAttrVal = node.getStringAttribute("link"))
                || (hrefAttrVal = src) != null)) {
            img = "<a class=\"image\" href=\"" + hrefAttrVal + "\"" + String.join("",
                    appendLinkConstraintAttrs(node, new ArrayList<>())) + ">" + img + "</a>";
        }
        String idAttr = node.getId() != null ? " id=\"" + node.getId() + "\"" : "";
        String classAttrVal = type;
        String role = node.getRole();
        if (role != null) {
            classAttrVal = node.hasAttribute("float") ? classAttrVal + " " + node.getAttribute("float") + " " + role
                    : classAttrVal + " " + role;
        } else if (node.hasAttribute("float")) {
            classAttrVal = classAttrVal + " " + node.getAttribute("float");
        }
        return "<span" + idAttr + " class=\"" + classAttrVal + "\">" + img + "</span>";
    }

    public String convertInlineIndexterm(Inline node) {
        return "visible".equals(node.getType()) ? node.getText() : "";
    }

    public String convertInlineKbd(Inline node) {
        List<?> keys = (List<?>) node.getAttribute("keys");
        if (keys.size() == 1) {
            return "<kbd>" + keys.get(0) + "</kbd>";
        }
        return "<span class=\"keyseq\"><kbd>" + String.join("</kbd>+<kbd>", keys.stream().map(String::valueOf)
                .toList()) + "</kbd></span>";
    }

    public String convertInlineMenu(Inline node) {
        String caret = node.getDocument().isAttribute("icons", "font")
                ? "&#160;<i class=\"fa fa-angle-right caret\"></i> " : "&#160;<b class=\"caret\">&#8250;</b> ";
        String submenuJoiner = "</b>" + caret + "<b class=\"submenu\">";
        Object menu = node.getAttribute("menu");
        List<?> submenus = (List<?>) node.getAttribute("submenus");
        if (submenus == null || submenus.isEmpty()) {
            Object menuitem = node.getAttribute("menuitem");
            if (menuitem != null) {
                return "<span class=\"menuseq\"><b class=\"menu\">" + menu + "</b>" + caret + "<b class=\"menuitem\">"
                        + menuitem + "</b></span>";
            }
            return "<b class=\"menuref\">" + menu + "</b>";
        }
        return "<span class=\"menuseq\"><b class=\"menu\">" + menu + "</b>" + caret + "<b class=\"submenu\">"
                + String.join(submenuJoiner, submenus.stream().map(String::valueOf).toList()) + "</b>" + caret
                + "<b class=\"menuitem\">" + node.getAttribute("menuitem") + "</b></span>";
    }

    public String convertInlineQuoted(Inline node) {
        QuoteTag quoteTag = node.getType() == null ? DEFAULT_QUOTE_TAG
                : QUOTE_TAGS.getOrDefault(node.getType(), DEFAULT_QUOTE_TAG);
        String open = quoteTag.open();
        String close = quoteTag.close();
        if (node.getId() != null) {
            String classAttr = node.getRole() != null ? " class=\"" + node.getRole() + "\"" : "";
            if (quoteTag.tag()) {
                return Strings.chop(open) + " id=\"" + node.getId() + "\"" + classAttr + ">" + node.getText() + close;
            }
            return "<span id=\"" + node.getId() + "\"" + classAttr + ">" + open + node.getText() + close + "</span>";
        } else if (node.getRole() != null) {
            if (quoteTag.tag()) {
                return Strings.chop(open) + " class=\"" + node.getRole() + "\">" + node.getText() + close;
            }
            return "<span class=\"" + node.getRole() + "\">" + open + node.getText() + close + "</span>";
        }
        return open + node.getText() + close;
    }

    /**
     * Reads the contents of the SVG file to embed it inline.
     *
     * <p>NOTE exposed for use by other converters.
     */
    public String readSvgContents(AbstractNode node, String target) {
        Map<String, Object> readOpts = new LinkedHashMap<>();
        readOpts.put("start", node.getDocument().getStringAttribute("imagesdir"));
        readOpts.put("normalize", true);
        readOpts.put("label", "SVG");
        readOpts.put("warn_if_empty", true);
        String svg = node.readContents(target, readOpts);
        if (svg != null) {
            if (svg.isEmpty()) {
                return null;
            }
            if (!svg.startsWith("<svg")) {
                svg = Rx.sub(SVG_PREAMBLE_RX, svg, "");
            }
            String oldStartTag = null;
            String newStartTag = null;
            boolean startTagMatched = false;
            boolean noMatch = false;
            // NOTE width, height and style attributes are removed if either width or height is specified
            for (String dim : List.of("width", "height")) {
                if (!node.hasAttribute(dim)) {
                    continue;
                }
                if (newStartTag == null) {
                    if (noMatch) {
                        continue;
                    }
                    if (!startTagMatched) {
                        startTagMatched = true;
                        Matcher m = Rx.match(SVG_START_TAG_RX, svg);
                        if (m == null) {
                            noMatch = true;
                            continue;
                        }
                        oldStartTag = m.group();
                    }
                    newStartTag = Rx.gsub(DIMENSION_ATTRIBUTE_RX, oldStartTag, "");
                }
                // NOTE a unitless value in HTML is assumed to be px, so we can pass the value straight through
                newStartTag = Strings.chop(newStartTag) + " " + dim + "=\"" + node.getAttribute(dim) + "\">";
            }
            if (newStartTag != null) {
                svg = newStartTag + svg.substring(oldStartTag.length());
            }
        }
        return svg;
    }

    private static String appendBooleanAttribute(String name, boolean xml) {
        return xml ? " " + name + "=\"" + name + "\"" : " " + name;
    }

    private static List<String> appendLinkConstraintAttrs(AbstractNode node, List<String> attrs) {
        String rel = node.isOption("nofollow") ? "nofollow" : null;
        Object window = node.getAttributes().get("window");
        if (window != null) {
            attrs.add(" target=\"" + window + "\"");
            if ("_blank".equals(window) || node.isOption("noopener")) {
                attrs.add(rel != null ? " rel=\"" + rel + " noopener\"" : " rel=\"noopener\"");
            }
        } else if (rel != null) {
            attrs.add(" rel=\"" + rel + "\"");
        }
        return attrs;
    }

    private static String encodeAttributeValue(String val) {
        return val.contains("\"") ? val.replace("\"", "&quot;") : val;
    }

    private static String generateMannameSection(Document node) {
        Object mannameTitle = node.getAttribute("manname-title", "Name");
        List<Section> sections = node.getSections();
        Section nextSection = sections.isEmpty() ? null : sections.get(0);
        String nextSectionTitle;
        if (nextSection != null && (nextSectionTitle = nextSection.getTitle()) != null
                && nextSectionTitle.equals(nextSectionTitle.toUpperCase())) {
            mannameTitle = mannameTitle.toString().toUpperCase();
        }
        Object mannameId = node.getAttribute("manname-id");
        String mannameIdAttr = mannameId != null ? " id=\"" + mannameId + "\"" : "";
        List<?> mannames = (List<?>) node.getAttribute("mannames");
        return "<h2" + mannameIdAttr + ">" + mannameTitle + "</h2>\n"
                + "<div class=\"sectionbody\">\n"
                + "<p>" + String.join(", ", mannames.stream().map(String::valueOf).toList()) + " - "
                + node.getAttribute("manpurpose") + "</p>\n"
                + "</div>";
    }

    private static Document getRootDocument(AbstractNode node) {
        Document doc = node.getDocument();
        while (doc.isNested()) {
            doc = doc.getParentDocument();
        }
        return doc;
    }
}
