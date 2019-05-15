package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.LocationType
import org.jruby.Ruby
import org.jruby.javasupport.JavaEmbedUtils

class CoderayHighlighter implements SyntaxHighlighterAdapter, Formatter, Highlighter {

    interface TestFormatter {
        List format(String lang, String source, String numberLines)
    }

    private TestFormatter formatter

    CoderayHighlighter() {
        Ruby ruby = JavaEmbedUtils.initialize([])

        ruby.evalScriptlet('''
require 'coderay'

module AsciidoctorjTest
    CodeCellStartTagCs = '<td class="code"><pre>\'
    
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
        ''')

        formatter = (TestFormatter) JavaEmbedUtils.rubyToJava(ruby, ruby.evalScriptlet('AsciidoctorjTest::TestCoderayFormatter::new'), TestFormatter)
    }

    @Override
    boolean hasDocInfo(LocationType location) {
        location == LocationType.FOOTER
    }

    @Override
    String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
        '''<link rel="stylesheet" href="./coderay-asciidoctor.css">'''

    }

    @Override
    String format(Block node, String lang, Map<String, Object> opts) {
        """<pre class="CodeRay highlight"><code data-lang="$lang">${node.content}</code></pre>"""
    }

    @Override
    HighlightResult highlight(Block node, String source, String lang, Map<String, Object> options) {
        List result = formatter.format(lang, source, (String) options['number_lines'])
        new HighlightResult(result.get(0) as String, result.get(1) as Integer)
    }
}
