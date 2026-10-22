package org.asciidoctor.integrationguide.syntaxhighlighter;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.io.IOUtils;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;
import org.asciidoctor.syntaxhighlighter.Formatter;
import org.asciidoctor.syntaxhighlighter.HighlightResult;
import org.asciidoctor.syntaxhighlighter.Highlighter;
import org.asciidoctor.syntaxhighlighter.StylesheetWriter;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

//tag::include[]
public class PrismJsHighlighter implements SyntaxHighlighterAdapter, Formatter, StylesheetWriter, Highlighter { // <1>

    private final ScriptEngine scriptEngine;

    public PrismJsHighlighter() {
        ScriptEngineFactory engine = new NashornScriptEngineFactory(); // <2>
        this.scriptEngine = engine.getScriptEngine();
        try {
            this.scriptEngine.eval(new InputStreamReader(getClass().getResourceAsStream("/prismjs/prism.js")));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasDocInfo(LocationType location) {
        return location == LocationType.HEADER;
    }

    @Override
    public String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
        if (document.hasAttribute("linkcss") && document.hasAttribute("copycss")) { // <3>
            return "<link href=\"prism.css\" rel=\"stylesheet\" />";
        } else {
            try (InputStream in = getClass().getResourceAsStream("/prismjs/prism.css")) {
                String css = IOUtils.toString(in);
                return "<style>\n" + css + "\n</style>";
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String format(Block node, String lang, Map<String, Object> opts) {
        return "<pre class='highlight'><code>"  // <3>
            + node.getContent()
            + "</code></pre>";
    }

    @Override
    public boolean isWriteStylesheet(Document doc) {
        return doc.hasAttribute("linkcss") && doc.hasAttribute("copycss");     // <3>
    }

    @Override
    public void writeStylesheet(Document doc, File toDir) {
        try (InputStream in1 = getClass().getResourceAsStream("/prismjs/prism.css"); // <3>
             OutputStream fout1 = new FileOutputStream(new File(toDir, "prism.css"))) {
            IOUtils.copy(in1, fout1);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public HighlightResult highlight(Block node,
                                     String source,
                                     String lang,
                                     Map<String, Object> options) {
        ScriptContext ctx = scriptEngine.getContext();                                     // <4>
        Bindings bindings = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("text", source);
        bindings.put("language", lang);

        try {
            String result = (String) scriptEngine.eval(
                "Prism.highlight(text, Prism.languages[language], language)", bindings);
            return new HighlightResult(result);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}
//end::include[]