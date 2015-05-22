package org.asciidoctor.ast;

import java.util.List;

public interface Block extends BlockNode {
    List<String> lines();
    String source();
}
