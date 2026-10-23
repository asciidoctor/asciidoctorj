package org.asciidoctor.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A thin wrapper around a W3C DOM node that mirrors the parts of the Nokogiri API used by the tests.
 */
public final class XmlNode {
    private final Node node;
    private final XmlDocument owner;

    XmlNode(Node node, XmlDocument owner) {
        this.node = node;
        this.owner = owner;
    }

    public Node getNode() {
        return node;
    }

    /**
     * @return the text content of this node
     */
    public String getText() {
        return node.getTextContent();
    }

    public String getContent() {
        return getText();
    }

    /**
     * @return the namespace declarations of this element and its ancestors, indexed by attribute name (e.g., xmlns or
     * xmlns:xl)
     */
    public Map<String, String> getNamespaces() {
        Map<String, String> namespaces = new LinkedHashMap<>();
        for (Node current = node; current instanceof Element element; current = current.getParentNode()) {
            NamedNodeMap attributes = element.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                String name = attribute.getNodeName();
                if (name.equals("xmlns") || name.startsWith("xmlns:")) {
                    namespaces.putIfAbsent(name, attribute.getNodeValue());
                }
            }
        }
        return namespaces;
    }

    /**
     * @return the value of this node (e.g., the value of an attribute node)
     */
    public String getValue() {
        return node.getNodeValue();
    }

    /**
     * @return the attribute node with the specified (qualified) name, or null if this element has no such attribute
     */
    public XmlNode attribute(String name) {
        if (node instanceof Element element && element.hasAttribute(name)) {
            return new XmlNode(element.getAttributeNode(name), owner);
        }
        return null;
    }

    /**
     * @return the name of this node (alias of {@link #getName()})
     */
    public String getNodeName() {
        return getName();
    }

    /**
     * @return the name of this node
     */
    public String getName() {
        return node.getNodeName();
    }

    /**
     * @return the value of the specified attribute, or null if not present
     */
    public String get(String name) {
        if (node instanceof Element element && element.hasAttribute(name)) {
            return element.getAttribute(name);
        }
        return null;
    }

    public String attr(String name) {
        return get(name);
    }

    public String getAttribute(String name) {
        return get(name);
    }

    public XmlNodeSet getChildren() {
        XmlNodeSet result = new XmlNodeSet();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            result.add(new XmlNode(children.item(i), owner));
        }
        return result;
    }

    /**
     * @return the element children of this node
     */
    public XmlNodeSet getElements() {
        XmlNodeSet result = new XmlNodeSet();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                result.add(new XmlNode(children.item(i), owner));
            }
        }
        return result;
    }

    /**
     * @return the next sibling of this node
     */
    public XmlNode getNext() {
        return node.getNextSibling() == null ? null : new XmlNode(node.getNextSibling(), owner);
    }

    /**
     * @return the previous sibling of this node
     */
    public XmlNode getPrevious() {
        return node.getPreviousSibling() == null ? null : new XmlNode(node.getPreviousSibling(), owner);
    }

    public XmlNode getParent() {
        return node.getParentNode() == null ? null : new XmlNode(node.getParentNode(), owner);
    }

    public XmlNodeSet xpath(String xpath) {
        return owner.xpathNodes(xpath, node);
    }

    public XmlNodeSet css(String css) {
        return owner.xpathNodes(CssToXpath.convert(css, owner.getElementPrefix()), node);
    }

    public XmlNode atXpath(String xpath) {
        XmlNodeSet nodes = xpath(xpath);
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    public XmlNode atCss(String css) {
        XmlNodeSet nodes = css(css);
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    /**
     * @return the serialized markup of this node
     */
    public String toHtml() {
        return serialize(node);
    }

    public String getInnerHtml() {
        StringBuilder result = new StringBuilder();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            result.append(serialize(children.item(i)));
        }
        return result.toString();
    }

    private String serialize(Node target) {
        if (!owner.isHtml()) {
            return XmlDocument.serialize(target);
        }
        StringBuilder result = new StringBuilder();
        HtmlParser.serialize(target, result);
        return result.toString();
    }

    @Override
    public String toString() {
        return toHtml();
    }
}
