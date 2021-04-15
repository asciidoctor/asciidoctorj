package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.*;
import org.asciidoctor.jruby.internal.RubyHashMapDecorator;
import org.asciidoctor.jruby.internal.RubyHashUtil;
import org.jruby.*;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocumentImpl extends StructuralNodeImpl implements Document {

    public DocumentImpl(IRubyObject document) {
        super(document);
    }

    @Override
    public boolean isBasebackend(String backend) {
        return getBoolean("basebackend?", backend);
    }

    @Override
    @Deprecated
    public boolean basebackend(String backend) {
        return isBasebackend(backend);
    }

    @Override
    public Map<Object, Object> getOptions() {
        return RubyHashUtil.convertRubyHashMapToMap((RubyHash) getRubyProperty("options"));
    }

    @Override
    public Title getStructuredDoctitle() {
        Ruby runtime = getRubyObject().getRuntime();
        RubyHash options = RubyHash.newHash(runtime);
        RubySymbol partitioned = RubySymbol.newSymbol(runtime, "partition");
        options.put(partitioned, RubyBoolean.newBoolean(runtime, true));

        Object doctitle = getRubyProperty("doctitle", options);

        return toJava((IRubyObject) doctitle, Title.class);

    }

    @Override
    public String getDoctitle() {
        return getString("doctitle");
    }

    @Override
    @Deprecated
    public String doctitle() {
        return getDoctitle();
    }

    @Override
    public List<Author> getAuthors() {
        return getList("authors", RubyStruct.class)
                .stream()
                .map(AuthorImpl::getInstance)
                .collect(Collectors.toList());
    }

    @Override
    public int getAndIncrementCounter(String name) {
        return getInt("counter", name);
    }

    @Override
    public int getAndIncrementCounter(String name, int initialValue) {
        return getInt("counter", name, initialValue);
    }

    @Override
    public boolean isSourcemap() {
        return getBoolean("sourcemap");
    }

    @Override
    public void setSourcemap(boolean state) {
        setBoolean("sourcemap", state);
    }

    @Override
    public Catalog getCatalog() {
        return new CatalogImpl(new RubyHashMapDecorator((RubyHash) getProperty("catalog")));
    }

    @Override
    public RevisionInfo getRevisionInfo() {
        return RevisionInfoImpl.getInstance(getAttributes());
    }
}
