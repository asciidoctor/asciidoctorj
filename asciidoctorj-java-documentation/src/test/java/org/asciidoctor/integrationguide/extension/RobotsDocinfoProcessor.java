package org.asciidoctor.integrationguide.extension;

//tag::include[]
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.Location;
import org.asciidoctor.extension.LocationType;

@Location(LocationType.HEADER)                                    // <1>
public class RobotsDocinfoProcessor extends DocinfoProcessor {    // <2>

    @Override
    public String process(Document document) {
        return "<meta name=\"robots\" content=\"index,follow\">"; // <3>
    }
}
//end::include[]
