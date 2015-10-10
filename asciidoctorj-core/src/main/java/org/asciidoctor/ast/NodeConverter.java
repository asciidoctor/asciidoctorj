package org.asciidoctor.ast;

import org.asciidoctor.ast.impl.BlockImpl;
import org.asciidoctor.ast.impl.CellImpl;
import org.asciidoctor.ast.impl.ColumnImpl;
import org.asciidoctor.ast.impl.DocumentImpl;
import org.asciidoctor.ast.impl.ListImpl;
import org.asciidoctor.ast.impl.ListItemImpl;
import org.asciidoctor.ast.impl.PhraseNodeImpl;
import org.asciidoctor.ast.impl.SectionImpl;
import org.asciidoctor.ast.impl.TableImpl;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.java.proxies.RubyObjectHolderProxy;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * A library class that allows to convert nodes from Asciidoctor Ruby
 * to its AsciidoctorJ counterparts.
 */
public final class NodeConverter {

    public enum NodeType {

        BLOCK_CLASS("Asciidoctor", "Block"),

        SECTION_CLASS("Asciidoctor", "Section"),

        DOCUMENT_CLASS("Asciidoctor", "Document"),

        INLINE_CLASS("Asciidoctor", "Inline"),

        LIST_CLASS("Asciidoctor", "List"),

        LIST_ITEM_CLASS("Asciidoctor", "ListItem"),

        TABLE_CLASS("Asciidoctor", "Table"),

        TABLE_COLUMN_CLASS("Asciidoctor", "Table", "Column"),

        TABLE_CELL_CLASS("Asciidoctor", "Table", "Cell");

        private String[] path;

        NodeType(String... path) {
            this.path = path;
        }

        public RubyClass getRubyClass(Ruby runtime) {
            RubyModule object = runtime.getModule(path[0]);

            RubyClass rubyClass = object.getClass(path[1]);

            if (path.length == 2) {
                return rubyClass;
            } else {
                return rubyClass.getClass(path[2]);
            }
        }

        public static NodeType getNodeType(RubyClass rubyClass) {
            Ruby runtime = rubyClass.getRuntime();
            for (NodeType nodeType: values()) {
                if (nodeType.getRubyClass(runtime).equals(rubyClass)) {
                    return nodeType;
                }
            }
            throw new IllegalArgumentException("Don't know what to do with a " + rubyClass);
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
            NodeCache nodeCache = NodeCache.get(rubyObject);
            ContentNode cachedNode = nodeCache.getASTNode();
            if (cachedNode != null) {
                return cachedNode;
            }

            Ruby runtime = rubyObject.getRuntime();

            RubyClass rubyClass = rubyObject.getMetaClass().getRealClass();
            ContentNode ret = null;

            switch (NodeType.getNodeType(rubyClass)) {
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
