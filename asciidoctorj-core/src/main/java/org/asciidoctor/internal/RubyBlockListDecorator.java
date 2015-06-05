package org.asciidoctor.internal;

import org.asciidoctor.ast.AbstractNode;
import org.asciidoctor.ast.AbstractNodeImpl;
import org.asciidoctor.ast.NodeConverter;
import org.jruby.RubyArray;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;



public class RubyBlockListDecorator<T extends AbstractNode> extends AbstractList<T> {

    private final RubyArray rubyBlockList;

    public RubyBlockListDecorator(RubyArray rubyBlockList) {
        this.rubyBlockList = rubyBlockList;
    }

    @Override
    public int size() {
        return rubyBlockList.size();
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof AbstractNodeImpl)) {
            return false;
        }
        return rubyBlockList.contains(((AbstractNodeImpl) o).getRubyObject());
    }

    @Override
    public boolean add(T abstractBlock) {
        return rubyBlockList.add(((AbstractNodeImpl) abstractBlock).getRubyObject());
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof AbstractNodeImpl)) {
            return false;
        }
        return rubyBlockList.remove(((AbstractNodeImpl) o).getRubyObject());
    }

    private Collection<Object> getDelegateCollection(Collection<?> c) {
        Collection<Object> delegateList = new ArrayList<Object>(c.size());
        for (Object o: c) {
            if (o instanceof AbstractNodeImpl) {
                delegateList.add(((AbstractNodeImpl) o).getRubyObject());
            } else {
                delegateList.add(o);
            }
        }
        return delegateList;
    }

    @Override
    public void clear() {
        rubyBlockList.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RubyBlockListDecorator) {
            return rubyBlockList.equals(((RubyBlockListDecorator) o).rubyBlockList);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return rubyBlockList.hashCode();
    }

    @Override
    public T get(int index) {
        return (T) NodeConverter.createASTNode(rubyBlockList.get(index));
    }

    @Override
    public T set(int index, T element) {
        Object oldObject = rubyBlockList.set(index, ((AbstractNodeImpl) element).getRubyObject());
        return (T) NodeConverter.createASTNode(oldObject);
    }

    @Override
    public void add(int index, T element) {
        rubyBlockList.add(index, ((AbstractNodeImpl) element).getRubyObject());
    }

    @Override
    public T remove(int index) {
        Object oldObject = rubyBlockList.remove(index);
        if (oldObject == null) {
            return null;
        } else {
            return (T) NodeConverter.createASTNode(oldObject);
        }
    }

    @Override
    public int indexOf(Object o) {
        if (o instanceof AbstractNodeImpl) {
            return rubyBlockList.indexOf(((AbstractNodeImpl) o).getRubyObject());
        } else {
            return -1;
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o instanceof AbstractNodeImpl) {
            return rubyBlockList.lastIndexOf(((AbstractNodeImpl) o).getRubyObject());
        } else {
            return -1;
        }
    }
}
