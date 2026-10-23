package org.asciidoctor.core.api.ast;

import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.ContentModel;

/**
 * The {@link StructuralNode} of the AsciidoctorJ API, which delegates to a block of the implementation.
 *
 * @param <T> the type of the block of the implementation
 */
public class StructuralNodeImpl<T extends AbstractBlock> extends ContentNodeImpl<T> implements StructuralNode {

    public StructuralNodeImpl(T delegate) {
        super(delegate);
    }

    @Override
    public String getTitle() {
        return delegate.getTitle();
    }

    @Override
    public void setTitle(String title) {
        delegate.setTitle(title);
    }

    @Override
    public String getCaption() {
        return delegate.getCaption();
    }

    @Override
    public void setCaption(String caption) {
        delegate.setCaption(caption);
    }

    @Override
    public String getStyle() {
        return delegate.getStyle();
    }

    @Override
    public void setStyle(String style) {
        delegate.setStyle(style);
    }

    @Override
    public List<StructuralNode> getBlocks() {
        return new NodeList<>(delegate.getBlocks());
    }

    @Override
    public void append(StructuralNode block) {
        delegate.append(NodeConverter.unwrap(block));
    }

    @Override
    public Object getContent() {
        return NodeConverter.toApi(delegate.getContent());
    }

    @Override
    public String convert() {
        return delegate.convert();
    }

    @Override
    public List<StructuralNode> findBy(Map<Object, Object> selector) {
        return new NodeList<>(delegate.findBy(selector));
    }

    @Override
    public int getLevel() {
        Integer level = delegate.getLevel();
        return level == null ? 0 : level;
    }

    @Override
    public void setLevel(int level) {
        delegate.setLevel(level);
    }

    @Override
    public String getContentModel() {
        ContentModel contentModel = delegate.getContentModel();
        return contentModel == null ? null : contentModel.toString();
    }

    @Override
    public Cursor getSourceLocation() {
        return CursorImpl.wrap(delegate.getSourceLocation());
    }

    @Override
    public List<String> getSubstitutions() {
        return delegate.getSubstitutions();
    }

    @Override
    public boolean isSubstitutionEnabled(String substitution) {
        return delegate.isSubstitutionEnabled(substitution);
    }

    @Override
    public void removeSubstitution(String substitution) {
        delegate.removeSubstitution(substitution);
    }

    @Override
    public void addSubstitution(String substitution) {
        delegate.addSubstitution(substitution);
    }

    @Override
    public void prependSubstitution(String substitution) {
        delegate.prependSubstitution(substitution);
    }

    @Override
    public void setSubstitutions(String... substitutions) {
        delegate.setSubstitutions(substitutions);
    }
}
