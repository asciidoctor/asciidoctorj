package org.asciidoctor.dom;

import java.util.List;

public interface Block extends AbstractBlock {
    List<String> lines();
}
