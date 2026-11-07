package org.asciidoctor.core.api.ast;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Catalog;
import org.asciidoctor.ast.Footnote;
import org.asciidoctor.ast.ImageReference;
import org.asciidoctor.ast.Link;
import org.asciidoctor.core.Document;

/**
 * The {@link Catalog} of the AsciidoctorJ API.
 */
public class CatalogImpl implements Catalog {
    private final Document.Catalog delegate;

    public CatalogImpl(Document.Catalog delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<Footnote> getFootnotes() {
        return delegate.getFootnotes().stream()
                .map(footnote -> new FootnoteImpl(footnote.getIndex() == null ? null : footnote.getIndex().longValue(),
                        footnote.getId(), footnote.getText()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<ImageReference> getImages() {
        return delegate.getImages().stream()
                .map(image -> new ImageReferenceImpl(image.getTarget(), image.getImagesdir()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Link> getLinks() {
        return delegate.getLinks().stream().map(LinkImpl::new).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Map<String, Object> getRefs() {
        Map<String, Object> refs = new LinkedHashMap<>();
        delegate.getRefs().forEach((id, node) -> refs.put(id, NodeConverter.wrap(node)));
        return Collections.unmodifiableMap(refs);
    }

    record FootnoteImpl(Long index, String id, String text) implements Footnote {
        @Override
        public Long getIndex() {
            return index;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getText() {
            return text;
        }
    }

    record ImageReferenceImpl(String target, String imagesdir) implements ImageReference {
        @Override
        public String getTarget() {
            return target;
        }

        @Override
        public String getImagesdir() {
            return imagesdir;
        }
    }

    record LinkImpl(String text) implements Link {
        @Override
        public String getText() {
            return text;
        }
    }
}
