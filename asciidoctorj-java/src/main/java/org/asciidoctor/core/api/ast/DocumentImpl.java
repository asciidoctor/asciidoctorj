package org.asciidoctor.core.api.ast;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Author;
import org.asciidoctor.ast.Catalog;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.RevisionInfo;
import org.asciidoctor.ast.Title;

/**
 * The {@link Document} of the AsciidoctorJ API.
 */
public class DocumentImpl extends StructuralNodeImpl<org.asciidoctor.core.Document> implements Document {

    public DocumentImpl(org.asciidoctor.core.Document delegate) {
        super(delegate);
    }

    @Override
    public Title getStructuredDoctitle() {
        return TitleImpl.wrap(delegate.getStructuredDoctitle());
    }

    @Override
    public String getDoctitle() {
        return delegate.getDoctitle();
    }

    @Override
    public List<Author> getAuthors() {
        return delegate.getAuthors().stream().map(AuthorImpl::new).collect(Collectors.toList());
    }

    @Override
    public String getSource() {
        return delegate.getSource();
    }

    @Override
    public List<String> getSourceLines() {
        return delegate.getSourceLines();
    }

    @Override
    public boolean isBasebackend(String backend) {
        return delegate.isBasebackend(backend);
    }

    @Override
    public Map<Object, Object> getOptions() {
        return new LinkedHashMap<>(delegate.getOptions());
    }

    @Override
    public int getAndIncrementCounter(String name) {
        return delegate.getAndIncrementCounter(name);
    }

    @Override
    public int getAndIncrementCounter(String name, int initialValue) {
        return delegate.getAndIncrementCounter(name, initialValue);
    }

    @Override
    public boolean isSourcemap() {
        return delegate.isSourcemap();
    }

    @Override
    public void setSourcemap(boolean state) {
        delegate.setSourcemap(state);
    }

    @Override
    public Catalog getCatalog() {
        return new CatalogImpl(delegate.getCatalog());
    }

    @Override
    public RevisionInfo getRevisionInfo() {
        return new RevisionInfoImpl(delegate.getRevisionInfo());
    }
}
