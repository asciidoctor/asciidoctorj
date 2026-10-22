require 'asciidoctor'
require 'asciidoctor/extensions'

class YellRubyBlock < Asciidoctor::Extensions::BlockProcessor
  option :name, :yell
  option :contexts, [:paragraph]
  option :content_model, :simple

  def process parent, reader, attributes
    lines = reader.lines.map {|line| line.upcase.gsub(/\.( |$)/, '!\\1') }
    Asciidoctor::Block.new parent, :paragraph, :source => lines, :attributes => attributes
  end
end
