package org.asciidoctor.jruby.internal;

import org.asciidoctor.jruby.ast.impl.ContentNodeImpl;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;


public class RubyBlockListDecorator<T> extends AbstractList<T> {

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
        if (!(o instanceof ContentNodeImpl)) {
            return false;
        }
        return rubyBlockList.contains(((ContentNodeImpl) o).getRubyObject());
    }

    @Override
    public boolean add(T abstractBlock) {
        return rubyBlockList.add(((ContentNodeImpl) abstractBlock).getRubyObject());
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof IRubyObject) {
            return rubyBlockList.remove(o);
        }
        if (!(o instanceof ContentNodeImpl)) {
            return false;
        }
        return rubyBlockList.remove(((ContentNodeImpl) o).getRubyObject());
    }

    private Collection<Object> getDelegateCollection(Collection<?> c) {
        Collection<Object> delegateList = new ArrayList<Object>(c.size());
        for (Object o: c) {
            if (o instanceof ContentNodeImpl) {
                delegateList.add(((ContentNodeImpl) o).getRubyObject());
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
        Object oldObject;
        if (element instanceof IRubyObject) {
            oldObject = rubyBlockList.set(index, element);
        } else {
            oldObject = rubyBlockList.set(index, ((ContentNodeImpl) element).getRubyObject());
        }
        return (T) NodeConverter.createASTNode(oldObject);
    }

    @Override
    public void add(int index, T element) {
        if (element instanceof IRubyObject) {
            rubyBlockList.set(index, element);
        } else {
            rubyBlockList.add(index, ((ContentNodeImpl) element).getRubyObject());
        }
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
        if (o instanceof IRubyObject) {
            return rubyBlockList.indexOf(o);
        } else if (o instanceof ContentNodeImpl) {
            return rubyBlockList.indexOf(((ContentNodeImpl) o).getRubyObject());
        } else {
            return -1;
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o instanceof IRubyObject) {
            return rubyBlockList.lastIndexOf(o);
        } else if (o instanceof ContentNodeImpl) {
            return rubyBlockList.lastIndexOf(((ContentNodeImpl) o).getRubyObject());
        } else {
            return -1;
        }
    }
}
