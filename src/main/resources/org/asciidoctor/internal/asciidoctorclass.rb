module AsciidoctorJ
    include_package 'org.asciidoctor'
    module Extensions
        include_package 'org.asciidoctor.extension'
    end
end

require 'java'
require 'asciidoctor'
require 'asciidoctor/extensions'

class AsciidoctorModule
	java_implements Java::Asciidoctor

    def unregister_all_extensions()
        Asciidoctor::Extensions.unregister_all
    end

    def treeprocessor(extensionName)
        Asciidoctor::Extensions.register do 
            treeprocessor extensionName
        end
    end
    
    def include_processor(extensionName)
        Asciidoctor::Extensions.register do
            include_processor extensionName
        end
    end

    def preprocessor(extensionName)
        Asciidoctor::Extensions.register do
            preprocessor extensionName
        end
    end
    
    def postprocessor(extensionName)
        Asciidoctor::Extensions.register do
            postprocessor extensionName
        end
    end

    def block_processor(blockSymbol, extensionName)
        Asciidoctor::Extensions.register do
            block blockSymbol, extensionName
        end
    end

    def block_macro(blockSymbol, extensionName)
        Asciidoctor::Extensions.register do
            block_macro blockSymbol, extensionName
        end
    end

    def inline_macro(blockSymbol, extensionName)
        Asciidoctor::Extensions.register do
            inline_macro blockSymbol, extensionName
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
	
	def asciidoctorRuntimeEnvironmentVersion()
	    return Asciidoctor::VERSION
	end
end