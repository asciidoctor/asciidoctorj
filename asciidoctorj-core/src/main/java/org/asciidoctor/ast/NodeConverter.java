package org.asciidoctor.ast;

import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
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

    private static final String TABLE_CLASS = "Asciidoctor::Table";

    private static final String TABLE_COLUMN_CLASS = "Asciidoctor::Table::Column";

    private static final String TABLE_CELL_CLASS = "Asciidoctor::Table::Cell";

    private NodeConverter() {}

    public static AbstractNode createASTNode(IRubyObject rubyObject) {
        String rubyClassName = rubyObject.getMetaClass().getRealClass().getName();
        Ruby runtime = rubyObject.getRuntime();
        if (BLOCK_CLASS.equals(rubyClassName)) {
            Block blockRuby = RubyUtils.rubyToJava(runtime, rubyObject, Block.class);
            return new BlockImpl(blockRuby, runtime);
        }
        else if (SECTION_CLASS.equals(rubyClassName)) {
            Section blockRuby = RubyUtils.rubyToJava(runtime, rubyObject, Section.class);
            return new SectionImpl(blockRuby, runtime);
        }
        else if (DOCUMENT_CLASS.equals(rubyClassName)) {
            DocumentRuby blockRuby = RubyUtils.rubyToJava(runtime, rubyObject, DocumentRuby.class);
            return new Document(blockRuby, runtime);
        }
        else if (INLINE_CLASS.equals(rubyClassName)) {
            Inline inline = RubyUtils.rubyToJava(runtime, rubyObject, Inline.class);
            return new InlineImpl(inline, runtime);
        }
        else if (LIST_CLASS.equals(rubyClassName)) {
            ListNode list = RubyUtils.rubyToJava(runtime, rubyObject, ListNode.class);
            return new ListImpl(list, runtime);
        }
        else if (LIST_ITEM_CLASS.equals(rubyClassName)) {
            ListItem list = RubyUtils.rubyToJava(runtime, rubyObject, ListItem.class);
            return new ListItemImpl(list, runtime);
        }
        else if (TABLE_CLASS.equals(rubyClassName)) {
            Table table = RubyUtils.rubyToJava(runtime, rubyObject, Table.class);
            return new TableImpl(table, runtime);
        }
        else if (TABLE_CELL_CLASS.equals(rubyClassName)) {
            Cell cell = RubyUtils.rubyToJava(runtime, rubyObject, Cell.class);
            return new CellImpl(cell, runtime);
        }
        else if (TABLE_COLUMN_CLASS.equals(rubyClassName)) {
            Column column = RubyUtils.rubyToJava(runtime, rubyObject, Column.class);
            return new ColumnImpl(column, runtime);
        }
        throw new IllegalArgumentException("Don't know what to do with a " + rubyObject);
    }

}
