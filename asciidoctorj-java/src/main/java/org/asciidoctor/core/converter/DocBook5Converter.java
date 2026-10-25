package org.asciidoctor.core.converter;

import static org.asciidoctor.core.Asciidoctor.LF;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.AbstractNode;
import org.asciidoctor.core.Asciidoctor;
import org.asciidoctor.core.AttributeList;
import org.asciidoctor.core.ContentModel;
import org.asciidoctor.core.DescriptionListEntry;
import org.asciidoctor.core.Document;
import org.asciidoctor.core.Document.Author;
import org.asciidoctor.core.Inline;
import org.asciidoctor.core.ListItem;
import org.asciidoctor.core.Rx;
import org.asciidoctor.core.Section;
import org.asciidoctor.core.Strings;
import org.asciidoctor.core.Substitution;
import org.asciidoctor.core.Table;

/**
 * A built-in {@link Converter} implementation that generates DocBook 5 output. The output is inspired by the output
 * produced by the docbook45 backend from AsciiDoc.py, except it has been migrated to the DocBook 5 specification.
 */
public class DocBook5Converter extends AbstractConverter<String> {

    /** The tags used to convert a description list. */
    private record DlistTags(String list, String entry, String label, String term, String item) {
    }

    // default represents variablelist
    private static final Map<String, DlistTags> DLIST_TAGS = Map.of(
            "qanda", new DlistTags("qandaset", "qandaentry", "question", "simpara", "answer"),
            "glossary", new DlistTags(null, "glossentry", null, "glossterm", "glossdef"));

    private static final DlistTags DEFAULT_DLIST_TAGS = new DlistTags("variablelist", "varlistentry", null, "term",
            "listitem");

    /** The open tag, close tag and whether the tag supports a nested phrase. */
    private record QuoteTag(String open, String close, boolean supportsPhrase) {
    }

    private static final Map<String, QuoteTag> QUOTE_TAGS = Map.of(
            "monospaced", new QuoteTag("<literal>", "</literal>", false),
            "emphasis", new QuoteTag("<emphasis>", "</emphasis>", true),
            "strong", new QuoteTag("<emphasis role=\"strong\">", "</emphasis>", true),
            "double", new QuoteTag("<quote role=\"double\">", "</quote>", true),
            "single", new QuoteTag("<quote role=\"single\">", "</quote>", true),
            "mark", new QuoteTag("<emphasis role=\"marked\">", "</emphasis>", false),
            "superscript", new QuoteTag("<superscript>", "</superscript>", false),
            "subscript", new QuoteTag("<subscript>", "</subscript>", false));

    private static final QuoteTag DEFAULT_QUOTE_TAG = new QuoteTag("", "", true);

    private static final Map<String, String> MANPAGE_SECTION_TAGS = Map.of(
            "section", "refsection",
            "synopsis", "refsynopsisdiv");

    private static final List<String> TABLE_PI_NAMES = List.of("dbhtml", "dbfo", "dblatex");

    private static final Pattern COPYRIGHT_RX = Rx.compile("^(" + Asciidoctor.CC_ANY + "+?)(?: ((?:\\d{4}-)?\\d{4}))?$");
    private static final Pattern IMAGE_MACRO_RX = Rx.compile("^image::?(\\S|\\S" + Asciidoctor.CC_ANY + "*?\\S)\\[("
            + Asciidoctor.CC_ANY + "+)?\\]$");

    private Boolean asciimathAvailable;

    public DocBook5Converter(String backend, Map<String, Object> opts) {
        super(backend, opts);
        initBackendTraits("docbook", "xml", null, ".xml", true);
        registerHandlers();
    }

    public DocBook5Converter(String backend) {
        this(backend, Map.of());
    }

    private void registerHandlers() {
        handle("document", Document.class, this::convertDocument);
        handle("embedded", Document.class, this::convertEmbedded);
        handle("section", Section.class, this::convertSection);
        handle("admonition", AbstractBlock.class, this::convertAdmonition);
        handle("audio", AbstractBlock.class, this::skip);
        handle("colist", org.asciidoctor.core.List.class, this::convertColist);
        handle("dlist", org.asciidoctor.core.List.class, this::convertDlist);
        handle("example", AbstractBlock.class, this::convertExample);
        handle("floating_title", AbstractBlock.class, this::convertFloatingTitle);
        handle("image", AbstractBlock.class, this::convertImage);
        handle("listing", AbstractBlock.class, this::convertListing);
        handle("literal", AbstractBlock.class, this::convertLiteral);
        handle("pass", AbstractBlock.class, this::contentOnly);
        handle("stem", AbstractBlock.class, this::convertStem);
        handle("olist", org.asciidoctor.core.List.class, this::convertOlist);
        handle("open", AbstractBlock.class, this::convertOpen);
        handle("page_break", AbstractBlock.class, this::convertPageBreak);
        handle("paragraph", AbstractBlock.class, this::convertParagraph);
        handle("preamble", AbstractBlock.class, this::convertPreamble);
        handle("quote", AbstractBlock.class, this::convertQuote);
        handle("thematic_break", AbstractBlock.class, this::convertThematicBreak);
        handle("sidebar", AbstractBlock.class, this::convertSidebar);
        handle("table", Table.class, this::convertTable);
        handle("toc", AbstractBlock.class, this::skip);
        handle("ulist", org.asciidoctor.core.List.class, this::convertUlist);
        handle("verse", AbstractBlock.class, this::convertVerse);
        handle("video", AbstractBlock.class, this::skip);
        handle("inline_anchor", Inline.class, this::convertInlineAnchor);
        handle("inline_break", Inline.class, this::convertInlineBreak);
        handle("inline_button", Inline.class, this::convertInlineButton);
        handle("inline_callout", Inline.class, this::convertInlineCallout);
        handle("inline_footnote", Inline.class, this::convertInlineFootnote);
        handle("inline_image", Inline.class, this::convertInlineImage);
        handle("inline_indexterm", Inline.class, this::convertInlineIndexterm);
        handle("inline_kbd", Inline.class, this::convertInlineKbd);
        handle("inline_menu", Inline.class, this::convertInlineMenu);
        handle("inline_quoted", Inline.class, this::convertInlineQuoted);
    }

    public String convertDocument(Document node) {
        List<String> result = new ArrayList<>();
        result.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        if (node.hasAttribute("toc")) {
            result.add(node.hasAttribute("toclevels") ? "<?asciidoc-toc maxdepth=\"" + node.getAttribute("toclevels")
                    + "\"?>" : "<?asciidoc-toc?>");
        }
        if (node.hasAttribute("sectnums")) {
            result.add(node.hasAttribute("sectnumlevels") ? "<?asciidoc-numbered maxdepth=\""
                    + node.getAttribute("sectnumlevels") + "\"?>" : "<?asciidoc-numbered?>");
        }
        String langAttribute = node.hasAttribute("nolang") ? "" : " xml:lang=\"" + node.getAttribute("lang", "en")
                + "\"";
        String rootTagName = node.getDoctype();
        boolean manpage = false;
        if ("manpage".equals(rootTagName)) {
            manpage = true;
            rootTagName = "article";
        }
        int rootTagIdx = result.size();
        String id = node.getId();
        AbstractBlock abstractBlock = findRootAbstract(node);
        if (!node.isNoheader()) {
            result.add(documentInfoTag(node, abstractBlock));
        }
        if (manpage) {
            result.add("<refentry>");
            result.add("<refmeta>");
            if (node.hasAttribute("mantitle")) {
                result.add("<refentrytitle>" + node.applyReftextSubs(node.getStringAttribute("mantitle"))
                        + "</refentrytitle>");
            }
            if (node.hasAttribute("manvolnum")) {
                result.add("<manvolnum>" + node.getAttribute("manvolnum") + "</manvolnum>");
            }
            result.add("<refmiscinfo class=\"source\">" + node.getAttribute("mansource", "&#160;")
                    + "</refmiscinfo>");
            result.add("<refmiscinfo class=\"manual\">" + node.getAttribute("manmanual", "&#160;")
                    + "</refmiscinfo>");
            result.add("</refmeta>");
            result.add("<refnamediv>");
            if (node.hasAttribute("mannames")) {
                for (Object n : (List<?>) node.getAttribute("mannames")) {
                    result.add("<refname>" + n + "</refname>");
                }
            }
            if (node.hasAttribute("manpurpose")) {
                result.add("<refpurpose>" + node.getAttribute("manpurpose") + "</refpurpose>");
            }
            result.add("</refnamediv>");
        }
        String docinfoContent = node.getDocinfo("header");
        if (!docinfoContent.isEmpty()) {
            result.add(docinfoContent);
        }
        AbstractBlock extracted = abstractBlock != null ? extractAbstract(node, abstractBlock) : null;
        if (node.hasBlocks()) {
            result.add(convertBlocks(node));
        }
        if (extracted != null) {
            restoreAbstract(extracted);
        }
        if (!(docinfoContent = node.getDocinfo("footer")).isEmpty()) {
            result.add(docinfoContent);
        }
        if (manpage) {
            result.add("</refentry>");
        }
        if (id == null) {
            id = node.getId();
            node.setId(null);
        }
        // defer adding root tag in case document ID is auto-generated on demand
        result.add(rootTagIdx, "<" + rootTagName + " xmlns=\"http://docbook.org/ns/docbook\" "
                + "xmlns:xl=\"http://www.w3.org/1999/xlink\" version=\"5.0\"" + langAttribute + commonAttributes(id,
                        null, null) + ">");
        result.add("</" + rootTagName + ">");
        return String.join(LF, result);
    }

    private static String convertBlocks(AbstractBlock node) {
        List<String> converted = new ArrayList<>();
        for (AbstractBlock block : node.getBlocks()) {
            String output = block.convert();
            if (output != null) {
                converted.add(output);
            }
        }
        return String.join(LF, converted);
    }

    public String convertEmbedded(Document node) {
        // NOTE in DocBook 5, the root abstract must be in the info tag and is thus not part of the body
        AbstractBlock extracted = null;
        AbstractBlock abstractBlock;
        if ("docbook5".equals(backend) && (abstractBlock = findRootAbstract(node)) != null) {
            extracted = extractAbstract(node, abstractBlock);
        }
        String result = convertBlocks(node);
        if (extracted != null) {
            restoreAbstract(extracted);
        }
        return result;
    }

    public String convertSection(Section node) {
        String tagName = node.getSectionName();
        if ("manpage".equals(node.getDocument().getDoctype())) {
            tagName = MANPAGE_SECTION_TAGS.getOrDefault(tagName, tagName);
        }
        String titleEl = node.isSpecial() && (node.isOption("notitle") || node.isOption("untitled")) ? ""
                : "<title>" + node.getTitle() + "</title>\n";
        return "<" + tagName + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                + titleEl + node.getContent() + "\n"
                + "</" + tagName + ">";
    }

    public String convertAdmonition(AbstractBlock node) {
        Object tagName = node.getAttribute("name");
        return "<" + tagName + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                + titleTag(node, true) + encloseContent(node) + "\n"
                + "</" + tagName + ">";
    }

    public String convertColist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        result.add("<calloutlist" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">");
        if (node.hasTitle()) {
            result.add("<title>" + node.getTitle() + "</title>");
        }
        for (AbstractBlock block : node.getItems()) {
            ListItem item = (ListItem) block;
            result.add("<callout arearefs=\"" + Strings.valueOf(item.getAttribute("coids")) + "\">");
            result.add("<para>" + item.getText() + "</para>");
            if (item.hasBlocks()) {
                result.add(item.getContent());
            }
            result.add("</callout>");
        }
        result.add("</calloutlist>");
        return String.join(LF, result);
    }

    public String convertDlist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        if ("horizontal".equals(node.getStyle())) {
            String tagName = node.hasTitle() ? "table" : "informaltable";
            result.add("<" + tagName + commonAttributes(node.getId(), node.getRole(), node.getReftext())
                    + " tabstyle=\"horizontal\" frame=\"none\" colsep=\"0\" rowsep=\"0\">\n"
                    + titleTag(node, true) + "<tgroup cols=\"2\">\n"
                    + "<colspec colwidth=\"" + node.getAttribute("labelwidth", 15) + "*\"/>\n"
                    + "<colspec colwidth=\"" + node.getAttribute("itemwidth", 85) + "*\"/>\n"
                    + "<tbody valign=\"top\">");
            for (AbstractBlock block : node.getItems()) {
                DescriptionListEntry entry = (DescriptionListEntry) block;
                result.add("<row>\n<entry>");
                for (ListItem dt : entry.getTerms()) {
                    result.add("<simpara>" + dt.getText() + "</simpara>");
                }
                result.add("</entry>\n<entry>");
                ListItem dd = entry.getDescription();
                if (dd != null) {
                    if (dd.hasText()) {
                        result.add("<simpara>" + dd.getText() + "</simpara>");
                    }
                    if (dd.hasBlocks()) {
                        result.add(dd.getContent());
                    }
                }
                result.add("</entry>\n</row>");
            }
            result.add("</tbody>\n</tgroup>\n</" + tagName + ">");
        } else {
            DlistTags tags = node.getStyle() == null ? DEFAULT_DLIST_TAGS
                    : DLIST_TAGS.getOrDefault(node.getStyle(), DEFAULT_DLIST_TAGS);
            String listTag = tags.list();
            String entryTag = tags.entry();
            String labelTag = tags.label();
            String termTag = tags.term();
            String itemTag = tags.item();
            if (listTag != null) {
                result.add("<" + listTag + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">");
                if (node.hasTitle()) {
                    result.add("<title>" + node.getTitle() + "</title>");
                }
            }

            for (AbstractBlock block : node.getItems()) {
                DescriptionListEntry entry = (DescriptionListEntry) block;
                result.add("<" + entryTag + ">");
                if (labelTag != null) {
                    result.add("<" + labelTag + ">");
                }
                for (ListItem dt : entry.getTerms()) {
                    result.add("<" + termTag + ">" + dt.getText() + "</" + termTag + ">");
                }
                if (labelTag != null) {
                    result.add("</" + labelTag + ">");
                }
                result.add("<" + itemTag + ">");
                ListItem dd = entry.getDescription();
                if (dd != null) {
                    if (dd.hasText()) {
                        result.add("<simpara>" + dd.getText() + "</simpara>");
                    }
                    if (dd.hasBlocks()) {
                        result.add(dd.getContent());
                    }
                }
                result.add("</" + itemTag + ">");
                result.add("</" + entryTag + ">");
            }

            if (listTag != null) {
                result.add("</" + listTag + ">");
            }
        }

        return String.join(LF, result);
    }

    public String convertExample(AbstractBlock node) {
        if (node.hasTitle()) {
            return "<example" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                    + "<title>" + node.getTitle() + "</title>\n"
                    + encloseContent(node) + "\n"
                    + "</example>";
        }
        return "<informalexample" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                + encloseContent(node) + "\n"
                + "</informalexample>";
    }

    public String convertFloatingTitle(AbstractBlock node) {
        return "<bridgehead" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + " renderas=\"sect"
                + node.getLevel() + "\">" + node.getTitle() + "</bridgehead>";
    }

    public String convertImage(AbstractBlock node) {
        String alignAttribute = node.hasAttribute("align") ? " align=\"" + node.getAttribute("align") + "\"" : "";
        String filerefAttrVal = node.imageUri(node.getStringAttribute("target"));
        String mediaobject = "<mediaobject>\n"
                + "<imageobject>\n"
                + "<imagedata fileref=\"" + filerefAttrVal + "\""
                + imageSizeAttributes(node.getAttributes()) + alignAttribute + "/>\n"
                + "</imageobject>\n"
                + "<textobject><phrase>" + node.getAlt() + "</phrase></textobject>\n"
                + "</mediaobject>";
        String hrefAttrVal;
        if (node.hasAttribute("link") && (!"self".equals(hrefAttrVal = node.getStringAttribute("link"))
                || (hrefAttrVal = filerefAttrVal) != null)) {
            mediaobject = "<link xl:href=\"" + hrefAttrVal + "\">" + mediaobject + "</link>";
        }

        if (node.hasTitle()) {
            return "<figure" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                    + "<title>" + node.getTitle() + "</title>\n"
                    + mediaobject + "\n"
                    + "</figure>";
        }
        return "<informalfigure" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                + mediaobject + "\n"
                + "</informalfigure>";
    }

    public String convertListing(AbstractBlock node) {
        boolean informal = !node.hasTitle();
        String commonAttrs = commonAttributes(node.getId(), node.getRole(), node.getReftext());
        String wrappedContent;
        if ("source".equals(node.getStyle())) {
            Map<String, Object> attrs = node.getAttributes();
            String numberingAttrs;
            if (node.isOption("linenums")) {
                numberingAttrs = attrs.containsKey("start") ? " linenumbering=\"numbered\" startinglinenumber=\""
                        + Strings.toInt(attrs.get("start")) + "\"" : " linenumbering=\"numbered\"";
            } else {
                numberingAttrs = " linenumbering=\"unnumbered\"";
            }
            if (attrs.containsKey("language")) {
                wrappedContent = "<programlisting" + (informal ? commonAttrs : "") + " language=\""
                        + attrs.get("language") + "\"" + numberingAttrs + ">" + node.getContent()
                        + "</programlisting>";
            } else {
                wrappedContent = "<screen" + (informal ? commonAttrs : "") + numberingAttrs + ">" + node.getContent()
                        + "</screen>";
            }
        } else {
            wrappedContent = "<screen" + (informal ? commonAttrs : "") + ">" + node.getContent() + "</screen>";
        }
        return informal ? wrappedContent : "<formalpara" + commonAttrs + ">\n"
                + "<title>" + node.getTitle() + "</title>\n"
                + "<para>\n"
                + wrappedContent + "\n"
                + "</para>\n"
                + "</formalpara>";
    }

    public String convertLiteral(AbstractBlock node) {
        if (node.hasTitle()) {
            return "<formalpara" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                    + "<title>" + node.getTitle() + "</title>\n"
                    + "<para>\n"
                    + "<literallayout class=\"monospaced\">" + node.getContent() + "</literallayout>\n"
                    + "</para>\n"
                    + "</formalpara>";
        }
        return "<literallayout" + commonAttributes(node.getId(), node.getRole(), node.getReftext())
                + " class=\"monospaced\">" + node.getContent() + "</literallayout>";
    }

    public String convertStem(AbstractBlock node) {
        String equation;
        int idx = node.getSubs().indexOf(Substitution.SPECIAL_CHARACTERS);
        if (idx >= 0) {
            node.getSubs().remove(idx);
            equation = node.getContent();
            node.getSubs().add(idx, Substitution.SPECIAL_CHARACTERS);
        } else {
            equation = node.getContent();
        }
        String equationData;
        if ("asciimath".equals(node.getStyle())) {
            // NOTE fop requires jeuclid to process mathml markup
            isAsciimathAvailable();
            equationData = "<mathphrase><![CDATA[" + equation + "]]></mathphrase>";
        } else {
            // unhandled math; pass source to alt and required mathphrase element; dblatex will process alt as LaTeX
            // math
            equationData = "<alt><![CDATA[" + equation + "]]></alt>\n<mathphrase><![CDATA[" + equation
                    + "]]></mathphrase>";
        }
        if (node.hasTitle()) {
            return "<equation" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                    + "<title>" + node.getTitle() + "</title>\n"
                    + equationData + "\n"
                    + "</equation>";
        }
        // WARNING dblatex displays the <informalequation> element inline instead of block as documented (except w/
        // mathml)
        return "<informalequation" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                + equationData + "\n"
                + "</informalequation>";
    }

    public String convertOlist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        String numAttribute = node.getStyle() != null ? " numeration=\"" + node.getStyle() + "\"" : "";
        String startAttribute = node.hasAttribute("start") ? " startingnumber=\"" + node.getAttribute("start") + "\""
                : "";
        result.add("<orderedlist" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + numAttribute
                + startAttribute + ">");
        if (node.hasTitle()) {
            result.add("<title>" + node.getTitle() + "</title>");
        }
        for (AbstractBlock block : node.getItems()) {
            ListItem item = (ListItem) block;
            result.add("<listitem" + commonAttributes(item.getId(), item.getRole(), null) + ">");
            result.add("<simpara>" + item.getText() + "</simpara>");
            if (item.hasBlocks()) {
                result.add(item.getContent());
            }
            result.add("</listitem>");
        }
        result.add("</orderedlist>");
        return String.join(LF, result);
    }

    public String convertOpen(AbstractBlock node) {
        String style = node.getStyle();
        switch (style == null ? "" : style) {
            case "abstract" -> {
                AbstractNode parent = node.getParent();
                if (parent == node.getDocument() && "book".equals(node.getDocument().getDoctype())) {
                    getLogger().warn("abstract block cannot be used in a document without a doctitle when doctype is "
                            + "book. Excluding block content.");
                    return "";
                }
                String result = "<abstract>\n" + titleTag(node, true) + encloseContent(node) + "\n</abstract>";
                if ("docbook5".equals(backend) && !node.isOption("root") && parent instanceof AbstractBlock parentBlock
                        && ("open".equals(parentBlock.getContext()) ? "partintro".equals(parentBlock.getStyle())
                                : "section".equals(parentBlock.getContext()) && parentBlock instanceof Section section
                                        && "partintro".equals(section.getSectionName()))
                        && !parentBlock.getBlocks().isEmpty() && node == parentBlock.getBlocks().get(0)) {
                    result = "<info>\n" + result + "\n</info>";
                }
                return result;
            }
            case "partintro" -> {
                if (node.getLevel() == 0 && "section".equals(node.getParent().getContext())
                        && "book".equals(node.getDocument().getDoctype())) {
                    return "<partintro" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                            + titleTag(node, true) + encloseContent(node) + "\n"
                            + "</partintro>";
                }
                getLogger().error("partintro block can only be used when doctype is book and must be a child of a "
                        + "book part. Excluding block content.");
                return "";
            }
            default -> {
                String id = node.getId();
                String reftext = id != null ? node.getReftext() : null;
                String role = node.getRole();
                if (node.hasTitle()) {
                    String contentSpacer = node.getContentModel() == ContentModel.COMPOUND ? LF : "";
                    return "<formalpara" + commonAttributes(id, role, reftext) + ">\n"
                            + "<title>" + node.getTitle() + "</title>\n"
                            + "<para>" + contentSpacer + node.getContent() + contentSpacer + "</para>\n"
                            + "</formalpara>";
                } else if (id != null || role != null) {
                    if (node.getContentModel() == ContentModel.COMPOUND) {
                        return "<para" + commonAttributes(id, role, reftext) + ">\n"
                                + node.getContent() + "\n"
                                + "</para>";
                    }
                    return "<simpara" + commonAttributes(id, role, reftext) + ">" + node.getContent() + "</simpara>";
                }
                return encloseContent(node);
            }
        }
    }

    public String convertPageBreak(AbstractBlock node) {
        return "<simpara><?asciidoc-pagebreak?></simpara>";
    }

    public String convertParagraph(AbstractBlock node) {
        if (node.hasTitle()) {
            return "<formalpara" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                    + "<title>" + node.getTitle() + "</title>\n"
                    + "<para>" + node.getContent() + "</para>\n"
                    + "</formalpara>";
        }
        return "<simpara" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">" + node.getContent()
                + "</simpara>";
    }

    public String convertPreamble(AbstractBlock node) {
        if ("book".equals(node.getDocument().getDoctype())) {
            return "<preface" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                    + titleTag(node, false) + node.getContent() + "\n"
                    + "</preface>";
        }
        return node.getContent();
    }

    public String convertQuote(AbstractBlock node) {
        return blockquoteTag(node, node.hasRole("epigraph") ? "epigraph" : null, () -> encloseContent(node));
    }

    public String convertThematicBreak(AbstractBlock node) {
        return "<simpara><?asciidoc-hr?></simpara>";
    }

    public String convertSidebar(AbstractBlock node) {
        return "<sidebar" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">\n"
                + titleTag(node, true) + encloseContent(node) + "\n"
                + "</sidebar>";
    }

    public String convertTable(Table node) {
        boolean hasBody = false;
        List<String> result = new ArrayList<>();
        String pgwideAttribute = node.isOption("pgwide") ? " pgwide=\"1\"" : "";
        String frame = node.getStringAttribute("frame", "all", "table-frame");
        if ("ends".equals(frame)) {
            frame = "topbot";
        }
        String grid = node.getStringAttribute("grid", null, "table-grid");
        String tagName = node.hasTitle() ? "table" : "informaltable";
        result.add("<" + tagName + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + pgwideAttribute
                + " frame=\"" + frame + "\" rowsep=\"" + ("none".equals(grid) || "cols".equals(grid) ? 0 : 1)
                + "\" colsep=\"" + ("none".equals(grid) || "rows".equals(grid) ? 0 : 1) + "\""
                + (node.hasAttribute("orientation", "landscape", "table-orientation") ? " orient=\"land\"" : "")
                + ">");
        if (node.isOption("unbreakable")) {
            result.add("<?dbfo keep-together=\"always\"?>");
        } else if (node.isOption("breakable")) {
            result.add("<?dbfo keep-together=\"auto\"?>");
        }
        if (tagName.equals("table")) {
            result.add("<title>" + node.getTitle() + "</title>");
        }
        String colWidthKey;
        Object width = node.hasAttribute("width") ? node.getAttribute("width") : null;
        if (width != null) {
            for (String piName : TABLE_PI_NAMES) {
                result.add("<?" + piName + " table-width=\"" + width + "\"?>");
            }
            colWidthKey = "colabswidth";
        } else {
            colWidthKey = "colpcwidth";
        }
        result.add("<tgroup cols=\"" + node.getAttribute("colcount") + "\">");
        for (Table.Column col : node.getColumns()) {
            result.add("<colspec colname=\"col_" + col.getAttribute("colnumber") + "\" colwidth=\""
                    + col.getAttribute(colWidthKey) + "*\"/>");
        }
        for (Map.Entry<String, List<Table.Row>> entry : node.getRows().bySection().entrySet()) {
            String tsec = entry.getKey();
            List<Table.Row> rows = entry.getValue();
            if (rows.isEmpty()) {
                continue;
            }
            if (tsec.equals("body")) {
                hasBody = true;
            }
            result.add("<t" + tsec + ">");
            for (Table.Row row : rows) {
                result.add("<row>");
                for (Table.Cell cell : row) {
                    String colspanAttribute = "";
                    if (cell.getColspan() != null) {
                        int colnum = Strings.toInt(cell.getColumn().getAttribute("colnumber"));
                        colspanAttribute = " namest=\"col_" + colnum + "\" nameend=\"col_"
                                + (colnum + cell.getColspan() - 1) + "\"";
                    }
                    String rowspanAttribute = cell.getRowspan() != null ? " morerows=\"" + (cell.getRowspan() - 1)
                            + "\"" : "";
                    // NOTE <entry> may not have whitespace (e.g., line breaks) as a direct descendant according to
                    // DocBook rules
                    String entryStart = "<entry align=\"" + cell.getAttribute("halign") + "\" valign=\""
                            + cell.getAttribute("valign") + "\"" + colspanAttribute + rowspanAttribute + ">";
                    String cellContent;
                    if (tsec.equals("head")) {
                        cellContent = cell.getText();
                    } else {
                        switch (cell.getStyle() == null ? "" : cell.getStyle()) {
                            case "asciidoc" -> cellContent = cell.getContent();
                            case "literal" -> cellContent = "<literallayout class=\"monospaced\">" + cell.getText()
                                    + "</literallayout>";
                            case "header" -> {
                                List<String> paragraphs = cell.getParagraphs();
                                cellContent = paragraphs.isEmpty() ? "" : "<simpara><emphasis role=\"strong\">"
                                        + String.join("</emphasis></simpara><simpara><emphasis role=\"strong\">",
                                                paragraphs) + "</emphasis></simpara>";
                            }
                            default -> {
                                List<String> paragraphs = cell.getParagraphs();
                                cellContent = paragraphs.isEmpty() ? "" : "<simpara>" + String.join(
                                        "</simpara><simpara>", paragraphs) + "</simpara>";
                            }
                        }
                    }
                    String entryEnd = node.getDocument().hasAttribute("cellbgcolor") ? "<?dbfo bgcolor=\""
                            + node.getDocument().getAttribute("cellbgcolor") + "\"?></entry>" : "</entry>";
                    result.add(entryStart + cellContent + entryEnd);
                }
                result.add("</row>");
            }
            result.add("</t" + tsec + ">");
        }
        result.add("</tgroup>");
        result.add("</" + tagName + ">");

        if (!hasBody) {
            getLogger().warn("tables must have at least one body row");
        }
        return String.join(LF, result);
    }

    public String convertUlist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        if ("bibliography".equals(node.getStyle())) {
            result.add("<bibliodiv" + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">");
            if (node.hasTitle()) {
                result.add("<title>" + node.getTitle() + "</title>");
            }
            for (AbstractBlock block : node.getItems()) {
                ListItem item = (ListItem) block;
                result.add("<bibliomixed>");
                result.add("<bibliomisc>" + item.getText() + "</bibliomisc>");
                if (item.hasBlocks()) {
                    result.add(item.getContent());
                }
                result.add("</bibliomixed>");
            }
            result.add("</bibliodiv>");
        } else {
            boolean checklist = node.isOption("checklist");
            String markType = checklist ? "none" : node.getStyle();
            String markAttribute = markType != null ? " mark=\"" + markType + "\"" : "";
            result.add("<itemizedlist" + commonAttributes(node.getId(), node.getRole(), node.getReftext())
                    + markAttribute + ">");
            if (node.hasTitle()) {
                result.add("<title>" + node.getTitle() + "</title>");
            }
            for (AbstractBlock block : node.getItems()) {
                ListItem item = (ListItem) block;
                String textMarker = checklist && item.hasAttribute("checkbox")
                        ? (item.hasAttribute("checked") ? "&#10003; " : "&#10063; ") : "";
                result.add("<listitem" + commonAttributes(item.getId(), item.getRole(), null) + ">");
                result.add("<simpara>" + textMarker + item.getText() + "</simpara>");
                if (item.hasBlocks()) {
                    result.add(item.getContent());
                }
                result.add("</listitem>");
            }
            result.add("</itemizedlist>");
        }
        return String.join(LF, result);
    }

    public String convertVerse(AbstractBlock node) {
        return blockquoteTag(node, node.hasRole("epigraph") ? "epigraph" : null,
                () -> "<literallayout>" + node.getContent() + "</literallayout>");
    }

    public String convertInlineAnchor(Inline node) {
        switch (node.getType() == null ? "" : node.getType()) {
            case "ref" -> {
                String id = node.getId();
                String reftext = node.getReftext();
                return "<anchor" + commonAttributes(id, null, reftext != null ? reftext : "[" + id + "]") + "/>";
            }
            case "xref" -> {
                Object path = node.getAttributes().get("path");
                if (path != null) {
                    return "<link xl:href=\"" + node.getTarget() + "\">" + (node.getText() != null ? node.getText()
                            : path) + "</link>";
                }
                String linkend = Strings.valueOf(node.getAttributes().get("refid"));
                if (Strings.isNullOrEmpty(linkend)) {
                    Document rootDoc = getRootDocument(node);
                    // Q: should we warn instead of generating a document ID on demand?
                    if (rootDoc.getId() == null) {
                        rootDoc.setId(generateDocumentId(rootDoc));
                    }
                    linkend = rootDoc.getId();
                }
                // NOTE the xref tag in DocBook does not support explicit link text, so the link tag must be used
                // instead The section at http://www.sagehill.net/docbookxsl/CrossRefs.html#IdrefLinks gives an
                // explanation for this choice "link - a cross reference where you supply the text of the reference
                // as the content of the link element."
                String text = node.getText();
                return text != null ? "<link linkend=\"" + linkend + "\">" + text + "</link>"
                        : "<xref linkend=\"" + linkend + "\"/>";
            }
            case "link" -> {
                return "<link xl:href=\"" + node.getTarget() + "\">" + node.getText() + "</link>";
            }
            case "bibref" -> {
                String reftext = node.getReftext();
                String text = "[" + (reftext != null ? reftext : node.getId()) + "]";
                return "<anchor" + commonAttributes(node.getId(), null, text) + "/>" + text;
            }
            default -> {
                getLogger().warn("unknown anchor type: " + Strings.inspect(node.getType()));
                return null;
            }
        }
    }

    public String convertInlineBreak(Inline node) {
        return node.getText() + "<?asciidoc-br?>";
    }

    public String convertInlineButton(Inline node) {
        return "<guibutton>" + node.getText() + "</guibutton>";
    }

    public String convertInlineCallout(Inline node) {
        return "<co" + commonAttributes(node.getId(), null, null) + "/>";
    }

    public String convertInlineFootnote(Inline node) {
        if ("xref".equals(node.getType())) {
            return "<footnoteref linkend=\"" + node.getTarget() + "\"/>";
        }
        return "<footnote" + commonAttributes(node.getId(), null, null) + "><simpara>" + node.getText()
                + "</simpara></footnote>";
    }

    public String convertInlineImage(Inline node) {
        String filerefAttrVal = null;
        String img = "<inlinemediaobject" + commonAttributes(node.getId(), node.getRole(), null) + ">\n"
                + "<imageobject>\n"
                + "<imagedata fileref=\"" + ("icon".equals(node.getType()) ? node.iconUri(node.getTarget())
                        : (filerefAttrVal = node.imageUri(node.getTarget()))) + "\""
                + imageSizeAttributes(node.getAttributes()) + "/>\n"
                + "</imageobject>\n"
                + "<textobject><phrase>" + node.getAlt() + "</phrase></textobject>\n"
                + "</inlinemediaobject>";
        String hrefAttrVal;
        if (filerefAttrVal != null && node.hasAttribute("link")
                && (!"self".equals(hrefAttrVal = node.getStringAttribute("link"))
                        || (hrefAttrVal = filerefAttrVal) != null)) {
            return "<link xl:href=\"" + hrefAttrVal + "\">" + img + "</link>";
        }
        return img;
    }

    public String convertInlineIndexterm(Inline node) {
        String rel;
        Object see = node.getAttribute("see");
        Object seeAlsoList;
        if (see != null) {
            rel = "\n<see>" + see + "</see>";
        } else if ((seeAlsoList = node.getAttribute("see-also")) != null) {
            StringBuilder builder = new StringBuilder();
            for (Object seeAlso : (List<?>) seeAlsoList) {
                builder.append("\n<seealso>").append(seeAlso).append("</seealso>");
            }
            rel = builder.toString();
        } else {
            rel = "";
        }
        if ("visible".equals(node.getType())) {
            return "<indexterm>\n"
                    + "<primary>" + node.getText() + "</primary>" + rel + "\n"
                    + "</indexterm>" + node.getText();
        }
        List<?> terms = (List<?>) node.getAttribute("terms");
        int numterms = terms.size();
        if (numterms > 2) {
            return "<indexterm>\n"
                    + "<primary>" + terms.get(0) + "</primary><secondary>" + terms.get(1) + "</secondary><tertiary>"
                    + terms.get(2) + "</tertiary>" + rel + "\n"
                    + "</indexterm>" + (node.getDocument().isOption("indexterm-promotion") ? "\n"
                            + "<indexterm>\n"
                            + "<primary>" + terms.get(1) + "</primary><secondary>" + terms.get(2) + "</secondary>\n"
                            + "</indexterm>\n"
                            + "<indexterm>\n"
                            + "<primary>" + terms.get(2) + "</primary>\n"
                            + "</indexterm>" : "");
        } else if (numterms > 1) {
            return "<indexterm>\n"
                    + "<primary>" + terms.get(0) + "</primary><secondary>" + terms.get(1) + "</secondary>" + rel + "\n"
                    + "</indexterm>" + (node.getDocument().isOption("indexterm-promotion") ? "\n"
                            + "<indexterm>\n"
                            + "<primary>" + terms.get(1) + "</primary>\n"
                            + "</indexterm>" : "");
        }
        return "<indexterm>\n"
                + "<primary>" + (terms.isEmpty() ? "" : terms.get(0)) + "</primary>" + rel + "\n"
                + "</indexterm>";
    }

    public String convertInlineKbd(Inline node) {
        List<?> keys = (List<?>) node.getAttribute("keys");
        if (keys.size() == 1) {
            return "<keycap>" + keys.get(0) + "</keycap>";
        }
        return "<keycombo><keycap>" + String.join("</keycap><keycap>", keys.stream().map(String::valueOf).toList())
                + "</keycap></keycombo>";
    }

    public String convertInlineMenu(Inline node) {
        Object menu = node.getAttribute("menu");
        List<?> submenus = (List<?>) node.getAttribute("submenus");
        if (submenus == null || submenus.isEmpty()) {
            Object menuitem = node.getAttribute("menuitem");
            if (menuitem != null) {
                return "<menuchoice><guimenu>" + menu + "</guimenu> <guimenuitem>" + menuitem
                        + "</guimenuitem></menuchoice>";
            }
            return "<guimenu>" + menu + "</guimenu>";
        }
        return "<menuchoice><guimenu>" + menu + "</guimenu> <guisubmenu>" + String.join(
                "</guisubmenu> <guisubmenu>", submenus.stream().map(String::valueOf).toList())
                + "</guisubmenu> <guimenuitem>" + node.getAttribute("menuitem") + "</guimenuitem></menuchoice>";
    }

    public String convertInlineQuoted(Inline node) {
        String type = node.getType();
        if ("asciimath".equals(type)) {
            // NOTE fop requires jeuclid to process mathml markup
            isAsciimathAvailable();
            return "<inlineequation><mathphrase><![CDATA[" + node.getText() + "]]></mathphrase></inlineequation>";
        } else if ("latexmath".equals(type)) {
            // unhandled math; pass source to alt and required mathphrase element; dblatex will process alt as LaTeX
            // math
            String equation = node.getText();
            return "<inlineequation><alt><![CDATA[" + equation + "]]></alt><mathphrase><![CDATA[" + equation
                    + "]]></mathphrase></inlineequation>";
        }
        QuoteTag quoteTag = type == null ? DEFAULT_QUOTE_TAG : QUOTE_TAGS.getOrDefault(type, DEFAULT_QUOTE_TAG);
        String open = quoteTag.open();
        String close = quoteTag.close();
        String text = node.getText();
        String quotedText;
        if (node.getRole() != null) {
            if (quoteTag.supportsPhrase()) {
                quotedText = open + "<phrase role=\"" + node.getRole() + "\">" + text + "</phrase>" + close;
            } else {
                quotedText = Strings.chop(open) + " role=\"" + node.getRole() + "\">" + text + close;
            }
        } else {
            quotedText = open + text + close;
        }

        return node.getId() != null ? "<anchor" + commonAttributes(node.getId(), null, null) + "/>" + quotedText
                : quotedText;
    }

    private static String commonAttributes(String id, String role, String reftext) {
        String attrs;
        if (id != null) {
            attrs = " xml:id=\"" + id + "\"" + (role != null ? " role=\"" + role + "\"" : "");
        } else if (role != null) {
            attrs = " role=\"" + role + "\"";
        } else {
            attrs = "";
        }
        if (reftext != null) {
            if (reftext.contains("<") && (reftext = Rx.gsub(Rx.XML_SANITIZE_RX, reftext, "")).contains(" ")) {
                reftext = Strings.strip(Strings.squeeze(reftext, ' '));
            }
            if (reftext.contains("\"")) {
                reftext = reftext.replace("\"", "&quot;");
            }
            return attrs + " xreflabel=\"" + reftext + "\"";
        }
        return attrs;
    }

    private static String imageSizeAttributes(Map<String, Object> attributes) {
        // NOTE according to the DocBook spec, content area, scaling, and scaling to fit are mutually exclusive
        // See http://tdg.docbook.org/tdg/4.5/imagedata-x.html#d0e79635
        if (attributes.containsKey("scaledwidth")) {
            return " width=\"" + attributes.get("scaledwidth") + "\"";
        } else if (attributes.containsKey("scale")) {
            // QUESTION should we set the viewport using width and depth? (the scaled image would be contained within
            // this box)
            return " scale=\"" + attributes.get("scale") + "\"";
        }
        String widthAttribute = attributes.containsKey("width") ? " contentwidth=\"" + attributes.get("width") + "\""
                : "";
        String depthAttribute = attributes.containsKey("height") ? " contentdepth=\"" + attributes.get("height")
                + "\"" : "";
        return widthAttribute + depthAttribute;
    }

    private static String authorTag(Document doc, Author author) {
        List<String> result = new ArrayList<>();
        result.add("<author>");
        result.add("<personname>");
        if (author.firstName() != null) {
            result.add("<firstname>" + doc.subReplacements(author.firstName()) + "</firstname>");
        }
        if (author.middleName() != null) {
            result.add("<othername>" + doc.subReplacements(author.middleName()) + "</othername>");
        }
        if (author.lastName() != null) {
            result.add("<surname>" + doc.subReplacements(author.lastName()) + "</surname>");
        }
        result.add("</personname>");
        if (author.email() != null) {
            result.add("<email>" + author.email() + "</email>");
        }
        result.add("</author>");
        return String.join(LF, result);
    }

    private String documentInfoTag(Document doc, AbstractBlock abstractBlock) {
        List<String> result = new ArrayList<>();
        result.add("<info>");
        if (!doc.isNotitle()) {
            Document.Title title = doc.getPartitionedDoctitle(null, false, true);
            if (title.hasSubtitle()) {
                result.add("<title>" + title.getMain() + "</title>\n<subtitle>" + title.getSubtitle() + "</subtitle>");
            } else {
                result.add("<title>" + title + "</title>");
            }
        }
        Object date = doc.hasAttribute("revdate") ? doc.getAttribute("revdate")
                : (doc.hasAttribute("reproducible") ? null : doc.getAttribute("docdate"));
        if (date != null) {
            result.add("<date>" + date + "</date>");
        }
        if (doc.hasAttribute("copyright")) {
            Matcher m = Rx.match(COPYRIGHT_RX, doc.getStringAttribute("copyright"));
            result.add("<copyright>");
            result.add("<holder>" + (m == null ? "" : m.group(1)) + "</holder>");
            if (m != null && m.group(2) != null) {
                result.add("<year>" + m.group(2) + "</year>");
            }
            result.add("</copyright>");
        }
        if (doc.hasHeader()) {
            List<Author> authors = doc.getAuthors();
            if (!authors.isEmpty()) {
                if (authors.size() > 1) {
                    result.add("<authorgroup>");
                    for (Author author : authors) {
                        result.add(authorTag(doc, author));
                    }
                    result.add("</authorgroup>");
                } else {
                    Author author = authors.get(0);
                    result.add(authorTag(doc, author));
                    if (author.initials() != null) {
                        result.add("<authorinitials>" + author.initials() + "</authorinitials>");
                    }
                }
            }
            if (doc.hasAttribute("revdate") && (doc.hasAttribute("revnumber") || doc.hasAttribute("revremark"))) {
                result.add("<revhistory>\n<revision>");
                if (doc.hasAttribute("revnumber")) {
                    result.add("<revnumber>" + doc.getAttribute("revnumber") + "</revnumber>");
                }
                if (doc.hasAttribute("revdate")) {
                    result.add("<date>" + doc.getAttribute("revdate") + "</date>");
                }
                if (doc.hasAttribute("authorinitials")) {
                    result.add("<authorinitials>" + doc.getAttribute("authorinitials") + "</authorinitials>");
                }
                if (doc.hasAttribute("revremark")) {
                    result.add("<revremark>" + doc.getAttribute("revremark") + "</revremark>");
                }
                result.add("</revision>\n</revhistory>");
            }
            if (doc.hasAttribute("front-cover-image") || doc.hasAttribute("back-cover-image")) {
                String backCoverTag = coverTag(doc, "back", false);
                String frontCoverTag;
                if (backCoverTag != null) {
                    result.add(coverTag(doc, "front", true));
                    result.add(backCoverTag);
                } else if ((frontCoverTag = coverTag(doc, "front", false)) != null) {
                    result.add(frontCoverTag);
                }
            }
            if (doc.hasAttribute("orgname")) {
                result.add("<orgname>" + doc.getAttribute("orgname") + "</orgname>");
            }
            String docinfoContent = doc.getDocinfo();
            if (!docinfoContent.isEmpty()) {
                result.add(docinfoContent);
            }
        }
        if (abstractBlock != null) {
            abstractBlock.setOption("root");
            result.add(convert(abstractBlock, abstractBlock.getNodeName()));
            abstractBlock.removeAttribute("root-option");
        }
        result.add("</info>");

        return String.join(LF, result);
    }

    private static AbstractBlock findRootAbstract(Document doc) {
        if (!doc.hasBlocks()) {
            return null;
        }
        AbstractBlock firstBlock = doc.getBlocks().get(0);
        if ("preamble".equals(firstBlock.getContext())) {
            if (firstBlock.getBlocks().isEmpty()) {
                return null;
            }
            firstBlock = firstBlock.getBlocks().get(0);
        } else if ("section".equals(firstBlock.getContext()) && firstBlock instanceof Section section) {
            if ("abstract".equals(section.getSectionName())) {
                return firstBlock;
            }
            if (!("preface".equals(section.getSectionName()) && !firstBlock.getBlocks().isEmpty())) {
                return null;
            }
            firstBlock = firstBlock.getBlocks().get(0);
        }
        return "abstract".equals(firstBlock.getStyle()) && "open".equals(firstBlock.getContext()) ? firstBlock : null;
    }

    private static AbstractBlock extractAbstract(Document document, AbstractBlock abstractBlock) {
        AbstractBlock parent = (AbstractBlock) abstractBlock.getParent();
        while (parent != document && parent.getBlocks().size() == 1) {
            parent = (AbstractBlock) parent.getParent();
        }
        return parent.getBlocks().remove(0);
    }

    private static void restoreAbstract(AbstractBlock abstractBlock) {
        ((AbstractBlock) abstractBlock.getParent()).getBlocks().add(0, abstractBlock);
    }

    private static Document getRootDocument(AbstractNode node) {
        Document doc = node.getDocument();
        while (doc.isNested()) {
            doc = doc.getParentDocument();
        }
        return doc;
    }

    private static String generateDocumentId(Document doc) {
        return "__" + doc.getDoctype() + "-root__";
    }

    // FIXME this should be handled through a template mechanism
    private static String encloseContent(AbstractBlock node) {
        return node.getContentModel() == ContentModel.COMPOUND ? node.getContent()
                : "<simpara>" + node.getContent() + "</simpara>";
    }

    private static String titleTag(AbstractBlock node, boolean optional) {
        return !optional || node.hasTitle() ? "<title>" + Objects.toString(node.getTitle(), "") + "</title>\n" : "";
    }

    private static String coverTag(Document doc, String face, boolean usePlaceholder) {
        String coverImage = doc.getStringAttribute(face + "-cover-image");
        if (coverImage != null) {
            String sizeAttrs = "";
            Matcher m;
            if (coverImage.contains(":") && (m = Rx.match(IMAGE_MACRO_RX, coverImage)) != null) {
                String target = m.group(1);
                String attrlist = m.group(2);
                coverImage = doc.imageUri(target);
                // NOTE scalefit="1" is the default for a cover image
                if (attrlist != null) {
                    sizeAttrs = imageSizeAttributes(new AttributeList(attrlist).parse(List.of("alt", "width",
                            "height")));
                }
            }
            return "<cover role=\"" + face + "\">\n"
                    + "<mediaobject>\n"
                    + "<imageobject>\n"
                    + "<imagedata fileref=\"" + coverImage + "\"" + sizeAttrs + "/>\n"
                    + "</imageobject>\n"
                    + "</mediaobject>\n"
                    + "</cover>";
        } else if (usePlaceholder) {
            return "<cover role=\"" + face + "\"/>";
        }
        return null;
    }

    private static String blockquoteTag(AbstractBlock node, String tagName, Supplier<String> content) {
        String startTag;
        String endTag;
        if (tagName != null) {
            startTag = "<" + tagName;
            endTag = "</" + tagName + ">";
        } else {
            startTag = "<blockquote";
            endTag = "</blockquote>";
        }
        List<String> result = new ArrayList<>();
        result.add(startTag + commonAttributes(node.getId(), node.getRole(), node.getReftext()) + ">");
        if (node.hasTitle()) {
            result.add("<title>" + node.getTitle() + "</title>");
        }
        if (node.hasAttribute("attribution") || node.hasAttribute("citetitle")) {
            result.add("<attribution>");
            if (node.hasAttribute("attribution")) {
                result.add(String.valueOf(node.getAttribute("attribution")));
            }
            if (node.hasAttribute("citetitle")) {
                result.add("<citetitle>" + node.getAttribute("citetitle") + "</citetitle>");
            }
            result.add("</attribution>");
        }
        result.add(content.get());
        result.add(endTag);
        return String.join(LF, result);
    }

    /**
     * Checks whether AsciiMath support is available. Since this implementation has no dependencies, AsciiMath is never
     * available; a warning is logged the first time it's requested.
     */
    private boolean isAsciimathAvailable() {
        if (asciimathAvailable == null) {
            asciimathAvailable = false;
            getLogger().warn("optional gem 'asciimath' is not available. Functionality disabled.");
        }
        return asciimathAvailable;
    }
}
