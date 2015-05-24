package org.asciidoctor.ast;

import java.util.List;

/**
 * Created by robertpanzer on 23.05.15.
 */
public interface StructuredDocument {
    List<? extends ContentPart> getParts();

    DocumentHeader getHeader();

    ContentPart getPartById(String id);

    ContentPart getPartByStyle(String style);

    ContentPart getPartByRole(String role);

    List<? extends ContentPart> getPartsByContext(String context);

    List<? extends ContentPart> getPartsByStyle(String style);

    List<? extends ContentPart> getPartsByRole(String role);
}
