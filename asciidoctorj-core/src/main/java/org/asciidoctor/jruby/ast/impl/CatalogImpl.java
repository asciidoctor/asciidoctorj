package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.Catalog;
import org.asciidoctor.ast.Footnote;
import org.jruby.RubyArray;
import org.jruby.RubyStruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CatalogImpl implements Catalog  {

    private final Map<String, Object> catalog;

    public CatalogImpl(Map<String, Object> catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<Footnote> getFootnotes() {
        RubyArray<?> rubyFootnotes = (RubyArray<?>) catalog.get("footnotes");
        List<Footnote> footnotes = new ArrayList<>();
        for (Object f : rubyFootnotes) {
            footnotes.add(FootnoteImpl.getInstance((RubyStruct) f));
        }
        return footnotes;
    }
}
