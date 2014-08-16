package org.asciidoctor.ast;

import java.util.List;

public interface Block extends AbstractBlock {
    List<String> lines();
    String source();
}
