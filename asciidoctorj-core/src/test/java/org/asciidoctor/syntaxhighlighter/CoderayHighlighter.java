package org.asciidoctor.syntaxhighlighter;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;
import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CoderayHighlighter implements SyntaxHighlighterAdapter, Formatter, Highlighter {

    public interface TestFormatter {
        List<?> format(String lang, String source, String numberLines);
    }

    private final TestFormatter formatter;

    public CoderayHighlighter() {
        Ruby ruby = JavaEmbedUtils.initialize(Collections.emptyList());

        ruby.evalScriptlet("""
                require 'coderay'
                
                module AsciidoctorjTest
                    CodeCellStartTagCs = '<td class="code"><pre>'
                
                    class TestCoderayFormatter
                
                        def format lang, source, line_numbers
                            lang = lang ? (::CodeRay::Scanners[lang = lang.to_sym] && lang rescue :text) : :text
                            highlighted = ::CodeRay::Duo[lang, :html,
                                css: :class,
                                line_numbers: line_numbers = line_numbers ? line_numbers.to_sym : false,
                                line_number_anchors: false,
                                ].highlight source
                            if line_numbers == :table
                              [highlighted, (idx = highlighted.index CodeCellStartTagCs) ? idx + CodeCellStartTagCs.length : nil]
                            else
                              [highlighted, nil]
                            end
                        end
                    end
                end
                """);

        formatter = JavaEmbedUtils.rubyToJava(ruby, ruby.evalScriptlet("AsciidoctorjTest::TestCoderayFormatter::new"), TestFormatter.class);
    }

    @Override
    public boolean hasDocInfo(LocationType location) {
        return location == LocationType.FOOTER;
    }

    @Override
    public String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
        return "<link rel=\"stylesheet\" href=\"./coderay-asciidoctor.css\">";
    }

    @Override
    public String format(Block node, String lang, Map<String, Object> opts) {
        return "<pre class=\"CodeRay highlight\"><code data-lang=\"" + lang + "\">" + node.getContent() + "</code></pre>";
    }

    @Override
    public HighlightResult highlight(Block node, String source, String lang, Map<String, Object> options) {
        List<?> result = formatter.format(lang, source, (String) options.get("number_lines"));
        Object lineOffset = result.get(1);
        return new HighlightResult((String) result.get(0), lineOffset == null ? null : ((Number) lineOffset).intValue());
    }
}
