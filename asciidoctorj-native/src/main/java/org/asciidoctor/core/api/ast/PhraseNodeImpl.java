package org.asciidoctor.core.api.ast;

import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.core.Inline;

/**
 * The {@link PhraseNode} of the AsciidoctorJ API.
 */
public class PhraseNodeImpl extends ContentNodeImpl<Inline> implements PhraseNode {

    public PhraseNodeImpl(Inline delegate) {
        super(delegate);
    }

    @Override
    public String convert() {
        return delegate.convert();
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public String getText() {
        return delegate.getText();
    }

    @Override
    public String getTarget() {
        return delegate.getTarget();
    }
}
