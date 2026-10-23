package org.asciidoctor.core;

import java.util.Map;

import org.asciidoctor.core.converter.Converter;

/**
 * Methods for managing inline elements in AsciiDoc block.
 */
public class Inline extends AbstractNode {
    private String text;
    private final String type;
    private String target;

    /**
     * Initialize an Inline object.
     *
     * @param parent the parent node
     * @param context the context of the inline node (e.g., anchor, quoted)
     * @param text the text of the inline node (may be null)
     * @param type the type (qualifier) of this inline element (e.g., link, strong) (may be null)
     * @param id the id of this inline element (may be null)
     * @param target the target (e.g., uri) of this inline element (may be null)
     * @param attributes the attributes of this inline element (may be null)
     */
    public Inline(AbstractNode parent, String context, String text, String type, String id, String target,
            Map<String, Object> attributes) {
        super(parent, context, attributes);
        this.nodeName = "inline_" + context;
        this.text = text;
        this.id = id;
        this.type = type;
        this.target = target;
    }

    /**
     * Initialize an Inline object using an options Map.
     *
     * @param parent the parent node
     * @param context the context of the inline node (e.g., anchor, quoted)
     * @param text the text of the inline node (may be null)
     * @param opts a Map of options (id, type, target and attributes)
     */
    @SuppressWarnings("unchecked")
    public Inline(AbstractNode parent, String context, String text, Map<String, Object> opts) {
        this(parent, context, text, opts == null ? null : Strings.valueOf(opts.get("type")),
                opts == null ? null : Strings.valueOf(opts.get("id")),
                opts == null ? null : Strings.valueOf(opts.get("target")),
                opts == null ? null : (Map<String, Object>) opts.get("attributes"));
    }

    public Inline(AbstractNode parent, String context, String text) {
        this(parent, context, text, null, null, null, null);
    }

    public Inline(AbstractNode parent, String context) {
        this(parent, context, null, null, null, null, null);
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public boolean isInline() {
        return true;
    }

    @Override
    public String convert() {
        return Converter.asString(getConverter().convert(this));
    }

    /**
     * @return the text of this inline element
     */
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the type (qualifier) of this inline element
     */
    public String getType() {
        return type;
    }

    /**
     * @return the target (e.g., uri) of this inline element
     */
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Get the converted result of this node's primary content (aka text).
     */
    public String getContent() {
        return text;
    }

    /**
     * Returns the converted alt text for this inline image.
     *
     * @return the String value of the alt attribute.
     */
    public String getAlt() {
        Object alt = getAttribute("alt");
        return alt == null ? "" : alt.toString();
    }

    /**
     * For a reference node (ref or bibref), the text is the reftext (and the reftext attribute is not set).
     */
    @Override
    public boolean isReftext() {
        return text != null && ("ref".equals(type) || "bibref".equals(type));
    }

    /**
     * For a reference node (ref or bibref), the text is the reftext (and the reftext attribute is not set).
     */
    @Override
    public String getReftext() {
        return text != null ? applyReftextSubs(text) : null;
    }

    /**
     * Generate cross reference text (xreftext) that can be used to refer to this inline node.
     *
     * <p>Use the explicit reftext for this inline node, if specified, retrieved by calling the reftext method.
     * Otherwise, returns null.
     *
     * @param xrefstyle Not currently used (default: null).
     * @return the String reftext to refer to this inline node or null if no reftext is defined.
     */
    @Override
    public String getXreftext(String xrefstyle) {
        return getReftext();
    }

}
