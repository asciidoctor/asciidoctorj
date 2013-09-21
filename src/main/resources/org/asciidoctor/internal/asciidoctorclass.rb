require 'java'
require 'asciidoctor'
require 'asciidoctor/extensions'

class AsciidoctorModule
	java_implements Java::Asciidoctor

    def treeprocessor(extensionName)
        Asciidoctor::Extensions.register do |document|
            treeprocessor extensionName
        end
    end
    
    def include_processor(extensionName)
        Asciidoctor::Extensions.register do |document|
            include_processor extensionName
        end
    end

    def preprocessor(extensionName)
        Asciidoctor::Extensions.register do |document|
            preprocessor extensionName
        end
    end
    
    def postprocessor(extensionName)
        Asciidoctor::Extensions.register do |document|
            postprocessor extensionName
        end
    end

    def block(blockSymbol, extensionName)
        Asciidoctor::Extensions.register do |document|
            block blockSymbol, extensionName
        end
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