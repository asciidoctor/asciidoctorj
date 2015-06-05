package org.asciidoctor.ast;

import java.util.List;

public interface Table extends AbstractBlock {

    boolean hasHeaderOption();

    List<Column> getColumns();

    List<Row> getHeader();

    List<Row> getFooter();

    List<Row> getBody();

}
