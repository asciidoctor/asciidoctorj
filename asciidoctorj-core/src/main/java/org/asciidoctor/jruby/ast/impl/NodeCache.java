package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.ContentNode;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * This class helps at attaching AsciidoctorJRuby specific information to Ruby AST nodes.
 * For example the Java counterpart of a Ruby node can be attached to the Ruby node to always return the
 * same Java instance for the same Ruby node
 */
public class NodeCache {


    private static final String ASCIIDOCTORJ_CACHE = "asciidoctorj_cache";

    private static final String KEY_AST_NODE = "asciidoctorj_node";

    private final RubyHash cache;
    private Ruby runtime;

    public static NodeCache get(IRubyObject rubyObject) {

        RubyHash cache = (RubyHash) rubyObject.getInstanceVariables().getInstanceVariable(ASCIIDOCTORJ_CACHE);
        if (cache == null) {
            cache = RubyHash.newHash(rubyObject.getRuntime());
            rubyObject.getInstanceVariables().setInstanceVariable(ASCIIDOCTORJ_CACHE, cache);
        }
        return new NodeCache(cache);
    }

    private NodeCache(RubyHash cache) {
        this.cache = cache;
        this.runtime = cache.getRuntime();
    }

    public ContentNode getASTNode() {
        ContentNode astNode = (ContentNode) cache.get(getRuntime().newSymbol(KEY_AST_NODE));
        return astNode;
    }

    public void setASTNode(ContentNode astNode) {
        cache.put(getRuntime().newSymbol(KEY_AST_NODE), astNode);
    }

    Ruby getRuntime() {
        return cache.getRuntime();
    }
}
