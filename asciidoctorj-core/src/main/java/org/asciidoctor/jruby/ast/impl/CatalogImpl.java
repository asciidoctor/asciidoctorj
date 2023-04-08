package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.Catalog;
import org.asciidoctor.ast.Footnote;
import org.asciidoctor.ast.ImageReference;
import org.asciidoctor.jruby.internal.RubyHashMapDecorator;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyStruct;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CatalogImpl implements Catalog {

    private final Map<String, Object> catalog;

    public CatalogImpl(Map<String, Object> catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<Footnote> getFootnotes() {
        return (List<Footnote>) ((RubyArray) catalog.get("footnotes"))
                .stream()
                .map(o -> FootnoteImpl.getInstance((RubyStruct) o))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<ImageReference> getImages() {
        return (List<ImageReference>) ((RubyArray) catalog.get("images"))
                .stream()
                .map(o -> ImageReferenceImpl.getInstance((RubyStruct) o))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Map<String, Object> getRefs() {
        Map<String, Object> refs = new RubyHashMapDecorator((RubyHash) catalog.get("refs"), String.class);
        return Collections.unmodifiableMap(refs);
    }
}
