package org.asciidoctor.core.api.ast;

import org.asciidoctor.ast.Author;
import org.asciidoctor.core.Document;

/**
 * The {@link Author} of the AsciidoctorJ API.
 */
public class AuthorImpl implements Author {
    private final Document.Author delegate;

    public AuthorImpl(Document.Author delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getFullName() {
        return delegate.getFullName();
    }

    @Override
    public String getLastName() {
        return delegate.getLastName();
    }

    @Override
    public String getFirstName() {
        return delegate.getFirstName();
    }

    @Override
    public String getMiddleName() {
        return delegate.getMiddleName();
    }

    @Override
    public String getEmail() {
        return delegate.getEmail();
    }

    @Override
    public String getInitials() {
        return delegate.getInitials();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (getFullName() != null) {
            result.append(getFullName());
        }
        if (getEmail() != null) {
            result.append(" <").append(getEmail()).append(">");
        }
        return result.toString();
    }
}
