package org.asciidoctor.internal;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.AbstractBlockImpl;
import org.asciidoctor.ast.NodeConverter;
import org.jruby.RubyArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;



public class RubyBlockListDecorator implements List<AbstractBlock> {

    private final RubyArray rubyBlockList;

    public RubyBlockListDecorator(RubyArray rubyBlockList) {
        this.rubyBlockList = rubyBlockList;
    }

    @Override
    public int size() {
        return rubyBlockList.size();
    }

    @Override
    public boolean isEmpty() {
        return rubyBlockList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof AbstractBlockImpl)) {
            return false;
        }
        return rubyBlockList.contains(((AbstractBlockImpl) o).getRubyObject());
    }

    @Override
    public Iterator<AbstractBlock> iterator() {
        return new Iterator<AbstractBlock>() {
            int index = 0;
            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public AbstractBlock next() {
                return get(index++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Object[] toArray() {
        return getJavaBlockList().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getJavaBlockList().toArray(a);
    }

    private List<AbstractBlock> getJavaBlockList() {
        List<AbstractBlock> javaBlockList = new ArrayList<AbstractBlock>(rubyBlockList.size());
        Object[] ret = new Object[size()];
        for (int i = 0; i < rubyBlockList.size(); i++) {
            javaBlockList.add((AbstractBlock) NodeConverter.createASTNode(rubyBlockList.get(i)));
        }
        return javaBlockList;
    }

    @Override
    public boolean add(AbstractBlock abstractBlock) {
        return rubyBlockList.add(((AbstractBlockImpl) abstractBlock).getRubyObject());
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof AbstractBlock)) {
            return false;
        }
        return rubyBlockList.remove(((AbstractBlockImpl) o).getRubyObject());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return rubyBlockList.containsAll(getDelegateCollection(c));
    }

    @Override
    public boolean addAll(Collection<? extends AbstractBlock> c) {
        return rubyBlockList.addAll(getDelegateCollection(c));
    }

    @Override
    public boolean addAll(int index, Collection<? extends AbstractBlock> c) {
        return rubyBlockList.addAll(index, getDelegateCollection(c));
    }

    private Collection<Object> getDelegateCollection(Collection<?> c) {
        Collection<Object> delegateList = new ArrayList<Object>(c.size());
        for (Object o: c) {
            if (o instanceof AbstractBlockImpl) {
                delegateList.add(((AbstractBlockImpl) o).getRubyObject());
            } else {
                delegateList.add(o);
            }
        }
        return delegateList;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return rubyBlockList.removeAll(getDelegateCollection(c));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return rubyBlockList.retainAll(getDelegateCollection(c));
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
    public AbstractBlock get(int index) {
        return (AbstractBlock) NodeConverter.createASTNode(rubyBlockList.get(index));
    }

    @Override
    public AbstractBlock set(int index, AbstractBlock element) {
        Object oldObject = rubyBlockList.set(index, ((AbstractBlockImpl) element).getRubyObject());
        return (AbstractBlock) NodeConverter.createASTNode(oldObject);
    }

    @Override
    public void add(int index, AbstractBlock element) {
        rubyBlockList.add(index, ((AbstractBlockImpl) element).getRubyObject());
    }

    @Override
    public AbstractBlock remove(int index) {
        Object oldObject = rubyBlockList.remove(index);
        if (oldObject == null) {
            return null;
        } else {
            return (AbstractBlock) NodeConverter.createASTNode(oldObject);
        }
    }

    @Override
    public int indexOf(Object o) {
        if (o instanceof AbstractBlock) {
            return rubyBlockList.indexOf(((AbstractBlockImpl) o).getRubyObject());
        } else {
            return -1;
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o instanceof AbstractBlock) {
            return rubyBlockList.lastIndexOf(((AbstractBlockImpl) o).getRubyObject());
        } else {
            return -1;
        }
    }

    @Override
    public ListIterator<AbstractBlock> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<AbstractBlock> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AbstractBlock> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
}
