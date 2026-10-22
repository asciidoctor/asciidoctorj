require 'asciidoctor'
require 'asciidoctor/extensions'

class ResponseIncludeProcessor < Asciidoctor::Extensions::IncludeProcessor
  def handles? target
    target == 'response'
  end

  def process doc, reader, target, attributes
    reader.push_include '42', target, target, 1, attributes
    reader
  end
end