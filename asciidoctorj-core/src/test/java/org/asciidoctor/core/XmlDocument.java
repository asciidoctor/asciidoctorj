package org.asciidoctor.core;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A parsed HTML or XML document (or fragment) that can be queried using XPath or CSS selectors, mirroring the behavior
 * of Nokogiri as used by the Ruby test suite.
 */
public final class XmlDocument {
    private static final Pattern RE_XMLNS_ATTRIBUTE = Pattern.compile(" xmlns=\"[^\"]+\"");
    private static final Pattern RE_DOCTYPE = Pattern.compile("\\s*<!DOCTYPE (.*)");

    private static final Pattern RE_XMLNS_PREFIX = Pattern.compile("\\bxmlns:(?=[a-zA-Z_*])");
    // the xmlns prefix is reserved by the XPath engine, so the default namespace is mapped to this prefix instead
    private static final String DEFAULT_NAMESPACE_PREFIX = "_default";

    private final Node root;
    private final String defaultNamespace;
    private final boolean html;

    private XmlDocument(Node root, String defaultNamespace, boolean html) {
        this.root = root;
        this.defaultNamespace = defaultNamespace;
        this.html = html;
    }

    /**
     * Parses the content as an XML document, an HTML document or an HTML fragment depending on its contents.
     */
    public static XmlDocument parse(String content) {
        if (content.startsWith("<?xml ") || RE_XMLNS_ATTRIBUTE.matcher(content).find()) {
            return parseXml(content);
        }
        Matcher m = RE_DOCTYPE.matcher(content);
        if (!m.lookingAt()) {
            return new XmlDocument(HtmlParser.parseFragment(content), null, true);
        } else if (m.group(1).startsWith("html")) {
            return new XmlDocument(HtmlParser.parseDocument(content), null, true);
        }
        return parseXml(content);
    }

    private static XmlDocument parseXml(String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(content)));
            Element documentElement = doc.getDocumentElement();
            String ns = documentElement == null ? null : documentElement.getNamespaceURI();
            return new XmlDocument(doc, ns, false);
        } catch (Exception e) {
            // fall back to the lenient parser
            return new XmlDocument(HtmlParser.parseFragment(content), null, true);
        }
    }

    /**
     * @return whether this document was parsed as HTML
     */
    public boolean isHtml() {
        return html;
    }

    public Node getRoot() {
        return root;
    }

    /**
     * Evaluates the XPath expression and returns the result: a List of XmlNode objects for a node-set, otherwise a
     * Boolean, Double or String.
     */
    public Object xpath(String expression, Node context) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return switch (prefix) {
                    case "xml" -> XMLConstants.XML_NS_URI;
                    case DEFAULT_NAMESPACE_PREFIX -> defaultNamespace == null ? XMLConstants.NULL_NS_URI : defaultNamespace;
                    case "xl", "xlink" -> "http://www.w3.org/1999/xlink";
                    case "mml" -> "http://www.w3.org/1998/Math/MathML";
                    default -> XMLConstants.NULL_NS_URI;
                };
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                return List.<String>of().iterator();
            }
        });
        String expr = RE_XMLNS_PREFIX.matcher(expression).replaceAll(DEFAULT_NAMESPACE_PREFIX + ":");
        if (!(root instanceof Document doc && doc.getDocumentElement() != null
                && doc.getDocumentElement().getNamespaceURI() != null)) {
            // non-namespace-aware DOM: refer to the xml:id attribute by name
            expr = expr.replace("@xml:id", "@*[name()='xml:id']").replace("@xml:lang", "@*[name()='xml:lang']")
                    .replace("@xl:href", "@*[name()='xl:href']");
        }
        try {
            NodeList nodes = (NodeList) xpath.evaluate(expr, context, XPathConstants.NODESET);
            XmlNodeSet result = new XmlNodeSet();
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(new XmlNode(nodes.item(i), this));
            }
            return result;
        } catch (XPathExpressionException e) {
            try {
                Object value = xpath.evaluate(expr, context, XPathConstants.BOOLEAN);
                if (expr.trim().startsWith("count(") || expr.trim().startsWith("string-length(")) {
                    return xpath.evaluate(expr, context, XPathConstants.NUMBER);
                }
                return value;
            } catch (XPathExpressionException e2) {
                throw new IllegalArgumentException("Invalid XPath expression: " + expression, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    XmlNodeSet xpathNodes(String expression, Node context) {
        Object result = xpath(expression, context);
        return result instanceof XmlNodeSet nodes ? nodes : new XmlNodeSet();
    }

    /**
     * Evaluates the XPath expression relative to the root of this document (the leading slash is replaced with ./ as
     * done by the Ruby test helper).
     */
    public Object xpath(String expression) {
        return xpath(expression.replaceFirst("/", "./"), root);
    }

    /**
     * Evaluates the CSS selector relative to the root of this document.
     */
    public XmlNodeSet css(String css) {
        return xpathNodes(CssToXpath.convert(css, getElementPrefix()), root);
    }

    /**
     * @return the namespace prefix to use for element names in CSS selectors (Nokogiri matches element names in the
     * default namespace of the document)
     */
    String getElementPrefix() {
        return defaultNamespace == null ? null : DEFAULT_NAMESPACE_PREFIX;
    }

    static String serialize(Node node) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            return node.getTextContent();
        }
    }
}
