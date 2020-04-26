package org.asciidoctor.extension


import org.asciidoctor.ast.StructuralNode

@Name('list')
class ListCreatorBlockMacro extends BlockMacroProcessor {

    String context

    ListCreatorBlockMacro(String context) {
        this.context = context
    }

    @Override
    Object process(StructuralNode parent, String target, Map<String, Object> attributes) {

        def attrs = new HashMap<String, Object>()
        attrs['start'] = '42'
        def opts = new HashMap<Object, Object>()

        org.asciidoctor.ast.List list = createList(parent, context, attrs, opts)

        list.getBlocks().add(createListItem(list, 'First item'))
        list.getBlocks().add(createListItem(list, 'Second item'))

        list
    }
}
