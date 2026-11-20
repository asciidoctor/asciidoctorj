require 'asciidoctor/extensions' unless RUBY_ENGINE == 'opal'

include Asciidoctor

# A block macro that embeds a Gist into the output document
#
# Usage
#
#   gist::12345[]
#
class GistBlockMacro < Extensions::BlockMacroProcessor
  use_dsl

  named :gist

  def process parent, target, attrs
    title_html = (attrs.has_key? 'title') ?
        %(<div class="title">#{attrs['title']}</div>\n) : nil

    html = %(<div class="openblock gist">
#{title_html}<div class="content">
<script src="https://gist.github.com/#{target}.js"></script>
</div>
</div>)

    create_pass_block parent, html, attrs, subs: nil
  end
end