package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.HashMap;
import java.util.Map;

//tag::include[]
@Name("foo")
public class ImageInlineMacroProcessor extends InlineMacroProcessor {

    @Override
    public Object process(ContentNode parent, String target, Map<String, Object> attributes) {

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("type", "image");                                            // <1>
        options.put("target", "http://foo.bar/" + target);                       // <2>

        String[] items = target.split("\\|");
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("alt", "Image not available");                                 // <3>
        attrs.put("width", "64");
        attrs.put("height", "64");

        return createPhraseNode(parent, "image", (String) null, attrs, options); // <4>
    }
}
//end::include[]
