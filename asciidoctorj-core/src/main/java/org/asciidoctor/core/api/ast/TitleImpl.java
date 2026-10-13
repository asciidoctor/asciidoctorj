package org.asciidoctor.core.api.ast;

import org.asciidoctor.ast.Title;
import org.asciidoctor.core.Document;

/**
 * The {@link Title} of the AsciidoctorJ API (the title of a document partitioned in a main title and a subtitle).
 */
public class TitleImpl implements Title {
    private final Document.Title delegate;

    public TitleImpl(Document.Title delegate) {
        this.delegate = delegate;
    }

    static Title wrap(Document.Title title) {
        return title == null ? null : new TitleImpl(title);
    }

    @Override
    public String getMain() {
        return delegate.getMain();
    }

    @Override
    public String getSubtitle() {
        return delegate.getSubtitle();
    }

    @Override
    public String getCombined() {
        return delegate.getCombined();
    }

    @Override
    public boolean isSanitized() {
        return delegate.isSanitized();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
