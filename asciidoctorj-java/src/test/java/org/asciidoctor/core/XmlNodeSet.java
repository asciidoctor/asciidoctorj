package org.asciidoctor.core;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A list of nodes, which mirrors the parts of the Nokogiri NodeSet API used by the tests.
 */
public final class XmlNodeSet extends ArrayList<XmlNode> {

    public XmlNodeSet() {
    }

    public XmlNodeSet(Collection<XmlNode> nodes) {
        super(nodes);
    }

    /**
     * @return the text content of the nodes in this set, concatenated
     */
    public String getText() {
        StringBuilder result = new StringBuilder();
        forEach(node -> result.append(node.getText()));
        return result.toString();
    }

    /**
     * @return the serialized markup of the nodes in this set, concatenated
     */
    public String toHtml() {
        StringBuilder result = new StringBuilder();
        forEach(node -> result.append(node.toHtml()));
        return result.toString();
    }
}
