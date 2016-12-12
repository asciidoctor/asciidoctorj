package org.asciidoctor.extension;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.asciidoctor.ast.Document;

public class PositionalAttrsIncludeProcessor extends IncludeProcessor {

    public PositionalAttrsIncludeProcessor() {
    }

    public PositionalAttrsIncludeProcessor(Map<String, Object> config) {
        super(config);
    }

    @Override
    public boolean handles(String target) {
        return true;
    }

    @Override
    public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
        Map<String, Object> treeMap = new TreeMap<String, Object>(attributes);

        String str = "";
        Iterator<Map.Entry<String, Object>> it = treeMap.entrySet().iterator();
        if (it.hasNext())
            str += it.next().getValue();
        while (it.hasNext()) {
            str += "," + it.next().getValue();
        }

        reader.push_include(str, target, target, 1, attributes);

    }

}
