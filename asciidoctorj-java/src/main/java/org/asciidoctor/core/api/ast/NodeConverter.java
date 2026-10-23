package org.asciidoctor.core.api.ast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.AbstractNode;
import org.asciidoctor.core.DescriptionListEntry;
import org.asciidoctor.core.Document;
import org.asciidoctor.core.Inline;
import org.asciidoctor.core.ListItem;
import org.asciidoctor.core.Section;
import org.asciidoctor.core.Table;

/**
 * Converts the nodes of the implementation to the nodes of the AsciidoctorJ API, and vice versa.
 *
 * <p>The node of the API that wraps a node is created on demand and cached on the node, so a node is always
 * represented by the same instance (like the NodeCache of AsciidoctorJ).
 */
public final class NodeConverter {

    private NodeConverter() {
    }

    /**
     * @return the node of the API that wraps the node, or null if the node is null
     */
    @SuppressWarnings("unchecked")
    public static <T extends ContentNode> T wrap(AbstractNode node) {
        if (node == null) {
            return null;
        }
        Object apiNode = node.getApiNode();
        if (apiNode == null) {
            apiNode = createApiNode(node);
            node.setApiNode(apiNode);
        }
        return (T) apiNode;
    }

    private static ContentNode createApiNode(AbstractNode node) {
        if (node instanceof Document document) {
            return new DocumentImpl(document);
        } else if (node instanceof Section section) {
            return new SectionImpl(section);
        } else if (node instanceof Table table) {
            return new TableImpl(table);
        } else if (node instanceof Table.Cell cell) {
            return new CellImpl(cell);
        } else if (node instanceof Table.Column column) {
            return new ColumnImpl(column);
        } else if (node instanceof org.asciidoctor.core.List list) {
            return "dlist".equals(list.getContext()) ? new DescriptionListImpl(list) : new ListImpl(list);
        } else if (node instanceof ListItem listItem) {
            return new ListItemImpl(listItem);
        } else if (node instanceof DescriptionListEntry entry) {
            return new DescriptionListEntryImpl(entry);
        } else if (node instanceof org.asciidoctor.core.Block block) {
            return new BlockImpl(block);
        } else if (node instanceof Inline inline) {
            return new PhraseNodeImpl(inline);
        } else if (node instanceof AbstractBlock block) {
            return new StructuralNodeImpl<>(block);
        }
        throw new IllegalArgumentException("Don't know what to do with a " + node.getClass().getName());
    }

    /**
     * @return the node of the implementation wrapped by the node of the API (or the node itself if it's already a node
     * of the implementation), or null if the node is null
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractNode> T unwrap(Object node) {
        if (node == null) {
            return null;
        } else if (node instanceof ContentNodeImpl<?> apiNode) {
            return (T) apiNode.getDelegate();
        } else if (node instanceof AbstractNode abstractNode) {
            return (T) abstractNode;
        }
        throw new IllegalArgumentException(node.getClass().getName() + " is not a node of the implementation");
    }

    /**
     * Converts the value returned by the implementation to a value of the API (i.e., wraps the nodes, including the
     * nodes in lists and maps).
     */
    public static Object toApi(Object value) {
        if (value instanceof AbstractNode node) {
            return wrap(node);
        } else if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>(list.size());
            list.forEach(item -> result.add(toApi(item)));
            return result;
        } else if (value instanceof Map<?, ?> map) {
            Map<Object, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put(key, toApi(item)));
            return result;
        }
        return value;
    }

    /**
     * Converts the value of the API to a value of the implementation (i.e., unwraps the nodes, including the nodes
     * in lists).
     */
    public static Object fromApi(Object value) {
        if (value instanceof ContentNodeImpl<?> node) {
            return node.getDelegate();
        } else if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>(list.size());
            list.forEach(item -> result.add(fromApi(item)));
            return result;
        }
        return value;
    }
}
