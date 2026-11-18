package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.*;
import org.asciidoctor.jruby.internal.RubyHashMapDecorator;
import org.asciidoctor.jruby.internal.RubyHashUtil;
import org.jruby.*;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DocumentImpl extends StructuralNodeImpl implements Document {

    public DocumentImpl(IRubyObject document) {
        super(document);
    }

    @Override
    public String getSource() {
        return getString("source");
    }

    @Override
    public List<String> getSourceLines() {
        return getList("source_lines", String.class);
    }

    @Override
    public boolean isBasebackend(String backend) {
        return getBoolean("basebackend?", backend);
    }

    @Override
    public boolean isAttributeSpecified(String name) {
        Objects.requireNonNull(name, "name");
        // TODO replace with a call to Document#attr_specified? once based on Asciidoctor 2.1 (#1379)
        // NOTE Asciidoctor (Ruby) doesn't provide such a method yet, see https://github.com/asciidoctor/asciidoctor/issues/4889
        // the implementation is the same as Document#attr_unspecified? in Asciidoctor PDF
        if (getBoolean("attribute_locked?", name)) {
            return true;
        }
        ThreadContext context = getRuntime().getCurrentContext();
        IRubyObject attributesModified = getRubyProperty("@attributes_modified");
        if (!attributesModified.isNil() && attributesModified.callMethod(context, "include?", getRuntime().newString(name)).isTrue()) {
            return true;
        }
        if (getBoolean("nested?")) {
            return new DocumentImpl(getRubyProperty("@parent_document")).isAttributeSpecified(name);
        }
        IRubyObject options = getRubyProperty("@options");
        if (options instanceof RubyHash optionsHash) {
            Object attributes = optionsHash.get(RubySymbol.newSymbol(getRuntime(), "attributes"));
            if (attributes instanceof RubyHash attributesHash) {
                String normalizedName = name.toLowerCase(Locale.ROOT);
                for (Object key : attributesHash.keySet()) {
                    // a soft set or soft unset attribute (e.g., name@) is not locked, so the document can still change it
                    if (isNamedBy(key.toString(), normalizedName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isNamedBy(String key, String name) {
        if (key.endsWith("@")) {
            key = key.substring(0, key.length() - 1);
        }
        if (key.startsWith("!")) {
            key = key.substring(1);
        } else if (key.endsWith("!")) {
            key = key.substring(0, key.length() - 1);
        }
        return key.toLowerCase(Locale.ROOT).equals(name);
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
