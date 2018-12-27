package org.asciidoctor.integrationguide.extension;

//tag::include[]
import org.asciidoctor.api.ast.Document;
import org.asciidoctor.api.extension.DocinfoProcessor;
import org.asciidoctor.api.extension.Location;
import org.asciidoctor.api.extension.LocationType;

@Location(LocationType.HEADER)                                    // <1>
public class RobotsDocinfoProcessor extends DocinfoProcessor {    // <2>

    @Override
    public String process(Document document) {
        return "<meta name=\"robots\" content=\"index,follow\">"; // <3>
    }
}
//end::include[]
