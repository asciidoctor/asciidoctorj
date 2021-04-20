package org.asciidoctor.jruby.extension.internal;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Map;

public class PreprocessorReaderImpl extends ReaderImpl implements PreprocessorReader {

    public PreprocessorReaderImpl(IRubyObject rubyNode) {
        super(rubyNode);
    }

    @Override
    public void push_include(String data, String file, String path, int lineNumber, Map<String, Object> attributes) {
        pushInclude(data, file, path, lineNumber, attributes);
    }

    @Override
    public void pushInclude(String data, String file, String path, int lineNumber, Map<String, Object> attributes) {
        RubyHash attributeHash = RubyHash.newHash(getRuntime());
        attributeHash.putAll(attributes);

        getRubyProperty("push_include", data, file, path, lineNumber, attributes);
    }

    @Override
    @Deprecated
    public Document document() {
        return getDocument();
    }

    @Override
    public Document getDocument() {
        return (Document) NodeConverter.createASTNode(getRubyProperty("document"));
    }

}
