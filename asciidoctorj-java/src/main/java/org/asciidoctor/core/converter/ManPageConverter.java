package org.asciidoctor.core.converter;

import static org.asciidoctor.core.Asciidoctor.BLOCK_MATH_DELIMITERS;
import static org.asciidoctor.core.Asciidoctor.CC_ANY;
import static org.asciidoctor.core.Asciidoctor.CG_BLANK;
import static org.asciidoctor.core.Asciidoctor.LF;
import static org.asciidoctor.core.Asciidoctor.TAB;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.AbstractNode;
import org.asciidoctor.core.ContentModel;
import org.asciidoctor.core.DescriptionListEntry;
import org.asciidoctor.core.Document;
import org.asciidoctor.core.Document.Author;
import org.asciidoctor.core.Document.Footnote;
import org.asciidoctor.core.Helpers;
import org.asciidoctor.core.Inline;
import org.asciidoctor.core.ListItem;
import org.asciidoctor.core.Rx;
import org.asciidoctor.core.Section;
import org.asciidoctor.core.Strings;
import org.asciidoctor.core.Table;

/**
 * A built-in {@link Converter} implementation that generates the man page (groff) format.
 *
 * <p>The output of this converter adheres to the man definition as defined by groff and uses the manpage output of the
 * DocBook toolchain as a foundation. That means if you've previously been generating man pages using the a2x tool from
 * AsciiDoc.py, you should be able to achieve a very similar result using this converter. Though you'll also get to
 * enjoy some notable enhancements that have been added since, such as the customizable linkstyle.
 *
 * <p>See http://www.gnu.org/software/groff/manual/html_node/Man-usage.html#Man-usage
 */
public class ManPageConverter extends AbstractConverter<String> {
    private static final String WHITESPACE = LF + TAB + " ";
    private static final String ET = " ".repeat(8);
    private static final String ESC = "\u001b"; // troff leader marker
    private static final String ESC_BS = ESC + "\\"; // escaped backslash (indicates troff formatting sequence)
    private static final String ESC_FS = ESC + "."; // escaped full stop (indicates troff macro)

    private static final Pattern LITERAL_BACKSLASH_RX = Rx.compile("\\A\\\\|(" + ESC + ")?\\\\");
    private static final Pattern LEADING_PERIOD_RX = Rx.compile("^\\.");
    private static final Pattern ESCAPED_MACRO_RX = Rx.compile("^(?:" + ESC + "\\\\c\\n)?" + ESC + "\\.((?:URL|MTO) \""
            + CC_ANY + "*?\" \"" + CC_ANY + "*?\" )( |[^\\s]*)(" + CC_ANY + "*?)(?: *" + ESC + "\\\\c)?$");
    private static final Pattern MALFORMED_ESCAPED_MACRO_RX = Rx.compile("(" + ESC + "\\\\c) (" + ESC
            + "\\.(?:URL|MTO) )");
    private static final Pattern MOCK_MACRO_RX = Rx.compile("</?(" + ESC + "\\\\[^>]+)>");
    private static final Pattern EM_DASH_CHAR_REF_RX = Rx.compile("&#8212;(?:&#8203;)?");
    private static final Pattern ELLIPSIS_CHAR_REF_RX = Rx.compile("&#8230;(?:&#8203;)?");
    private static final Pattern WRAPPED_INDENT_RX = Rx.compile(CG_BLANK + "*" + LF + CG_BLANK + "*");
    private static final Pattern REPEATING_SPACES_RX = Rx.compile("(^)?  +");
    private static final Pattern XML_MARKUP_RX = Rx.compile("&#?[a-z\\d]+;|<");
    private static final Pattern PCDATA_FILTER_RX = Rx.compile("(&#?[a-z\\d]+;|<" + ESC + "\\\\f\\(CR.*?</" + ESC
            + "\\\\fP>|<[^>]+>)|([^&<]+)");

    /** How whitespace is handled by {@link #manify(String, Whitespace)}. */
    private enum Whitespace {
        /** Preserve spaces (only expanding tabs). */
        PRESERVE,
        /** Normalize whitespace (remove spaces around newlines). */
        NORMALIZE,
        /** Collapse adjacent whitespace to a single space. */
        COLLAPSE
    }

    private Map<String, AbstractNode> refs;
    private boolean resolvingXref;

    public ManPageConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
        initBackendTraits("manpage", "man", null, ".man", true);
        registerHandlers();
    }

    public ManPageConverter(String backend) {
        this(backend, Map.of());
    }

    private void registerHandlers() {
        handle("document", Document.class, this::convertDocument);
        handle("embedded", Document.class, this::convertEmbedded);
        handle("section", Section.class, this::convertSection);
        handle("admonition", AbstractBlock.class, this::convertAdmonition);
        handle("colist", org.asciidoctor.core.List.class, this::convertColist);
        handle("dlist", org.asciidoctor.core.List.class, this::convertDlist);
        handle("example", AbstractBlock.class, this::convertExample);
        handle("floating_title", AbstractBlock.class, this::convertFloatingTitle);
        handle("image", AbstractBlock.class, this::convertImage);
        handle("listing", AbstractBlock.class, this::convertListing);
        handle("literal", AbstractBlock.class, this::convertLiteral);
        handle("sidebar", AbstractBlock.class, this::convertSidebar);
        handle("olist", org.asciidoctor.core.List.class, this::convertOlist);
        handle("open", AbstractBlock.class, this::convertOpen);
        handle("page_break", AbstractBlock.class, this::convertPageBreak);
        handle("paragraph", AbstractBlock.class, this::convertParagraph);
        handle("pass", AbstractBlock.class, this::contentOnly);
        handle("preamble", AbstractBlock.class, this::contentOnly);
        handle("quote", AbstractBlock.class, this::convertQuote);
        handle("stem", AbstractBlock.class, this::convertStem);
        handle("table", Table.class, this::convertTable);
        handle("thematic_break", AbstractBlock.class, this::convertThematicBreak);
        handle("toc", AbstractBlock.class, this::skip);
        handle("ulist", org.asciidoctor.core.List.class, this::convertUlist);
        handle("verse", AbstractBlock.class, this::convertVerse);
        handle("video", AbstractBlock.class, this::convertVideo);
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
        if (!node.hasAttribute("mantitle")) {
            throw new IllegalStateException("asciidoctor: ERROR: doctype must be set to manpage when using manpage "
                    + "backend");
        }
        String mantitle = Rx.gsub(Rx.INVALID_SECTION_ID_CHARS_RX, node.getStringAttribute("mantitle"), "");
        Object manvolnum = node.getAttribute("manvolnum", "1");
        String manname = node.getStringAttribute("manname", mantitle);
        String manmanual = node.getStringAttribute("manmanual");
        String mansource = node.getStringAttribute("mansource");
        Object docdate = node.hasAttribute("reproducible") ? null : node.getAttribute("docdate");
        // NOTE the first line enables the table (tbl) preprocessor, necessary for non-Linux systems
        List<String> result = new ArrayList<>();
        result.add("'\\\" t\n"
                + ".\\\"     Title: " + mantitle + "\n"
                + ".\\\"    Author: " + (node.hasAttribute("authors") ? node.getAttribute("authors")
                        : "[see the \"AUTHOR(S)\" section]") + "\n"
                + ".\\\" Generator: AsciidoctorJ " + node.getAttribute("asciidoctor-version"));
        if (docdate != null) {
            result.add(".\\\"      Date: " + docdate);
        }
        result.add(".\\\"    Manual: " + (manmanual != null ? Strings.trS(manmanual, WHITESPACE, " ") : "\\ \\&") + "\n"
                + ".\\\"    Source: " + (mansource != null ? Strings.trS(mansource, WHITESPACE, " ") : "\\ \\&") + "\n"
                + ".\\\"  Language: English\n"
                + ".\\\"");
        // TODO add document-level setting to disable capitalization of manname
        result.add(".TH \"" + manify(manname.toUpperCase(Locale.ROOT), Whitespace.COLLAPSE) + "\" \"" + manvolnum
                + "\" \"" + (docdate == null ? "" : docdate) + "\" \"" + (mansource != null
                        ? manify(mansource, Whitespace.COLLAPSE) : "\\ \\&") + "\" \"" + (manmanual != null
                                ? manify(manmanual, Whitespace.COLLAPSE) : "\\ \\&") + "\"");
        // define portability settings
        // see http://bugs.debian.org/507673
        // see http://lists.gnu.org/archive/html/groff/2009-02/msg00013.html
        result.add(".ie \\n(.g .ds Aq \\(aq");
        result.add(".el       .ds Aq '");
        // set sentence_space_size to 0 to prevent extra space between sentences separated by a newline
        // the alternative is to add \& at the end of the line
        result.add(".ss \\n[.ss] 0");
        // disable hyphenation
        result.add(".nh");
        // disable justification (adjust text to left margin only)
        result.add(".ad l");
        // define URL macro for portability
        // see http://web.archive.org/web/20060102165607/http://people.debian.org/~branden/talks/wtfm/wtfm.pdf
        //
        // Usage
        //
        // .URL "http://www.debian.org" "Debian" "."
        //
        // * First argument: the URL
        // * Second argument: text to be hyperlinked
        // * Third (optional) argument: text that needs to immediately trail the hyperlink without intervening
        //   whitespace
        result.add(".de URL\n"
                + "\\fI\\\\$2\\fP <\\\\$1>\\\\$3\n"
                + "..\n"
                + ".als MTO URL\n"
                + ".if \\n[.g] \\{\\\n"
                + ".  mso www.tmac\n"
                + ".  am URL\n"
                + ".    ad l\n"
                + ".  .\n"
                + ".  am MTO\n"
                + ".    ad l\n"
                + ".  .");
        result.add(".  LINKSTYLE " + node.getAttribute("man-linkstyle", "blue R < >"));
        result.add(".\\}");

        if (!node.isNoheader()) {
            if (node.hasAttribute("manpurpose")) {
                Object mannamesVal = node.getAttribute("mannames", List.of(manname));
                List<String> mannames = new ArrayList<>();
                for (Object n : (List<?>) mannamesVal) {
                    mannames.add(manify(String.valueOf(n), Whitespace.COLLAPSE).replace("\\-", "-"));
                }
                result.add(".SH \"" + node.getStringAttribute("manname-title", "NAME").toUpperCase(Locale.ROOT)
                        + "\"\n" + String.join(", ", mannames) + " \\- " + manify(node.getStringAttribute(
                                "manpurpose"), Whitespace.NORMALIZE));
            }
        }

        result.add(node.getContent());

        // QUESTION should NOTES come after AUTHOR(S)?
        appendFootnotes(result, node);

        List<Author> authors = node.getAuthors();
        if (!authors.isEmpty()) {
            if (authors.size() > 1) {
                result.add(".SH \"AUTHORS\"");
                for (Author author : authors) {
                    result.add(".sp\n" + author.fullName());
                }
            } else {
                result.add(".SH \"AUTHOR\"\n.sp\n" + authors.get(0).fullName());
            }
        }

        return String.join(LF, result);
    }

    // NOTE embedded doesn't really make sense in the manpage backend
    public String convertEmbedded(Document node) {
        List<String> result = new ArrayList<>();
        result.add(node.getContent());

        appendFootnotes(result, node);

        // QUESTION should we add an AUTHOR(S) section?

        return String.join(LF, result);
    }

    public String convertSection(Section node) {
        String macro;
        String stitle;
        if (node.getLevel() > 1) {
            macro = "SS";
            // QUESTION why captioned title? why not when level == 1?
            stitle = node.getCaptionedTitle();
        } else {
            macro = "SH";
            stitle = uppercasePcdata(node.getTitle());
        }
        return "." + macro + " \"" + manify(stitle, Whitespace.COLLAPSE) + "\"\n" + node.getContent();
    }

    public String convertAdmonition(AbstractBlock node) {
        return ".if n .sp\n"
                + ".RS 4\n"
                + ".it 1 an-trap\n"
                + ".nr an-no-space-flag 1\n"
                + ".nr an-break-flag 1\n"
                + ".br\n"
                + ".ps +1\n"
                + ".B " + node.getAttribute("textlabel") + (node.hasTitle() ? "\\fP: " + manify(node.getTitle(),
                        Whitespace.COLLAPSE) : "") + "\n"
                + ".ps -1\n"
                + ".br\n"
                + encloseContent(node) + "\n"
                + ".sp .5v\n"
                + ".RE";
    }

    public String convertColist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        if (node.hasTitle()) {
            result.add(".sp\n.B " + manify(node.getTitle(), Whitespace.COLLAPSE) + "\n.br");
        }
        result.add(".TS\ntab(:);\nr lw(\\n(.lu*75u/100u).");

        int num = 0;
        for (AbstractBlock block : node.getItems()) {
            ListItem item = (ListItem) block;
            result.add("\\fB(" + (++num) + ")\\fP\\h'-2n':T{");
            result.add(manify(item.getText(), Whitespace.NORMALIZE));
            if (item.hasBlocks()) {
                result.add(item.getContent());
            }
            result.add("T}");
        }
        result.add(".TE");
        return String.join(LF, result);
    }

    // TODO implement horizontal (if it makes sense)
    public String convertDlist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        if (node.hasTitle()) {
            result.add(".sp\n.B " + manify(node.getTitle(), Whitespace.COLLAPSE) + "\n.br");
        }
        int counter = 0;
        for (AbstractBlock block : node.getItems()) {
            DescriptionListEntry entry = (DescriptionListEntry) block;
            counter++;
            List<String> termTexts = new ArrayList<>();
            for (ListItem dt : entry.getTerms()) {
                termTexts.add(dt.getText());
            }
            if ("qanda".equals(node.getStyle())) {
                result.add(".sp\n" + counter + ". " + manify(String.join(" ", termTexts), Whitespace.COLLAPSE)
                        + "\n.RS 4");
            } else {
                result.add(".sp\n" + manify(String.join(", ", termTexts), Whitespace.NORMALIZE) + "\n.RS 4");
            }
            ListItem dd = entry.getDescription();
            if (dd != null) {
                boolean hasText = dd.hasText();
                if (hasText) {
                    result.add(manify(dd.getText(), Whitespace.NORMALIZE));
                }
                if (dd.hasBlocks()) {
                    String ddContent = dd.getContent();
                    if (!hasText && ddContent.startsWith(".sp\n")) {
                        ddContent = ddContent.substring(4);
                    }
                    result.add(ddContent);
                }
            }
            result.add(".RE");
        }
        return String.join(LF, result);
    }

    public String convertExample(AbstractBlock node) {
        List<String> result = new ArrayList<>();
        result.add(node.hasTitle() ? ".sp\n.B " + manify(node.getCaptionedTitle(), Whitespace.COLLAPSE) + "\n.br"
                : ".sp");
        result.add(".RS 4\n" + encloseContent(node) + "\n.RE");
        return String.join(LF, result);
    }

    public String convertFloatingTitle(AbstractBlock node) {
        return ".SS \"" + manify(node.getTitle(), Whitespace.COLLAPSE) + "\"";
    }

    public String convertImage(AbstractBlock node) {
        List<String> result = new ArrayList<>();
        result.add(node.hasTitle() ? ".sp\n.B " + manify(node.getCaptionedTitle(), Whitespace.COLLAPSE) + "\n.br"
                : ".sp");
        result.add("[" + manify(node.getAlt(), Whitespace.COLLAPSE) + "]");
        return String.join(LF, result);
    }

    public String convertListing(AbstractBlock node) {
        List<String> result = new ArrayList<>();
        if (node.hasTitle()) {
            result.add(".sp\n.B " + manify(node.getCaptionedTitle(), Whitespace.COLLAPSE) + "\n.br");
        }
        result.add(".sp\n"
                + ".if n .RS 4\n"
                + ".nf\n"
                + ".fam C\n"
                + manify(node.getContent(), Whitespace.PRESERVE) + "\n"
                + ".fam\n"
                + ".fi\n"
                + ".if n .RE");
        return String.join(LF, result);
    }

    public String convertLiteral(AbstractBlock node) {
        List<String> result = new ArrayList<>();
        if (node.hasTitle()) {
            result.add(".sp\n.B " + manify(node.getTitle(), Whitespace.COLLAPSE) + "\n.br");
        }
        result.add(".sp\n"
                + ".if n .RS 4\n"
                + ".nf\n"
                + ".fam C\n"
                + manify(node.getContent(), Whitespace.PRESERVE) + "\n"
                + ".fam\n"
                + ".fi\n"
                + ".if n .RE");
        return String.join(LF, result);
    }

    public String convertSidebar(AbstractBlock node) {
        List<String> result = new ArrayList<>();
        result.add(node.hasTitle() ? ".sp\n.B " + manify(node.getTitle(), Whitespace.COLLAPSE) + "\n.br" : ".sp");
        result.add(".RS 4\n" + encloseContent(node) + "\n.RE");
        return String.join(LF, result);
    }

    public String convertOlist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        if (node.hasTitle()) {
            result.add(".sp\n.B " + manify(node.getTitle(), Whitespace.COLLAPSE) + "\n.br");
        }

        int start = Strings.toInt(node.getAttribute("start", 1));
        List<AbstractBlock> items = node.getItems();
        for (int idx = 0; idx < items.size(); idx++) {
            ListItem item = (ListItem) items.get(idx);
            int numeral = idx + start;
            String listText = manify(item.getText(), Whitespace.NORMALIZE);
            result.add(".sp\n"
                    + ".RS 4\n"
                    + ".ie n \\{\\\n"
                    + "\\h'-04' " + numeral + ".\\h'+01'\\c\n"
                    + ".\\}\n"
                    + ".el \\{\\\n"
                    + ".  sp -1\n"
                    + ".  IP \" " + numeral + ".\" 4.2\n"
                    + ".\\}" + (listText.isEmpty() ? "" : LF + listText));
            if (item.hasBlocks()) {
                String itemContent = item.getContent();
                if (listText.isEmpty() && itemContent.startsWith(".sp\n")) {
                    itemContent = itemContent.substring(4);
                }
                result.add(itemContent);
            }
            result.add(".RE");
        }
        return String.join(LF, result);
    }

    public String convertOpen(AbstractBlock node) {
        String style = node.getStyle();
        if ("abstract".equals(style) || "partintro".equals(style)) {
            return encloseContent(node);
        }
        return node.getContent();
    }

    public String convertPageBreak(AbstractBlock node) {
        return ".bp";
    }

    public String convertParagraph(AbstractBlock node) {
        if (node.hasTitle()) {
            return ".sp\n"
                    + ".B " + manify(node.getTitle(), Whitespace.COLLAPSE) + "\n"
                    + ".br\n"
                    + manify(node.getContent(), Whitespace.NORMALIZE);
        }
        return ".sp\n" + manify(node.getContent(), Whitespace.NORMALIZE);
    }

    public String convertQuote(AbstractBlock node) {
        List<String> result = new ArrayList<>();
        if (node.hasTitle()) {
            result.add(".sp\n.RS 3\n.B " + manify(node.getTitle(), Whitespace.COLLAPSE) + "\n.br\n.RE");
        }
        String attributionLine = node.hasAttribute("citetitle") ? node.getAttribute("citetitle") + " " : null;
        attributionLine = node.hasAttribute("attribution") ? (attributionLine == null ? "" : attributionLine)
                + "\\(em " + node.getAttribute("attribution") : null;
        result.add(".RS 3\n"
                + ".ll -.6i\n"
                + encloseContent(node) + "\n"
                + ".br\n"
                + ".RE\n"
                + ".ll");
        if (attributionLine != null) {
            result.add(".RS 5\n"
                    + ".ll -.10i\n"
                    + attributionLine + "\n"
                    + ".RE\n"
                    + ".ll");
        }
        return String.join(LF, result);
    }

    public String convertStem(AbstractBlock node) {
        List<String> result = new ArrayList<>();
        result.add(node.hasTitle() ? ".sp\n.B " + manify(node.getTitle(), Whitespace.COLLAPSE) + "\n.br" : ".sp");
        List<String> delimiters = BLOCK_MATH_DELIMITERS.get(node.getStyle());
        String equation = node.getContent();
        if (delimiters != null) {
            String open = delimiters.get(0);
            String close = delimiters.get(1);
            if (equation.startsWith(open) && equation.endsWith(close)) {
                equation = equation.substring(open.length(), equation.length() - close.length());
            }
        }
        result.add(manify(equation, Whitespace.PRESERVE) + " (" + node.getStyle() + ")");
        return String.join(LF, result);
    }

    /**
     * NOTE This handler inserts empty cells to account for colspans and rowspans. In order to support colspans and
     * rowspans properly, that information must be computed up front and consulted when rendering the cell as this
     * information is not available on the cell itself.
     */
    public String convertTable(Table node) {
        List<String> result = new ArrayList<>();
        if (node.hasTitle()) {
            result.add(".sp\n"
                    + ".it 1 an-trap\n"
                    + ".nr an-no-space-flag 1\n"
                    + ".nr an-break-flag 1\n"
                    + ".br\n"
                    + ".B " + manify(node.getCaptionedTitle(), Whitespace.COLLAPSE) + "\n");
        }
        result.add(".TS" + LF + "allbox tab(:);");
        List<List<List<String>>> rowHeader = new ArrayList<>();
        List<List<String>> rowText = new ArrayList<>();
        int rowIndex = 0;
        for (Map.Entry<String, List<Table.Row>> entry : node.getRows().bySection().entrySet()) {
            String tsec = entry.getKey();
            List<Table.Row> rows = entry.getValue();
            if (rows.isEmpty()) {
                continue;
            }
            for (Table.Row row : rows) {
                ensureSize(rowHeader, rowIndex + 1);
                if (rowHeader.get(rowIndex) == null) {
                    rowHeader.set(rowIndex, new ArrayList<>());
                }
                ensureSize(rowText, rowIndex + 1);
                if (rowText.get(rowIndex) == null) {
                    rowText.set(rowIndex, new ArrayList<>());
                }
                List<List<String>> header = rowHeader.get(rowIndex);
                List<String> text = rowText.get(rowIndex);
                int remainingCells = row.size();
                for (int cellIndex = 0; cellIndex < row.size(); cellIndex++) {
                    Table.Cell cell = row.get(cellIndex);
                    remainingCells--;
                    cellAt(header, cellIndex);
                    // add an empty cell as a placeholder if this is a rowspan cell
                    if (header.get(cellIndex).equals(List.of("^t"))) {
                        text.add("T{" + LF + "T}:");
                    }
                    text.add("T{" + LF);
                    String cellHalign = Strings.chr(String.valueOf(cell.getAttribute("halign", "left")));
                    if (tsec.equals("body")) {
                        if (header.isEmpty() || header.get(cellIndex).isEmpty()) {
                            header.get(cellIndex).add(cellHalign + "t");
                        } else {
                            cellAt(header, cellIndex + 1).add(cellHalign + "t");
                        }
                        String cellContent;
                        switch (cell.getStyle() == null ? "" : cell.getStyle()) {
                            case "asciidoc" -> cellContent = cell.getContent();
                            case "literal" -> cellContent = ".nf" + LF + manify(cell.getText(), Whitespace.PRESERVE)
                                    + LF + ".fi";
                            default -> {
                                List<String> paragraphs = new ArrayList<>();
                                for (String p : cell.getParagraphs()) {
                                    paragraphs.add(manify(p, Whitespace.NORMALIZE));
                                }
                                cellContent = String.join(LF + ".sp" + LF, paragraphs);
                            }
                        }
                        text.add(cellContent + LF);
                    } else { // tsec == head || tsec == foot
                        if (header.isEmpty() || header.get(cellIndex).isEmpty()) {
                            header.get(cellIndex).add(cellHalign + "tB");
                        } else {
                            cellAt(header, cellIndex + 1).add(cellHalign + "tB");
                        }
                        text.add(manify(cell.getText(), Whitespace.NORMALIZE) + LF);
                    }
                    if (cell.getColspan() != null && cell.getColspan() > 1) {
                        for (int i = 0; i < cell.getColspan() - 1; i++) {
                            if (header.isEmpty() || header.get(cellIndex).isEmpty()) {
                                cellAt(header, cellIndex + i).add("st");
                            } else {
                                cellAt(header, cellIndex + 1 + i).add("st");
                            }
                        }
                    }
                    if (cell.getRowspan() != null && cell.getRowspan() > 1) {
                        for (int i = 0; i < cell.getRowspan() - 1; i++) {
                            ensureSize(rowHeader, rowIndex + 2 + i);
                            if (rowHeader.get(rowIndex + 1 + i) == null) {
                                rowHeader.set(rowIndex + 1 + i, new ArrayList<>());
                            }
                            List<List<String>> spanned = rowHeader.get(rowIndex + 1 + i);
                            if (spanned.isEmpty() || cellIndex >= spanned.size() || spanned.get(cellIndex) == null
                                    || spanned.get(cellIndex).isEmpty()) {
                                cellAt(spanned, cellIndex).add("^t");
                            } else {
                                cellAt(spanned, cellIndex + 1).add("^t");
                            }
                        }
                    }
                    if (remainingCells >= 1) {
                        text.add("T}:");
                    } else {
                        text.add("T}" + LF);
                    }
                }
                rowIndex++;
            }
        }

        if (node.hasHeaderOption() && !rowText.isEmpty() && rowText.get(0) != null) {
            List<String> headerRowText = rowText.get(0);
            result.add(LF + joinFlattened(rowHeader.get(0)) + ".");
            result.add(LF + String.join("", headerRowText));
            result.add(".T&");
            rowText = rowText.subList(1, rowText.size());
        }
        List<String> lts = new ArrayList<>();
        for (int i = 0; i < (rowHeader.isEmpty() || rowHeader.get(0) == null ? 0 : rowHeader.get(0).size());
                i++) {
            lts.add("lt");
        }
        result.add(LF + String.join(" ", lts) + "." + LF);
        for (List<String> row : rowText) {
            result.add(row == null ? "" : String.join("", row));
        }
        result.add(".TE" + LF + ".sp");
        return String.join("", result);
    }

    private static <T> void ensureSize(List<T> list, int size) {
        while (list.size() < size) {
            list.add(null);
        }
    }

    private static List<String> cellAt(List<List<String>> header, int index) {
        ensureSize(header, index + 1);
        if (header.get(index) == null) {
            header.set(index, new ArrayList<>());
        }
        return header.get(index);
    }

    private static String joinFlattened(List<List<String>> header) {
        List<String> flattened = new ArrayList<>();
        for (List<String> cell : header) {
            if (cell == null) {
                flattened.add("");
            } else {
                flattened.addAll(cell);
            }
        }
        return String.join(" ", flattened);
    }

    public String convertThematicBreak(AbstractBlock node) {
        return ".sp\n.ce\n\\l'\\n(.lu*25u/100u\\(ap'";
    }

    public String convertUlist(org.asciidoctor.core.List node) {
        List<String> result = new ArrayList<>();
        if (node.hasTitle()) {
            result.add(".sp\n.B " + manify(node.getTitle(), Whitespace.COLLAPSE) + "\n.br");
        }
        for (AbstractBlock block : node.getItems()) {
            ListItem item = (ListItem) block;
            String listText = manify(item.getText(), Whitespace.NORMALIZE);
            result.add(".sp\n"
                    + ".RS 4\n"
                    + ".ie n \\{\\\n"
                    + "\\h'-04'\\(bu\\h'+03'\\c\n"
                    + ".\\}\n"
                    + ".el \\{\\\n"
                    + ".  sp -1\n"
                    + ".  IP \\(bu 2.3\n"
                    + ".\\}" + (listText.isEmpty() ? "" : LF + listText));
            if (item.hasBlocks()) {
                String itemContent = item.getContent();
                if (listText.isEmpty() && itemContent.startsWith(".sp\n")) {
                    itemContent = itemContent.substring(4);
                }
                result.add(itemContent);
            }
            result.add(".RE");
        }
        return String.join(LF, result);
    }

    public String convertVerse(AbstractBlock node) {
        List<String> result = new ArrayList<>();
        if (node.hasTitle()) {
            result.add(".sp\n.B " + manify(node.getTitle(), Whitespace.COLLAPSE) + "\n.br");
        }
        String attributionLine = node.hasAttribute("citetitle") ? node.getAttribute("citetitle") + " " : null;
        attributionLine = node.hasAttribute("attribution") ? (attributionLine == null ? "" : attributionLine)
                + "\\(em " + node.getAttribute("attribution") : null;
        result.add(".sp\n"
                + ".nf\n"
                + manify(node.getContent(), Whitespace.PRESERVE) + "\n"
                + ".fi\n"
                + ".br");
        if (attributionLine != null) {
            result.add(".in +.5i\n"
                    + ".ll -.5i\n"
                    + attributionLine + "\n"
                    + ".in\n"
                    + ".ll");
        }
        return String.join(LF, result);
    }

    public String convertVideo(AbstractBlock node) {
        String startParam = node.hasAttribute("start") ? "&start=" + node.getAttribute("start") : "";
        String endParam = node.hasAttribute("end") ? "&end=" + node.getAttribute("end") : "";
        List<String> result = new ArrayList<>();
        result.add(node.hasTitle() ? ".sp\n.B " + manify(node.getTitle(), Whitespace.COLLAPSE) + "\n.br" : ".sp");
        result.add("<" + node.mediaUri(node.getStringAttribute("target")) + startParam + endParam + "> (video)");
        return String.join(LF, result);
    }

    public String convertInlineAnchor(Inline node) {
        String target = node.getTarget();
        switch (node.getType() == null ? "" : node.getType()) {
            case "link" -> {
                String macro;
                if (target.startsWith("mailto:")) {
                    macro = "MTO";
                    target = target.substring(7);
                } else {
                    macro = "URL";
                }
                String text = node.getText();
                if (target.equals(text)) {
                    text = "";
                } else {
                    text = text.replace("\"", ESC_BS + "(dq");
                }
                if (macro.equals("MTO")) {
                    target = target.replaceFirst("@", java.util.regex.Matcher.quoteReplacement(ESC_BS + "(at"));
                }
                return ESC_BS + "c" + LF + ESC_FS + macro + " \"" + target + "\" \"" + text + "\" ";
            }
            case "xref" -> {
                String text = node.getText();
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
                        boolean outer = !resolvingXref;
                        if (outer) {
                            resolvingXref = true;
                        }
                        try {
                            if (outer && (text = ref.getXreftext(node.getStringAttribute("xrefstyle", null,
                                    "xrefstyle"))) != null) {
                                if ("section".equals(ref.getContext()) && ((AbstractBlock) ref).getLevel() < 2
                                        && text.equals(((AbstractBlock) ref).getTitle())) {
                                    text = uppercasePcdata(text);
                                }
                            } else {
                                text = top != null ? "[^top]" : "[" + refid + "]";
                            }
                        } finally {
                            if (outer) {
                                resolvingXref = false;
                            }
                        }
                    } else {
                        text = "[" + refid + "]";
                    }
                }
                return text;
            }
            case "ref", "bibref" -> {
                // These are anchor points, which shouldn't be visible
                return "";
            }
            default -> {
                getLogger().warn("unknown anchor type: " + Strings.inspect(node.getType()));
                return null;
            }
        }
    }

    public String convertInlineBreak(Inline node) {
        return node.getText() + LF + ESC_FS + "br";
    }

    public String convertInlineButton(Inline node) {
        return "<" + ESC_BS + "fB>[" + ESC_BS + "0" + node.getText() + ESC_BS + "0]</" + ESC_BS + "fP>";
    }

    public String convertInlineCallout(Inline node) {
        return "<" + ESC_BS + "fB>(" + node.getText() + ")<" + ESC_BS + "fP>";
    }

    public String convertInlineFootnote(Inline node) {
        Object index = node.getAttribute("index");
        if (index != null) {
            return "[" + index + "]";
        } else if ("xref".equals(node.getType())) {
            return "[" + node.getText() + "]";
        }
        return null;
    }

    public String convertInlineImage(Inline node) {
        return node.hasAttribute("link") ? "[" + node.getAlt() + "] <" + node.getAttribute("link") + ">"
                : "[" + node.getAlt() + "]";
    }

    public String convertInlineIndexterm(Inline node) {
        return "visible".equals(node.getType()) ? node.getText() : "";
    }

    public String convertInlineKbd(Inline node) {
        List<?> keys = (List<?>) node.getAttribute("keys");
        return "<" + ESC_BS + "f(CR>" + (keys.size() == 1 ? String.valueOf(keys.get(0))
                : String.join(ESC_BS + "0+" + ESC_BS + "0", keys.stream().map(String::valueOf).toList())) + "</"
                + ESC_BS + "fP>";
    }

    public String convertInlineMenu(Inline node) {
        String caret = ESC_BS + "0" + ESC_BS + "(fc" + ESC_BS + "0";
        Object menu = node.getAttribute("menu");
        List<?> submenus = (List<?>) node.getAttribute("submenus");
        Object menuitem;
        if (submenus != null && !submenus.isEmpty()) {
            List<String> path = new ArrayList<>();
            for (Object item : submenus) {
                path.add("<" + ESC_BS + "fI>" + item + "</" + ESC_BS + "fP>");
            }
            String submenuPath = String.join(caret, path);
            return "<" + ESC_BS + "fI>" + menu + "</" + ESC_BS + "fP>" + caret + submenuPath + caret + "<" + ESC_BS
                    + "fI>" + node.getAttribute("menuitem") + "</" + ESC_BS + "fP>";
        } else if ((menuitem = node.getAttribute("menuitem")) != null) {
            return "<" + ESC_BS + "fI>" + menu + caret + menuitem + "</" + ESC_BS + "fP>";
        }
        return "<" + ESC_BS + "fI>" + menu + "</" + ESC_BS + "fP>";
    }

    // NOTE use fake XML elements to prevent creating artificial word boundaries
    public String convertInlineQuoted(Inline node) {
        return switch (node.getType() == null ? "" : node.getType()) {
            case "emphasis" -> "<" + ESC_BS + "fI>" + node.getText() + "</" + ESC_BS + "fP>";
            case "strong" -> "<" + ESC_BS + "fB>" + node.getText() + "</" + ESC_BS + "fP>";
            case "monospaced" -> "<" + ESC_BS + "f(CR>" + node.getText() + "</" + ESC_BS + "fP>";
            case "single" -> "<" + ESC_BS + "(oq>" + node.getText() + "</" + ESC_BS + "(cq>";
            case "double" -> "<" + ESC_BS + "(lq>" + node.getText() + "</" + ESC_BS + "(rq>";
            default -> node.getText();
        };
    }

    /**
     * Writes the alternate pages (i.e., pages that source the primary page) for the additional man names.
     *
     * @param mannames the List of man names (the first is the primary page)
     * @param manvolnum the man volume number
     * @param target the path of the primary page
     */
    public static void writeAlternatePages(Object mannames, String manvolnum, String target) {
        if (!(mannames instanceof List<?> names) || names.size() <= 1) {
            return;
        }
        List<?> alternates = names.subList(1, names.size());
        String manvolext = "." + manvolnum;
        String dir = Helpers.dirname(target);
        String basename = Helpers.basename(target);
        for (Object manname : alternates) {
            Helpers.writeFile(Helpers.joinPath(dir, manname + manvolext), ".so " + basename);
        }
    }

    private void appendFootnotes(List<String> result, Document node) {
        if (!(node.hasFootnotes() && !node.hasAttribute("nofootnotes"))) {
            return;
        }
        result.add(".SH \"NOTES\"");
        for (Footnote fn : node.getFootnotes()) {
            result.add(".IP [" + fn.index() + "]");
            // NOTE restore newline in escaped macro that gets removed by normalize_text in substitutor
            String text = fn.text();
            if (text.contains(ESC + "\\c " + ESC + ".")) {
                text = Strings.chomp(manify(Rx.gsub(MALFORMED_ESCAPED_MACRO_RX, text,
                        m -> m.group(1) + LF + m.group(2)) + " ", Whitespace.NORMALIZE), " ");
            } else {
                text = manify(text, Whitespace.NORMALIZE);
            }
            result.add(text);
        }
    }

    /**
     * Converts HTML entity references back to their original form, escapes special man characters and strips trailing
     * whitespace.
     *
     * <p>It's crucial that text only ever pass through manify once.
     *
     * @param str the String to convert
     * @param whitespace an enum that indicates how to handle whitespace
     * @return the converted String
     */
    private static String manify(String str, Whitespace whitespace) {
        if (str == null) {
            return "";
        }
        switch (whitespace) {
            case PRESERVE -> str = Rx.gsub(REPEATING_SPACES_RX, str.replace(TAB, ET),
                    m -> m.group(1) != null ? m.group() : ESC_BS + "&" + m.group());
            case NORMALIZE -> str = Rx.gsub(WRAPPED_INDENT_RX, str, LF);
            default -> str = Strings.trS(str, WHITESPACE, " ");
        }
        // literal backslash (not a troff escape sequence)
        str = Rx.gsub(LITERAL_BACKSLASH_RX, str, m -> m.group(1) != null ? m.group() : "\\(rs");
        // horizontal ellipsis (emulate appearance)
        str = Rx.gsub(ELLIPSIS_CHAR_REF_RX, str, ".\\|.\\|.");
        // leading . is used in troff for macro call or other formatting; replace with \&.
        str = Rx.gsub(LEADING_PERIOD_RX, str, "\\&.");
        // drop orphaned \c escape lines, unescape troff macro, quote adjacent character, isolate macro line
        str = Rx.gsub(ESCAPED_MACRO_RX, str, m -> {
            String rest = Strings.lstrip(m.group(3));
            return rest.isEmpty() ? "." + m.group(1) + "\"" + m.group(2) + "\""
                    : "." + m.group(1) + "\"" + Strings.rstrip(m.group(2)) + "\"" + LF + rest;
        });
        str = str.replace("-", "\\-")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&#43;", "+") // plus sign; alternately could use \c(pl
                .replace("&#160;", "\\~") // non-breaking space
                .replace("&#169;", "\\(co") // copyright sign
                .replace("&#174;", "\\(rg") // registered sign
                .replace("&#8482;", "\\(tm") // trademark sign
                .replace("&#176;", "\\(de") // degree sign
                .replace("&#8201;", " ") // thin space
                .replace("&#8211;", "\\(en"); // en dash
        str = Rx.gsub(EM_DASH_CHAR_REF_RX, str, "\\(em"); // em dash
        str = str.replace("&#8216;", "\\(oq") // left single quotation mark
                .replace("&#8217;", "\\(cq") // right single quotation mark
                .replace("&#8220;", "\\(lq") // left double quotation mark
                .replace("&#8221;", "\\(rq") // right double quotation mark
                .replace("&#8592;", "\\(<-") // leftwards arrow
                .replace("&#8594;", "\\(->") // rightwards arrow
                .replace("&#8656;", "\\(lA") // leftwards double arrow
                .replace("&#8658;", "\\(rA") // rightwards double arrow
                .replace("&#8203;", "\\:") // zero width space
                .replace("&amp;", "&") // literal ampersand (NOTE must take place after any other replacement that
                                       // includes &)
                .replace("'", "\\*(Aq"); // apostrophe / neutral single quote
        str = Rx.gsub(MOCK_MACRO_RX, str, m -> m.group(1)); // mock boundary
        str = str.replace(ESC_BS, "\\") // unescape troff backslash (NOTE update if more escapes are added)
                .replace(ESC_FS, "."); // unescape full stop in troff commands (NOTE must take place after
                                       // gsub(LeadingPeriodRx))
        return Strings.rstrip(str); // strip trailing space
    }

    private static String uppercasePcdata(String string) {
        if (Rx.matches(XML_MARKUP_RX, string)) {
            return Rx.gsub(PCDATA_FILTER_RX, string, m -> m.group(2) != null ? m.group(2).toUpperCase(Locale.ROOT)
                    : m.group(1));
        }
        return string.toUpperCase(Locale.ROOT);
    }

    private static String encloseContent(AbstractBlock node) {
        return node.getContentModel() == ContentModel.COMPOUND ? node.getContent()
                : ".sp" + LF + manify(node.getContent(), Whitespace.NORMALIZE);
    }

    private static Document getRootDocument(AbstractNode node) {
        Document doc = node.getDocument();
        while (doc.isNested()) {
            doc = doc.getParentDocument();
        }
        return doc;
    }
}
