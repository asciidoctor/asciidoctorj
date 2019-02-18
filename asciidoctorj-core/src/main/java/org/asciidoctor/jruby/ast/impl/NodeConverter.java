package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.jruby.internal.RubyObjectWrapper;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.java.proxies.RubyObjectHolderProxy;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * A library class that allows to convert nodes from Asciidoctor Ruby
 * to its AsciidoctorJRuby counterparts.
 */
public final class NodeConverter {

    public enum NodeType {

        BLOCK_CLASS("Asciidoctor", "Block"),

        SECTION_CLASS("Asciidoctor", "Section"),

        DOCUMENT_CLASS("Asciidoctor", "Document"),

        INLINE_CLASS("Asciidoctor", "Inline"),

        LIST_CLASS("Asciidoctor", "List") {
            @Override
            public boolean isInstance(IRubyObject object) {
                return super.isInstance(object) && !"dlist".equals(new RubyObjectWrapper(object).getString("context"));
            }
        },

        DEFINITIONLIST_CLASS("Asciidoctor", "List") {
            @Override
            public boolean isInstance(IRubyObject object) {
                return super.isInstance(object) && "dlist".equals(new RubyObjectWrapper(object).getString("context"));
            }
        },

        DEFINITIONLIST_ITEM_CLASS("Array") {
            @Override
            public boolean isInstance(IRubyObject object) {
                if (!super.isInstance(object)) {
                    return false;
                }
                RubyArray array = (RubyArray) object;
                boolean ret = array.size() == 2
                        && object.getRuntime().getArray().isInstance((IRubyObject) array.get(0))
                        && (null == array.get(1) || LIST_ITEM_CLASS.isInstance((IRubyObject) array.get(1)));
                return ret;
            }

        },

        LIST_ITEM_CLASS("Asciidoctor", "ListItem"),

        TABLE_CLASS("Asciidoctor", "Table"),

        TABLE_COLUMN_CLASS("Asciidoctor", "Table", "Column"),

        TABLE_CELL_CLASS("Asciidoctor", "Table", "Cell");

        private String[] path;

        NodeType(String... path) {
            this.path = path;
        }

        public RubyClass getRubyClass(Ruby runtime) {

            if (path.length == 1) {
                return runtime.getClass(path[0]);
            } else {
                RubyModule object = runtime.getModule(path[0]);

                RubyClass rubyClass = object.getClass(path[1]);

                if (path.length == 2) {
                    return rubyClass;
                } else {
                    return rubyClass.getClass(path[2]);
                }
            }
        }

        private static NodeType getNodeType(IRubyObject rubyObject) {
            for (NodeType nodeType: values()) {
                if (nodeType.isInstance(rubyObject)) {
                    return nodeType;
                }
            }
            throw new IllegalArgumentException("Don't know what to do with a " + rubyObject.getMetaClass());
        }

        /**
         * @param object
         * @return {@code true} if the given Ruby object is recognized as this node type.
         */
        public boolean isInstance(IRubyObject object) {
            Ruby rubyRuntime = object.getRuntime();
            return getRubyClass(rubyRuntime).equals(object.getMetaClass().getRealClass());
        }
    }

    private NodeConverter() {}

    public static ContentNode createASTNode(Ruby runtime, NodeType nodeType, IRubyObject... args) {
        IRubyObject node = nodeType.getRubyClass(runtime).callMethod(runtime.getCurrentContext(), "new", args);
        return createASTNode(node);
    }


    public static ContentNode createASTNode(Object object) {

        if (object instanceof IRubyObject || object instanceof RubyObjectHolderProxy) {
            IRubyObject rubyObject = asRubyObject(object);
            if (rubyObject.isNil()) {
                return null;
            }
            NodeCache nodeCache = NodeCache.get(rubyObject);
            ContentNode cachedNode = nodeCache.getASTNode();
            if (cachedNode != null) {
                return cachedNode;
            }

            ContentNode ret;

            switch (NodeType.getNodeType(rubyObject)) {
                case BLOCK_CLASS:
                    ret = new BlockImpl(rubyObject);
                    break;
                case SECTION_CLASS:
                    ret = new SectionImpl(rubyObject);
                    break;
                case DOCUMENT_CLASS:
                    ret = new DocumentImpl(rubyObject);
                    break;
                case INLINE_CLASS:
                    ret = new PhraseNodeImpl(rubyObject);
                    break;
                case LIST_CLASS:
                    ret = new ListImpl(rubyObject);
                    break;
                case LIST_ITEM_CLASS:
                    ret = new ListItemImpl(rubyObject);
                    break;
                case DEFINITIONLIST_CLASS:
                    ret = new DescriptionListImpl(rubyObject);
                    break;
                case DEFINITIONLIST_ITEM_CLASS:
                    ret = new DescriptionListEntryImpl(rubyObject);
                    break;
                case TABLE_CLASS:
                    ret = new TableImpl(rubyObject);
                    break;
                case TABLE_COLUMN_CLASS:
                    ret = new ColumnImpl(rubyObject);
                    break;
                case TABLE_CELL_CLASS:
                    ret = new CellImpl(rubyObject);
                    break;
                default:
                    throw new IllegalArgumentException("Don't know what to do with a " + rubyObject);
            }

            nodeCache.setASTNode(ret);

            return ret;
        } else if (object instanceof ContentNode) {

            return (ContentNode) object;

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
