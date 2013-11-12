package org.asciidoctor.internal;

import java.util.List;

public interface Block extends AbstractBlock {
    List<String> lines();
}
