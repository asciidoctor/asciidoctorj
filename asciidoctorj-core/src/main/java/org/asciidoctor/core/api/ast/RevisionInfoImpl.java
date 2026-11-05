package org.asciidoctor.core.api.ast;

import org.asciidoctor.ast.RevisionInfo;
import org.asciidoctor.core.Document;

/**
 * The {@link RevisionInfo} of the AsciidoctorJ API.
 */
public class RevisionInfoImpl implements RevisionInfo {
    private final Document.RevisionInfo delegate;

    public RevisionInfoImpl(Document.RevisionInfo delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getDate() {
        return delegate.getDate();
    }

    @Override
    public String getNumber() {
        return delegate.getNumber();
    }

    @Override
    public String getRemark() {
        return delegate.getRemark();
    }
}
