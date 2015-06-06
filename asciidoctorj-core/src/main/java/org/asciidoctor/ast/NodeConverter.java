package org.asciidoctor.ast;

import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubySymbol;
import org.jruby.java.proxies.RubyObjectHolderProxy;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * A library class that allows to convert nodes from Asciidoctor Ruby
 * to its AsciidoctorJ counterparts.
 */
public final class NodeConverter {

    private static final String BLOCK_CLASS = "Asciidoctor::Block";

    private static final String SECTION_CLASS = "Asciidoctor::Section";

    private static final String DOCUMENT_CLASS = "Asciidoctor::Document";

    private static final String INLINE_CLASS = "Asciidoctor::Inline";

    private static final String LIST_CLASS = "Asciidoctor::List";

    private static final String LIST_ITEM_CLASS = "Asciidoctor::ListItem";

    private NodeConverter() {}

    public static AbstractNode createASTNode(Object object) {

        if (object instanceof IRubyObject || object instanceof RubyObjectHolderProxy) {
            IRubyObject rubyObject = asRubyObject(object);
            NodeCache nodeCache = NodeCache.get(rubyObject);
            AbstractNode cachedNode = nodeCache.getASTNode();
            if (cachedNode != null) {
                return cachedNode;
            }

            Ruby runtime = rubyObject.getRuntime();

            String rubyClassName = rubyObject.getMetaClass().getRealClass().getName();
            AbstractNode ret = null;
            if (BLOCK_CLASS.equals(rubyClassName)) {
                ret = new BlockImpl(rubyObject);
            } else if (SECTION_CLASS.equals(rubyClassName)) {
                ret = new SectionImpl(rubyObject);
            } else if (DOCUMENT_CLASS.equals(rubyClassName)) {
                ret = new Document(rubyObject);
            } else if (INLINE_CLASS.equals(rubyClassName)) {
                ret = new InlineImpl(rubyObject);
            } else if (LIST_CLASS.equals(rubyClassName)) {
                ret = new ListImpl(rubyObject);
            } else if (LIST_ITEM_CLASS.equals(rubyClassName)) {
                ret = new ListItemImpl(rubyObject);
            } else {
                throw new IllegalArgumentException("Don't know what to do with a " + rubyObject);
            }

            nodeCache.setASTNode(ret);

            return ret;
        } else if (object instanceof AbstractNode) {

            return (AbstractNode) object;

        } else {
            throw new IllegalArgumentException(object != null ? object.toString() : "null");
        }
    }

    public static IRubyObject asRubyObject(Object o) {
        if (o instanceof IRubyObject) {
            return (IRubyObject) o;
        } else if (o instanceof RubyObjectHolderProxy) {
            return ((RubyObjectHolderProxy) o).__ruby_object();
        } else {
            throw new IllegalArgumentException(o.getClass() + " is not a IRubyObject nor a RubyObjectHolderProxy!");
        }
    }
}
