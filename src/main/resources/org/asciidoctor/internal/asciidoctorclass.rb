require 'java'
require 'asciidoctor'
require 'asciidoctor/extensions'

class FrontMatterPreprocessor < Asciidoctor::Extensions::Preprocessor
  def process reader, lines
    return reader if lines.empty?
    front_matter = []
    if lines.first.chomp == '---'
      original_lines = lines.dup
      lines.shift
      while !lines.empty? && lines.first.chomp != '---'
        front_matter << lines.shift
      end

      if (first = lines.first).nil? || first.chomp != '---'
        lines = original_lines
      else
        lines.shift
        @document.attributes['front-matter'] = front_matter.join.chomp
        # advance the reader by the number of lines taken
        (front_matter.length + 2).times { reader.advance }
      end
    end
    reader
  end
end

class AsciidoctorModule
	java_implements Java::Asciidoctor

    def render_file_extension(content, extensionName, options = {})
        Asciidoctor::Extensions.register do |document|
            preprocessor extensionName
        end
        return Asciidoctor.render_file(content, options)
    end

	def render_file(content, options = {})
		return Asciidoctor.render_file(content, options)
	end

	def render(content, options = {})
		return Asciidoctor.render(content, options)
	end

	def load_file(content, options = {})
		return Asciidoctor.load_file(content, options)
	end

	def load(content, options = {})
		return Asciidoctor.load(content, options)
	end
end