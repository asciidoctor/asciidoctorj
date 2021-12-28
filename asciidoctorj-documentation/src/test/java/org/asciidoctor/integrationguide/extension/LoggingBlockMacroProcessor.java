package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.Map;

//tag::include[]
@Name("log")
public class LoggingBlockMacroProcessor extends BlockMacroProcessor {

    @Override
    public Object process(
            StructuralNode parent, String target, Map<String, Object> attributes) {

        String message = target;

        log(new org.asciidoctor.log.LogRecord(        // <1>
                org.asciidoctor.log.Severity.INFO,
                parent.getSourceLocation(),           // <2>
                message));

        return createBlock(parent, "paragraph", "Hello from the logging macro");
    }

}
//end::include[]
