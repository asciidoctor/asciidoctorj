package org.asciidoctor.jruby.ast.impl;

import org.jruby.RubyStruct;

import org.asciidoctor.ast.Footnote;
import org.jruby.javasupport.JavaEmbedUtils;

public class FootnoteImpl implements Footnote {

    private static final String INDEX_KEY_NAME = "index";
    private static final String ID_KEY_NAME = "id";
    private static final String TEXT_KEY_NAME = "text";

    private Long index;
    private String id;
    private String text;

    private static Object aref(RubyStruct s, String key)  {
       return JavaEmbedUtils.rubyToJava(s.aref(s.getRuntime().newString(key)));
    }

    public static Footnote getInstance(Long index, String id, String text)  {
        FootnoteImpl footnote = new FootnoteImpl();
        footnote.index = index;
        footnote.id = id;
        footnote.text = text;
        return footnote;
    }

    public static Footnote getInstance(RubyStruct rubyFootnote) {
        return FootnoteImpl.getInstance(
                (Long) aref(rubyFootnote, INDEX_KEY_NAME),
                (String) aref(rubyFootnote, ID_KEY_NAME),
                (String) aref(rubyFootnote, TEXT_KEY_NAME));
    }

    @Override
    public Long getIndex() { return index; }

    @Override
    public String getId() { return id; }

    @Override
    public String getText() { return text; }
}
