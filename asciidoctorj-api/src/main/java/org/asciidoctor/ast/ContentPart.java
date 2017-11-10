package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface ContentPart {

    String getId();

    int getLevel();

    String getContext();

    String getStyle();

    String getRole();

    String getTitle();

    Map<String, Object> getAttributes();

    String getContent();

    List<? extends ContentPart> getParts();
}
