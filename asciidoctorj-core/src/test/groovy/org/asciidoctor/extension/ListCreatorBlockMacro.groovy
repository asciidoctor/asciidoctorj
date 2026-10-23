package org.asciidoctor.extension


import org.asciidoctor.ast.StructuralNode

@Name('list')
class ListCreatorBlockMacro extends BlockMacroProcessor {

    String context
    boolean attributesInOptions

    ListCreatorBlockMacro(String context, boolean attributesInOptions = false) {
        this.context = context
        this.attributesInOptions = attributesInOptions
    }

    @Override
    StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {

        def attrs = new HashMap<String, Object>()
        attrs['start'] = '42'
        def opts = new HashMap<Object, Object>()

        org.asciidoctor.ast.List list
        if (attributesInOptions) {
            opts['attributes'] = attrs
            list = createList(parent, context, opts)
        } else {
            list = createList(parent, context, attrs, opts)
        }

        list.getBlocks().add(createListItem(list, 'First item'))
        list.getBlocks().add(createListItem(list, 'Second item'))

        list
    }
}
