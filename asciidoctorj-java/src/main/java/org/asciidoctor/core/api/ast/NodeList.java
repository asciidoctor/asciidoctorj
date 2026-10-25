package org.asciidoctor.core.api.ast;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

import org.asciidoctor.core.AbstractNode;

/**
 * A live view of a list of nodes of the implementation as a list of nodes of the API: the changes made to this list
 * are applied to the underlying list (like the RubyBlockListDecorator of AsciidoctorJ).
 *
 * @param <T> the type of the nodes of the API
 */
final class NodeList<T> extends AbstractList<T> implements RandomAccess {
    private final List<Object> nodes;

    @SuppressWarnings("unchecked")
    NodeList(List<? extends AbstractNode> nodes) {
        this.nodes = (List<Object>) (List<?>) nodes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) NodeConverter.wrap((AbstractNode) nodes.get(index));
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public T set(int index, T element) {
        T previous = get(index);
        nodes.set(index, NodeConverter.unwrap(element));
        return previous;
    }

    @Override
    public void add(int index, T element) {
        nodes.add(index, NodeConverter.unwrap(element));
    }

    @Override
    public T remove(int index) {
        T previous = get(index);
        nodes.remove(index);
        return previous;
    }

    @Override
    public int indexOf(Object o) {
        return o instanceof ContentNodeImpl<?> node ? nodes.indexOf(node.getDelegate()) : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return o instanceof ContentNodeImpl<?> node ? nodes.lastIndexOf(node.getDelegate()) : -1;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }
}
